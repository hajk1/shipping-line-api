package com.shipping.freightops.schema;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.shipping.freightops.enums.PriceSuggestionConfidence;
import java.io.IOException;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

/**
 * Builds the price suggestion JSON schema with enum values injected from PriceSuggestionConfidence.
 */
@Component
public class PriceSuggestionSchemaBuilder {

  private static final String SCHEMA_PATH = "schemas/price-suggestion.json";

  private final ObjectMapper objectMapper;

  public PriceSuggestionSchemaBuilder(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  public String build() throws JsonProcessingException, IOException {
    ObjectNode root =
        (ObjectNode) objectMapper.readTree(new ClassPathResource(SCHEMA_PATH).getInputStream());
    ObjectNode properties = (ObjectNode) root.get("properties");
    ObjectNode confidence = (ObjectNode) properties.get("confidence");
    ArrayNode enumArray = objectMapper.createArrayNode();
    for (PriceSuggestionConfidence c : PriceSuggestionConfidence.values()) {
      enumArray.add(c.name());
    }
    confidence.set("enum", enumArray);
    return objectMapper.writeValueAsString(root);
  }
}
