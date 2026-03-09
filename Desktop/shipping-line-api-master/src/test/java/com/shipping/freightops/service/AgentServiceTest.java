package com.shipping.freightops.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.shipping.freightops.dto.AgentCreateRequest;
import com.shipping.freightops.dto.AgentUpdateRequest;
import com.shipping.freightops.entity.Agent;
import com.shipping.freightops.enums.AgentType;
import com.shipping.freightops.repository.AgentRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** Unit tests for {@link AgentService}. */
@ExtendWith(MockitoExtension.class)
class AgentServiceTest {

  @Mock private AgentRepository agentRepository;

  @InjectMocks private AgentService agentService;

  // ── CREATE ────────────────────────────────────────────────

  @Nested
  @DisplayName("createAgent")
  class CreateAgent {

    @Test
    @DisplayName("maps request fields to entity and saves")
    void mapsFieldsAndSaves() {
      AgentCreateRequest request = new AgentCreateRequest();
      request.setName("Alice");
      request.setEmail("alice@test.com");
      request.setCommissionPercent(new BigDecimal("7.50"));
      request.setType(AgentType.INTERNAL);

      Agent saved = buildAgent(1L, "Alice", "alice@test.com", AgentType.INTERNAL, true);
      saved.setCommissionPercent(new BigDecimal("7.50"));
      when(agentRepository.save(any(Agent.class))).thenReturn(saved);

      Agent result = agentService.createAgent(request);

      ArgumentCaptor<Agent> captor = ArgumentCaptor.forClass(Agent.class);
      verify(agentRepository).save(captor.capture());
      Agent captured = captor.getValue();

      assertThat(captured.getName()).isEqualTo("Alice");
      assertThat(captured.getEmail()).isEqualTo("alice@test.com");
      assertThat(captured.getCommissionPercent()).isEqualByComparingTo("7.50");
      assertThat(captured.getType()).isEqualTo(AgentType.INTERNAL);
      assertThat(result.getId()).isEqualTo(1L);
    }
  }

  // ── GET BY ID ─────────────────────────────────────────────

  @Nested
  @DisplayName("getAgent")
  class GetAgent {

    @Test
    @DisplayName("returns agent when found")
    void returnsAgentWhenFound() {
      Agent agent = buildAgent(1L, "Bob", "bob@test.com", AgentType.EXTERNAL, true);
      when(agentRepository.findById(1L)).thenReturn(Optional.of(agent));

      Agent result = agentService.getAgent(1L);

      assertThat(result.getName()).isEqualTo("Bob");
    }

    @Test
    @DisplayName("throws when agent not found")
    void throwsWhenNotFound() {
      when(agentRepository.findById(99L)).thenReturn(Optional.empty());

      assertThatThrownBy(() -> agentService.getAgent(99L))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("Agent not found: 99");
    }
  }

  // ── LIST WITH FILTERS ────────────────────────────────────

  @Nested
  @DisplayName("listAgents")
  class ListAgents {

    @Test
    @DisplayName("no filters → findAll")
    void noFilters_callsFindAll() {
      Agent a = buildAgent(1L, "A", "a@test.com", AgentType.INTERNAL, true);
      Agent b = buildAgent(2L, "B", "b@test.com", AgentType.EXTERNAL, false);
      when(agentRepository.findAll()).thenReturn(List.of(a, b));

      List<Agent> result = agentService.listAgents(null, null);

      assertThat(result).hasSize(2);
      verify(agentRepository).findAll();
    }

    @Test
    @DisplayName("type filter only → findByType")
    void typeFilterOnly() {
      Agent a = buildAgent(1L, "A", "a@test.com", AgentType.INTERNAL, true);
      when(agentRepository.findByType(AgentType.INTERNAL)).thenReturn(List.of(a));

      List<Agent> result = agentService.listAgents(AgentType.INTERNAL, null);

      assertThat(result).hasSize(1);
      verify(agentRepository).findByType(AgentType.INTERNAL);
    }

