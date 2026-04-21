package com.shipping.freightops.dto;

import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Represents a maritime news article used for shipping risk analysis and freight pricing decisions.
 * Contains headline, source, publication date, and summary information relevant to maritime
 * operations.
 */
@Getter
@Setter
@NoArgsConstructor
public class MaritimeNewsArticle {
  private String headline;
  private String source;
  private LocalDate publishedDate;
  private String summary;

  public MaritimeNewsArticle(
      String headline, String source, LocalDate publishedDate, String summary) {
    this.headline = headline;
    this.source = source;
    this.publishedDate = publishedDate;
    this.summary = summary;
  }
}
