package com.example.ticketservice.controller;

import com.example.ticketservice.dto.StatusUpdateRequest;
import com.example.ticketservice.entity.StatusHistory;
import com.example.ticketservice.dto.StatusHistoryResponse;
import java.util.List;
import com.example.ticketservice.dto.TicketCreateRequest;
import com.example.ticketservice.dto.TicketResponse;
import com.example.ticketservice.service.TicketService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
public class TicketController {
    private final TicketService ticketService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TicketResponse create(@Valid @RequestBody TicketCreateRequest request) {
        return ticketService.create(request);
    }

    @GetMapping("/{id}")
    public TicketResponse getById(@PathVariable Long id) {
        return ticketService.getById(id);
    }

    @PatchMapping("/{id}/status")
    public TicketResponse changeStatus(@PathVariable Long id, @Valid @RequestBody StatusUpdateRequest request) {
        return ticketService.changeStatus(id, request);
    }

    @GetMapping("/{id}/history")
    public List<StatusHistoryResponse> getHistory(@PathVariable Long id) {
        return ticketService.getHistory(id);
    }

    @GetMapping
    public org.springframework.data.domain.Page<TicketResponse> search(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String priority,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Boolean overdue,
            @org.springdoc.core.annotations.ParameterObject org.springframework.data.domain.Pageable pageable) {
        return ticketService.search(status, priority, categoryId, overdue, pageable);
    }
}