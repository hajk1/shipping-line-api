package com.shipping.freightops.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.shipping.freightops.enums.ContainerSize;
import com.shipping.freightops.enums.PriceSuggestionConfidence;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
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
  private List<RiskFactor> riskFactors;

  @JsonProperty("suggestedPriceLowUsd")
  public BigDecimal getSuggestedPriceLowUsd() {
    return suggestedPriceLowUsd;
  }

  @JsonProperty("suggestedPriceHighUsd")
  public BigDecimal getSuggestedPriceHighUsd() {
    return suggestedPriceHighUsd;
  }

  @JsonProperty("historicalAvgUsd")
  public BigDecimal getHistoricalAvgUsd() {
    return historicalAvgUsd;
  }

  @JsonProperty("historicalMinUsd")
  public BigDecimal getHistoricalMinUsd() {
    return historicalMinUsd;
  }

  @JsonProperty("historicalMaxUsd")
  public BigDecimal getHistoricalMaxUsd() {
    return historicalMaxUsd;
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
    response.setRiskFactors(new ArrayList<>());
    return response;
  }
}
