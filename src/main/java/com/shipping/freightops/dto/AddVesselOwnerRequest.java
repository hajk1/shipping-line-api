package com.shipping.freightops.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Payload for adding a new owner to a vessel. */
@Getter
@Setter
@NoArgsConstructor
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
}
