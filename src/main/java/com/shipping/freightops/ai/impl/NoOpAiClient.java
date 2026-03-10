package com.shipping.freightops.ai.impl;

import com.shipping.freightops.ai.AiClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.ai.provider", havingValue = "noop", matchIfMissing = true)
public class NoOpAiClient implements AiClient {

  private static final String SCHEMA_COMPLIANT_MOCK =
      """
      {
        "suggestedPriceLowUsd": 1000.00,
        "suggestedPriceHighUsd": 1200.00,
        "confidence": "MEDIUM",
        "reasoning": "Mock response from NoOpAiClient. No real AI analysis performed.",
        "dataPoints": 0
      }
      """;

  @Override
  public String complete(String systemPrompt, String userPrompt) {
    return "{\"welcome\":\"This is a mock AI response of the Noop implementation!\", \"mock\":true, \"prompt\":\""
        + userPrompt
        + "\"}";
  }

  @Override
  public String completeWithSchema(String systemPrompt, String userPrompt, String jsonSchema) {
    return SCHEMA_COMPLIANT_MOCK;
  }
}
