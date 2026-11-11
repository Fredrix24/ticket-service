package com.example.ticketservice.service;

import com.example.ticketservice.dto.TicketCreateRequest;
import com.example.ticketservice.dto.TicketResponse;
import com.example.ticketservice.entity.Category;
import com.example.ticketservice.entity.Ticket;
import com.example.ticketservice.repository.CategoryRepository;
import com.example.ticketservice.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
public class TicketService {
    private final TicketRepository ticketRepository;
    private final CategoryRepository categoryRepository;
    private final Clock clock;

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
        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public TicketResponse getById(Long id) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Ticket not found"));
        return mapToResponse(ticket);
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
}