package com.shipping.freightops.dto;

import jakarta.validation.constraints.NotNull;

public class BookingStatusUpdateRequest {

  @NotNull private boolean bookingOpen;

  public boolean isBookingOpen() {
    return bookingOpen;
  }

  public void setBookingOpen(boolean bookingOpen) {
    this.bookingOpen = bookingOpen;
  }
}
