package com.shipping.freightops.dto;

import com.shipping.freightops.entity.Customer;
import java.time.LocalDateTime;

public class CustomerResponse {
  private Long id;
  private String companyName;
  private String contactName;
  private String email;
  private String phone;
  private String address;
  private LocalDateTime createdAt;

  public static CustomerResponse fromEntity(Customer customer) {
    CustomerResponse dto = new CustomerResponse();
    dto.id = customer.getId();
    dto.companyName = customer.getCompanyName();
    dto.contactName = customer.getContactName();
    dto.email = customer.getEmail();
    dto.phone = customer.getPhone();
    dto.address = customer.getAddress();
    dto.createdAt = customer.getCreatedAt();
    return dto;
  }

  public Long getId() {
    return id;
  }

  public String getCompanyName() {
    return companyName;
  }

  public String getContactName() {
    return contactName;
  }

  public String getEmail() {
    return email;
  }

  public String getPhone() {
    return phone;
  }

  public String getAddress() {
    return address;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }
}
