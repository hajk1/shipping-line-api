package com.shipping.freightops.controller;

import com.shipping.freightops.ai.AiClient;
import com.shipping.freightops.ai.dto.AiTestRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/ai")
@ConditionalOnProperty(name = "app.ai.test.enabled", havingValue = "true")
public class AiTestController {
  private final AiClient aiClient;

  public AiTestController(AiClient aiClient) {
    this.aiClient = aiClient;
  }

  @Operation(
      summary = "Test AI completion",
      description =
          "Sends a prompt to the configured AI provider (Claude or OpenAI, ...) and returns the RAW response.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "AI response returned successfully"),
    @ApiResponse(
        responseCode = "400",
        description = "Bad request, e.g., invalid input or missing prompt"),
    @ApiResponse(
        responseCode = "503",
        description = "Service unavailable, e.g., AI provider unreachable")
  })
  @PostMapping("/test")
  public ResponseEntity<String> test(@Valid @RequestBody AiTestRequest request) {
    String response = aiClient.complete("You are a helpful assistant.", request.getPrompt());
    return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(response);
  }
}
