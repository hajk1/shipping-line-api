package com.shipping.freightops.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public class CreateVoyageCostRequest {
  @NotBlank private String description;

  @NotNull @Positive private BigDecimal amountUsd;

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public BigDecimal getAmountUsd() {
    return amountUsd;
  }

  public void setAmountUsd(BigDecimal amountUsd) {
    this.amountUsd = amountUsd;
  }
}
