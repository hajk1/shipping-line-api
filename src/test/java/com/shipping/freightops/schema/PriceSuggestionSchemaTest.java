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
    PriceSuggestionSchemaBuilder schemaBuilder =
        new PriceSuggestionSchemaBuilder(new ObjectMapper());
    String schemaJson = schemaBuilder.build();
    JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SCHEMA_VERSION);
    jsonSchema = factory.getSchema(schemaJson);
    objectMapper = new ObjectMapper();
  }

  @Test
  @DisplayName("Schema file is valid JSON Schema")
  void schemaIsValid() {
    assertTrue(jsonSchema != null);
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
  @DisplayName("JSON with wrong type for confidence is rejected")
  void rejectsWrongConfidenceType() throws Exception {
    String invalidJson =
        """
        {
          "suggestedPriceLowUsd": 1100.00,
          "suggestedPriceHighUsd": 1350.00,
          "confidence": 123,
          "reasoning": "Test",
          "dataPoints": 12
        }
        """;
    JsonNode node = objectMapper.readTree(invalidJson);
    Set<ValidationMessage> errors = jsonSchema.validate(node);
    assertFalse(errors.isEmpty());
  }

  @Test
  @DisplayName("JSON with invalid enum value for confidence is rejected")
  void rejectsInvalidConfidenceEnum() throws Exception {
    String invalidJson =
        """
        {
          "suggestedPriceLowUsd": 1100.00,
          "suggestedPriceHighUsd": 1350.00,
          "confidence": "INVALID",
          "reasoning": "Test",
          "dataPoints": 12
        }
        """;
    JsonNode node = objectMapper.readTree(invalidJson);
    Set<ValidationMessage> errors = jsonSchema.validate(node);
    assertFalse(errors.isEmpty());
  }

  @Test
  @DisplayName("JSON with additional properties is rejected when additionalProperties is false")
  void rejectsAdditionalProperties() throws Exception {
    String invalidJson =
        """
        {
          "suggestedPriceLowUsd": 1100.00,
          "suggestedPriceHighUsd": 1350.00,
          "confidence": "MEDIUM",
          "reasoning": "Test",
          "dataPoints": 12,
          "extraField": "not allowed"
        }
        """;
    JsonNode node = objectMapper.readTree(invalidJson);
    Set<ValidationMessage> errors = jsonSchema.validate(node);
    assertFalse(errors.isEmpty());
  }

  @Test
  @DisplayName("Minimal valid JSON with only required fields passes")
  void acceptsMinimalValidJson() throws Exception {
    String minimalJson =
        """
        {
          "suggestedPriceLowUsd": 1000.00,
          "suggestedPriceHighUsd": 1200.00,
          "confidence": "LOW",
          "reasoning": "Insufficient data.",
          "dataPoints": 0
        }
        """;
    JsonNode node = objectMapper.readTree(minimalJson);
    Set<ValidationMessage> errors = jsonSchema.validate(node);
    assertTrue(errors.isEmpty(), "Expected no validation errors: " + errors);
  }
}
