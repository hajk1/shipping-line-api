package com.shipping.freightops.schema;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.shipping.freightops.enums.RiskImpact;
import java.io.IOException;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

/** Builds the risk factor JSON schema with enum values injected from RiskImpact. */
@Component
public class RiskFactorSchemaBuilder {

  private static final String SCHEMA_PATH = "schemas/risk-factor.json";

  private final ObjectMapper objectMapper;

  public RiskFactorSchemaBuilder(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  public String build() throws JsonProcessingException, IOException {
    ObjectNode root =
        (ObjectNode) objectMapper.readTree(new ClassPathResource(SCHEMA_PATH).getInputStream());
    ObjectNode properties = (ObjectNode) root.get("properties");

    // Inject RiskImpact enum values
    ObjectNode impact = (ObjectNode) properties.get("impact");
    ArrayNode impactEnumArray = objectMapper.createArrayNode();
    for (RiskImpact r : RiskImpact.values()) {
      impactEnumArray.add(r.name());
    }
    impact.set("enum", impactEnumArray);

    return objectMapper.writeValueAsString(root);
  }
}
