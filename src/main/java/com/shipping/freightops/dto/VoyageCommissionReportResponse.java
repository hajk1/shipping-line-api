package com.shipping.freightops.dto;

import java.math.BigDecimal;
import java.util.List;

public class VoyageCommissionReportResponse {

  private String voyageNumber;
  private List<AgentCommissionResponse> agents;
  private BigDecimal totalCommissionsUsd;

  public String getVoyageNumber() {
    return voyageNumber;
  }

  public void setVoyageNumber(String voyageNumber) {
    this.voyageNumber = voyageNumber;
  }

  public List<AgentCommissionResponse> getAgents() {
    return agents;
  }

  public void setAgents(List<AgentCommissionResponse> agents) {
    this.agents = agents;
  }

  public BigDecimal getTotalCommissionsUsd() {
    return totalCommissionsUsd;
  }

  public void setTotalCommissionsUsd(BigDecimal totalCommissionsUsd) {
    this.totalCommissionsUsd = totalCommissionsUsd;
  }
}
