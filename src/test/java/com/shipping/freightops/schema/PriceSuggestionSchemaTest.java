package com.shipping.freightops.schema;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PriceSuggestionSchemaTest {

  private JsonSchema jsonSchema;
  private ObjectMapper objectMapper;
  private static final SpecVersion.VersionFlag SCHEMA_VERSION = SpecVersion.VersionFlag.V202012;

  @BeforeEach
  void setUp() throws Exception {
    ObjectMapper mapper = new ObjectMapper();
    PriceSuggestionSchemaBuilder priceSuggestionBuilder = new PriceSuggestionSchemaBuilder(mapper);
    RiskFactorSchemaBuilder riskFactorBuilder = new RiskFactorSchemaBuilder(mapper);
    CompositeSchemaBuilder compositeBuilder =
        new CompositeSchemaBuilder(priceSuggestionBuilder, riskFactorBuilder, mapper);
    String schemaJson = compositeBuilder.buildCompositeSchema();
    JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SCHEMA_VERSION);
    jsonSchema = factory.getSchema(schemaJson);
    objectMapper = mapper;
  }

  @Test
  @DisplayName("Valid JSON with all required fields passes validation")
  void acceptsValidJson() throws Exception {
    String validJson =
        """
        {
          "suggestedPriceLowUsd": 1100.00,
          "suggestedPriceHighUsd": 1350.00,
          "confidence": "MEDIUM",
          "reasoning": "Based on 12 past voyages.",
          "dataPoints": 12,
          "historicalAvgUsd": 1180.00,
          "historicalMinUsd": 950.00,
          "historicalMaxUsd": 1400.00
        }
        """;
    JsonNode node = objectMapper.readTree(validJson);
    Set<ValidationMessage> errors = jsonSchema.validate(node);
    assertTrue(errors.isEmpty(), "Expected no validation errors: " + errors);
  }

  @Test
  @DisplayName("JSON missing required field reasoning is rejected")
  void rejectsMissingReasoning() throws Exception {
    String invalidJson =
        """
        {
          "suggestedPriceLowUsd": 1100.00,
          "suggestedPriceHighUsd": 1350.00,
          "confidence": "MEDIUM",
          "dataPoints": 12
        }
        """;
    JsonNode node = objectMapper.readTree(invalidJson);
    Set<ValidationMessage> errors = jsonSchema.validate(node);
    assertFalse(errors.isEmpty());
    assertTrue(
        errors.stream().anyMatch(e -> e.getMessage().contains("reasoning")),
        "Expected error about reasoning: " + errors);
  }

  @Test
  @DisplayName("Valid JSON with risk factors passes validation")
  void acceptsValidJsonWithRiskFactors() throws Exception {
    String validJsonWithRiskFactors =
        """
        {
          "suggestedPriceLowUsd": 1100.00,
          "suggestedPriceHighUsd": 1350.00,
          "confidence": "MEDIUM",
          "reasoning": "Based on 12 past voyages with risk factors considered.",
          "dataPoints": 12,
          "historicalAvgUsd": 1180.00,
          "historicalMinUsd": 950.00,
          "historicalMaxUsd": 1400.00,
          "riskFactors": [
            {
              "factor": "Port congestion in Shanghai",
              "impact": "HIGH",
              "description": "Severe delays expected due to COVID-19 restrictions affecting port operations"
            },
            {
              "factor": "Fuel price volatility",
              "impact": "MEDIUM",
              "description": "Oil prices fluctuating due to geopolitical tensions"
            }
          ]
        }
        """;
    JsonNode node = objectMapper.readTree(validJsonWithRiskFactors);
    Set<ValidationMessage> errors = jsonSchema.validate(node);
    assertTrue(errors.isEmpty(), "Expected no validation errors: " + errors);
  }
}
