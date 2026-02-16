package com.shipping.freightops.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/** Payload for creating a new freight order. */
public class CreateFreightOrderRequest {

  @NotNull(message = "Voyage ID is required")
  private Long voyageId;

  @NotNull(message = "Container ID is required")
  private Long containerId;

  @NotNull(message = "Customer ID is required")
  private Long customerId;

  @NotBlank(message = "orderedBy is required")
  private String orderedBy;

  private String notes;

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
}
