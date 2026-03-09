package com.shipping.freightops.dto;

import java.math.BigDecimal;

public class OwnerFinancialShareResponse {
  private String ownerName;
  private BigDecimal sharePercent;
  private BigDecimal revenueShareUsd;
  private BigDecimal costShareUsd;
  private BigDecimal profitShareUsd;

  public static OwnerFinancialShareResponse fromValues(
      String ownerName,
      BigDecimal sharePercent,
      BigDecimal revenueShareUsd,
      BigDecimal costShareUsd,
      BigDecimal profitShareUsd) {
    OwnerFinancialShareResponse response = new OwnerFinancialShareResponse();
    response.ownerName = ownerName;
    response.sharePercent = sharePercent;
    response.revenueShareUsd = revenueShareUsd;
    response.costShareUsd = costShareUsd;
    response.profitShareUsd = profitShareUsd;
    return response;
  }

  public String getOwnerName() {
    return ownerName;
  }

  public BigDecimal getSharePercent() {
    return sharePercent;
  }

  public BigDecimal getRevenueShareUsd() {
    return revenueShareUsd;
  }

  public BigDecimal getCostShareUsd() {
    return costShareUsd;
  }

  public BigDecimal getProfitShareUsd() {
    return profitShareUsd;
  }
}
