package com.shipping.freightops.dto;

import com.shipping.freightops.entity.Voyage;

public class LoadSummaryResponse {
  private String voyageNumber;
  private int maxCapacityTeu;
  private int currentLoadTeu;
  private double utilizationPercent;
  private boolean bookingOpen;
  private int containerCount;

  public static LoadSummaryResponse fromEntity(
      Voyage voyage, int currentLoadTeu, int containerCount) {
    LoadSummaryResponse dto = new LoadSummaryResponse();
    dto.voyageNumber = voyage.getVoyageNumber();
    dto.maxCapacityTeu = voyage.getMaxCapacityTeu();
    dto.currentLoadTeu = currentLoadTeu;
    dto.containerCount = containerCount;
    dto.bookingOpen = voyage.isBookingOpen();
    dto.utilizationPercent = calculateUtilization(dto.currentLoadTeu, dto.maxCapacityTeu);
    return dto;
  }

  private static double calculateUtilization(int current, int max) {
    if (max == 0) return 0.0;
    return Math.round((current * 1000.0) / max) / 10.0;
  }

  public String getVoyageNumber() {
    return voyageNumber;
  }

  public int getMaxCapacityTeu() {
    return maxCapacityTeu;
  }

  public int getCurrentLoadTeu() {
    return currentLoadTeu;
  }

  public double getUtilizationPercent() {
    return utilizationPercent;
  }

  public boolean isBookingOpen() {
    return bookingOpen;
  }

  public int getContainerCount() {
    return containerCount;
  }
}
