package com.example.ticketservice.service;

import com.example.ticketservice.entity.Ticket;
import org.springframework.data.jpa.domain.Specification;
import java.time.OffsetDateTime;

public class TicketSpecifications {

    public static Specification<Ticket> hasStatus(String status) {
        return (root, query, cb) -> status == null ? null : cb.equal(root.get("status"), status);
    }

    public static Specification<Ticket> hasPriority(String priority) {
        return (root, query, cb) -> priority == null ? null : cb.equal(root.get("priority"), priority);
    }

    public static Specification<Ticket> hasCategory(Long categoryId) {
        return (root, query, cb) -> categoryId == null ? null : cb.equal(root.get("category").get("id"), categoryId);
    }

    public static Specification<Ticket> isOverdue(OffsetDateTime now) {
        return (root, query, cb) -> {
            var openStatuses = cb.in(root.get("status")).value("NEW").value("IN_PROGRESS");
            var dueDateExpired = cb.lessThan(root.get("dueAt"), now);
            return cb.and(openStatuses, dueDateExpired);
        };
    }
}