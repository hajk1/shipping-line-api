package com.shipping.freightops.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shipping.freightops.ai.AiClient;
import com.shipping.freightops.dto.PriceSuggestionResponse;
import com.shipping.freightops.dto.RiskFactor;
import com.shipping.freightops.entity.Port;
import com.shipping.freightops.entity.Voyage;
import com.shipping.freightops.entity.VoyagePrice;
import com.shipping.freightops.enums.ContainerSize;
import com.shipping.freightops.enums.RiskImpact;
import com.shipping.freightops.repository.FreightOrderRepository;
import com.shipping.freightops.repository.PortRepository;
import com.shipping.freightops.repository.VoyagePriceRepository;
import com.shipping.freightops.repository.VoyageRepository;
import com.shipping.freightops.schema.CompositeSchemaBuilder;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;

class PriceSuggestionServiceTest {

  private static class TestRiskAnalysisService extends RiskAnalysisService {
    private String mockNewsContext = "";
    private List<RiskFactor> mockRiskFactors = new ArrayList<>();

    public TestRiskAnalysisService() {
      super(null, null); // We won't use the parent functionality
    }

    public void setMockNewsContext(String context) {
      this.mockNewsContext = context;
    }

    public void setMockRiskFactors(List<RiskFactor> riskFactors) {
      this.mockRiskFactors = riskFactors;
    }

    @Override
    public String fetchAndBuildNewsContext(String route) {
      return mockNewsContext;
    }

    @Override
    public List<RiskFactor> parseRiskFactors(JsonNode parsed) {
      return new ArrayList<>(mockRiskFactors);
    }
  }

  private static class TestCompositeSchemaBuilder extends CompositeSchemaBuilder {
    public TestCompositeSchemaBuilder() {
      super(null, null, null); // We won't use the parent functionality
    }

    @Override
    public String buildCompositeSchema() throws JsonProcessingException, IOException {
      return "{}"; // Simple empty schema for testing
    }
  }

  private VoyageRepository voyageRepository;
  private VoyagePriceRepository voyagePriceRepository;
  private FreightOrderRepository freightOrderRepository;
  private PortRepository portRepository;
  private AiClient aiClient;

  private PriceSuggestionService priceSuggestionService;
  private ObjectMapper objectMapper;
  private TestRiskAnalysisService testRiskAnalysisService;
  private TestCompositeSchemaBuilder testCompositeSchemaBuilder;

  @BeforeEach
  void setUp() throws Exception {
    voyageRepository = mock(VoyageRepository.class);
    voyagePriceRepository = mock(VoyagePriceRepository.class);
    freightOrderRepository = mock(FreightOrderRepository.class);
    portRepository = mock(PortRepository.class);
    aiClient = mock(AiClient.class);

    objectMapper = new ObjectMapper();
    testRiskAnalysisService = new TestRiskAnalysisService();
    testCompositeSchemaBuilder = new TestCompositeSchemaBuilder();

    priceSuggestionService =
        new PriceSuggestionService(
            voyageRepository,
            voyagePriceRepository,
            freightOrderRepository,
            portRepository,
            aiClient,
            objectMapper,
            testRiskAnalysisService,
            testCompositeSchemaBuilder);
  }

