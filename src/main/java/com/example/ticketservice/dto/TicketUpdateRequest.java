package com.example.ticketservice.dto;

import java.time.OffsetDateTime;

public record TicketUpdateRequest(
        String description,
        String priority,
        OffsetDateTime dueAt
) {}