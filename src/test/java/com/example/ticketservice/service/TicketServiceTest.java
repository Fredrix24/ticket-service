package com.example.ticketservice.service;

import com.example.ticketservice.dto.StatusUpdateRequest;
import com.example.ticketservice.dto.TicketCreateRequest;
import com.example.ticketservice.dto.TicketResponse;
import com.example.ticketservice.entity.Category;
import com.example.ticketservice.entity.Ticket;
import com.example.ticketservice.exception.ConflictException;
import com.example.ticketservice.repository.CategoryRepository;
import com.example.ticketservice.repository.StatusHistoryRepository;
import com.example.ticketservice.repository.TicketRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TicketServiceTest {

    @Mock
    private TicketRepository ticketRepository;
    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private StatusHistoryRepository historyRepository;

    private final Clock clock = Clock.fixed(Instant.parse("2026-11-14T12:00:00Z"), ZoneOffset.UTC);

    private TicketService ticketService;

    @BeforeEach
    void setUp() {
        ticketService = new TicketService(ticketRepository, categoryRepository, historyRepository, clock);
    }

    @Test
    void createTicket_CategoryNotFound_ThrowsException() {
        TicketCreateRequest request = new TicketCreateRequest("Title", "Desc", 99L, "HIGH", null);
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> ticketService.create(request));
    }

    @Test
    void createTicket_Success_ReturnsNewStatus() {
        Category category = new Category();
        category.setId(1L);
        TicketCreateRequest request = new TicketCreateRequest("Title", "Desc", 1L, "HIGH", null);

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(i -> i.getArgument(0));

        TicketResponse response = ticketService.create(request);

        assertEquals("NEW", response.status());
        verify(historyRepository, times(1)).save(any());
    }

    @Test
    void changeStatus_NewToInProgress_Success() {
        Ticket ticket = createMockTicket(1L, "NEW");
        when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(i -> i.getArgument(0));

        TicketResponse response = ticketService.changeStatus(1L, new StatusUpdateRequest("IN_PROGRESS", "comment"));

        assertEquals("IN_PROGRESS", response.status());
        assertNull(response.closedAt());
    }

    @Test
    void changeStatus_InProgressToResolved_SetsClosedAt() {
        Ticket ticket = createMockTicket(1L, "IN_PROGRESS");
        when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(i -> i.getArgument(0));

        TicketResponse response = ticketService.changeStatus(1L, new StatusUpdateRequest("RESOLVED", "done"));

        assertEquals("RESOLVED", response.status());
        assertNotNull(response.closedAt());
        assertEquals(OffsetDateTime.now(clock), response.closedAt());
    }

    @Test
    void changeStatus_NewToCancelled_Success() {
        Ticket ticket = createMockTicket(1L, "NEW");
        when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(i -> i.getArgument(0));

        TicketResponse response = ticketService.changeStatus(1L, new StatusUpdateRequest("CANCELLED", "cancel"));

        assertEquals("CANCELLED", response.status());
    }

    @Test
    void changeStatus_ResolvedToInProgress_ThrowsConflict() {
        Ticket ticket = createMockTicket(1L, "RESOLVED");
        when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));

        assertThrows(ConflictException.class, () ->
                ticketService.changeStatus(1L, new StatusUpdateRequest("IN_PROGRESS", ""))
        );
        verify(ticketRepository, never()).save(any());
    }

    @Test
    void changeStatus_NewToResolved_ThrowsConflict() {
        Ticket ticket = createMockTicket(1L, "NEW");
        when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));

        assertThrows(ConflictException.class, () ->
                ticketService.changeStatus(1L, new StatusUpdateRequest("RESOLVED", ""))
        );
    }

    @Test
    void changeStatus_CancelledToNew_ThrowsConflict() {
        Ticket ticket = createMockTicket(1L, "CANCELLED");
        when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));

        assertThrows(ConflictException.class, () ->
                ticketService.changeStatus(1L, new StatusUpdateRequest("NEW", ""))
        );
    }

    @Test
    void changeStatus_SameStatus_ThrowsConflict() {
        Ticket ticket = createMockTicket(1L, "NEW");
        when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));

        assertThrows(ConflictException.class, () ->
                ticketService.changeStatus(1L, new StatusUpdateRequest("NEW", ""))
        );
    }

    @Test
    void getHistory_TicketNotFound_ThrowsException() {
        when(ticketRepository.existsById(99L)).thenReturn(false);
        assertThrows(IllegalArgumentException.class, () -> ticketService.getHistory(99L));
    }

    private Ticket createMockTicket(Long id, String status) {
        Category category = new Category();
        category.setId(1L);
        category.setName("Test Cat");

        Ticket ticket = new Ticket();
        ticket.setId(id);
        ticket.setTitle("Test");
        ticket.setCategory(category);
        ticket.setPriority("HIGH");
        ticket.setStatus(status);
        ticket.setCreatedAt(OffsetDateTime.now(clock));
        return ticket;
    }
}