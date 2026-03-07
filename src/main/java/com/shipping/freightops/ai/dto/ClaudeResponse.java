package com.shipping.freightops.ai.dto;

public record ClaudeResponse(Usage usage) {
  public record Usage(int input_tokens, int output_tokens) {}
}
