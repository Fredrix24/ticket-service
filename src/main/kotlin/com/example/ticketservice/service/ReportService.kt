package com.example.ticketservice.service

import com.example.ticketservice.dto.ReportSummary
import com.example.ticketservice.repository.TicketReportDataPort
import com.example.ticketservice.repository.TicketReportRow
import org.springframework.stereotype.Service
import java.time.Duration
import java.time.OffsetDateTime

@Service
class ReportService(
    private val dataPort: TicketReportDataPort
) {
    fun getSummary(from: OffsetDateTime, to: OffsetDateTime): ReportSummary {
        if (from >= to) {
            throw com.example.ticketservice.exception.BadRequestException("Invalid period: 'from' must be before 'to'")
        }

        val tickets: List<TicketReportRow> = dataPort.findTicketsCreatedBetween(from, to)
        val now = OffsetDateTime.now()

        val totalCreated = tickets.size

        val byStatus = tickets.groupingBy { it.status }.eachCount()

        val byPriority = tickets.groupingBy { it.priority }.eachCount()

        val openOverdue = tickets.count {
            (it.status == "NEW" || it.status == "IN_PROGRESS") &&
                    it.dueAt != null && it.dueAt.isBefore(now)
        }

        val resolvedTickets = tickets.filter { it.status == "RESOLVED" && it.closedAt != null }
        val avgResolutionTime: Duration? = if (resolvedTickets.isEmpty()) {
            null
        } else {
            val totalSeconds = resolvedTickets.sumOf {
                Duration.between(it.createdAt, it.closedAt).seconds
            }
            Duration.ofSeconds(totalSeconds / resolvedTickets.size)
        }

        return ReportSummary(
            totalCreated = totalCreated,
            byStatus = byStatus,
            byPriority = byPriority,
            openOverdue = openOverdue,
            avgResolutionTime = avgResolutionTime
        )
    }
}