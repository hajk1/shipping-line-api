package com.shipping.freightops.ai.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shipping.freightops.ai.AiClient;
import com.shipping.freightops.ai.config.AiProperties;
import com.shipping.freightops.ai.dto.ClaudeResponse;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@ConditionalOnProperty(name = "app.ai.provider", havingValue = "claude")
public class ClaudeAiClient implements AiClient {
  private final RestClient restClient;
  private final AiProperties aiProperties;
  private final ObjectMapper objectMapper;
  private static final Logger log = LoggerFactory.getLogger(ClaudeAiClient.class);

  public ClaudeAiClient(
      RestClient.Builder builder, AiProperties aiProperties, ObjectMapper objectMapper) {
    this.aiProperties = aiProperties;
    this.objectMapper = objectMapper;

    this.restClient =
        builder
            .defaultHeader("x-api-key", aiProperties.getApiKey())
            .defaultHeader("anthropic-version", aiProperties.getAnthropic_version())
            .build();
  }

  @Override
  public String complete(String systemPrompt, String userPrompt) {
    Map<String, Object> request =
        Map.of(
            "model", aiProperties.getModel(),
            "max_tokens", aiProperties.getMaxTokens(),
            "system", systemPrompt,
            "messages", List.of(Map.of("role", "user", "content", userPrompt)));

    String response =
        restClient.post().uri("/v1/messages").body(request).retrieve().body(String.class);

    logUsage(response);

    return response;
  }

  private void logUsage(String rawResponse) {
    try {
      ClaudeResponse parsed = objectMapper.readValue(rawResponse, ClaudeResponse.class);
      if (parsed != null && parsed.usage() != null) {
        log.info(
            "Claude tokens: input={}, output={}",
            parsed.usage().input_tokens(),
            parsed.usage().output_tokens());
      }
    } catch (Exception e) {
      log.warn("Could not parse Claude usage info");
    }
  }
}
