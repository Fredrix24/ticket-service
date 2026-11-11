package com.example.ticketservice.repository;

import com.example.ticketservice.entity.StatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface StatusHistoryRepository extends JpaRepository<StatusHistory, Long> {
    List<StatusHistory> findByTicketIdOrderByChangedAtAsc(Long ticketId);
}