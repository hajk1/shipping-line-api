package com.shipping.freightops.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.shipping.freightops.enums.EventType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;

@Entity
public class TrackingEvent extends BaseEntity {
  @ManyToOne(optional = false)
  @JsonIgnore
  private FreightOrder freightOrder;

  private EventType eventType;

  @Column(nullable = false)
  @NotBlank(message = "description is required")
  private String description;

  private String location;
  private String performedBy;
  private LocalDateTime eventTime;

  public TrackingEvent() {}

  public TrackingEvent(
      FreightOrder freightOrder,
      EventType eventType,
      String description,
      String location,
      String performedBy,
      LocalDateTime eventTime) {
    this.freightOrder = freightOrder;
    this.description = description;
    this.location = location;
    this.performedBy = performedBy;
    this.eventTime = eventTime;
  }

  public FreightOrder getFreightOrder() {
    return freightOrder;
  }

  public void setFreightOrder(FreightOrder freightOrder) {
    this.freightOrder = freightOrder;
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

  public LocalDateTime getEventTime() {
    return eventTime;
  }

  public void setEventTime(LocalDateTime eventTime) {
    this.eventTime = eventTime;
  }
}
