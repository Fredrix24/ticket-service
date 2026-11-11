package com.example.ticketservice.service;

import org.springframework.data.jpa.domain.Specification;
import com.example.ticketservice.dto.StatusUpdateRequest;
import com.example.ticketservice.dto.TicketCreateRequest;
import com.example.ticketservice.dto.TicketResponse;
import com.example.ticketservice.entity.Category;
import com.example.ticketservice.entity.StatusHistory;
import com.example.ticketservice.dto.StatusHistoryResponse;
import com.example.ticketservice.entity.Ticket;
import com.example.ticketservice.exception.ConflictException;
import com.example.ticketservice.repository.CategoryRepository;
import com.example.ticketservice.repository.StatusHistoryRepository;
import com.example.ticketservice.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class TicketService {
    private final TicketRepository ticketRepository;
    private final CategoryRepository categoryRepository;
    private final StatusHistoryRepository historyRepository;
    private final Clock clock;

    private static final Set<String> ALLOWED_TRANSITIONS = Set.of(
            "NEW->IN_PROGRESS", "IN_PROGRESS->RESOLVED",
            "NEW->CANCELLED", "IN_PROGRESS->CANCELLED"
    );

    @Transactional
    public TicketResponse create(TicketCreateRequest request) {
        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new IllegalArgumentException("Category not found"));

        Ticket ticket = new Ticket();
        ticket.setTitle(request.title());
        ticket.setDescription(request.description());
        ticket.setCategory(category);
        ticket.setPriority(request.priority());
        ticket.setStatus("NEW");
        ticket.setCreatedAt(OffsetDateTime.now(clock));
        ticket.setDueAt(request.dueAt());

        Ticket saved = ticketRepository.save(ticket);
        saveHistory(saved, null, "NEW", "Ticket created");
        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public TicketResponse getById(Long id) {
        return mapToResponse(findTicket(id));
    }

    @Transactional
    public TicketResponse changeStatus(Long id, StatusUpdateRequest request) {
        Ticket ticket = findTicket(id);
        String currentStatus = ticket.getStatus();
        String newStatus = request.status();

        if (currentStatus.equals(newStatus)) {
            throw new ConflictException("Ticket is already in status " + newStatus);
        }

        String transition = currentStatus + "->" + newStatus;
        if (!ALLOWED_TRANSITIONS.contains(transition)) {
            throw new ConflictException("Transition " + transition + " is not allowed");
        }

        ticket.setStatus(newStatus);
        if ("RESOLVED".equals(newStatus)) {
            ticket.setClosedAt(OffsetDateTime.now(clock));
        }

        Ticket saved = ticketRepository.save(ticket);
        saveHistory(saved, currentStatus, newStatus, request.comment());
        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<StatusHistoryResponse> getHistory(Long id) {
        if (!ticketRepository.existsById(id)) {
            throw new IllegalArgumentException("Ticket not found");
        }
        return historyRepository.findByTicketIdOrderByChangedAtAsc(id).stream()
                .map(h -> new StatusHistoryResponse(
                        h.getId(),
                        h.getTicket().getId(),
                        h.getFromStatus(),
                        h.getToStatus(),
                        h.getChangedAt()
                ))
                .toList();
    }

    private Ticket findTicket(Long id) {
        return ticketRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Ticket not found"));
    }

    private void saveHistory(Ticket ticket, String from, String to, String comment) {
        StatusHistory history = new StatusHistory();
        history.setTicket(ticket);
        history.setFromStatus(from);
        history.setToStatus(to);
        history.setChangedAt(OffsetDateTime.now(clock));
        historyRepository.save(history);
    }

    private TicketResponse mapToResponse(Ticket ticket) {
        return new TicketResponse(
                ticket.getId(),
                ticket.getTitle(),
                ticket.getDescription(),
                ticket.getCategory().getId(),
                ticket.getCategory().getName(),
                ticket.getPriority(),
                ticket.getStatus(),
                ticket.getCreatedAt(),
                ticket.getDueAt(),
                ticket.getClosedAt()
        );
    }

    @Transactional(readOnly = true)
    public org.springframework.data.domain.Page<TicketResponse> search(
            String status, String priority, Long categoryId, Boolean overdue,
            org.springframework.data.domain.Pageable pageable) {

        Specification<Ticket> spec = (root, query, cb) -> cb.conjunction();
        if (status != null) spec = spec.and(TicketSpecifications.hasStatus(status));
        if (priority != null) spec = spec.and(TicketSpecifications.hasPriority(priority));
        if (categoryId != null) spec = spec.and(TicketSpecifications.hasCategory(categoryId));
        if (Boolean.TRUE.equals(overdue)) spec = spec.and(TicketSpecifications.isOverdue(OffsetDateTime.now(clock)));

        return ticketRepository.findAll(spec, pageable).map(this::mapToResponse);
    }
}