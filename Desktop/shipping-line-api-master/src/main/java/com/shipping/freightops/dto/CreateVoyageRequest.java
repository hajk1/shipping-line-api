package com.shipping.freightops.dto;

import com.shipping.freightops.enums.VoyageStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

// payload for creation new voyage
public class CreateVoyageRequest {
  @NotBlank private String voyageNumber;

  @NotNull(message = "vesselId is required")
  private Long vesselId;

  @NotNull(message = "departure port is required")
  private Long departurePortId;

  @NotNull(message = "arrivalId is required")
  private Long arrivalPortId;

  @NotNull(message = "departure date is required")
  private LocalDateTime departureTime;

  @NotNull(message = "arrival date is required")
  private LocalDateTime arrivalTime;

  @NotNull(message = "status is required")
  private VoyageStatus status;

  public CreateVoyageRequest(
      String voyageNumber,
      Long vesselId,
      Long departurePortId,
      Long arrivalPortId,
      LocalDateTime departureTime,
      LocalDateTime arrivalTime,
      VoyageStatus status) {
    this.voyageNumber = voyageNumber;
    this.vesselId = vesselId;
    this.departurePortId = departurePortId;
    this.arrivalPortId = arrivalPortId;
    this.departureTime = departureTime;
    this.arrivalTime = arrivalTime;
    this.status = status;
  }

  public VoyageStatus getStatus() {
    return status;
  }

  public void setStatus(VoyageStatus status) {
    this.status = status;
  }

  public CreateVoyageRequest() {}

  public String getVoyageNumber() {
    return voyageNumber;
  }

  public void setVoyageNumber(String voyageNumber) {
    this.voyageNumber = voyageNumber;
  }

  public Long getVesselId() {
    return vesselId;
  }

  public void setVesselId(Long vesselId) {
    this.vesselId = vesselId;
  }

  public Long getArrivalPortId() {
    return arrivalPortId;
  }

  public void setArrivalPortId(Long arrivalId) {
    this.arrivalPortId = arrivalId;
  }

  public LocalDateTime getDepartureTime() {
    return departureTime;
  }

  public void setDepartureTime(LocalDateTime departureDate) {
    this.departureTime = departureDate;
  }

  public LocalDateTime getArrivalTime() {
    return arrivalTime;
  }

  public void setArrivalTime(LocalDateTime arrivalDate) {
    this.arrivalTime = arrivalDate;
  }

  public Long getDeparturePortId() {
    return departurePortId;
  }

  public void setDeparturePortId(Long departurePortId) {
    this.departurePortId = departurePortId;
  }
}
