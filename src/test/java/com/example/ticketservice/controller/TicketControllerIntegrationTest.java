package com.example.ticketservice.controller;

import com.example.ticketservice.dto.TicketCreateRequest;
import tools.jackson.databind.json.JsonMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection; // <-- Важный импорт!
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.postgresql.PostgreSQLContainer; // <-- Пакет для Testcontainers 2.x
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.OffsetDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
public class TicketControllerIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:16");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JsonMapper objectMapper;

    @Test
    void createTicket_Returns201() throws Exception {
        TicketCreateRequest request = new TicketCreateRequest(
                "Integration Test", "Desc", 1L, "HIGH", OffsetDateTime.now().plusDays(1)
        );

        mockMvc.perform(post("/api/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("NEW"))
                .andExpect(jsonPath("$.id").exists());
    }

    @Test
    void createTicket_NoTitle_Returns400() throws Exception {
        TicketCreateRequest request = new TicketCreateRequest(
                null, "Desc", 1L, "HIGH", null
        );

        mockMvc.perform(post("/api/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getTicket_NotFound_Returns404() throws Exception {
        mockMvc.perform(get("/api/tickets/9999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void changeStatus_AndGetHistory_ReturnsHistory() throws Exception {
        TicketCreateRequest request = new TicketCreateRequest("History Test", "Desc", 1L, "HIGH", null);
        String response = mockMvc.perform(post("/api/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long id = objectMapper.readTree(response).get("id").asLong();

        mockMvc.perform(patch("/api/tickets/" + id + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\": \"IN_PROGRESS\"}"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/tickets/" + id + "/history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void changeStatus_ForbiddenTransition_Returns409() throws Exception {
        TicketCreateRequest request = new TicketCreateRequest("Conflict Test", "Desc", 1L, "HIGH", null);
        String response = mockMvc.perform(post("/api/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andReturn().getResponse().getContentAsString();

        Long id = objectMapper.readTree(response).get("id").asLong();

        mockMvc.perform(patch("/api/tickets/" + id + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\": \"RESOLVED\"}"))
                .andExpect(status().isConflict());
    }

    @Test
    void getTickets_FilterByStatus_ReturnsFiltered() throws Exception {
        mockMvc.perform(get("/api/tickets")
                        .param("status", "NEW")
                        .param("page", "0")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void e2e_FullLifecycle_CreateInProgressResolveAndReport() throws Exception {
        TicketCreateRequest request = new TicketCreateRequest("E2E Lifecycle", "Desc", 1L, "HIGH", null);
        String createResponse = mockMvc.perform(post("/api/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        Long id = objectMapper.readTree(createResponse).get("id").asLong();

        mockMvc.perform(patch("/api/tickets/" + id + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\": \"IN_PROGRESS\"}"))
                .andExpect(status().isOk());

        mockMvc.perform(patch("/api/tickets/" + id + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\": \"RESOLVED\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.closedAt").isNotEmpty());

        mockMvc.perform(get("/api/reports/summary")
                        .param("from", OffsetDateTime.now().minusDays(1).toString())
                        .param("to", OffsetDateTime.now().plusDays(1).toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.byStatus.RESOLVED").value(1));
    }

    @Test
    void e2e_ForbiddenOperation_DataNotChanged() throws Exception {
        TicketCreateRequest request = new TicketCreateRequest("E2E Forbidden", "Desc", 1L, "HIGH", null);
        String createResponse = mockMvc.perform(post("/api/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andReturn().getResponse().getContentAsString();
        Long id = objectMapper.readTree(createResponse).get("id").asLong();

        mockMvc.perform(patch("/api/tickets/" + id + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\": \"RESOLVED\"}"))
                .andExpect(status().isConflict());

        mockMvc.perform(get("/api/tickets/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("NEW"));

        mockMvc.perform(get("/api/tickets/" + id + "/history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void regression_UpdateClosedTicket_Returns409() throws Exception {
        TicketCreateRequest request = new TicketCreateRequest("Regression", "Desc", 1L, "HIGH", null);
        String response = mockMvc.perform(post("/api/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andReturn().getResponse().getContentAsString();
        Long id = objectMapper.readTree(response).get("id").asLong();

        mockMvc.perform(patch("/api/tickets/" + id + "/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\": \"IN_PROGRESS\"}"));

        mockMvc.perform(patch("/api/tickets/" + id + "/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\": \"RESOLVED\"}"));

        mockMvc.perform(patch("/api/tickets/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"description\": \"Hacked!\"}"))
                .andExpect(status().isConflict());
    }
}