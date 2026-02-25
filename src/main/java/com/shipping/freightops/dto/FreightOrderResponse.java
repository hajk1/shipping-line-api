package com.shipping.freightops.dto;

import com.shipping.freightops.entity.FreightOrder;
import com.shipping.freightops.enums.OrderStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Read-only view of a freight order returned by the API. */
public class FreightOrderResponse {

  private Long id;
  private String voyageNumber;
  private String containerCode;
  private Long agentId;
  private String agentName;
  private String customerName;
  private String customerEmail;
  private String orderedBy;
  private String notes;
  private OrderStatus status;
  private BigDecimal basePriceUsd;
  private BigDecimal discountPercent;
  private BigDecimal finalPrice;
  private String discountReason;
  private LocalDateTime createdAt;

  /** Factory method to map entity → response DTO. */
  public static FreightOrderResponse fromEntity(FreightOrder order) {
    FreightOrderResponse dto = new FreightOrderResponse();
    dto.id = order.getId();
    dto.voyageNumber = order.getVoyage().getVoyageNumber();
    dto.containerCode = order.getContainer().getContainerCode();
    dto.agentId = order.getAgent().getId();
    dto.agentName = order.getAgent().getName();
    dto.customerName = order.getCustomer().getCompanyName();
    dto.customerEmail = order.getCustomer().getEmail();
    dto.orderedBy = order.getOrderedBy();
    dto.notes = order.getNotes();
    dto.status = order.getStatus();
    dto.createdAt = order.getCreatedAt();
    dto.discountPercent = order.getDiscountPercent();
    dto.finalPrice = order.getFinalPrice();
    dto.basePriceUsd = order.getBasePriceUsd();
    dto.discountReason = order.getDiscountReason();
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

  public Long getAgentId() {
    return agentId;
  }

  public String getAgentName() {
    return agentName;
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

  public BigDecimal getBasePriceUsd() {
    return basePriceUsd;
  }

  public BigDecimal getDiscountPercent() {
    return discountPercent;
  }

  public BigDecimal getFinalPrice() {
    return finalPrice;
  }

  public String getDiscountReason() {
    return discountReason;
  }
}