  @Test
  @DisplayName("End-to-end price suggestion with news integration")
  void testPriceSuggestionWithNewsIntegration() throws Exception {
    // Given
    Long voyageId = 1L;
    ContainerSize containerSize = ContainerSize.TWENTY_FOOT;

    // Mock voyage
    Voyage voyage = createMockVoyage();
    when(voyageRepository.findById(voyageId)).thenReturn(Optional.of(voyage));

    // Mock historical prices
    VoyagePrice historicalPrice = createMockVoyagePrice();
    when(voyagePriceRepository.findHistoricalPricesSameRoute(
            anyLong(), anyLong(), anyLong(), eq(containerSize), any(PageRequest.class)))
        .thenReturn(List.of(historicalPrice));

    List<Object[]> mockCountResult = Collections.singletonList(new Object[] {1L, 5L});
    when(freightOrderRepository.countByVoyageIds(anyList()))
        .thenReturn(mockCountResult); // voyageId=1L, count=5

    // Setup test news context
    testRiskAnalysisService.setMockNewsContext(
        "Recent relevant news:\n- Port congestion in Shanghai\n\n");

    // Mock AI response with risk factors
    String aiResponse =
        """
        {
          "suggestedPriceLowUsd": 1200.00,
          "suggestedPriceHighUsd": 1400.00,
          "confidence": "MEDIUM",
          "reasoning": "Based on historical data and current news",
          "dataPoints": 5,
          "riskFactors": [
            {
              "factor": "Port congestion",
              "impact": "HIGH",
              "description": "Delays expected"
            }
          ]
        }
        """;
    when(aiClient.completeWithSchema(anyString(), anyString(), anyString())).thenReturn(aiResponse);

    // Mock risk factor parsing
    RiskFactor riskFactor = new RiskFactor();
    riskFactor.setFactor("Port congestion");
    riskFactor.setImpact(RiskImpact.HIGH);
    riskFactor.setDescription("Delays expected");
    testRiskAnalysisService.setMockRiskFactors(List.of(riskFactor));

    // When
    PriceSuggestionResponse response =
        priceSuggestionService.getPriceSuggestion(voyageId, containerSize);

    // Then
    assertNotNull(response);
    assertEquals(0, new BigDecimal("1200.00").compareTo(response.getSuggestedPriceLowUsd()));
    assertEquals(0, new BigDecimal("1400.00").compareTo(response.getSuggestedPriceHighUsd()));
    assertNotNull(response.getRiskFactors());
    assertEquals(1, response.getRiskFactors().size());
    assertEquals("Port congestion", response.getRiskFactors().get(0).getFactor());
    assertEquals(RiskImpact.HIGH, response.getRiskFactors().get(0).getImpact());
  }

  @Test
  @DisplayName("Fallback behavior when risk analysis fails")
  void testFallbackWhenRiskAnalysisFails() throws Exception {
    // Given
    Long voyageId = 1L;
    ContainerSize containerSize = ContainerSize.TWENTY_FOOT;

    // Mock voyage
    Voyage voyage = createMockVoyage();
    when(voyageRepository.findById(voyageId)).thenReturn(Optional.of(voyage));

    // Mock historical prices
    VoyagePrice historicalPrice = createMockVoyagePrice();
    when(voyagePriceRepository.findHistoricalPricesSameRoute(
            anyLong(), anyLong(), anyLong(), eq(containerSize), any(PageRequest.class)))
        .thenReturn(List.of(historicalPrice));

    List<Object[]> mockCountResult = Collections.singletonList(new Object[] {1L, 5L});
    when(freightOrderRepository.countByVoyageIds(anyList()))
        .thenReturn(mockCountResult); // voyageId=1L, count=5

    // Setup empty news context (simulating failure)
    testRiskAnalysisService.setMockNewsContext(""); // Empty context (news service failed)

    // Mock AI response without risk factors
    String aiResponse =
        """
        {
          "suggestedPriceLowUsd": 1200.00,
          "suggestedPriceHighUsd": 1400.00,
          "confidence": "MEDIUM",
          "reasoning": "Based on historical data only",
          "dataPoints": 5
        }
        """;
    when(aiClient.completeWithSchema(anyString(), anyString(), anyString())).thenReturn(aiResponse);

    testRiskAnalysisService.setMockRiskFactors(List.of());

    // When
    PriceSuggestionResponse response =
        priceSuggestionService.getPriceSuggestion(voyageId, containerSize);

    // Then
    assertNotNull(response);
    assertEquals(0, new BigDecimal("1200.00").compareTo(response.getSuggestedPriceLowUsd()));
    assertEquals(0, new BigDecimal("1400.00").compareTo(response.getSuggestedPriceHighUsd()));
    assertNotNull(response.getRiskFactors());
    assertTrue(response.getRiskFactors().isEmpty());
  }

