package com.shipping.freightops.dto;

import com.shipping.freightops.entity.Vessel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Read-only view of a vessel returned by the API. */
@Getter
@Setter
@NoArgsConstructor
public class VesselResponse {
  private String name;
  private String imoNumber;
  private int capacityTeu;

  /** Factory method to map entity → response DTO. */
  public static VesselResponse fromEntity(Vessel vessel) {
    VesselResponse dto = new VesselResponse();
    dto.name = vessel.getName();
    dto.capacityTeu = vessel.getCapacityTeu();
    dto.imoNumber = vessel.getImoNumber();
    return dto;
  }
}
