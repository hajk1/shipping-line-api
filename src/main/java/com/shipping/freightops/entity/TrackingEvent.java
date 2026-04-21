package com.shipping.freightops.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.shipping.freightops.enums.EventType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
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
}
