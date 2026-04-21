package com.shipping.freightops.dto;

import com.shipping.freightops.enums.RiskImpact;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class RiskFactor {
  private String factor;
  private RiskImpact impact;
  private String description;

  public RiskFactor(String factor, RiskImpact impact, String description) {
    this.factor = factor;
    this.impact = impact;
    this.description = description;
  }
}
