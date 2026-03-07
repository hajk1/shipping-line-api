package com.shipping.freightops.ai;

public interface AiClient {
  String complete(String systemPrompt, String userPrompt);
}
