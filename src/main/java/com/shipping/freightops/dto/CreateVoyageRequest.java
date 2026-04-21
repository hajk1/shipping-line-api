package com.shipping.freightops.dto;

import com.shipping.freightops.enums.VoyageStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// payload for creation new voyage
@Getter
@Setter
@NoArgsConstructor
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
}
