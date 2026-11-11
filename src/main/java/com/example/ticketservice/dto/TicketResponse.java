package com.example.ticketservice.dto;

import java.time.OffsetDateTime;

public record TicketResponse(
        Long id,
        String title,
        String description,
        Long categoryId,
        String categoryName,
        String priority,
        String status,
        OffsetDateTime createdAt,
        OffsetDateTime dueAt,
        OffsetDateTime closedAt
) {}