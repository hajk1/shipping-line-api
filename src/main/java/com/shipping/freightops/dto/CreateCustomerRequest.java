package com.shipping.freightops.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

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

  public String getCompanyName() {
    return companyName;
  }

  public void setCompanyName(String companyName) {
    this.companyName = companyName;
  }

  public String getContactName() {
    return contactName;
  }

  public void setContactName(String contactName) {
    this.contactName = contactName;
  }

  public String getPhone() {
    return phone;
  }

  public void setPhone(String phone) {
    this.phone = phone;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getAddress() {
    return address;
  }

  public void setAddress(String address) {
    this.address = address;
  }
}
