package com.shipping.freightops.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

/** Payload for creating a new freight order. */
@Getter
@Setter
public class CreateFreightOrderRequest {

  @NotNull(message = "Voyage ID is required")
  private Long voyageId;

  @NotNull(message = "Container ID is required")
  private Long containerId;

  @NotNull(message = "Agent ID is required")
  private Long agentId;

  @NotNull(message = "Customer ID is required")
  private Long customerId;

  @NotBlank(message = "orderedBy is required")
  private String orderedBy;

  private String notes;

  @DecimalMax(value = "100", inclusive = true)
  @DecimalMin(value = "0", inclusive = true)
  private BigDecimal discountPercent;
}
