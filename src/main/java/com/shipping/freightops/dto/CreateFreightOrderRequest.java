package com.shipping.freightops.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

/** Payload for creating a new freight order. */
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

  public Long getVoyageId() {
    return voyageId;
  }

  public void setVoyageId(Long voyageId) {
    this.voyageId = voyageId;
  }

  public Long getContainerId() {
    return containerId;
  }

  public void setContainerId(Long containerId) {
    this.containerId = containerId;
  }

  public Long getAgentId() {
    return agentId;
  }

  public void setAgentId(Long agentId) {
    this.agentId = agentId;
  }

  public Long getCustomerId() {
    return customerId;
  }

  public void setCustomerId(Long customerId) {
    this.customerId = customerId;
  }

  public String getOrderedBy() {
    return orderedBy;
  }

  public void setOrderedBy(String orderedBy) {
    this.orderedBy = orderedBy;
  }

  public String getNotes() {
    return notes;
  }

  public void setNotes(String notes) {
    this.notes = notes;
  }

  public BigDecimal getDiscountPercent() {
    return discountPercent;
  }

  public void setDiscountPercent(BigDecimal discountPercent) {
    this.discountPercent = discountPercent;
  }
}
