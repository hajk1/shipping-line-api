package com.shipping.freightops.news.config;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.news")
public class NewsProperties {

  private String provider = "static";
  private List<String> feeds = List.of();
  private int maxHeadlines = 5;
  private int connectTimeout = 10;
  private int readTimeout = 30;

  public String getProvider() {
    return provider;
  }

  public void setProvider(String provider) {
    this.provider = provider;
  }

  public List<String> getFeeds() {
    return feeds;
  }

  public void setFeeds(List<String> feeds) {
    this.feeds = feeds;
  }

  public int getMaxHeadlines() {
    return maxHeadlines;
  }

  public void setMaxHeadlines(int maxHeadlines) {
    this.maxHeadlines = maxHeadlines;
  }

  public int getConnectTimeout() {
    return connectTimeout;
  }

  public void setConnectTimeout(int connectTimeout) {
    this.connectTimeout = connectTimeout;
  }

  public int getReadTimeout() {
    return readTimeout;
  }

  public void setReadTimeout(int readTimeout) {
    this.readTimeout = readTimeout;
  }
}
