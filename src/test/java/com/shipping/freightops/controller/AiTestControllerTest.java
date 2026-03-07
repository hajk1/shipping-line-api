package com.shipping.freightops.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
public class AiTestControllerTest {
  @Autowired private MockMvc mockMvc;

  @Test
  void testAiEndpoint_returnsMockResponse() throws Exception {
    String jsonRequest =
        """
            {
                "prompt": "Hello AI"
            }
        """;

    mockMvc
        .perform(
            post("/api/v1/ai/test").contentType(MediaType.APPLICATION_JSON).content(jsonRequest))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(
            jsonPath("$.welcome").value("This is a mock AI response of the Noop implementation!"))
        .andExpect(jsonPath("$.mock").value(true))
        .andExpect(jsonPath("$.prompt").value("Hello AI"));
  }
}
