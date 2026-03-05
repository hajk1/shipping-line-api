package com.shipping.freightops.dto;

import com.shipping.freightops.entity.VesselOwner;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Read-only view of a vessel owner returned by the API. */
public class VesselOwnerResponse {

  private Long id;
  private String ownerName;
  private String ownerEmail;
  private BigDecimal sharePercent;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;

  /** Factory method to map entity to response DTO. */
  public static VesselOwnerResponse fromEntity(VesselOwner owner) {
    VesselOwnerResponse dto = new VesselOwnerResponse();
    dto.id = owner.getId();
    dto.ownerName = owner.getOwnerName();
    dto.ownerEmail = owner.getOwnerEmail();
    dto.sharePercent = owner.getSharePercent();
    dto.createdAt = owner.getCreatedAt();
    dto.updatedAt = owner.getUpdatedAt();
    return dto;
  }

  public Long getId() {
    return id;
  }

  public String getOwnerName() {
    return ownerName;
  }

  public String getOwnerEmail() {
    return ownerEmail;
  }

  public BigDecimal getSharePercent() {
    return sharePercent;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public LocalDateTime getUpdatedAt() {
    return updatedAt;
  }
}
