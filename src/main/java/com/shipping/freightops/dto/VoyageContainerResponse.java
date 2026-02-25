package com.shipping.freightops.dto;

import com.shipping.freightops.entity.FreightOrder;
import com.shipping.freightops.enums.ContainerSize;
import com.shipping.freightops.enums.ContainerType;
import com.shipping.freightops.enums.OrderStatus;

public class VoyageContainerResponse {

  private String containerCode;
  private ContainerSize containerSize;
  private ContainerType containerType;
  private String orderedBy;
  private OrderStatus orderStatus;

  public static VoyageContainerResponse fromEntity(FreightOrder order) {
    VoyageContainerResponse dto = new VoyageContainerResponse();
    dto.containerCode = order.getContainer().getContainerCode();
    dto.containerSize = order.getContainer().getSize();
    dto.containerType = order.getContainer().getType();
    dto.orderedBy = order.getOrderedBy();
    dto.orderStatus = order.getStatus();
    return dto;
  }

  public String getContainerCode() {
    return containerCode;
  }

  public ContainerSize getContainerSize() {
    return containerSize;
  }

  public ContainerType getContainerType() {
    return containerType;
  }

  public String getOrderedBy() {
    return orderedBy;
  }

  public OrderStatus getOrderStatus() {
    return orderStatus;
  }
}
