package com.shipping.freightops.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AgentUpdateRequest {

  @DecimalMin("0.0")
  @DecimalMax("100.0")
  private BigDecimal commissionPercent;

  private Boolean active;
}
