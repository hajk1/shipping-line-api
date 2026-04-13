package com.shipping.freightops.news.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

import com.shipping.freightops.dto.MaritimeNewsArticle;
import com.shipping.freightops.news.config.NewsProperties;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings({"unchecked", "rawtypes"})
class RssMaritimeNewsSourceTest {

  @Mock private RestClient.Builder restClientBuilder;
  @Mock private RestClient restClient;
  @Mock private RestClient.RequestHeadersUriSpec requestSpec;
  @Mock private RestClient.ResponseSpec responseSpec;

  private NewsProperties newsProperties;
  private RssMaritimeNewsSource rssNewsSource;

  private static final String SAMPLE_RSS_XML =
      """
      <?xml version="1.0" encoding="UTF-8"?>
      <rss version="2.0">
        <channel>
          <title>Maritime News</title>
          <item>
            <title>Red Sea Disruptions Force Major Shipping Lines to Reroute</title>
            <description>Ongoing Houthi attacks in the Red Sea have forced major container lines to avoid the Suez Canal.</description>
            <pubDate>Mon, 01 Jan 2024 10:00:00 GMT</pubDate>
          </item>
          <item>
            <title>Shanghai Port Experiences Severe Congestion</title>
            <description>Shanghai terminals report 3-5 day delays as export volumes surge.</description>
            <pubDate>Sun, 31 Dec 2023 15:30:00 GMT</pubDate>
          </item>
        </channel>
      </rss>
      """;

  private static final String MALFORMED_RSS_XML =
      """
      <?xml version="1.0" encoding="UTF-8"?>
      <rss version="2.0">
        <channel>
          <title>Maritime News</title>
          <item>
            <title>Incomplete Item
            <description>Missing closing tags
      """;

  @BeforeEach
  void setUp() {
    newsProperties = new NewsProperties();
    newsProperties.setFeeds(List.of("https://example.com/feed.xml"));
    newsProperties.setMaxHeadlines(5);

    lenient().when(restClientBuilder.build()).thenReturn(restClient);
    lenient().when(restClient.get()).thenReturn(requestSpec);
    lenient().when(requestSpec.uri(anyString())).thenReturn(requestSpec);
    lenient().when(requestSpec.retrieve()).thenReturn(responseSpec);

    rssNewsSource = new RssMaritimeNewsSource(restClientBuilder, newsProperties);
  }

  @Test
  @DisplayName("Successfully parses RSS feed with mock RestClient")
  void testSuccessfulRssFeedParsing() {
    // Given
    when(responseSpec.body(String.class)).thenReturn(SAMPLE_RSS_XML);

    // When
    List<MaritimeNewsArticle> articles = rssNewsSource.getRecentHeadlines("test-route", 10);

    // Then
    assertNotNull(articles);
    assertEquals(2, articles.size());

    MaritimeNewsArticle firstArticle = articles.get(0);
    assertEquals(
        "Red Sea Disruptions Force Major Shipping Lines to Reroute", firstArticle.getHeadline());
    assertEquals("example.com", firstArticle.getSource());
    assertNotNull(firstArticle.getPublishedDate());
    assertTrue(firstArticle.getSummary().contains("Houthi attacks"));

    MaritimeNewsArticle secondArticle = articles.get(1);
    assertEquals("Shanghai Port Experiences Severe Congestion", secondArticle.getHeadline());
    assertEquals("example.com", secondArticle.getSource());
    assertNotNull(secondArticle.getPublishedDate());
    assertTrue(secondArticle.getSummary().contains("3-5 day delays"));
  }

  @Test
  @DisplayName("Handles network failure gracefully with empty result")
  void testNetworkFailureHandling() {
    // Given
    when(responseSpec.body(String.class)).thenThrow(new RestClientException("Network error"));

    // When
    List<MaritimeNewsArticle> articles = rssNewsSource.getRecentHeadlines("test-route", 10);

    // Then
    assertNotNull(articles);
    assertTrue(articles.isEmpty());
  }

