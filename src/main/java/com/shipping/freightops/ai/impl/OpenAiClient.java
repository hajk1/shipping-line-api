package com.shipping.freightops.ai.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shipping.freightops.ai.AiClient;
import com.shipping.freightops.ai.config.AiProperties;
import com.shipping.freightops.ai.dto.OpenAiResponse;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@ConditionalOnProperty(name = "app.ai.provider", havingValue = "openai")
public class OpenAiClient implements AiClient {

  private final RestClient restClient;
  private final AiProperties aiProperties;
  private final ObjectMapper objectMapper;
  private static final Logger log = LoggerFactory.getLogger(OpenAiClient.class);

  public OpenAiClient(
      RestClient.Builder builder, AiProperties aiProperties, ObjectMapper objectMapper) {
    this.aiProperties = aiProperties;
    this.objectMapper = objectMapper;
    this.restClient =
        builder.defaultHeader("Authorization", "Bearer " + aiProperties.getApiKey()).build();
  }

  @Override
  public String complete(String systemPrompt, String userPrompt) {
    Map<String, Object> request =
        Map.of(
            "model", aiProperties.getModel(),
            "messages",
                List.of(
                    Map.of("role", "system", "content", systemPrompt),
                    Map.of("role", "user", "content", userPrompt)),
            "max_completion_tokens", aiProperties.getMaxTokens());

    String response =
        restClient.post().uri("/chat/completions").body(request).retrieve().body(String.class);

    logUsage(response);

    return response;
  }

  private void logUsage(String rawResponse) {
    try {
      OpenAiResponse parsed = objectMapper.readValue(rawResponse, OpenAiResponse.class);

      if (parsed != null && parsed.usage() != null) {
        log.info(
            "OpenAI tokens: prompt={}, completion={}, total={}",
            parsed.usage().prompt_tokens(),
            parsed.usage().completion_tokens(),
            parsed.usage().total_tokens());
      }

    } catch (Exception e) {
      log.warn("Could not parse OpenAI usage info");
    }
  }
}
