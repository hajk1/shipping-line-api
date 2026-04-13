package com.shipping.freightops.news.impl;

import static org.assertj.core.api.Assertions.assertThat;

import com.shipping.freightops.dto.MaritimeNewsArticle;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class StaticMaritimeNewsSourceTest {

  private StaticMaritimeNewsSource staticMaritimeNewsSource;

  @BeforeEach
  void setUp() {
    staticMaritimeNewsSource = new StaticMaritimeNewsSource();
  }

  @Test
  void getRecentHeadlines_returnsStaticNews() {
    List<MaritimeNewsArticle> result = staticMaritimeNewsSource.getRecentHeadlines("Any Route", 10);

    assertThat(result).isNotEmpty();
    assertThat(result).hasSize(10);
    assertThat(result.get(0).getHeadline()).isNotBlank();
    assertThat(result.get(0).getSource()).isNotBlank();
    assertThat(result.get(0).getPublishedDate()).isNotNull();
  }

  @Test
  void getRecentHeadlines_respectsMaxResults() {
    List<MaritimeNewsArticle> result = staticMaritimeNewsSource.getRecentHeadlines("Any Route", 3);

    assertThat(result).hasSize(3);
  }

  @Test
  void getRecentHeadlines_handlesLargeMaxResults() {
    List<MaritimeNewsArticle> result =
        staticMaritimeNewsSource.getRecentHeadlines("Any Route", 1000);

    assertThat(result).hasSizeLessThanOrEqualTo(1000);
  }

  @Test
  void getRecentHeadlines_handlesZeroMaxResults() {
    List<MaritimeNewsArticle> result = staticMaritimeNewsSource.getRecentHeadlines("Any Route", 0);

    assertThat(result).isEmpty();
  }
}
