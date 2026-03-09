package com.shipping.freightops.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

/** Payload for creating a new vessel. */
public class CreateVesselRequest {

  @NotBlank(message = "Vessel name is required")
  private String name;

  @NotBlank(message = "Vessel imoNumber is required")
  @Pattern(regexp = "\\d{7}", message = "IMO number must be exactly 7 digits")
  private String imoNumber;

  @NotNull(message = "Vessel capacityTeu is required")
  @Positive(message = "Vessel capacityTeu must be greater than 0")
  private int capacityTeu;

  public String getName() {
    return name;
  }

  public String getImoNumber() {
    return imoNumber;
  }

  public int getCapacityTeu() {
    return capacityTeu;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setImoNumber(String imoNumber) {
    this.imoNumber = imoNumber;
  }

  public void setCapacityTeu(int capacityTeu) {
    this.capacityTeu = capacityTeu;
  }
}
