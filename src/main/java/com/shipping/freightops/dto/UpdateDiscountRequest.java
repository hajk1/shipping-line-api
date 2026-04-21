package com.shipping.freightops.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UpdateDiscountRequest {
  @NotNull(message = "discountPercent is required")
  @DecimalMin(value = "0", inclusive = true)
  @DecimalMax(value = "100", inclusive = true)
  private BigDecimal discountPercent;

  @NotBlank(message = "Reason is required")
  private String reason;
}
