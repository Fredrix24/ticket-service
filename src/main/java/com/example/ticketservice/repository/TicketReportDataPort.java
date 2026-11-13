package com.example.ticketservice.repository;

import java.time.OffsetDateTime;
import java.util.List;

public interface TicketReportDataPort {
    List<TicketReportRow> findTicketsCreatedBetween(OffsetDateTime from, OffsetDateTime to);
}