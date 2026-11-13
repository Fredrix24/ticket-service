package com.example.ticketservice.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;

@Repository
public class TicketReportDataPortImpl implements TicketReportDataPort {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<TicketReportRow> findTicketsCreatedBetween(OffsetDateTime from, OffsetDateTime to) {
        String jpql = """
            SELECT new com.example.ticketservice.repository.TicketReportRow(
                t.status, t.priority, t.createdAt, t.closedAt, t.dueAt
            )
            FROM Ticket t
            WHERE t.createdAt >= :from AND t.createdAt < :to
        """;
        return entityManager.createQuery(jpql, TicketReportRow.class)
                .setParameter("from", from)
                .setParameter("to", to)
                .getResultList();
    }
}