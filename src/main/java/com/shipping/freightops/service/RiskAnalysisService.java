package com.shipping.freightops.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.shipping.freightops.dto.MaritimeNewsArticle;
import com.shipping.freightops.dto.RiskFactor;
import com.shipping.freightops.enums.RiskImpact;
import com.shipping.freightops.news.ShippingNewsAnalyzer;
import com.shipping.freightops.news.config.NewsProperties;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Service responsible for risk factor analysis and news integration for shipping price suggestions.
 * Handles fetching relevant news, building news context for prompts, and parsing risk factors from
 * AI responses.
 */
@Service
public class RiskAnalysisService {

  private static final Logger logger = LoggerFactory.getLogger(RiskAnalysisService.class);

  private final ShippingNewsAnalyzer shippingNewsAnalyzer;
  private final NewsProperties newsProperties;

  public RiskAnalysisService(
      ShippingNewsAnalyzer shippingNewsAnalyzer, NewsProperties newsProperties) {
    this.shippingNewsAnalyzer = shippingNewsAnalyzer;
    this.newsProperties = newsProperties;
  }

  public List<MaritimeNewsArticle> fetchRelevantNews(String route) {
    try {
      return shippingNewsAnalyzer.getRelevantHeadlines(route, newsProperties.getMaxHeadlines());
    } catch (Exception e) {
      logger.warn(
          "News fetch failed for route '{}', continuing without news: {}", route, e.getMessage());
      return List.of();
    }
  }

  public String buildNewsContext(List<MaritimeNewsArticle> relevantNews) {
    if (relevantNews == null || relevantNews.isEmpty()) {
      return "";
    }

    StringBuilder context = new StringBuilder("Recent relevant news:\n");

    for (MaritimeNewsArticle news : relevantNews) {
      context.append("- ").append(news.getHeadline());
      String summary = news.getSummary();
      if (summary != null && !summary.trim().isEmpty()) {
        context.append(" (").append(summary.trim()).append(")");
      }
      context.append("\n");
    }

    return context.append("\n").toString();
  }

  public List<RiskFactor> parseRiskFactors(JsonNode parsed) {
    JsonNode riskFactorsNode = parsed.path("riskFactors");
    if (!riskFactorsNode.isArray()) {
      return List.of();
    }

    List<RiskFactor> riskFactors = new ArrayList<>();
    int skippedCount = 0;

    for (JsonNode riskNode : riskFactorsNode) {
      try {
        RiskFactor riskFactor = parseRiskFactor(riskNode);
        if (riskFactor != null) {
          riskFactors.add(riskFactor);
          continue;
        }
        skippedCount++;

      } catch (Exception e) {
        skippedCount++;
        logger.debug("Skipped invalid risk factor: {}", riskNode);
      }
    }

    if (skippedCount > 0) {
      logger.info(
          "Parsed {} risk factors, skipped {} invalid entries", riskFactors.size(), skippedCount);
    }

    return riskFactors;
  }

  private RiskFactor parseRiskFactor(JsonNode riskNode) {
    String factorText = riskNode.path("factor").asText("").trim();
    if (factorText.isEmpty()) {
      return null;
    }

    RiskFactor riskFactor = new RiskFactor();
    riskFactor.setFactor(factorText);
    riskFactor.setImpact(parseRiskImpact(riskNode.path("impact").asText("").trim()));

    String description = riskNode.path("description").asText("").trim();
    if (!description.isEmpty()) {
      riskFactor.setDescription(description);
    }

    return riskFactor;
  }

  private RiskImpact parseRiskImpact(String impactStr) {
    if (impactStr.isEmpty()) {
      return RiskImpact.LOW;
    }

    try {
      return RiskImpact.valueOf(impactStr.toUpperCase());
    } catch (IllegalArgumentException e) {
      return RiskImpact.LOW;
    }
  }

  public String fetchAndBuildNewsContext(String route) {
    List<MaritimeNewsArticle> relevantNews = fetchRelevantNews(route);
    return buildNewsContext(relevantNews);
  }
}
