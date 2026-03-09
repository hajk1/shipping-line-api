package com.shipping.freightops.ai.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
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

  private static final String OPENAI_CHAT_PATH = "/v1/chat/completions";
  private static final String JSON_SCHEMA_TYPE = "json_schema";
  private static final String RESPONSE_FORMAT_KEY = "response_format";
  private static final String SCHEMA_NAME = "structured_output";
  private static final String SCHEMA_KEY = "schema";
  private static final String NAME_KEY = "name";
  private static final String STRICT_KEY = "strict";
  private static final String CHOICES_KEY = "choices";
  private static final String MESSAGE_KEY = "message";
  private static final String CONTENT_KEY = "content";
  private static final String MODEL_KEY = "model";
  private static final String MESSAGES_KEY = "messages";
  private static final String ROLE_KEY = "role";
  private static final String SYSTEM_ROLE = "system";
  private static final String USER_ROLE = "user";
  private static final String MAX_COMPLETION_TOKENS_KEY = "max_completion_tokens";

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
            MODEL_KEY, aiProperties.getModel(),
            MESSAGES_KEY,
                List.of(
                    Map.of(ROLE_KEY, SYSTEM_ROLE, CONTENT_KEY, systemPrompt),
                    Map.of(ROLE_KEY, USER_ROLE, CONTENT_KEY, userPrompt)),
            MAX_COMPLETION_TOKENS_KEY, aiProperties.getMaxTokens());

    String response =
        restClient.post().uri(OPENAI_CHAT_PATH).body(request).retrieve().body(String.class);

    logUsage(response);

    return response;
  }

  @Override
  public String completeWithSchema(String systemPrompt, String userPrompt, String jsonSchema) {
    try {
      Map<String, Object> responseFormat = buildResponseFormat(jsonSchema);
      Map<String, Object> request =
          buildStructuredRequest(systemPrompt, userPrompt, responseFormat);
      String rawResponse =
          restClient.post().uri(OPENAI_CHAT_PATH).body(request).retrieve().body(String.class);

      logUsage(rawResponse);

      return extractTextFromResponse(rawResponse);
    } catch (Exception e) {
      log.warn("Failed to extract content from OpenAI response", e);
      throw new RuntimeException("OpenAI structured output failed", e);
    }
  }

  private Map<String, Object> buildResponseFormat(String jsonSchema) throws Exception {
    Map<String, Object> schemaMap =
        objectMapper.readValue(jsonSchema, new TypeReference<Map<String, Object>>() {});
    Map<String, Object> jsonSchemaConfig =
        Map.of(NAME_KEY, SCHEMA_NAME, STRICT_KEY, true, SCHEMA_KEY, schemaMap);
    return Map.of("type", JSON_SCHEMA_TYPE, "json_schema", jsonSchemaConfig);
  }

  private Map<String, Object> buildStructuredRequest(
      String systemPrompt, String userPrompt, Map<String, Object> responseFormat) {
    return Map.of(
        MODEL_KEY, aiProperties.getModel(),
        MESSAGES_KEY,
            List.of(
                Map.of(ROLE_KEY, SYSTEM_ROLE, CONTENT_KEY, systemPrompt),
                Map.of(ROLE_KEY, USER_ROLE, CONTENT_KEY, userPrompt)),
        MAX_COMPLETION_TOKENS_KEY, aiProperties.getMaxTokens(),
        RESPONSE_FORMAT_KEY, responseFormat);
  }

  private String extractTextFromResponse(String rawResponse) throws Exception {
    JsonNode root = objectMapper.readTree(rawResponse);
    JsonNode choices = root.get(CHOICES_KEY);

    if (choices == null || !choices.isArray() || choices.isEmpty()) {
      return rawResponse;
    }

    JsonNode message = choices.get(0).get(MESSAGE_KEY);
    if (message == null) {
      return rawResponse;
    }

    JsonNode content = message.get(CONTENT_KEY);
    if (content != null && content.isTextual()) {
      return content.asText();
    }

    return rawResponse;
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
