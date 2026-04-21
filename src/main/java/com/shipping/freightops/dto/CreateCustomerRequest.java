package com.shipping.freightops.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CreateCustomerRequest {

  @NotBlank(message = "Company name is required")
  private String companyName;

  @NotBlank(message = "Contact name is required")
  private String contactName;

  @NotBlank(message = "Email is required")
  @Email(message = "Email should be valid")
  private String email;

  @Size(max = 20, message = "Phone must be less than 20 characters")
  private String phone;

  private String address;
}
