package com.shipping.freightops.news.impl;

import static org.assertj.core.api.Assertions.assertThat;

import com.shipping.freightops.dto.MaritimeNewsArticle;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class NoOpMaritimeNewsSourceTest {

  private NoOpMaritimeNewsSource noOpMaritimeNewsSource;

  @BeforeEach
  void setUp() {
    noOpMaritimeNewsSource = new NoOpMaritimeNewsSource();
  }

  @Test
  void getRecentHeadlines_alwaysReturnsEmpty() {
    List<MaritimeNewsArticle> result = noOpMaritimeNewsSource.getRecentHeadlines("Any Route", 10);

    assertThat(result).isEmpty();
  }

  @Test
  void getRecentHeadlines_returnsEmptyRegardlessOfMaxResults() {
    List<MaritimeNewsArticle> result1 = noOpMaritimeNewsSource.getRecentHeadlines("Route 1", 1);
    List<MaritimeNewsArticle> result2 = noOpMaritimeNewsSource.getRecentHeadlines("Route 2", 100);

    assertThat(result1).isEmpty();
    assertThat(result2).isEmpty();
  }
}
