package com.shipping.freightops.dto;

import com.shipping.freightops.entity.FreightOrder;
import com.shipping.freightops.entity.TrackingEvent;
import com.shipping.freightops.enums.ContainerSize;
import com.shipping.freightops.enums.ContainerType;
import com.shipping.freightops.enums.OrderStatus;
import com.shipping.freightops.enums.VoyageStatus;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
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
  private List<TrackingEvent> events = new ArrayList<>();

  public static OrderTrackingResponse fromEntity(FreightOrder order) {
    OrderTrackingResponse dto = new OrderTrackingResponse();
    dto.orderId = order.getId();
    dto.status = order.getStatus();
    dto.ContainerCode = order.getContainer().getContainerCode();
    dto.containerSize = order.getContainer().getSize();
    dto.containerType = order.getContainer().getType();
    dto.events = order.getEvents();
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
}
