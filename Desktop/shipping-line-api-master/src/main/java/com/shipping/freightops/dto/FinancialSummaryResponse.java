package com.shipping.freightops.dto;

import java.math.BigDecimal;
import java.util.List;

public class FinancialSummaryResponse {
  private String voyageNumber;
  private BigDecimal totalRevenueUsd;
  private BigDecimal totalCostsUsd;
  private BigDecimal netProfitUsd;
  private int orderCount;
  private List<OwnerFinancialShareResponse> owners;

  public static FinancialSummaryResponse fromValues(
      String voyageNumber,
      BigDecimal totalRevenueUsd,
      BigDecimal totalCostsUsd,
      BigDecimal netProfitUsd,
      int orderCount,
      List<OwnerFinancialShareResponse> owners) {
    FinancialSummaryResponse response = new FinancialSummaryResponse();
    response.voyageNumber = voyageNumber;
    response.totalRevenueUsd = totalRevenueUsd;
    response.totalCostsUsd = totalCostsUsd;
    response.netProfitUsd = netProfitUsd;
    response.orderCount = orderCount;
    response.owners = owners;
    return response;
  }

  public String getVoyageNumber() {
    return voyageNumber;
  }

  public BigDecimal getTotalRevenueUsd() {
    return totalRevenueUsd;
  }

  public BigDecimal getTotalCostsUsd() {
    return totalCostsUsd;
  }

  public BigDecimal getNetProfitUsd() {
    return netProfitUsd;
  }

  public int getOrderCount() {
    return orderCount;
  }

  public List<OwnerFinancialShareResponse> getOwners() {
    return owners;
  }
}