  @Test
  @DisplayName("Response includes risk factors when news is available")
  void testResponseIncludesRiskFactorsWhenNewsAvailable() throws Exception {
    // Given
    Long voyageId = 1L;
    ContainerSize containerSize = ContainerSize.TWENTY_FOOT;

    // Mock voyage
    Voyage voyage = createMockVoyage();
    when(voyageRepository.findById(voyageId)).thenReturn(Optional.of(voyage));

    // Mock historical prices
    VoyagePrice historicalPrice = createMockVoyagePrice();
    when(voyagePriceRepository.findHistoricalPricesSameRoute(
            anyLong(), anyLong(), anyLong(), eq(containerSize), any(PageRequest.class)))
        .thenReturn(List.of(historicalPrice));

    List<Object[]> mockCountResult = Collections.singletonList(new Object[] {1L, 5L});
    when(freightOrderRepository.countByVoyageIds(anyList()))
        .thenReturn(mockCountResult); // voyageId=1L, count=5

    String newsContext =
        """
        Recent relevant news:
        - Red Sea disruptions affecting shipping routes
        - Shanghai port experiencing congestion

        """;
    testRiskAnalysisService.setMockNewsContext(newsContext);

    // Mock AI response
    String aiResponse =
        """
        {
          "suggestedPriceLowUsd": 1300.00,
          "suggestedPriceHighUsd": 1600.00,
          "confidence": "HIGH",
          "reasoning": "Historical data shows increased prices due to current disruptions",
          "dataPoints": 8,
          "riskFactors": [
            {
              "factor": "Red Sea disruptions",
              "impact": "HIGH",
              "description": "Alternative routes increase transit time and fuel costs"
            },
            {
              "factor": "Port congestion",
              "impact": "MEDIUM",
              "description": "Delays at Shanghai port affecting schedule reliability"
            }
          ]
        }
        """;
    when(aiClient.completeWithSchema(anyString(), anyString(), anyString())).thenReturn(aiResponse);

    // Mock risk factor parsing
    RiskFactor riskFactor1 = new RiskFactor();
    riskFactor1.setFactor("Red Sea disruptions");
    riskFactor1.setImpact(RiskImpact.HIGH);
    riskFactor1.setDescription("Alternative routes increase transit time and fuel costs");

    RiskFactor riskFactor2 = new RiskFactor();
    riskFactor2.setFactor("Port congestion");
    riskFactor2.setImpact(RiskImpact.MEDIUM);
    riskFactor2.setDescription("Delays at Shanghai port affecting schedule reliability");

    testRiskAnalysisService.setMockRiskFactors(List.of(riskFactor1, riskFactor2));

    // When
    PriceSuggestionResponse response =
        priceSuggestionService.getPriceSuggestion(voyageId, containerSize);

    // Then
    assertNotNull(response);
    assertNotNull(response.getRiskFactors());
    assertEquals(2, response.getRiskFactors().size());

    // Verify first risk factor
    RiskFactor firstRisk = response.getRiskFactors().get(0);
    assertEquals("Red Sea disruptions", firstRisk.getFactor());
    assertEquals(RiskImpact.HIGH, firstRisk.getImpact());
    assertTrue(firstRisk.getDescription().contains("Alternative routes"));

    // Verify second risk factor
    RiskFactor secondRisk = response.getRiskFactors().get(1);
    assertEquals("Port congestion", secondRisk.getFactor());
    assertEquals(RiskImpact.MEDIUM, secondRisk.getImpact());
    assertTrue(secondRisk.getDescription().contains("Shanghai port"));

    // Verify news integration was properly invoked
    verify(aiClient).completeWithSchema(anyString(), contains(newsContext), anyString());
  }

  private Voyage createMockVoyage() {
    Voyage voyage = new Voyage();
    voyage.setId(1L);
    voyage.setVoyageNumber("TEST001");

    Port departurePort = new Port();
    departurePort.setId(1L);
    departurePort.setName("Shanghai");
    departurePort.setUnlocode("CNSHA");

    Port arrivalPort = new Port();
    arrivalPort.setId(2L);
    arrivalPort.setName("Rotterdam");
    arrivalPort.setUnlocode("NLRTM");

    voyage.setDeparturePort(departurePort);
    voyage.setArrivalPort(arrivalPort);
    voyage.setDepartureTime(LocalDateTime.now().plusDays(7));
    voyage.setArrivalTime(LocalDateTime.now().plusDays(21));

    return voyage;
  }

  private VoyagePrice createMockVoyagePrice() {
    VoyagePrice voyagePrice = new VoyagePrice();
    voyagePrice.setId(1L);
    voyagePrice.setContainerSize(ContainerSize.TWENTY_FOOT);
    voyagePrice.setBasePriceUsd(new BigDecimal("1250.00"));

    // Create and set the associated voyage
    Voyage associatedVoyage = createMockVoyage();
    voyagePrice.setVoyage(associatedVoyage);

    return voyagePrice;
  }
}
