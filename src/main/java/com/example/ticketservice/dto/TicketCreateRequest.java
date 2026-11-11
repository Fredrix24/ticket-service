package com.example.ticketservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.OffsetDateTime;

public record TicketCreateRequest(
        @NotBlank(message = "Title is required") String title,
        String description,
        @NotNull(message = "Category ID is required") Long categoryId,
        @NotBlank(message = "Priority is required") String priority,
        OffsetDateTime dueAt
) {}