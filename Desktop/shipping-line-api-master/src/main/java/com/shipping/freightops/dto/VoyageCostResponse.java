package com.shipping.freightops.dto;

import com.shipping.freightops.entity.VoyageCost;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class VoyageCostResponse {
  private Long id;
  private Long voyageId;
  private String description;
  private BigDecimal amountUsd;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;

  public static VoyageCostResponse fromEntity(VoyageCost entity) {
    VoyageCostResponse dto = new VoyageCostResponse();
    dto.id = entity.getId();
    dto.voyageId = entity.getVoyage().getId();
    dto.description = entity.getDescription();
    dto.amountUsd = entity.getAmountUsd();
    dto.createdAt = entity.getCreatedAt();
    dto.updatedAt = entity.getUpdatedAt();
    return dto;
  }

  public Long getId() {
    return id;
  }

  public Long getVoyageId() {
    return voyageId;
  }

  public String getDescription() {
    return description;
  }

  public BigDecimal getAmountUsd() {
    return amountUsd;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public LocalDateTime getUpdatedAt() {
    return updatedAt;
  }
}
