package com.shipping.freightops.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import com.shipping.freightops.ai.AiClient;
import com.shipping.freightops.dto.PriceSuggestionResponse;
import com.shipping.freightops.entity.Voyage;
import com.shipping.freightops.entity.VoyagePrice;
import com.shipping.freightops.enums.ContainerSize;
import com.shipping.freightops.enums.PriceSuggestionConfidence;
import com.shipping.freightops.repository.FreightOrderRepository;
import com.shipping.freightops.repository.PortRepository;
import com.shipping.freightops.repository.VoyagePriceRepository;
import com.shipping.freightops.repository.VoyageRepository;
import com.shipping.freightops.schema.PriceSuggestionSchemaBuilder;
import com.shipping.freightops.util.CountryRegionMapper;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class PriceSuggestionService {

  private static final int MAX_HISTORICAL_VOYAGES = 50;
  private static final SpecVersion.VersionFlag SCHEMA_VERSION = SpecVersion.VersionFlag.V202012;

  private final VoyageRepository voyageRepository;
  private final VoyagePriceRepository voyagePriceRepository;
  private final FreightOrderRepository freightOrderRepository;
  private final PortRepository portRepository;
  private final AiClient aiClient;
  private final ObjectMapper objectMapper;
  private final String schemaJson;
  private final JsonSchema jsonSchema;
  private final String systemPrompt;

  public PriceSuggestionService(
      VoyageRepository voyageRepository,
      VoyagePriceRepository voyagePriceRepository,
      FreightOrderRepository freightOrderRepository,
      PortRepository portRepository,
      AiClient aiClient,
      ObjectMapper objectMapper,
      PriceSuggestionSchemaBuilder schemaBuilder) {
    this.voyageRepository = voyageRepository;
    this.voyagePriceRepository = voyagePriceRepository;
    this.freightOrderRepository = freightOrderRepository;
    this.portRepository = portRepository;
    this.aiClient = aiClient;
    this.objectMapper = objectMapper;
    try {
      this.schemaJson = schemaBuilder.build();
      this.systemPrompt = loadSystemPrompt();
    } catch (IOException e) {
      throw new IllegalStateException("Failed to load price suggestion resources", e);
    }
    this.jsonSchema = createJsonSchema();
  }

  private String loadSystemPrompt() throws IOException {
    try (var inputStream =
        new ClassPathResource("prompts/price-suggestion-system.txt").getInputStream()) {
      return new String(inputStream.readAllBytes());
    }
  }

  private JsonSchema createJsonSchema() {
    JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SCHEMA_VERSION);
    return factory.getSchema(schemaJson);
  }

  @Transactional(readOnly = true)
  public PriceSuggestionResponse getPriceSuggestion(Long voyageId, ContainerSize containerSize) {
    Voyage voyage =
        voyageRepository
            .findById(voyageId)
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Voyage not found"));

    String route = buildRoute(voyage);
    PageRequest pageRequest = PageRequest.of(0, MAX_HISTORICAL_VOYAGES);

    List<VoyagePrice> historicalPrices =
        voyagePriceRepository.findHistoricalPricesSameRoute(
            voyage.getDeparturePort().getId(),
            voyage.getArrivalPort().getId(),
            voyage.getId(),
            containerSize,
            pageRequest);

    if (historicalPrices.isEmpty()) {
      List<String> depCountries =
          CountryRegionMapper.getCountriesInSameRegion(voyage.getDeparturePort().getCountry());
      List<String> arrCountries =
          CountryRegionMapper.getCountriesInSameRegion(voyage.getArrivalPort().getCountry());

      if (!depCountries.isEmpty() && !arrCountries.isEmpty()) {
        List<Long> depPortIds =
            portRepository.findByCountryIn(depCountries).stream()
                .map(p -> p.getId())
                .collect(Collectors.toList());
        List<Long> arrPortIds =
            portRepository.findByCountryIn(arrCountries).stream()
                .map(p -> p.getId())
                .collect(Collectors.toList());

        if (!depPortIds.isEmpty() && !arrPortIds.isEmpty()) {
          historicalPrices =
              voyagePriceRepository.findHistoricalPricesSimilarRoute(
                  depPortIds, arrPortIds, voyage.getId(), containerSize, pageRequest);
        }
      }
    }

    if (historicalPrices.isEmpty()) {
      return PriceSuggestionResponse.fallback(
          voyage.getVoyageNumber(),
          route,
          containerSize,
          "No historical data for this route; please use manual pricing.");
    }

    String prompt = buildPrompt(route, containerSize, historicalPrices);

    try {
      String rawResponse = aiClient.completeWithSchema(systemPrompt, prompt, schemaJson);

      JsonNode parsed = objectMapper.readTree(rawResponse);
      Set<ValidationMessage> errors = jsonSchema.validate(parsed);
      if (!errors.isEmpty()) {
        return PriceSuggestionResponse.fallback(
            voyage.getVoyageNumber(),
            route,
            containerSize,
            "Could not parse AI suggestion; schema validation failed. Please use manual pricing.");
      }

      return mapToResponse(voyage, route, containerSize, historicalPrices, parsed);
    } catch (Exception e) {
      return PriceSuggestionResponse.fallback(
          voyage.getVoyageNumber(),
          route,
          containerSize,
          "Could not parse AI suggestion; please use manual pricing.");
    }
  }

  private String buildRoute(Voyage voyage) {
    return voyage.getDeparturePort().getName() + " → " + voyage.getArrivalPort().getName();
  }

  private Map<Long, Long> toOrderCountMap(List<Object[]> rows) {
    return rows.stream()
        .collect(Collectors.toMap(row -> (Long) row[0], row -> ((Number) row[1]).longValue()));
  }

  private String buildPrompt(
      String route, ContainerSize containerSize, List<VoyagePrice> historicalPrices) {
    List<Long> voyageIds = historicalPrices.stream().map(vp -> vp.getVoyage().getId()).toList();
    Map<Long, Long> orderCounts =
        toOrderCountMap(freightOrderRepository.countByVoyageIds(voyageIds));

    StringBuilder sb = new StringBuilder();
    sb.append("Route: ").append(route).append("\n");
    sb.append("Container size for which to suggest price: ").append(containerSize).append("\n\n");
    sb.append("Historical data:\n");
    sb.append("voyageNumber | departureDate | priceUsd | orderCount | containerSize\n");
    sb.append("-------------|---------------|----------|------------|---------------\n");

    for (VoyagePrice vp : historicalPrices) {
      Voyage v = vp.getVoyage();
      long orderCount = orderCounts.getOrDefault(v.getId(), 0L);
      sb.append(
          String.format(
              "%-13s| %-15s| %-10s| %-11s| %s%n",
              v.getVoyageNumber(),
              v.getDepartureTime().toLocalDate(),
              vp.getBasePriceUsd(),
              orderCount,
              vp.getContainerSize()));
    }

    sb.append("\nData points: ").append(historicalPrices.size());
    sb.append(
        ". If fewer than 3 data points, set confidence to LOW and note insufficient data in reasoning.");

    return sb.toString();
  }

  private PriceSuggestionResponse mapToResponse(
      Voyage voyage,
      String route,
      ContainerSize containerSize,
      List<VoyagePrice> historicalPrices,
      JsonNode parsed) {
    PriceSuggestionResponse response = new PriceSuggestionResponse();
    response.setVoyageNumber(voyage.getVoyageNumber());
    response.setRoute(route);
    response.setContainerSize(containerSize);
    response.setSuggestedPriceLowUsd(
        parsed.has("suggestedPriceLowUsd")
            ? BigDecimal.valueOf(parsed.get("suggestedPriceLowUsd").asDouble())
            : null);
    response.setSuggestedPriceHighUsd(
        parsed.has("suggestedPriceHighUsd")
            ? BigDecimal.valueOf(parsed.get("suggestedPriceHighUsd").asDouble())
            : null);
    response.setConfidence(
        PriceSuggestionConfidence.valueOf(
            parsed.get("confidence").asText(PriceSuggestionConfidence.LOW.name())));
    response.setReasoning(parsed.has("reasoning") ? parsed.get("reasoning").asText() : null);
    int dataPoints = parsed.has("dataPoints") ? parsed.get("dataPoints").asInt(0) : 0;
    response.setDataPoints(dataPoints > 0 ? dataPoints : historicalPrices.size());

    response.setHistoricalAvgUsd(computeAvg(historicalPrices));
    response.setHistoricalMinUsd(computeMin(historicalPrices));
    response.setHistoricalMaxUsd(computeMax(historicalPrices));

    return response;
  }

  private BigDecimal computeAvg(List<VoyagePrice> prices) {
    if (prices.isEmpty()) return null;
    return prices.stream()
        .map(VoyagePrice::getBasePriceUsd)
        .reduce(BigDecimal.ZERO, BigDecimal::add)
        .divide(BigDecimal.valueOf(prices.size()), 2, java.math.RoundingMode.HALF_UP);
  }

  private BigDecimal computeMin(List<VoyagePrice> prices) {
    return prices.stream()
        .map(VoyagePrice::getBasePriceUsd)
        .min(BigDecimal::compareTo)
        .orElse(null);
  }

  private BigDecimal computeMax(List<VoyagePrice> prices) {
    return prices.stream()
        .map(VoyagePrice::getBasePriceUsd)
        .max(BigDecimal::compareTo)
        .orElse(null);
  }
}
