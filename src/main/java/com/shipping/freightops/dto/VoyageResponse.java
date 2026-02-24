package com.shipping.freightops.dto;

import com.shipping.freightops.entity.Voyage;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class VoyageResponse {
  private String voyageNumber;
  private String vesselName;
  private String departurePortName;
  private String arrivalPortName;
  private LocalDateTime departureTime;
  private LocalDateTime arrivalTime;

  // voyage response format
  public VoyageResponse(Voyage voyage) {
    voyageNumber = voyage.getVoyageNumber();
    vesselName = voyage.getVessel().getName();
    departurePortName = voyage.getDeparturePort().getName();
    arrivalPortName = voyage.getArrivalPort().getName();
    departureTime = voyage.getDepartureTime();
    arrivalTime = voyage.getArrivalTime();
  }

  // List of voyage response format
  public static List<VoyageResponse> VoyageResponses(List<Voyage> voyages) {
    if (voyages == null || voyages.isEmpty()) return List.of();
    List<VoyageResponse> responses = new ArrayList<>(voyages.size());
    voyages.forEach(
        voyage -> {
          VoyageResponse response = new VoyageResponse(voyage);
          responses.add(response);
        });
    return responses;
  }

  public String getVoyageNumber() {
    return voyageNumber;
  }

  public void setVoyageNumber(String voyageNumber) {
    this.voyageNumber = voyageNumber;
  }

  public String getVesselName() {
    return vesselName;
  }

  public void setVesselName(String vesselName) {
    this.vesselName = vesselName;
  }

  public String getDeparturePortName() {
    return departurePortName;
  }

  public void setDeparturePortName(String departurePortName) {
    this.departurePortName = departurePortName;
  }

  public String getArrivalPortName() {
    return arrivalPortName;
  }

  public void setArrivalPortName(String arrivalPortName) {
    this.arrivalPortName = arrivalPortName;
  }

  public LocalDateTime getDepartureDate() {
    return departureTime;
  }

  public void setDepartureDate(LocalDateTime departureTime) {
    this.departureTime = departureTime;
  }

  public LocalDateTime getArrivalDate() {
    return arrivalTime;
  }

  public void setArrivalDate(LocalDateTime departureTime) {
    this.arrivalTime = arrivalTime;
  }
}
