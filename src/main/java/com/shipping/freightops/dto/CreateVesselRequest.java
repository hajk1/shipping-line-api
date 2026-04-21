package com.shipping.freightops.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Payload for creating a new vessel. */
@Getter
@Setter
@NoArgsConstructor
public class CreateVesselRequest {

  @NotBlank(message = "Vessel name is required")
  private String name;

  @NotBlank(message = "Vessel imoNumber is required")
  @Pattern(regexp = "\\d{7}", message = "IMO number must be exactly 7 digits")
  private String imoNumber;

  @NotNull(message = "Vessel capacityTeu is required")
  @Positive(message = "Vessel capacityTeu must be greater than 0")
  private int capacityTeu;
}
