package com.shipping.freightops.dto;

import java.time.LocalDate;

/**
 * Represents a maritime news article used for shipping risk analysis and freight pricing decisions.
 * Contains headline, source, publication date, and summary information relevant to maritime
 * operations.
 */
public class MaritimeNewsArticle {
  private String headline;
  private String source;
  private LocalDate publishedDate;
  private String summary;

  public MaritimeNewsArticle() {}

  public MaritimeNewsArticle(
      String headline, String source, LocalDate publishedDate, String summary) {
    this.headline = headline;
    this.source = source;
    this.publishedDate = publishedDate;
    this.summary = summary;
  }

  public String getHeadline() {
    return headline;
  }

  public void setHeadline(String headline) {
    this.headline = headline;
  }

  public String getSource() {
    return source;
  }

  public void setSource(String source) {
    this.source = source;
  }

  public LocalDate getPublishedDate() {
    return publishedDate;
  }

  public void setPublishedDate(LocalDate publishedDate) {
    this.publishedDate = publishedDate;
  }

  public String getSummary() {
    return summary;
  }

  public void setSummary(String summary) {
    this.summary = summary;
  }
}
