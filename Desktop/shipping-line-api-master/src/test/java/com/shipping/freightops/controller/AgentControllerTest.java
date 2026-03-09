package com.shipping.freightops.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shipping.freightops.dto.AgentCreateRequest;
import com.shipping.freightops.dto.AgentUpdateRequest;
import com.shipping.freightops.entity.Agent;
import com.shipping.freightops.enums.AgentType;
import com.shipping.freightops.repository.AgentRepository;
import com.shipping.freightops.repository.FreightOrderRepository;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/** Integration tests for {@link AgentController}. */
@SpringBootTest
@AutoConfigureMockMvc
class AgentControllerTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;
  @Autowired private AgentRepository agentRepository;
  @Autowired private FreightOrderRepository freightOrderRepository;

  @BeforeEach
  void setUp() {
    freightOrderRepository.deleteAll();
    agentRepository.deleteAll();
  }

  // ── CREATE ──

  @Test
  @DisplayName("POST /api/v1/agents → 201 Created")
  void createAgent_returnsCreated() throws Exception {
    AgentCreateRequest request = new AgentCreateRequest();
    request.setName("Alice Johnson");
    request.setEmail("alice@freightops.com");
    request.setCommissionPercent(new BigDecimal("5.00"));
    request.setType(AgentType.INTERNAL);

    mockMvc
        .perform(
            post("/api/v1/agents")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.name").value("Alice Johnson"))
        .andExpect(jsonPath("$.email").value("alice@freightops.com"))
        .andExpect(jsonPath("$.commissionPercent").value(5.00))
        .andExpect(jsonPath("$.type").value("INTERNAL"))
        .andExpect(jsonPath("$.active").value(true))
        .andExpect(jsonPath("$.id").isNumber())
        .andExpect(jsonPath("$.createdAt").isNotEmpty())
        .andExpect(jsonPath("$.updatedAt").isNotEmpty());
  }

  @Test
  @DisplayName("POST /api/v1/agents with missing fields → 400 Bad Request")
  void createAgent_withMissingFields_returnsBadRequest() throws Exception {
    AgentCreateRequest request = new AgentCreateRequest();
    // intentionally leaving required fields empty

    mockMvc
        .perform(
            post("/api/v1/agents")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("POST /api/v1/agents with invalid email → 400 Bad Request")
  void createAgent_withInvalidEmail_returnsBadRequest() throws Exception {
    AgentCreateRequest request = new AgentCreateRequest();
    request.setName("Bad Email");
    request.setEmail("not-an-email");
    request.setCommissionPercent(new BigDecimal("5.00"));
    request.setType(AgentType.EXTERNAL);

    mockMvc
        .perform(
            post("/api/v1/agents")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());
  }

  // ── GET BY ID ──

  @Test
  @DisplayName("GET /api/v1/agents/{id} → 200 OK")
  void getById_returnsOk() throws Exception {
    Agent agent = saveAgent("Bob Smith", "bob@test.com", AgentType.EXTERNAL, true);

    mockMvc
        .perform(get("/api/v1/agents/" + agent.getId()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name").value("Bob Smith"))
        .andExpect(jsonPath("$.email").value("bob@test.com"))
        .andExpect(jsonPath("$.type").value("EXTERNAL"))
        .andExpect(jsonPath("$.active").value(true));
  }

  @Test
  @DisplayName("GET /api/v1/agents/{id} not found → 404")
  void getById_notFound_returns404() throws Exception {
    mockMvc.perform(get("/api/v1/agents/9999")).andExpect(status().isNotFound());
  }

  // ── LIST WITH FILTERS ──

  @Test
  @DisplayName("GET /api/v1/agents → 200 OK with all agents")
  void listAgents_returnsAll() throws Exception {
    saveAgent("Agent A", "a@test.com", AgentType.INTERNAL, true);
    saveAgent("Agent B", "b@test.com", AgentType.EXTERNAL, false);

    mockMvc
        .perform(get("/api/v1/agents"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(2));
  }

  @Test
  @DisplayName("GET /api/v1/agents?type=INTERNAL → filters by type")
  void listAgents_filterByType() throws Exception {
    saveAgent("Internal", "i@test.com", AgentType.INTERNAL, true);
    saveAgent("External", "e@test.com", AgentType.EXTERNAL, true);

    mockMvc
        .perform(get("/api/v1/agents").param("type", "INTERNAL"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(1))
        .andExpect(jsonPath("$[0].name").value("Internal"));
  }

  @Test
  @DisplayName("GET /api/v1/agents?active=true → filters by active")
  void listAgents_filterByActive() throws Exception {
    saveAgent("Active", "active@test.com", AgentType.INTERNAL, true);
    saveAgent("Inactive", "inactive@test.com", AgentType.INTERNAL, false);

    mockMvc
        .perform(get("/api/v1/agents").param("active", "true"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(1))
        .andExpect(jsonPath("$[0].name").value("Active"));
  }

  @Test
  @DisplayName("GET /api/v1/agents?type=EXTERNAL&active=true → filters by both")
  void listAgents_filterByTypeAndActive() throws Exception {
    saveAgent("Int Active", "ia@test.com", AgentType.INTERNAL, true);
    saveAgent("Ext Active", "ea@test.com", AgentType.EXTERNAL, true);
    saveAgent("Ext Inactive", "ei@test.com", AgentType.EXTERNAL, false);

    mockMvc
        .perform(get("/api/v1/agents").param("type", "EXTERNAL").param("active", "true"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(1))
        .andExpect(jsonPath("$[0].name").value("Ext Active"));
  }

  // ── PATCH ──

  @Test
  @DisplayName("PATCH /api/v1/agents/{id} → update commissionPercent")
  void patchAgent_updateCommission() throws Exception {
    Agent agent = saveAgent("Patchable", "p@test.com", AgentType.INTERNAL, true);

    AgentUpdateRequest patch = new AgentUpdateRequest();
    patch.setCommissionPercent(new BigDecimal("12.50"));

    mockMvc
        .perform(
            patch("/api/v1/agents/" + agent.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(patch)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.commissionPercent").value(12.50))
        .andExpect(jsonPath("$.active").value(true));
  }

  @Test
  @DisplayName("PATCH /api/v1/agents/{id} → deactivate agent")
  void patchAgent_deactivate() throws Exception {
    Agent agent = saveAgent("Deactivatable", "d@test.com", AgentType.EXTERNAL, true);

    AgentUpdateRequest patch = new AgentUpdateRequest();
    patch.setActive(false);

    mockMvc
        .perform(
            patch("/api/v1/agents/" + agent.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(patch)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.active").value(false));
  }

  @Test
  @DisplayName("PATCH /api/v1/agents/{id} not found → 404")
  void patchAgent_notFound_returns404() throws Exception {
    AgentUpdateRequest patch = new AgentUpdateRequest();
    patch.setActive(false);

    mockMvc
        .perform(
            patch("/api/v1/agents/9999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(patch)))
        .andExpect(status().isNotFound());
  }

  // ── HELPER ──

  private Agent saveAgent(String name, String email, AgentType type, boolean active) {
    Agent agent = new Agent();
    agent.setName(name);
    agent.setEmail(email);
    agent.setCommissionPercent(new BigDecimal("5.00"));
    agent.setType(type);
    agent.setActive(active);
    return agentRepository.save(agent);
  }
}
