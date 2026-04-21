package com.shipping.freightops.dto;

import com.shipping.freightops.entity.VoyageCost;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
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
}