  @Test
  @DisplayName("Handles malformed and empty RSS content gracefully")
  void testMalformedAndEmptyRssHandling() {
    // Test malformed RSS XML
    when(responseSpec.body(String.class)).thenReturn(MALFORMED_RSS_XML);
    List<MaritimeNewsArticle> articles = rssNewsSource.getRecentHeadlines("test-route", 10);
    assertNotNull(articles);
    assertTrue(articles.isEmpty());

    // Test empty RSS content
    when(responseSpec.body(String.class)).thenReturn("");
    articles = rssNewsSource.getRecentHeadlines("test-route", 10);
    assertNotNull(articles);
    assertTrue(articles.isEmpty());

    // Test null RSS content
    when(responseSpec.body(String.class)).thenReturn(null);
    articles = rssNewsSource.getRecentHeadlines("test-route", 10);
    assertNotNull(articles);
    assertTrue(articles.isEmpty());
  }

  @Test
  @DisplayName("Respects result limiting configuration")
  void testResultLimiting() {
    // Given
    newsProperties.setMaxHeadlines(1);
    when(responseSpec.body(String.class)).thenReturn(SAMPLE_RSS_XML);

    // When
    List<MaritimeNewsArticle> articles = rssNewsSource.getRecentHeadlines("test-route", 10);

    // Then
    assertNotNull(articles);
    assertEquals(1, articles.size());
    assertEquals(
        "Red Sea Disruptions Force Major Shipping Lines to Reroute", articles.get(0).getHeadline());
  }

  @Test
  @DisplayName("Processes multiple feeds correctly")
  void testMultipleFeedsProcessing() {
    // Given
    newsProperties.setFeeds(List.of("https://feed1.com/rss", "https://feed2.com/rss"));
    when(responseSpec.body(String.class)).thenReturn(SAMPLE_RSS_XML);

    // When
    List<MaritimeNewsArticle> articles = rssNewsSource.getRecentHeadlines("test-route", 10);

    // Then
    assertNotNull(articles);
    assertEquals(4, articles.size()); // 2 articles from each feed

    // Verify both feeds were called
    verify(requestSpec, times(1)).uri("https://feed1.com/rss");
    verify(requestSpec, times(1)).uri("https://feed2.com/rss");
  }

  @Test
  @DisplayName("Handles mixed feed scenario - one succeeds, one fails")
  void testMixedFeedScenario() {
    // Given
    newsProperties.setFeeds(List.of("https://good-feed.com/rss", "https://bad-feed.com/rss"));

    when(requestSpec.uri("https://good-feed.com/rss")).thenReturn(requestSpec);
    when(requestSpec.uri("https://bad-feed.com/rss")).thenReturn(requestSpec);

    // First call (good feed) returns valid RSS, second call (bad feed) throws exception
    when(responseSpec.body(String.class))
        .thenReturn(SAMPLE_RSS_XML)
        .thenThrow(new RestClientException("Network error"));

    // When
    List<MaritimeNewsArticle> articles = rssNewsSource.getRecentHeadlines("test-route", 10);

    // Then
    assertNotNull(articles);
    assertEquals(2, articles.size()); // Only articles from successful feed
    assertEquals(
        "Red Sea Disruptions Force Major Shipping Lines to Reroute", articles.get(0).getHeadline());
    assertEquals("Shanghai Port Experiences Severe Congestion", articles.get(1).getHeadline());

    // Verify both feeds were attempted
    verify(requestSpec, times(1)).uri("https://good-feed.com/rss");
    verify(requestSpec, times(1)).uri("https://bad-feed.com/rss");
  }

  @Test
  @DisplayName("Handles empty feeds configuration gracefully")
  void testEmptyFeedsConfiguration() {
    // Given - create a fresh instance with empty feeds to avoid unnecessary stubbing
    NewsProperties emptyNewsProperties = new NewsProperties();
    emptyNewsProperties.setFeeds(List.of()); // No feeds configured
    emptyNewsProperties.setMaxHeadlines(5);

    RssMaritimeNewsSource emptyFeedsSource =
        new RssMaritimeNewsSource(restClientBuilder, emptyNewsProperties);

    // When
    List<MaritimeNewsArticle> articles = emptyFeedsSource.getRecentHeadlines("test-route", 10);

    // Then
    assertNotNull(articles);
    assertTrue(articles.isEmpty());

    // Verify no HTTP calls were made (no need to verify since no RestClient was built)
  }
}
