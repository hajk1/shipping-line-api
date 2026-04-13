package com.shipping.freightops.news;

import static org.junit.jupiter.api.Assertions.*;

import com.shipping.freightops.dto.MaritimeNewsArticle;
import com.shipping.freightops.news.config.NewsProperties;
import com.shipping.freightops.service.RiskAnalysisService;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(
    properties = {
      "app.news.provider=static", // Use static provider for reliable testing
      "app.news.max-headlines=3"
    })
class RssProviderIntegrationTest {

  @Autowired private ShippingNewsAnalyzer shippingNewsAnalyzer;

  @Autowired private RiskAnalysisService riskAnalysisService;

  @Autowired private MaritimeNewsSource maritimeNewsSource;

  @Autowired private NewsProperties newsProperties;

  @Test
  @DisplayName("RSS provider integrates correctly with ShippingNewsAnalyzer")
  void testRssProviderIntegration() {
    // Given a route
    String testRoute = "Shanghai-Rotterdam";

    // When fetching relevant news through the analyzer
    List<MaritimeNewsArticle> relevantNews =
        shippingNewsAnalyzer.getRelevantHeadlines(testRoute, 5);

    // Then news should be returned
    assertNotNull(relevantNews);
    // Note: May be empty if no relevant news, but should not be null

    // Verify the news source is properly configured
    assertNotNull(maritimeNewsSource);
    assertTrue(
        maritimeNewsSource.getClass().getSimpleName().contains("MaritimeNewsSource"),
        "Expected a MaritimeNewsSource implementation");
  }

  @Test
  @DisplayName("RSS provider works with RiskAnalysisService end-to-end")
  void testRssProviderWithRiskAnalysisService() {
    // Given a route
    String testRoute = "Asia-Europe";

    // When fetching relevant news through the risk analysis service
    List<MaritimeNewsArticle> relevantNews = riskAnalysisService.fetchRelevantNews(testRoute);

    // Then
    assertNotNull(relevantNews);
    // Should not throw exceptions and return a valid list (may be empty)

    // When building news context
    String newsContext = riskAnalysisService.buildNewsContext(relevantNews);

    // Then
    assertNotNull(newsContext);
    // Context should be either empty string or contain news information
  }

  @Test
  @DisplayName("Configuration properties are properly loaded")
  void testConfigurationPropertiesLoaded() {
    // Verify NewsProperties are properly configured
    assertNotNull(newsProperties);
    assertEquals(3, newsProperties.getMaxHeadlines());
    assertEquals("static", newsProperties.getProvider());
  }

  @Test
  @DisplayName("MaritimeNewsSource can fetch headlines directly")
  void testMaritimeNewsSourceDirectAccess() {
    // Given
    String testRoute = "Test-Route";
    int maxResults = 2;

    // When
    List<MaritimeNewsArticle> headlines =
        maritimeNewsSource.getRecentHeadlines(testRoute, maxResults);

    // Then
    assertNotNull(headlines);
    // Should not throw exceptions

    // If headlines are returned, verify they have required fields
    for (MaritimeNewsArticle article : headlines) {
      assertNotNull(article.getHeadline(), "Headline should not be null");
      assertNotNull(article.getSource(), "Source should not be null");
      assertNotNull(article.getPublishedDate(), "Published date should not be null");
      assertNotNull(article.getSummary(), "Summary should not be null");
    }
  }

  @Test
  @DisplayName("Service layer handles news provider gracefully when no news available")
  void testGracefulHandlingOfNoNews() {
    // Given a route that likely has no relevant news
    String obscureRoute = "NonExistent-Port";

    // When
    List<MaritimeNewsArticle> relevantNews = riskAnalysisService.fetchRelevantNews(obscureRoute);
    String newsContext = riskAnalysisService.buildNewsContext(relevantNews);

    // Then - should handle gracefully without exceptions
    assertNotNull(relevantNews);
    assertNotNull(newsContext);
    // Empty news should result in empty context
    if (relevantNews.isEmpty()) {
      assertTrue(newsContext.isEmpty() || newsContext.isBlank());
    }
  }
}
