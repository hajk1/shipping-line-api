package com.shipping.freightops.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class BookingStatusUpdateRequest {

  @NotNull private boolean bookingOpen;
}
