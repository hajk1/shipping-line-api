package com.shipping.freightops.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
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

  public Customer(String companyName, String contactName, String email) {
    this.companyName = companyName;
    this.contactName = contactName;
    this.email = email;
  }
}
