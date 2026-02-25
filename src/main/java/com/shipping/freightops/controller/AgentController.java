package com.shipping.freightops.controller;

import com.shipping.freightops.dto.AgentCreateRequest;
import com.shipping.freightops.dto.AgentResponse;
import com.shipping.freightops.dto.AgentUpdateRequest;
import com.shipping.freightops.entity.Agent;
import com.shipping.freightops.enums.AgentType;
import com.shipping.freightops.service.AgentService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** REST controller for Agent CRUD operations. */
@RestController
@RequestMapping("/api/v1/agents")
public class AgentController {

  private final AgentService agentService;

  public AgentController(AgentService agentService) {
    this.agentService = agentService;
  }

  /** Create a new agent. */
  @PostMapping
  public ResponseEntity<AgentResponse> create(@Valid @RequestBody AgentCreateRequest request) {
    Agent agent = agentService.createAgent(request);
    AgentResponse body = toResponse(agent);
    URI location = URI.create("/api/v1/agents/" + agent.getId());
    return ResponseEntity.created(location).body(body);
  }

  /** List all agents, with optional type and active filters. */
  @GetMapping
  public ResponseEntity<List<AgentResponse>> list(
      @RequestParam(required = false) AgentType type,
      @RequestParam(required = false) Boolean active) {
    List<Agent> agents = agentService.listAgents(type, active);
    List<AgentResponse> body = agents.stream().map(this::toResponse).toList();
    return ResponseEntity.ok(body);
  }

  /** Get a single agent by ID. */
  @GetMapping("/{id}")
  public ResponseEntity<AgentResponse> getById(@PathVariable Long id) {
    Agent agent = agentService.getAgent(id);
    return ResponseEntity.ok(toResponse(agent));
  }

  /** Partial update: commissionPercent and/or active. */
  @PatchMapping("/{id}")
  public ResponseEntity<AgentResponse> update(
      @PathVariable Long id, @Valid @RequestBody AgentUpdateRequest request) {
    Agent agent = agentService.updateAgent(id, request);
    return ResponseEntity.ok(toResponse(agent));
  }

  private AgentResponse toResponse(Agent agent) {
    AgentResponse dto = new AgentResponse();
    dto.setId(agent.getId());
    dto.setName(agent.getName());
    dto.setEmail(agent.getEmail());
    dto.setCommissionPercent(agent.getCommissionPercent());
    dto.setType(agent.getType());
    dto.setActive(agent.isActive());
    dto.setCreatedAt(agent.getCreatedAt());
    dto.setUpdatedAt(agent.getUpdatedAt());
    return dto;
  }
}
