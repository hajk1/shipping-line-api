package com.shipping.freightops.dto;

import com.shipping.freightops.enums.AgentType;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public class AgentCreateRequest {

  @NotBlank private String name;

  @NotBlank @Email private String email;

  @NotNull
  @DecimalMin("0.0")
  @DecimalMax("100.0")
  private BigDecimal commissionPercent;

  @NotNull private AgentType type;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public BigDecimal getCommissionPercent() {
    return commissionPercent;
  }

  public void setCommissionPercent(BigDecimal commissionPercent) {
    this.commissionPercent = commissionPercent;
  }

  public AgentType getType() {
    return type;
  }

  public void setType(AgentType type) {
    this.type = type;
  }
}
