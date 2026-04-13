package com.shipping.freightops.news.impl;

import com.shipping.freightops.dto.MaritimeNewsArticle;
import com.shipping.freightops.news.MaritimeNewsSource;
import java.util.List;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.news.provider", havingValue = "noop")
public class NoOpMaritimeNewsSource implements MaritimeNewsSource {

  @Override
  public List<MaritimeNewsArticle> getRecentHeadlines(String route, int maxResults) {
    return List.of();
  }
}
