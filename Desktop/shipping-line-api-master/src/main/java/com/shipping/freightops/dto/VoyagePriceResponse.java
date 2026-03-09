package com.shipping.freightops.dto;

import com.shipping.freightops.entity.VoyagePrice;
import com.shipping.freightops.enums.ContainerSize;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class VoyagePriceResponse {
  private Long voyageId;
  private ContainerSize containerSize;
  private BigDecimal basePriceUsd;
  private LocalDateTime createdAt;

  public static VoyagePriceResponse fromEntity(VoyagePrice entity) {
    VoyagePriceResponse dto = new VoyagePriceResponse();
    dto.voyageId = entity.getVoyage().getId();
    dto.containerSize = entity.getContainerSize();
    dto.basePriceUsd = entity.getBasePriceUsd();
    dto.createdAt = entity.getCreatedAt();
    return dto;
  }

  public Long getVoyageId() {
    return voyageId;
  }

  public ContainerSize getContainerSize() {
    return containerSize;
  }

  public BigDecimal getBasePriceUsd() {
    return basePriceUsd;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }
}
