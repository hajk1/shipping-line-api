package com.shipping.freightops.repository;

import com.shipping.freightops.entity.Agent;
import com.shipping.freightops.enums.AgentType;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AgentRepository extends JpaRepository<Agent, Long> {
  List<Agent> findByTypeAndActive(AgentType type, boolean active);

  List<Agent> findByType(AgentType type);

  List<Agent> findByActive(boolean active);
}
