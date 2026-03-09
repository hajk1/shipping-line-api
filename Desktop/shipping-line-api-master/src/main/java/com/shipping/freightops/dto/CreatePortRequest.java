package com.shipping.freightops.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Payload for creating a new port. */
public class CreatePortRequest {

  @NotBlank(message = "unlocode is required")
  @Size(min = 5, max = 5, message = "unlocode must be exactly 5 characters")
  private String unlocode;

  @NotBlank(message = "name is required")
  private String name;

  @NotBlank(message = "country is required")
  private String country;

  public String getUnlocode() {
    return unlocode;
  }

  public void setUnlocode(String unlocode) {
    this.unlocode = unlocode;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getCountry() {
    return country;
  }

  public void setCountry(String country) {
    this.country = country;
  }
}
