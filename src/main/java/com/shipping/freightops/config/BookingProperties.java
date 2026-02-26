package com.shipping.freightops.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "app.booking")
public class BookingProperties {
  private int autoCutoffPercent;

  public int getAutoCutoffPercent() {
    return autoCutoffPercent;
  }
}
