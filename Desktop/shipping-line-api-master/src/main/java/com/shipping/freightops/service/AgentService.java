package com.shipping.freightops.service;

import com.shipping.freightops.dto.AgentCreateRequest;
import com.shipping.freightops.dto.AgentUpdateRequest;
import com.shipping.freightops.entity.Agent;
import com.shipping.freightops.enums.AgentType;
import com.shipping.freightops.repository.AgentRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Handles Agent CRUD operations and queries. */
@Service
public class AgentService {

  private final AgentRepository agentRepository;

  public AgentService(AgentRepository agentRepository) {
    this.agentRepository = agentRepository;
  }

  @Transactional
  public Agent createAgent(AgentCreateRequest request) {
    Agent agent = new Agent();
    agent.setName(request.getName());
    agent.setEmail(request.getEmail());
    agent.setCommissionPercent(request.getCommissionPercent());
    agent.setType(request.getType());
    return agentRepository.save(agent);
  }

  @Transactional(readOnly = true)
  public Agent getAgent(Long id) {
    return agentRepository
        .findById(id)
        .orElseThrow(() -> new IllegalArgumentException("Agent not found: " + id));
  }

  @Transactional(readOnly = true)
  public List<Agent> listAgents(AgentType type, Boolean active) {
    if (type != null && active != null) {
      return agentRepository.findByTypeAndActive(type, active);
    }
    if (type != null) {
      return agentRepository.findByType(type);
    }
    if (active != null) {
      return agentRepository.findByActive(active);
    }
    return agentRepository.findAll();
  }

  @Transactional
  public Agent updateAgent(Long id, AgentUpdateRequest request) {
    Agent agent =
        agentRepository
            .findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Agent not found: " + id));

    if (request.getCommissionPercent() != null) {
      agent.setCommissionPercent(request.getCommissionPercent());
    }
    if (request.getActive() != null) {
      agent.setActive(request.getActive());
    }

    return agentRepository.save(agent);
  }
}
