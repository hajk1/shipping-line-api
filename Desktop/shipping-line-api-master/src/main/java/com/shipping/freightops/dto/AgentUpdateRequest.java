package com.shipping.freightops.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import java.math.BigDecimal;

public class AgentUpdateRequest {

  @DecimalMin("0.0")
  @DecimalMax("100.0")
  private BigDecimal commissionPercent;

  private Boolean active;

  public BigDecimal getCommissionPercent() {
    return commissionPercent;
  }

  public void setCommissionPercent(BigDecimal commissionPercent) {
    this.commissionPercent = commissionPercent;
  }

  public Boolean getActive() {
    return active;
  }

  public void setActive(Boolean active) {
    this.active = active;
  }
}
