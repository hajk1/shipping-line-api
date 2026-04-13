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

class RiskFactorSchemaTest {

  private JsonSchema jsonSchema;
  private ObjectMapper objectMapper;
  private static final SpecVersion.VersionFlag SCHEMA_VERSION = SpecVersion.VersionFlag.V202012;

  @BeforeEach
  void setUp() throws Exception {
    RiskFactorSchemaBuilder schemaBuilder = new RiskFactorSchemaBuilder(new ObjectMapper());
    String schemaJson = schemaBuilder.build();
    JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SCHEMA_VERSION);
    jsonSchema = factory.getSchema(schemaJson);
    objectMapper = new ObjectMapper();
  }

  @Test
  @DisplayName("Valid risk factor with all required fields passes validation")
  void acceptsValidRiskFactor() throws Exception {
    String validRiskFactor =
        """
        {
          "factor": "Port congestion in Shanghai",
          "impact": "HIGH",
          "description": "Severe delays expected due to COVID-19 restrictions affecting port operations"
        }
        """;
    JsonNode node = objectMapper.readTree(validRiskFactor);
    Set<ValidationMessage> errors = jsonSchema.validate(node);
    assertTrue(errors.isEmpty(), "Expected no validation errors: " + errors);
  }

  @Test
  @DisplayName("Risk factor with invalid impact enum is rejected")
  void rejectsInvalidImpactEnum() throws Exception {
    String invalidRiskFactor =
        """
        {
          "factor": "Test factor",
          "impact": "INVALID_IMPACT",
          "description": "Test description"
        }
        """;
    JsonNode node = objectMapper.readTree(invalidRiskFactor);
    Set<ValidationMessage> errors = jsonSchema.validate(node);
    assertFalse(errors.isEmpty());
    assertTrue(
        errors.stream()
            .anyMatch(e -> e.getMessage().contains("does not have a value in the enumeration")),
        "Expected error about invalid impact enum: " + errors);
  }
}
