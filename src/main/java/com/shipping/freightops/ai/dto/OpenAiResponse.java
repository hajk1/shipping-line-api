package com.shipping.freightops.ai.dto;

import java.awt.*;

public record OpenAiResponse(Usage usage) {
  public record Usage(int prompt_tokens, int completion_tokens, int total_tokens) {}
}
