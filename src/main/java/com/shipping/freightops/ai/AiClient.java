package com.shipping.freightops.ai;

public interface AiClient {
  String complete(String systemPrompt, String userPrompt);

  /**
   * Same as complete(), but enforces a JSON Schema on the output. Returns the raw JSON response.
   */
  String completeWithSchema(String systemPrompt, String userPrompt, String jsonSchema);
}
