package com.shipping.freightops.ai.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
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

  private static final String CLAUDE_MESSAGES_PATH = "/v1/messages";
  private static final String JSON_SCHEMA_TYPE = "json_schema";
  private static final String OUTPUT_CONFIG_KEY = "output_config";
  private static final String FORMAT_KEY = "format";
  private static final String SCHEMA_KEY = "schema";
  private static final String CONTENT_KEY = "content";
  private static final String TYPE_KEY = "type";
  private static final String TEXT_KEY = "text";
  private static final String TEXT_BLOCK_TYPE = "text";
  private static final String MODEL_KEY = "model";
  private static final String MAX_TOKENS_KEY = "max_tokens";
  private static final String SYSTEM_KEY = "system";
  private static final String MESSAGES_KEY = "messages";
  private static final String ROLE_KEY = "role";
  private static final String USER_ROLE = "user";

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
            MODEL_KEY, aiProperties.getModel(),
            MAX_TOKENS_KEY, aiProperties.getMaxTokens(),
            SYSTEM_KEY, systemPrompt,
            MESSAGES_KEY, List.of(Map.of(ROLE_KEY, USER_ROLE, CONTENT_KEY, userPrompt)));

    String response =
        restClient.post().uri(CLAUDE_MESSAGES_PATH).body(request).retrieve().body(String.class);

    logUsage(response);

    return response;
  }

  @Override
  public String completeWithSchema(String systemPrompt, String userPrompt, String jsonSchema) {
    try {
      Map<String, Object> outputConfig = buildOutputConfig(jsonSchema);
      Map<String, Object> request = buildStructuredRequest(systemPrompt, userPrompt, outputConfig);
      String rawResponse =
          restClient.post().uri(CLAUDE_MESSAGES_PATH).body(request).retrieve().body(String.class);

      logUsage(rawResponse);

      return extractTextFromResponse(rawResponse);
    } catch (Exception e) {
      log.warn("Failed to extract content from Claude response", e);
      throw new RuntimeException("Claude structured output failed", e);
    }
  }

  private Map<String, Object> buildOutputConfig(String jsonSchema) throws Exception {
    Map<String, Object> schemaMap =
        objectMapper.readValue(jsonSchema, new TypeReference<Map<String, Object>>() {});
    return Map.of(FORMAT_KEY, Map.of(TYPE_KEY, JSON_SCHEMA_TYPE, SCHEMA_KEY, schemaMap));
  }

  private Map<String, Object> buildStructuredRequest(
      String systemPrompt, String userPrompt, Map<String, Object> outputConfig) {
    return Map.of(
        MODEL_KEY, aiProperties.getModel(),
        MAX_TOKENS_KEY, aiProperties.getMaxTokens(),
        SYSTEM_KEY, systemPrompt,
        MESSAGES_KEY, List.of(Map.of(ROLE_KEY, USER_ROLE, CONTENT_KEY, userPrompt)),
        OUTPUT_CONFIG_KEY, outputConfig);
  }

  private String extractTextFromResponse(String rawResponse) throws Exception {
    JsonNode root = objectMapper.readTree(rawResponse);
    JsonNode content = root.get(CONTENT_KEY);

    if (content == null || !content.isArray() || content.isEmpty()) {
      return rawResponse;
    }

    JsonNode firstBlock = content.get(0);
    if (firstBlock == null || !TEXT_BLOCK_TYPE.equals(firstBlock.path(TYPE_KEY).asText(null))) {
      return rawResponse;
    }

    JsonNode text = firstBlock.get(TEXT_KEY);
    if (text != null && text.isTextual()) {
      return text.asText();
    }

    return rawResponse;
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
