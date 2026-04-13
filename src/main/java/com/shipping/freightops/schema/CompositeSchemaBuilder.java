package com.shipping.freightops.schema;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import org.springframework.stereotype.Component;

/**
 * Builds a composite schema by combining the main price suggestion schema with embedded risk factor
 * definitions to resolve external references.
 */
@Component
public class CompositeSchemaBuilder {

  private final PriceSuggestionSchemaBuilder priceSuggestionSchemaBuilder;
  private final RiskFactorSchemaBuilder riskFactorSchemaBuilder;
  private final ObjectMapper objectMapper;

  public CompositeSchemaBuilder(
      PriceSuggestionSchemaBuilder priceSuggestionSchemaBuilder,
      RiskFactorSchemaBuilder riskFactorSchemaBuilder,
      ObjectMapper objectMapper) {
    this.priceSuggestionSchemaBuilder = priceSuggestionSchemaBuilder;
    this.riskFactorSchemaBuilder = riskFactorSchemaBuilder;
    this.objectMapper = objectMapper;
  }

  /**
   * Builds a composite schema with risk factor definitions embedded as $defs to resolve external
   * references.
   */
  public String buildCompositeSchema() throws JsonProcessingException, IOException {
    // Get the main schema
    String mainSchemaJson = priceSuggestionSchemaBuilder.build();
    ObjectNode mainSchema = (ObjectNode) objectMapper.readTree(mainSchemaJson);

    // Get the risk factor schema
    String riskFactorSchemaJson = riskFactorSchemaBuilder.build();
    JsonNode riskFactorSchema = objectMapper.readTree(riskFactorSchemaJson);

    // Add risk factor as a $defs entry to resolve external references
    ObjectNode defs = objectMapper.createObjectNode();
    defs.set("riskFactor", riskFactorSchema);
    mainSchema.set("$defs", defs);

    // Update the reference to use internal $defs instead of external file
    ObjectNode riskFactorsProperty = (ObjectNode) mainSchema.at("/properties/riskFactors");
    if (riskFactorsProperty != null) {
      ObjectNode items = (ObjectNode) riskFactorsProperty.get("items");
      if (items != null) {
        items.put("$ref", "#/$defs/riskFactor");
      }
    }

    return objectMapper.writeValueAsString(mainSchema);
  }
}
