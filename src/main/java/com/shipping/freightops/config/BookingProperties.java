package com.shipping.freightops.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.booking")
public class BookingProperties {
  private int autoCutoffPercent;

  public int getAutoCutoffPercent() {
    return autoCutoffPercent;
  }

  public void setAutoCutoffPercent(int autoCutoffPercent) {
    this.autoCutoffPercent = autoCutoffPercent;
  }
}
