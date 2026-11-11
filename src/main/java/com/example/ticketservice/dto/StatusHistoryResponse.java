package com.example.ticketservice.dto;

import java.time.OffsetDateTime;

public record StatusHistoryResponse(
        Long id,
        Long ticketId,
        String fromStatus,
        String toStatus,
        OffsetDateTime changedAt
) {}