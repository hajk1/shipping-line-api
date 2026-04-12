package com.shipping.freightops.news;

import com.shipping.freightops.dto.MaritimeNewsArticle;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class ShippingNewsAnalyzer {

  private static final String ROUTE_SPLIT_REGEX = "[→\\-\\s]+";

  private static final Set<String> SHIPPING_KEYWORDS =
      Set.of(
          "port",
          "shipping",
          "container",
          "vessel",
          "freight",
          "cargo",
          "maritime",
          "ocean",
          "terminal",
          "logistics",
          "trade",
          "export",
          "import",
          "suez",
          "panama",
          "canal",
          "strait",
          "route",
          "disruption");

  private final MaritimeNewsSource maritimeNewsSource;

  public ShippingNewsAnalyzer(MaritimeNewsSource maritimeNewsSource) {
    this.maritimeNewsSource = maritimeNewsSource;
  }

  public List<MaritimeNewsArticle> getRelevantHeadlines(String route, int maxResults) {
    List<MaritimeNewsArticle> allHeadlines =
        maritimeNewsSource.getRecentHeadlines(route, maxResults * 2);

    if (allHeadlines.isEmpty()) {
      return List.of();
    }

    Set<String> routeKeywords = extractRouteKeywords(route);

    return allHeadlines.stream()
        .filter(item -> isRelevantToRoute(item, routeKeywords))
        .limit(maxResults)
        .collect(Collectors.toList());
  }

  private Set<String> extractRouteKeywords(String route) {
    return Arrays.stream(route.split(ROUTE_SPLIT_REGEX))
        .map(String::trim)
        .map(String::toLowerCase)
        .filter(keyword -> !keyword.isEmpty())
        .collect(Collectors.toSet());
  }

  private boolean isRelevantToRoute(MaritimeNewsArticle item, Set<String> routeKeywords) {
    String content = (item.getHeadline() + " " + item.getSummary()).toLowerCase();

    boolean matchesRouteKeywords =
        routeKeywords.stream().anyMatch(keyword -> content.contains(keyword));
    boolean matchesShippingKeywords = containsShippingKeywords(content);

    return matchesRouteKeywords || matchesShippingKeywords;
  }

  private boolean containsShippingKeywords(String content) {
    return SHIPPING_KEYWORDS.stream().anyMatch(content::contains);
  }
}
