package com.shipping.freightops.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.shipping.freightops.entity.Agent;
import com.shipping.freightops.enums.AgentType;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

/** Integration tests for {@link AgentRepository}. */
@DataJpaTest
class AgentRepositoryTest {

  @Autowired private TestEntityManager entityManager;
  @Autowired private AgentRepository agentRepository;

  @BeforeEach
  void setUp() {
    agentRepository.deleteAll();
  }

  @Nested
  @DisplayName("findByType")
  class FindByType {

    @Test
    @DisplayName("returns only agents with matching type")
    void returnsMatchingType() {
      Agent internal1 = createAgent("Internal One", "i1@test.com", AgentType.INTERNAL, true);
      Agent internal2 = createAgent("Internal Two", "i2@test.com", AgentType.INTERNAL, false);
      Agent external = createAgent("External", "e@test.com", AgentType.EXTERNAL, true);

      entityManager.persist(internal1);
      entityManager.persist(internal2);
      entityManager.persist(external);
      entityManager.flush();

      List<Agent> result = agentRepository.findByType(AgentType.INTERNAL);

      assertThat(result)
          .hasSize(2)
          .extracting(Agent::getName)
          .containsExactlyInAnyOrder("Internal One", "Internal Two");
    }

    @Test
    @DisplayName("returns empty list when no agents match type")
    void returnsEmptyWhenNoMatch() {
      Agent internal = createAgent("Internal", "i@test.com", AgentType.INTERNAL, true);
      entityManager.persist(internal);
      entityManager.flush();

      List<Agent> result = agentRepository.findByType(AgentType.EXTERNAL);

      assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("returns empty list when repository is empty")
    void returnsEmptyWhenRepositoryEmpty() {
      List<Agent> result = agentRepository.findByType(AgentType.INTERNAL);

      assertThat(result).isEmpty();
    }
  }

  @Nested
  @DisplayName("findByActive")
  class FindByActive {

    @Test
    @DisplayName("returns only active agents when active=true")
    void returnsActiveAgents() {
      Agent active1 = createAgent("Active One", "a1@test.com", AgentType.INTERNAL, true);
      Agent active2 = createAgent("Active Two", "a2@test.com", AgentType.EXTERNAL, true);
      Agent inactive = createAgent("Inactive", "i@test.com", AgentType.INTERNAL, false);

      entityManager.persist(active1);
      entityManager.persist(active2);
      entityManager.persist(inactive);
      entityManager.flush();

      List<Agent> result = agentRepository.findByActive(true);

      assertThat(result)
          .hasSize(2)
          .extracting(Agent::getName)
          .containsExactlyInAnyOrder("Active One", "Active Two");
    }

    @Test
    @DisplayName("returns only inactive agents when active=false")
    void returnsInactiveAgents() {
      Agent active = createAgent("Active", "a@test.com", AgentType.INTERNAL, true);
      Agent inactive1 = createAgent("Inactive One", "i1@test.com", AgentType.EXTERNAL, false);
      Agent inactive2 = createAgent("Inactive Two", "i2@test.com", AgentType.INTERNAL, false);

      entityManager.persist(active);
      entityManager.persist(inactive1);
      entityManager.persist(inactive2);
      entityManager.flush();

      List<Agent> result = agentRepository.findByActive(false);

      assertThat(result)
          .hasSize(2)
          .extracting(Agent::getName)
          .containsExactlyInAnyOrder("Inactive One", "Inactive Two");
    }

    @Test
    @DisplayName("returns empty list when no agents match active status")
    void returnsEmptyWhenNoMatch() {
      Agent active = createAgent("Active", "a@test.com", AgentType.INTERNAL, true);
      entityManager.persist(active);
      entityManager.flush();

      List<Agent> result = agentRepository.findByActive(false);

      assertThat(result).isEmpty();
    }
  }

  @Nested
  @DisplayName("findByTypeAndActive")
  class FindByTypeAndActive {

    @Test
    @DisplayName("returns agents matching both type and active status")
    void returnsMatchingBothCriteria() {
      Agent target1 = createAgent("Target One", "t1@test.com", AgentType.EXTERNAL, true);
      Agent target2 = createAgent("Target Two", "t2@test.com", AgentType.EXTERNAL, true);
      Agent wrongType = createAgent("Wrong Type", "wt@test.com", AgentType.INTERNAL, true);
      Agent wrongActive = createAgent("Wrong Active", "wa@test.com", AgentType.EXTERNAL, false);

      entityManager.persist(target1);
      entityManager.persist(target2);
      entityManager.persist(wrongType);
      entityManager.persist(wrongActive);
      entityManager.flush();

      List<Agent> result = agentRepository.findByTypeAndActive(AgentType.EXTERNAL, true);

      assertThat(result)
          .hasSize(2)
          .extracting(Agent::getName)
          .containsExactlyInAnyOrder("Target One", "Target Two");
    }

