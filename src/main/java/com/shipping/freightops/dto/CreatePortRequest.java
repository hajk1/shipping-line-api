package com.shipping.freightops.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Payload for creating a new port. */
@Getter
@Setter
@NoArgsConstructor
public class CreatePortRequest {

  @NotBlank(message = "unlocode is required")
  @Size(min = 5, max = 5, message = "unlocode must be exactly 5 characters")
  private String unlocode;

  @NotBlank(message = "name is required")
  private String name;

  @NotBlank(message = "country is required")
  private String country;
}
