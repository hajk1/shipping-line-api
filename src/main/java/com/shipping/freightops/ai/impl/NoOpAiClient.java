package com.shipping.freightops.ai.impl;

import com.shipping.freightops.ai.AiClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.ai.provider", havingValue = "noop", matchIfMissing = true)
public class NoOpAiClient implements AiClient {

  @Override
  public String complete(String systemPrompt, String userPrompt) {
    return "{\"welcome\":\"This is a mock AI response of the Noop implementation!\", \"mock\":true, \"prompt\":\""
        + userPrompt
        + "\"}";
  }
}