    @Test
    @DisplayName("active filter only → findByActive")
    void activeFilterOnly() {
      Agent a = buildAgent(1L, "A", "a@test.com", AgentType.INTERNAL, true);
      when(agentRepository.findByActive(true)).thenReturn(List.of(a));

      List<Agent> result = agentService.listAgents(null, true);

      assertThat(result).hasSize(1);
      verify(agentRepository).findByActive(true);
    }

    @Test
    @DisplayName("both filters → findByTypeAndActive")
    void bothFilters() {
      Agent a = buildAgent(1L, "A", "a@test.com", AgentType.EXTERNAL, true);
      when(agentRepository.findByTypeAndActive(AgentType.EXTERNAL, true)).thenReturn(List.of(a));

      List<Agent> result = agentService.listAgents(AgentType.EXTERNAL, true);

      assertThat(result).hasSize(1);
      verify(agentRepository).findByTypeAndActive(AgentType.EXTERNAL, true);
    }
  }

  // ── UPDATE (PATCH) ────────────────────────────────────────

  @Nested
  @DisplayName("updateAgent")
  class UpdateAgent {

    @Test
    @DisplayName("updates commissionPercent when provided")
    void updatesCommission() {
      Agent existing = buildAgent(1L, "X", "x@test.com", AgentType.INTERNAL, true);
      existing.setCommissionPercent(new BigDecimal("5.00"));
      when(agentRepository.findById(1L)).thenReturn(Optional.of(existing));
      when(agentRepository.save(any(Agent.class))).thenAnswer(inv -> inv.getArgument(0));

      AgentUpdateRequest patch = new AgentUpdateRequest();
      patch.setCommissionPercent(new BigDecimal("12.00"));

      Agent result = agentService.updateAgent(1L, patch);

      assertThat(result.getCommissionPercent()).isEqualByComparingTo("12.00");
      assertThat(result.isActive()).isTrue(); // unchanged
    }

    @Test
    @DisplayName("updates active flag when provided")
    void updatesActiveFlag() {
      Agent existing = buildAgent(1L, "Y", "y@test.com", AgentType.EXTERNAL, true);
      existing.setCommissionPercent(new BigDecimal("5.00"));
      when(agentRepository.findById(1L)).thenReturn(Optional.of(existing));
      when(agentRepository.save(any(Agent.class))).thenAnswer(inv -> inv.getArgument(0));

      AgentUpdateRequest patch = new AgentUpdateRequest();
      patch.setActive(false);

      Agent result = agentService.updateAgent(1L, patch);

      assertThat(result.isActive()).isFalse();
      assertThat(result.getCommissionPercent()).isEqualByComparingTo("5.00"); // unchanged
    }

    @Test
    @DisplayName("updates both fields when both provided")
    void updatesBothFields() {
      Agent existing = buildAgent(1L, "Z", "z@test.com", AgentType.INTERNAL, true);
      existing.setCommissionPercent(new BigDecimal("5.00"));
      when(agentRepository.findById(1L)).thenReturn(Optional.of(existing));
      when(agentRepository.save(any(Agent.class))).thenAnswer(inv -> inv.getArgument(0));

      AgentUpdateRequest patch = new AgentUpdateRequest();
      patch.setCommissionPercent(new BigDecimal("20.00"));
      patch.setActive(false);

      Agent result = agentService.updateAgent(1L, patch);

      assertThat(result.getCommissionPercent()).isEqualByComparingTo("20.00");
      assertThat(result.isActive()).isFalse();
    }

    @Test
    @DisplayName("throws when agent not found")
    void throwsWhenNotFound() {
      when(agentRepository.findById(99L)).thenReturn(Optional.empty());

      AgentUpdateRequest patch = new AgentUpdateRequest();
      patch.setActive(false);

      assertThatThrownBy(() -> agentService.updateAgent(99L, patch))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("Agent not found: 99");
    }
  }

  // ── HELPERS ───────────────────────────────────────────────

  private Agent buildAgent(Long id, String name, String email, AgentType type, boolean active) {
    Agent agent = new Agent();
    agent.setId(id);
    agent.setName(name);
    agent.setEmail(email);
    agent.setType(type);
    agent.setActive(active);
    agent.setCommissionPercent(new BigDecimal("5.00"));
    return agent;
  }
}
