package com.shipping.freightops.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name = "customers")
public class Customer extends BaseEntity {

  @NotBlank
  @Column(nullable = false)
  private String companyName;

  @NotBlank
  @Column(nullable = false)
  private String contactName;

  @NotBlank
  @Column(nullable = false)
  @Email(message = "Email should be valid")
  private String email;

  private String phone;

  private String address;

  public Customer() {}

  public Customer(String companyName, String contactName, String email) {
    this.companyName = companyName;
    this.contactName = contactName;
    this.email = email;
  }

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

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getPhone() {
    return phone;
  }

  public void setPhone(String phone) {
    this.phone = phone;
  }

  public String getAddress() {
    return address;
  }

  public void setAddress(String address) {
    this.address = address;
  }
}
