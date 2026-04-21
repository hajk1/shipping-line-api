package com.shipping.freightops.dto;

import com.shipping.freightops.entity.Voyage;
import com.shipping.freightops.enums.VoyageStatus;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class VoyageTrackingResponse {
  private String voyageNumber;
  private VoyageStatus status;
  private String departurePort;
  private String arrivalPort;
  private LocalDateTime departureTime;
  private LocalDateTime arrivalTime;

  public static VoyageTrackingResponse fromEntity(Voyage voyage) {
    VoyageTrackingResponse dto = new VoyageTrackingResponse();
    dto.voyageNumber = voyage.getVoyageNumber();
    dto.status = voyage.getStatus();
    dto.departurePort = voyage.getDeparturePort().getName();
    dto.arrivalPort = voyage.getArrivalPort().getName();
    dto.departureTime = voyage.getDepartureTime();
    dto.arrivalTime = voyage.getArrivalTime();
    return dto;
  }
}
