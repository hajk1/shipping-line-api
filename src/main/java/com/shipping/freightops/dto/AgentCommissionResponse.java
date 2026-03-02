package com.shipping.freightops.dto;

import com.shipping.freightops.enums.AgentType;
import java.math.BigDecimal;

public class AgentCommissionResponse {

  private String agentName;
  private AgentType type;
  private BigDecimal commissionPercent;
  private int orderCount;
  private BigDecimal totalOrderValueUsd;
  private BigDecimal commissionEarnedUsd;

  public String getAgentName() {
    return agentName;
  }

  public void setAgentName(String agentName) {
    this.agentName = agentName;
  }

  public AgentType getType() {
    return type;
  }

  public void setType(AgentType type) {
    this.type = type;
  }

  public BigDecimal getCommissionPercent() {
    return commissionPercent;
  }

  public void setCommissionPercent(BigDecimal commissionPercent) {
    this.commissionPercent = commissionPercent;
  }

  public int getOrderCount() {
    return orderCount;
  }

  public void setOrderCount(int orderCount) {
    this.orderCount = orderCount;
  }

  public BigDecimal getTotalOrderValueUsd() {
    return totalOrderValueUsd;
  }

  public void setTotalOrderValueUsd(BigDecimal totalOrderValueUsd) {
    this.totalOrderValueUsd = totalOrderValueUsd;
  }

  public BigDecimal getCommissionEarnedUsd() {
    return commissionEarnedUsd;
  }

  public void setCommissionEarnedUsd(BigDecimal commissionEarnedUsd) {
    this.commissionEarnedUsd = commissionEarnedUsd;
  }
}
