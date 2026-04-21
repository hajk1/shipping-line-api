package com.shipping.freightops.dto;

import com.shipping.freightops.entity.Container;
import com.shipping.freightops.entity.FreightOrder;
import com.shipping.freightops.enums.ContainerSize;
import com.shipping.freightops.enums.ContainerType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ContainerTrackingResponse {
  private String containerCode;
  private ContainerSize containerSize;
  private ContainerType containerType;
  private List<VoyageTrackingResponse> voyages;

  /** Creates a response for a container with no associated orders. */
  public static ContainerTrackingResponse fromContainer(Container container) {
    if (container == null) return null;
    ContainerTrackingResponse dto = new ContainerTrackingResponse();
    dto.containerCode = container.getContainerCode();
    dto.containerSize = container.getSize();
    dto.containerType = container.getType();
    dto.voyages = Collections.emptyList();
    return dto;
  }

  public static ContainerTrackingResponse fromEntities(List<FreightOrder> orders) {
    if (orders == null || orders.isEmpty()) return null;
    ContainerTrackingResponse dto = new ContainerTrackingResponse();
    FreightOrder firstOrder = orders.get(0);
    dto.containerCode = firstOrder.getContainer().getContainerCode();
    dto.containerSize = firstOrder.getContainer().getSize();
    dto.containerType = firstOrder.getContainer().getType();
    dto.voyages =
        new ArrayList<>(
            orders.stream()
                .filter(order -> order.getVoyage() != null)
                .map(order -> VoyageTrackingResponse.fromEntity(order.getVoyage()))
                .toList());
    dto.voyages.sort((v1, v2) -> v1.getDepartureTime().compareTo(v2.getDepartureTime()));

    return dto;
  }
}
