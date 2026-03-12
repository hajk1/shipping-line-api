package com.shipping.freightops.dto;

import com.shipping.freightops.enums.EventType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class TrackingEventRequest {
  @NotNull private EventType eventType;

  @NotBlank(message = "description is required")
  private String description;

  private String location;
  private String performedBy;

  public TrackingEventRequest() {}

  public TrackingEventRequest(
      EventType eventType, String description, String location, String performedBy) {
    this.eventType = eventType;
    this.description = description;
    this.location = location;
    this.performedBy = performedBy;
  }

  public EventType getEventType() {
    return eventType;
  }

  public void setEventType(EventType eventType) {
    this.eventType = eventType;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getLocation() {
    return location;
  }

  public void setLocation(String location) {
    this.location = location;
  }

  public String getPerformedBy() {
    return performedBy;
  }

  public void setPerformedBy(String performedBy) {
    this.performedBy = performedBy;
  }
}
