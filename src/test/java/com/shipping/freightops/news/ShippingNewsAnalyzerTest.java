package com.shipping.freightops.news;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.shipping.freightops.dto.MaritimeNewsArticle;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class ShippingNewsAnalyzerTest {

  @Mock private MaritimeNewsSource maritimeNewsSource;

  private ShippingNewsAnalyzer shippingNewsAnalyzer;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    shippingNewsAnalyzer = new ShippingNewsAnalyzer(maritimeNewsSource);
  }

  @Test
  void getRelevantHeadlines_filtersRouteRelevantNews() {
    List<MaritimeNewsArticle> mockNews =
        List.of(
            new MaritimeNewsArticle(
                "Shanghai Port Congestion Delays",
                "Maritime News",
                LocalDate.now(),
                "Port congestion in Shanghai"),
            new MaritimeNewsArticle(
                "Los Angeles Terminal Expansion",
                "Port Authority",
                LocalDate.now(),
                "New terminal in Los Angeles"),
            new MaritimeNewsArticle(
                "Weather Update for Europe",
                "Weather Service",
                LocalDate.now(),
                "Storms expected in Europe"));

    when(maritimeNewsSource.getRecentHeadlines(anyString(), anyInt())).thenReturn(mockNews);

    List<MaritimeNewsArticle> result =
        shippingNewsAnalyzer.getRelevantHeadlines("Shanghai → Los Angeles", 5);

    assertThat(result).hasSize(2);
    assertThat(result.get(0).getHeadline()).contains("Shanghai");
    assertThat(result.get(1).getHeadline()).contains("Los Angeles");
  }

  @Test
  void getRelevantHeadlines_filtersShippingKeywords() {
    List<MaritimeNewsArticle> mockNews =
        List.of(
            new MaritimeNewsArticle(
                "Container Freight Rates Rise",
                "Shipping Times",
                LocalDate.now(),
                "Container shipping costs increase"),
            new MaritimeNewsArticle(
                "New Restaurant Opens",
                "Local News",
                LocalDate.now(),
                "A new restaurant opened downtown"),
            new MaritimeNewsArticle(
                "Maritime Safety Regulations",
                "Industry News",
                LocalDate.now(),
                "New maritime safety rules"));

    when(maritimeNewsSource.getRecentHeadlines(anyString(), anyInt())).thenReturn(mockNews);

    List<MaritimeNewsArticle> result =
        shippingNewsAnalyzer.getRelevantHeadlines("CityA → CityB", 5);

    assertThat(result).hasSize(2);
    assertThat(result.get(0).getHeadline()).contains("Container");
    assertThat(result.get(1).getHeadline()).contains("Maritime");
  }

  @Test
  void getRelevantHeadlines_respectsMaxResults() {
    List<MaritimeNewsArticle> mockNews =
        List.of(
            new MaritimeNewsArticle("Port News 1", "Source", LocalDate.now(), "Port operations"),
            new MaritimeNewsArticle("Port News 2", "Source", LocalDate.now(), "Port congestion"),
            new MaritimeNewsArticle("Port News 3", "Source", LocalDate.now(), "Port expansion"),
            new MaritimeNewsArticle("Port News 4", "Source", LocalDate.now(), "Port delays"));

    when(maritimeNewsSource.getRecentHeadlines(anyString(), anyInt())).thenReturn(mockNews);

    List<MaritimeNewsArticle> result = shippingNewsAnalyzer.getRelevantHeadlines("Test Route", 2);

    assertThat(result).hasSize(2);
  }

  @Test
  void getRelevantHeadlines_returnsEmptyWhenNoNews() {
    when(maritimeNewsSource.getRecentHeadlines(anyString(), anyInt())).thenReturn(List.of());

    List<MaritimeNewsArticle> result = shippingNewsAnalyzer.getRelevantHeadlines("Any Route", 5);

    assertThat(result).isEmpty();
  }

  @Test
  void getRelevantHeadlines_filtersIrrelevantNews() {
    List<MaritimeNewsArticle> mockNews =
        List.of(
            new MaritimeNewsArticle(
                "Basketball Game", "Sports News", LocalDate.now(), "Local team wins championship"),
            new MaritimeNewsArticle(
                "Weather Update", "Weather Service", LocalDate.now(), "Sunny skies tomorrow"),
            new MaritimeNewsArticle(
                "Election News", "News Agency", LocalDate.now(), "Voting completed successfully"));

    when(maritimeNewsSource.getRecentHeadlines(anyString(), anyInt())).thenReturn(mockNews);

    List<MaritimeNewsArticle> result =
        shippingNewsAnalyzer.getRelevantHeadlines("CityA → CityB", 5);

    assertThat(result).isEmpty();
  }

  @Test
  void getRelevantHeadlines_matchesRouteKeywords() {
    List<MaritimeNewsArticle> mockNews =
        List.of(
            new MaritimeNewsArticle(
                "Shanghai Economic Update",
                "Economic News",
                LocalDate.now(),
                "Shanghai economy grows"),
            new MaritimeNewsArticle(
                "Los Angeles Traffic Report",
                "Traffic News",
                LocalDate.now(),
                "Heavy traffic in Los Angeles"),
            new MaritimeNewsArticle(
                "New York Weather",
                "Weather Service",
                LocalDate.now(),
                "Rain expected in New York"));

    when(maritimeNewsSource.getRecentHeadlines(anyString(), anyInt())).thenReturn(mockNews);

    List<MaritimeNewsArticle> result =
        shippingNewsAnalyzer.getRelevantHeadlines("Shanghai → Los Angeles", 5);

    assertThat(result).hasSize(2);
    assertThat(result.get(0).getHeadline()).contains("Shanghai");
    assertThat(result.get(1).getHeadline()).contains("Los Angeles");
  }

  @Test
  void getRelevantHeadlines_handlesDashSeparatedRoutes() {
    List<MaritimeNewsArticle> mockNews =
        List.of(
            new MaritimeNewsArticle(
                "Hamburg Port Update", "Port News", LocalDate.now(), "Hamburg port operations"),
            new MaritimeNewsArticle(
                "Rotterdam Expansion",
                "Maritime News",
                LocalDate.now(),
                "Rotterdam terminal expansion"));

    when(maritimeNewsSource.getRecentHeadlines(anyString(), anyInt())).thenReturn(mockNews);

    List<MaritimeNewsArticle> result =
        shippingNewsAnalyzer.getRelevantHeadlines("Hamburg-Rotterdam", 5);

    assertThat(result).hasSize(2);
  }

  @Test
  void getRelevantHeadlines_filtersWhenNoRelevantMatches() {
    List<MaritimeNewsArticle> mockNews =
        List.of(
            new MaritimeNewsArticle(
                "Technology News", "Tech Source", LocalDate.now(), "New smartphone released"),
            new MaritimeNewsArticle(
                "Entertainment Update",
                "Entertainment News",
                LocalDate.now(),
                "Movie premiere scheduled"));

    when(maritimeNewsSource.getRecentHeadlines(anyString(), anyInt())).thenReturn(mockNews);

    List<MaritimeNewsArticle> result =
        shippingNewsAnalyzer.getRelevantHeadlines("Unknown → Route", 5);

    assertThat(result).isEmpty();
  }
}
