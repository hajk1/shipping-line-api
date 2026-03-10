package com.shipping.freightops.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.shipping.freightops.enums.ContainerSize;
import com.shipping.freightops.enums.PriceSuggestionConfidence;
import java.math.BigDecimal;

public class PriceSuggestionResponse {
  private String voyageNumber;
  private String route;
  private ContainerSize containerSize;
  private BigDecimal suggestedPriceLowUsd;
  private BigDecimal suggestedPriceHighUsd;
  private PriceSuggestionConfidence confidence;
  private String reasoning;
  private int dataPoints;
  private BigDecimal historicalAvgUsd;
  private BigDecimal historicalMinUsd;
  private BigDecimal historicalMaxUsd;

  public String getVoyageNumber() {
    return voyageNumber;
  }

  public void setVoyageNumber(String voyageNumber) {
    this.voyageNumber = voyageNumber;
  }

  public String getRoute() {
    return route;
  }

  public void setRoute(String route) {
    this.route = route;
  }

  public ContainerSize getContainerSize() {
    return containerSize;
  }

  public void setContainerSize(ContainerSize containerSize) {
    this.containerSize = containerSize;
  }

  @JsonProperty("suggestedPriceLowUsd")
  public BigDecimal getSuggestedPriceLowUsd() {
    return suggestedPriceLowUsd;
  }

  public void setSuggestedPriceLowUsd(BigDecimal suggestedPriceLowUsd) {
    this.suggestedPriceLowUsd = suggestedPriceLowUsd;
  }

  @JsonProperty("suggestedPriceHighUsd")
  public BigDecimal getSuggestedPriceHighUsd() {
    return suggestedPriceHighUsd;
  }

  public void setSuggestedPriceHighUsd(BigDecimal suggestedPriceHighUsd) {
    this.suggestedPriceHighUsd = suggestedPriceHighUsd;
  }

  public PriceSuggestionConfidence getConfidence() {
    return confidence;
  }

  public void setConfidence(PriceSuggestionConfidence confidence) {
    this.confidence = confidence;
  }

  public String getReasoning() {
    return reasoning;
  }

  public void setReasoning(String reasoning) {
    this.reasoning = reasoning;
  }

  public int getDataPoints() {
    return dataPoints;
  }

  public void setDataPoints(int dataPoints) {
    this.dataPoints = dataPoints;
  }

  @JsonProperty("historicalAvgUsd")
  public BigDecimal getHistoricalAvgUsd() {
    return historicalAvgUsd;
  }

  public void setHistoricalAvgUsd(BigDecimal historicalAvgUsd) {
    this.historicalAvgUsd = historicalAvgUsd;
  }

  @JsonProperty("historicalMinUsd")
  public BigDecimal getHistoricalMinUsd() {
    return historicalMinUsd;
  }

  public void setHistoricalMinUsd(BigDecimal historicalMinUsd) {
    this.historicalMinUsd = historicalMinUsd;
  }

  @JsonProperty("historicalMaxUsd")
  public BigDecimal getHistoricalMaxUsd() {
    return historicalMaxUsd;
  }

  public void setHistoricalMaxUsd(BigDecimal historicalMaxUsd) {
    this.historicalMaxUsd = historicalMaxUsd;
  }

  /** Creates a fallback response for no-data or parse-failure scenarios. */
  public static PriceSuggestionResponse fallback(
      String voyageNumber, String route, ContainerSize containerSize, String reasoning) {
    PriceSuggestionResponse response = new PriceSuggestionResponse();
    response.setVoyageNumber(voyageNumber);
    response.setRoute(route);
    response.setContainerSize(containerSize);
    response.setSuggestedPriceLowUsd(null);
    response.setSuggestedPriceHighUsd(null);
    response.setConfidence(PriceSuggestionConfidence.LOW);
    response.setReasoning(reasoning);
    response.setDataPoints(0);
    response.setHistoricalAvgUsd(null);
    response.setHistoricalMinUsd(null);
    response.setHistoricalMaxUsd(null);
    return response;
  }
}
