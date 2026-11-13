package com.example.ticketservice.repository;

import java.time.OffsetDateTime;

public record TicketReportRow(
        String status,
        String priority,
        OffsetDateTime createdAt,
        OffsetDateTime closedAt,
        OffsetDateTime dueAt
) {}