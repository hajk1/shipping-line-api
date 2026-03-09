package com.shipping.freightops.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

/** Payload for adding a new owner to a vessel. */
public class AddVesselOwnerRequest {

  @NotBlank(message = "Owner name is required")
  private String ownerName;

  @NotBlank(message = "Owner email is required")
  @Email(message = "Owner email must be valid")
  private String ownerEmail;

  @NotNull(message = "Share percent is required")
  @DecimalMin(value = "0.01", message = "Share percent must be greater than 0")
  @DecimalMax(value = "100.00", message = "Share percent must not exceed 100")
  private BigDecimal sharePercent;

  public String getOwnerName() {
    return ownerName;
  }

  public void setOwnerName(String ownerName) {
    this.ownerName = ownerName;
  }

  public String getOwnerEmail() {
    return ownerEmail;
  }

  public void setOwnerEmail(String ownerEmail) {
    this.ownerEmail = ownerEmail;
  }

  public BigDecimal getSharePercent() {
    return sharePercent;
  }

  public void setSharePercent(BigDecimal sharePercent) {
    this.sharePercent = sharePercent;
  }
}
