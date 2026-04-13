package com.shipping.freightops.service;

import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shipping.freightops.dto.MaritimeNewsArticle;
import com.shipping.freightops.dto.RiskFactor;
import com.shipping.freightops.enums.RiskImpact;
import com.shipping.freightops.news.config.NewsProperties;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RiskAnalysisServiceTest {

  private RiskAnalysisService riskAnalysisService;
  private ObjectMapper objectMapper;

  @BeforeEach
  void setUp() {
    // Create a simple test instance focusing only on standalone methods
    // We'll test parseRiskFactors and buildNewsContext which don't need complex dependencies
    objectMapper = new ObjectMapper();

    // Create minimal dependencies for the service
    NewsProperties newsProperties = new NewsProperties();
    newsProperties.setMaxHeadlines(5);

    // Use null for ShippingNewsAnalyzer since we won't test methods that use it
    riskAnalysisService = new RiskAnalysisService(null, newsProperties);
  }

  @Test
  @DisplayName("parseRiskFactors handles valid JSON array correctly")
  void testParseRiskFactorsWithValidJson() throws Exception {
    // Given
    String validJson =
        """
        {
          "riskFactors": [
            {
              "factor": "Port congestion in Shanghai",
              "impact": "HIGH",
              "description": "Severe delays expected due to restrictions"
            },
            {
              "factor": "Fuel price volatility",
              "impact": "MEDIUM",
              "description": "Oil prices fluctuating"
            }
          ]
        }
        """;
    JsonNode parsed = objectMapper.readTree(validJson);

    // When
    List<RiskFactor> riskFactors = riskAnalysisService.parseRiskFactors(parsed);

    // Then
    assertEquals(2, riskFactors.size());

    RiskFactor firstRisk = riskFactors.get(0);
    assertEquals("Port congestion in Shanghai", firstRisk.getFactor());
    assertEquals(RiskImpact.HIGH, firstRisk.getImpact());
    assertEquals("Severe delays expected due to restrictions", firstRisk.getDescription());

    RiskFactor secondRisk = riskFactors.get(1);
    assertEquals("Fuel price volatility", secondRisk.getFactor());
    assertEquals(RiskImpact.MEDIUM, secondRisk.getImpact());
    assertEquals("Oil prices fluctuating", secondRisk.getDescription());
  }

  @Test
  @DisplayName("parseRiskFactors handles malformed and null input gracefully")
  void testParseRiskFactorsWithMalformedInput() throws Exception {
    // Test with non-array riskFactors
    String nonArrayJson =
        """
        {
          "riskFactors": "not an array"
        }
        """;
    JsonNode parsed = objectMapper.readTree(nonArrayJson);
    List<RiskFactor> result = riskAnalysisService.parseRiskFactors(parsed);
    assertTrue(result.isEmpty());

    // Test with missing riskFactors field
    String missingFieldJson =
        """
        {
          "otherField": "value"
        }
        """;
    parsed = objectMapper.readTree(missingFieldJson);
    result = riskAnalysisService.parseRiskFactors(parsed);
    assertTrue(result.isEmpty());

    // Test with empty factor (should be skipped)
    String emptyFactorJson =
        """
        {
          "riskFactors": [
            {
              "factor": "",
              "impact": "HIGH",
              "description": "Should be skipped"
            },
            {
              "factor": "Valid factor",
              "impact": "LOW",
              "description": "Should be included"
            }
          ]
        }
        """;
    parsed = objectMapper.readTree(emptyFactorJson);
    result = riskAnalysisService.parseRiskFactors(parsed);
    assertEquals(1, result.size());
    assertEquals("Valid factor", result.get(0).getFactor());

    // Test with invalid impact (should default to LOW)
    String invalidImpactJson =
        """
        {
          "riskFactors": [
            {
              "factor": "Test factor",
              "impact": "INVALID_IMPACT",
              "description": "Test description"
            }
          ]
        }
        """;
    parsed = objectMapper.readTree(invalidImpactJson);
    result = riskAnalysisService.parseRiskFactors(parsed);
    assertEquals(1, result.size());
    assertEquals(RiskImpact.LOW, result.get(0).getImpact());
  }

  @Test
  @DisplayName("parseRiskFactors handles empty and missing fields gracefully")
  void testParseRiskFactorsEdgeCases() throws Exception {
    // Test with missing description field
    String missingDescJson =
        """
        {
          "riskFactors": [
            {
              "factor": "Test factor",
              "impact": "HIGH"
            }
          ]
        }
        """;
    JsonNode parsed = objectMapper.readTree(missingDescJson);
    List<RiskFactor> result = riskAnalysisService.parseRiskFactors(parsed);
    assertEquals(1, result.size());
    assertEquals("Test factor", result.get(0).getFactor());
    assertEquals(RiskImpact.HIGH, result.get(0).getImpact());
    assertNull(result.get(0).getDescription()); // Should be null when missing

    // Test with missing impact field (should default to LOW)
    String missingImpactJson =
        """
        {
          "riskFactors": [
            {
              "factor": "Another test factor"
            }
          ]
        }
        """;
    parsed = objectMapper.readTree(missingImpactJson);
    result = riskAnalysisService.parseRiskFactors(parsed);
    assertEquals(1, result.size());
    assertEquals(RiskImpact.LOW, result.get(0).getImpact());
  }

  @Test
  @DisplayName("buildNewsContext handles empty news and returns empty string")
  void testBuildNewsContextWithEmptyNews() {
    // Test with null news
    String result = riskAnalysisService.buildNewsContext(null);
    assertEquals("", result);

    // Test with empty list
    result = riskAnalysisService.buildNewsContext(List.of());
    assertEquals("", result);

    // Test with non-empty news
    MaritimeNewsArticle article1 =
        new MaritimeNewsArticle(
            "Port congestion reported", "Maritime News", LocalDate.now(), "Delays expected");
    MaritimeNewsArticle article2 =
        new MaritimeNewsArticle("Fuel prices rising", "Shipping Today", LocalDate.now(), null);

    result = riskAnalysisService.buildNewsContext(List.of(article1, article2));

    assertNotNull(result);
    assertTrue(result.contains("Recent relevant news:"));
    assertTrue(result.contains("Port congestion reported"));
    assertTrue(result.contains("(Delays expected)"));
    assertTrue(result.contains("Fuel prices rising"));
    assertFalse(result.contains("(null)"));
  }
}
