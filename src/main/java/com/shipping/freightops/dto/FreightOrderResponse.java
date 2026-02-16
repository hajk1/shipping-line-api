package com.shipping.freightops.dto;

import com.shipping.freightops.entity.FreightOrder;
import com.shipping.freightops.enums.OrderStatus;
import java.time.LocalDateTime;

/** Read-only view of a freight order returned by the API. */
public class FreightOrderResponse {

  private Long id;
  private String voyageNumber;
  private String containerCode;
  private String customerName;
  private String customerEmail;
  private String orderedBy;
  private String notes;
  private OrderStatus status;
  private LocalDateTime createdAt;

  /** Factory method to map entity → response DTO. */
  public static FreightOrderResponse fromEntity(FreightOrder order) {
    FreightOrderResponse dto = new FreightOrderResponse();
    dto.id = order.getId();
    dto.voyageNumber = order.getVoyage().getVoyageNumber();
    dto.containerCode = order.getContainer().getContainerCode();
    dto.customerName = order.getCustomer().getCompanyName();
    dto.customerEmail = order.getCustomer().getEmail();
    dto.orderedBy = order.getOrderedBy();
    dto.notes = order.getNotes();
    dto.status = order.getStatus();
    dto.createdAt = order.getCreatedAt();
    return dto;
  }

  public Long getId() {
    return id;
  }

  public String getVoyageNumber() {
    return voyageNumber;
  }

  public String getContainerCode() {
    return containerCode;
  }

  public String getCustomerName() {
    return customerName;
  }

  public String getCustomerEmail() {
    return customerEmail;
  }

  public String getOrderedBy() {
    return orderedBy;
  }

  public String getNotes() {
    return notes;
  }

  public OrderStatus getStatus() {
    return status;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }
}
