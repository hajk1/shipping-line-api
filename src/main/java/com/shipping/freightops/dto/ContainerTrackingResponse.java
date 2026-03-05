package com.shipping.freightops.dto;

import com.shipping.freightops.entity.FreightOrder;
import com.shipping.freightops.enums.ContainerSize;
import com.shipping.freightops.enums.ContainerType;
import java.util.List;

public class ContainerTrackingResponse {
  private String containerCode;
  private ContainerSize containerSize;
  private ContainerType containerType;
  private List<VoyageTrackingResponse> voyages;

  public static ContainerTrackingResponse fromEntities(List<FreightOrder> orders) {
    if (orders == null || orders.isEmpty()) return null;
    ContainerTrackingResponse dto = new ContainerTrackingResponse();
    FreightOrder firstOrder = orders.get(0);
    dto.containerCode = firstOrder.getContainer().getContainerCode();
    dto.containerSize = firstOrder.getContainer().getSize();
    dto.containerType = firstOrder.getContainer().getType();
    dto.voyages =
        orders.stream()
            .filter(order -> order.getVoyage() != null)
            .map(order -> VoyageTrackingResponse.fromEntity(order.getVoyage()))
            .toList();
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

  public List<VoyageTrackingResponse> getVoyages() {
    return voyages;
  }
}
