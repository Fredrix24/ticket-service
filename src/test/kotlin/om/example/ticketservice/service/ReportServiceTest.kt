package com.example.ticketservice.service

import com.example.ticketservice.repository.TicketReportDataPort
import com.example.ticketservice.repository.TicketReportRow
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.time.Duration
import java.time.OffsetDateTime

class ReportServiceTest {

    private class FakeDataPort(private val tickets: List<TicketReportRow>) : TicketReportDataPort {
        override fun findTicketsCreatedBetween(from: OffsetDateTime, to: OffsetDateTime): List<TicketReportRow> {
            return tickets
        }
    }

    @Test
    fun `should calculate correct summary when no resolved tickets`() {
        val now = OffsetDateTime.now()
        val tickets = listOf(
            TicketReportRow("NEW", "HIGH", now, null, now.plusDays(1)),
            TicketReportRow("IN_PROGRESS", "MEDIUM", now, null, now.plusDays(2))
        )
        val service = ReportService(FakeDataPort(tickets))

        val summary = service.getSummary(now.minusDays(1), now.plusDays(1))

        assertEquals(2, summary.totalCreated)
        assertEquals(mapOf("NEW" to 1, "IN_PROGRESS" to 1), summary.byStatus)
        assertNull(summary.avgResolutionTime)
    }

    @Test
    fun `should calculate avg resolution time and overdue correctly`() {
        val now = OffsetDateTime.now()
        val created = now.minusDays(2)
        val closed = now.minusHours(12)
        val tickets = listOf(
            TicketReportRow("RESOLVED", "HIGH", created, closed, now),
            TicketReportRow("NEW", "LOW", now, null, now.minusHours(1))
        )
        val service = ReportService(FakeDataPort(tickets))

        val summary = service.getSummary(now.minusDays(3), now.plusDays(1))

        assertEquals(2, summary.totalCreated)
        assertEquals(1, summary.openOverdue)
        assertNotNull(summary.avgResolutionTime)
        assertEquals(129600, summary.avgResolutionTime?.seconds)
    }

    @Test
    fun `should throw exception when from is after to`() {
        val service = ReportService(FakeDataPort(emptyList()))
        val from = OffsetDateTime.now()
        val to = from.minusDays(1)

        val exception = assertThrows(IllegalArgumentException::class.java) {
            service.getSummary(from, to)
        }
        assertTrue(exception.message?.contains("Invalid period") == true)
    }
}