package com.shipping.freightops.dto;

import com.shipping.freightops.enums.RiskImpact;

public class RiskFactor {
  private String factor;
  private RiskImpact impact;
  private String description;

  public RiskFactor() {}

  public RiskFactor(String factor, RiskImpact impact, String description) {
    this.factor = factor;
    this.impact = impact;
    this.description = description;
  }

  public String getFactor() {
    return factor;
  }

  public void setFactor(String factor) {
    this.factor = factor;
  }

  public RiskImpact getImpact() {
    return impact;
  }

  public void setImpact(RiskImpact impact) {
    this.impact = impact;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }
}
