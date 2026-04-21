package com.shipping.freightops.dto;

import com.shipping.freightops.entity.Port;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Read-only view of a port returned by the API. */
@Getter
@Setter
@NoArgsConstructor
public class PortResponse {

  private Long id;
  private String unlocode;
  private String name;
  private String country;
  private LocalDateTime createdAt;

  /** Factory method to map entity → response DTO. */
  public static PortResponse fromEntity(Port port) {
    PortResponse dto = new PortResponse();
    dto.id = port.getId();
    dto.unlocode = port.getUnlocode();
    dto.name = port.getName();
    dto.country = port.getCountry();
    dto.createdAt = port.getCreatedAt();
    return dto;
  }
}
