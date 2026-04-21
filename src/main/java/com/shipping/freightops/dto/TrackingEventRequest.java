package com.shipping.freightops.dto;

import com.shipping.freightops.enums.EventType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class TrackingEventRequest {
  @NotNull private EventType eventType;

  @NotBlank(message = "description is required")
  private String description;

  private String location;
  private String performedBy;

  public TrackingEventRequest(
      EventType eventType, String description, String location, String performedBy) {
    this.eventType = eventType;
    this.description = description;
    this.location = location;
    this.performedBy = performedBy;
  }
}
