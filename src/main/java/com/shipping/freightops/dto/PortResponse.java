package com.shipping.freightops.dto;

import com.shipping.freightops.entity.Port;
import java.time.LocalDateTime;

/** Read-only view of a port returned by the API. */
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

  public Long getId() {
    return id;
  }

  public String getUnlocode() {
    return unlocode;
  }

  public String getName() {
    return name;
  }

  public String getCountry() {
    return country;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }
}
