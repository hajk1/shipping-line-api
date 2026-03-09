package com.shipping.freightops.dto;

import com.shipping.freightops.entity.FreightOrder;
import com.shipping.freightops.enums.ContainerSize;
import com.shipping.freightops.enums.ContainerType;
import com.shipping.freightops.enums.OrderStatus;
import com.shipping.freightops.enums.VoyageStatus;
import java.time.LocalDateTime;

public class OrderTrackingResponse {
  private Long orderId;
  private OrderStatus status;
  private String ContainerCode;
  private ContainerSize containerSize;
  private ContainerType containerType;
  private String voyageNumber;
  private String vesselName;
  private String departurePort;
  private String arrivalPort;
  private LocalDateTime departureTime;
  private LocalDateTime estimatedArrival;
  private VoyageStatus voyageStatus;

  public static OrderTrackingResponse fromEntity(FreightOrder order) {
    OrderTrackingResponse dto = new OrderTrackingResponse();
    dto.orderId = order.getId();
    dto.status = order.getStatus();
    dto.ContainerCode = order.getContainer().getContainerCode();
    dto.containerSize = order.getContainer().getSize();
    dto.containerType = order.getContainer().getType();
    if (order.getVoyage() != null) {
      dto.voyageNumber = order.getVoyage().getVoyageNumber();
      dto.vesselName = order.getVoyage().getVessel().getName();
      dto.departurePort = order.getVoyage().getDeparturePort().getName();
      dto.arrivalPort = order.getVoyage().getArrivalPort().getName();
      dto.departureTime = order.getVoyage().getDepartureTime();
      dto.estimatedArrival = order.getVoyage().getArrivalTime();
      dto.voyageStatus = order.getVoyage().getStatus();
    }
    return dto;
  }

  public String getVoyageNumber() {
    return voyageNumber;
  }

  public String getVesselName() {
    return vesselName;
  }

  public String getDeparturePort() {
    return departurePort;
  }

  public String getArrivalPort() {
    return arrivalPort;
  }

  public LocalDateTime getDepartureTime() {
    return departureTime;
  }

  public LocalDateTime getEstimatedArrival() {
    return estimatedArrival;
  }

  public VoyageStatus getVoyageStatus() {
    return voyageStatus;
  }

  public Long getOrderId() {
    return orderId;
  }

  public OrderStatus getStatus() {
    return status;
  }

  public String getContainerCode() {
    return ContainerCode;
  }

  public ContainerSize getContainerSize() {
    return containerSize;
  }

  public ContainerType getContainerType() {
    return containerType;
  }
}
