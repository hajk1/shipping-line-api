package com.shipping.freightops.dto;

import com.shipping.freightops.entity.Voyage;
import com.shipping.freightops.enums.VoyageStatus;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class VoyageResponse {
  private String voyageNumber;
  private String vesselName;
  private String departurePortName;
  private String arrivalPortName;
  private LocalDateTime departureTime;
  private LocalDateTime arrivalTime;
  private boolean bookingOpen;
  private int maxCapacityTeu;
  private VoyageStatus status;

  // voyage response format
  public VoyageResponse(Voyage voyage) {
    voyageNumber = voyage.getVoyageNumber();
    vesselName = voyage.getVessel().getName();
    departurePortName = voyage.getDeparturePort().getName();
    arrivalPortName = voyage.getArrivalPort().getName();
    departureTime = voyage.getDepartureTime();
    arrivalTime = voyage.getArrivalTime();
    bookingOpen = voyage.isBookingOpen();
    maxCapacityTeu = voyage.getMaxCapacityTeu();
    status = voyage.getStatus();
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
}
