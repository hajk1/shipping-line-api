package com.shipping.freightops.ai.dto;

import jakarta.validation.constraints.NotBlank;

public class AiTestRequest {
  @NotBlank(message = "Prompt must not be blank")
  private String prompt;

  public String getPrompt() {
    return prompt;
  }

  public void setPrompt(String prompt) {
    this.prompt = prompt;
  }
}
