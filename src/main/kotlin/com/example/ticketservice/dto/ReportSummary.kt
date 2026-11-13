package com.example.ticketservice.dto

import java.time.Duration

data class ReportSummary(
    val totalCreated: Int,
    val byStatus: Map<String, Int>,
    val byPriority: Map<String, Int>,
    val openOverdue: Int,
    val avgResolutionTime: Duration?
)