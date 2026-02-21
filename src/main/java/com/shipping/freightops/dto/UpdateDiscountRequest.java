package com.shipping.freightops.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class UpdateDiscountRequest {
  @NotNull(message = "discountPercent is required")
  @DecimalMin(value = "0", inclusive = true)
  @DecimalMax(value = "100", inclusive = true)
  private BigDecimal discountPercent;

  @NotBlank(message = "Reason is required")
  private String reason;

  public BigDecimal getDiscountPercent() {
    return discountPercent;
  }

  public void setDiscountPercent(BigDecimal discountPercent) {
    this.discountPercent = discountPercent;
  }

  public String getReason() {
    return reason;
  }

  public void setReason(String reason) {
    this.reason = reason;
  }
}