    @Test
    @DisplayName("returns empty list when no agents match both criteria")
    void returnsEmptyWhenNoMatch() {
      Agent agent1 = createAgent("Agent One", "a1@test.com", AgentType.INTERNAL, true);
      Agent agent2 = createAgent("Agent Two", "a2@test.com", AgentType.EXTERNAL, false);

      entityManager.persist(agent1);
      entityManager.persist(agent2);
      entityManager.flush();

      List<Agent> result = agentRepository.findByTypeAndActive(AgentType.EXTERNAL, true);

      assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("handles all combinations of type and active status")
    void handlesAllCombinations() {
      Agent intActive = createAgent("Internal Active", "ia@test.com", AgentType.INTERNAL, true);
      Agent intInactive =
          createAgent("Internal Inactive", "ii@test.com", AgentType.INTERNAL, false);
      Agent extActive = createAgent("External Active", "ea@test.com", AgentType.EXTERNAL, true);
      Agent extInactive =
          createAgent("External Inactive", "ei@test.com", AgentType.EXTERNAL, false);

      entityManager.persist(intActive);
      entityManager.persist(intInactive);
      entityManager.persist(extActive);
      entityManager.persist(extInactive);
      entityManager.flush();

      // Test all 4 combinations
      List<Agent> intActiveResult = agentRepository.findByTypeAndActive(AgentType.INTERNAL, true);
      assertThat(intActiveResult)
          .hasSize(1)
          .extracting(Agent::getName)
          .containsExactly("Internal Active");

      List<Agent> intInactiveResult =
          agentRepository.findByTypeAndActive(AgentType.INTERNAL, false);
      assertThat(intInactiveResult)
          .hasSize(1)
          .extracting(Agent::getName)
          .containsExactly("Internal Inactive");

      List<Agent> extActiveResult = agentRepository.findByTypeAndActive(AgentType.EXTERNAL, true);
      assertThat(extActiveResult)
          .hasSize(1)
          .extracting(Agent::getName)
          .containsExactly("External Active");

      List<Agent> extInactiveResult =
          agentRepository.findByTypeAndActive(AgentType.EXTERNAL, false);
      assertThat(extInactiveResult)
          .hasSize(1)
          .extracting(Agent::getName)
          .containsExactly("External Inactive");
    }
  }

  @Nested
  @DisplayName("Basic JPA operations")
  class BasicOperations {

    @Test
    @DisplayName("save persists agent with all fields")
    void savePersistsAllFields() {
      Agent agent = createAgent("Test Agent", "test@test.com", AgentType.INTERNAL, true);
      agent.setCommissionPercent(new BigDecimal("15.75"));

      Agent saved = agentRepository.save(agent);
      entityManager.flush();
      entityManager.clear();

      Agent found = agentRepository.findById(saved.getId()).orElseThrow();

      assertThat(found.getName()).isEqualTo("Test Agent");
      assertThat(found.getEmail()).isEqualTo("test@test.com");
      assertThat(found.getCommissionPercent()).isEqualByComparingTo("15.75");
      assertThat(found.getType()).isEqualTo(AgentType.INTERNAL);
      assertThat(found.isActive()).isTrue();
      assertThat(found.getCreatedAt()).isNotNull();
      assertThat(found.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("findById returns empty when agent doesn't exist")
    void findByIdReturnsEmpty() {
      assertThat(agentRepository.findById(999L)).isEmpty();
    }

    @Test
    @DisplayName("findAll returns all agents")
    void findAllReturnsAll() {
      Agent agent1 = createAgent("Agent 1", "a1@test.com", AgentType.INTERNAL, true);
      Agent agent2 = createAgent("Agent 2", "a2@test.com", AgentType.EXTERNAL, false);
      Agent agent3 = createAgent("Agent 3", "a3@test.com", AgentType.INTERNAL, true);

      entityManager.persist(agent1);
      entityManager.persist(agent2);
      entityManager.persist(agent3);
      entityManager.flush();

      List<Agent> all = agentRepository.findAll();

      assertThat(all).hasSize(3);
    }

    @Test
    @DisplayName("delete removes agent from repository")
    void deleteRemovesAgent() {
      Agent agent = createAgent("To Delete", "delete@test.com", AgentType.INTERNAL, true);
      Agent saved = entityManager.persist(agent);
      entityManager.flush();

      Long id = saved.getId();
      agentRepository.delete(saved);
      entityManager.flush();

      assertThat(agentRepository.findById(id)).isEmpty();
    }

    @Test
    @DisplayName("update modifies existing agent")
    void updateModifiesAgent() {
      Agent agent = createAgent("Original", "original@test.com", AgentType.INTERNAL, true);
      Agent saved = entityManager.persist(agent);
      entityManager.flush();
      entityManager.clear();

      Agent found = agentRepository.findById(saved.getId()).orElseThrow();
      found.setName("Updated Name");
      found.setActive(false);
      found.setCommissionPercent(new BigDecimal("25.00"));

      agentRepository.save(found);
      entityManager.flush();
      entityManager.clear();

      Agent updated = agentRepository.findById(saved.getId()).orElseThrow();

      assertThat(updated.getName()).isEqualTo("Updated Name");
      assertThat(updated.isActive()).isFalse();
      assertThat(updated.getCommissionPercent()).isEqualByComparingTo("25.00");
      assertThat(updated.getEmail()).isEqualTo("original@test.com"); // unchanged
    }
  }

  // ── HELPER ──

  private Agent createAgent(String name, String email, AgentType type, boolean active) {
    Agent agent = new Agent();
    agent.setName(name);
    agent.setEmail(email);
    agent.setCommissionPercent(new BigDecimal("5.00"));
    agent.setType(type);
    agent.setActive(active);
    return agent;
  }
}
