package com.shipping.freightops.ai.config;

import java.net.http.HttpClient;
import java.time.Duration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class AiConfig {

  @Bean
  public RestClient.Builder aiRestClientBuilder(AiProperties properties) {
    var requestFactory =
        new JdkClientHttpRequestFactory(
            HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(properties.getConnectTimeout()))
                .build());

    return RestClient.builder().requestFactory(requestFactory).baseUrl(properties.getBaseUrl());
  }
}
