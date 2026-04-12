package com.shipping.freightops.news.impl;

import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.SyndFeedInput;
import com.shipping.freightops.dto.MaritimeNewsArticle;
import com.shipping.freightops.news.MaritimeNewsSource;
import com.shipping.freightops.news.config.NewsProperties;
import java.io.StringReader;
import java.net.URI;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
@ConditionalOnProperty(name = "app.news.provider", havingValue = "rss")
public class RssMaritimeNewsSource implements MaritimeNewsSource {

  private static final Logger logger = LoggerFactory.getLogger(RssMaritimeNewsSource.class);
  private static final int MAX_SUMMARY_LENGTH = 500;

  private final RestClient restClient;
  private final NewsProperties newsProperties;

  public RssMaritimeNewsSource(
      RestClient.Builder restClientBuilder, NewsProperties newsProperties) {
    this.restClient = restClientBuilder.build();
    this.newsProperties = newsProperties;
  }

  @Override
  public List<MaritimeNewsArticle> getRecentHeadlines(String route, int maxResults) {
    List<MaritimeNewsArticle> allItems = new ArrayList<>();

    for (String feedUrl : newsProperties.getFeeds()) {
      try {
        List<MaritimeNewsArticle> feedItems = processFeed(feedUrl);
        allItems.addAll(feedItems);
      } catch (Exception e) {
        logger.warn("Failed to fetch feed: {}", feedUrl, e);
      }
    }

    return allItems.stream()
        .distinct()
        .limit(Math.min(maxResults, newsProperties.getMaxHeadlines()))
        .collect(Collectors.toList());
  }

  private List<MaritimeNewsArticle> processFeed(String feedUrl) {
    try {
      String rssContent = fetchRssContent(feedUrl);
      if (isEmptyContent(rssContent)) {
        return List.of();
      }

      SyndFeed feed = parseRssFeed(rssContent);
      return transformEntriesToArticles(feed, feedUrl);

    } catch (RestClientException e) {
      logger.warn("Network error fetching RSS feed {}: {}", feedUrl, e.getMessage());
      throw e;
    } catch (Exception e) {
      logger.warn("Error parsing RSS feed {}: {}", feedUrl, e.getMessage());
      throw new RuntimeException("RSS parsing failed", e);
    }
  }

  private String fetchRssContent(String feedUrl) {
    return restClient.get().uri(feedUrl).retrieve().body(String.class);
  }

  private boolean isEmptyContent(String content) {
    return content == null || content.trim().isEmpty();
  }

  private SyndFeed parseRssFeed(String rssContent) throws Exception {
    SyndFeedInput input = new SyndFeedInput();
    return input.build(new StringReader(rssContent));
  }

  private List<MaritimeNewsArticle> transformEntriesToArticles(SyndFeed feed, String feedUrl) {
    return feed.getEntries().stream()
        .map(entry -> transformEntry(entry, feedUrl))
        .filter(article -> article != null)
        .collect(Collectors.toList());
  }

  private MaritimeNewsArticle transformEntry(SyndEntry entry, String feedUrl) {
    String headline = entry.getTitle();
    if (isInvalidHeadline(headline)) {
      return null;
    }

    String source = extractSourceFromFeedUrl(feedUrl);
    LocalDate publishedDate = parsePublishedDate(entry);
    String summary = truncateDescription(entry.getDescription());

    return new MaritimeNewsArticle(headline.trim(), source, publishedDate, summary);
  }

  private boolean isInvalidHeadline(String headline) {
    return headline == null || headline.trim().isEmpty();
  }

  private String extractSourceFromFeedUrl(String feedUrl) {
    try {
      URI uri = URI.create(feedUrl);
      String host = uri.getHost();
      if (host != null) {
        return removeWwwPrefix(host);
      }
    } catch (Exception e) {
      // Silently fall back to default source
    }
    return "RSS Feed";
  }

  private String removeWwwPrefix(String host) {
    return host.startsWith("www.") ? host.substring(4) : host;
  }

  private LocalDate parsePublishedDate(SyndEntry entry) {
    LocalDate date = tryParseDate(entry.getPublishedDate());
    if (date != null) {
      return date;
    }

    date = tryParseDate(entry.getUpdatedDate());
    if (date != null) {
      return date;
    }

    return LocalDate.now();
  }

  private LocalDate tryParseDate(Date date) {
    try {
      if (date != null) {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
      }
    } catch (Exception e) {
      // Silently ignore date parsing errors
    }
    return null;
  }

  private String truncateDescription(Object description) {
    if (description == null) {
      return "";
    }

    String desc = description.toString().trim();
    if (desc.length() <= MAX_SUMMARY_LENGTH) {
      return desc;
    }

    return truncateAtWordBoundary(desc);
  }

  private String truncateAtWordBoundary(String text) {
    int lastSpace = text.lastIndexOf(' ', MAX_SUMMARY_LENGTH);
    if (lastSpace > 0) {
      return text.substring(0, lastSpace) + "...";
    }
    return text.substring(0, MAX_SUMMARY_LENGTH) + "...";
  }
}
