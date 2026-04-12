package com.shipping.freightops.news.impl;

import com.shipping.freightops.dto.MaritimeNewsArticle;
import com.shipping.freightops.news.MaritimeNewsSource;
import java.time.LocalDate;
import java.util.List;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.news.provider", havingValue = "static", matchIfMissing = true)
public class StaticMaritimeNewsSource implements MaritimeNewsSource {

  private static final List<MaritimeNewsArticle> SAMPLE_NEWS =
      List.of(
          new MaritimeNewsArticle(
              "Red Sea Disruptions Force Major Shipping Lines to Reroute via Cape of Good Hope",
              "Lloyd's List",
              LocalDate.now().minusDays(2),
              "Ongoing Houthi attacks in the Red Sea have forced major container lines to avoid the Suez Canal, adding 10-14 days to transit times and significantly increasing fuel costs."),
          new MaritimeNewsArticle(
              "Shanghai Port Experiences Severe Congestion Amid Export Surge",
              "The Loadstar",
              LocalDate.now().minusDays(1),
              "Shanghai terminals report 3-5 day delays as export volumes surge ahead of Q2. Container availability remains tight across major Chinese ports."),
          new MaritimeNewsArticle(
              "Panama Canal Implements New Water Conservation Measures",
              "gCaptain",
              LocalDate.now().minusDays(3),
              "Drought conditions force Panama Canal Authority to reduce daily transits and implement strict draft restrictions, affecting global shipping schedules."),
          new MaritimeNewsArticle(
              "Los Angeles Port Workers Reach Tentative Labor Agreement",
              "Maritime Executive",
              LocalDate.now().minusDays(1),
              "Tentative agreement reached between ILWU and port operators, potentially avoiding strikes that could have disrupted West Coast cargo operations."),
          new MaritimeNewsArticle(
              "Container Freight Rates Surge on Asia-Europe Routes",
              "The Loadstar",
              LocalDate.now().minusDays(4),
              "Spot rates on major Asia-Europe trade lanes increase by 25% week-over-week due to capacity constraints and Red Sea diversions."),
          new MaritimeNewsArticle(
              "Singapore Port Authority Announces Terminal Expansion",
              "Lloyd's List",
              LocalDate.now().minusDays(5),
              "PSA Singapore unveils plans for new automated terminal to handle growing transshipment volumes and larger container vessels."),
          new MaritimeNewsArticle(
              "Maersk Reports Strong Q1 Earnings Despite Route Disruptions",
              "Maritime Executive",
              LocalDate.now().minusDays(3),
              "Danish shipping giant posts solid quarterly results while managing increased operational costs from Red Sea route diversions."),
          new MaritimeNewsArticle(
              "New Environmental Regulations Impact Vessel Operations in European Ports",
              "gCaptain",
              LocalDate.now().minusDays(6),
              "EU's updated emissions standards require additional compliance measures for vessels calling at European terminals, potentially affecting scheduling."),
          new MaritimeNewsArticle(
              "Typhoon Season Preparations Underway at Asian Ports",
              "Maritime Executive",
              LocalDate.now().minusDays(2),
              "Major ports across Southeast Asia implement enhanced weather monitoring and cargo protection measures ahead of typhoon season."),
          new MaritimeNewsArticle(
              "Hamburg Port Invests in Digital Infrastructure Upgrades",
              "Lloyd's List",
              LocalDate.now().minusDays(4),
              "Port of Hamburg announces major digitalization initiative to improve cargo tracking and reduce vessel turnaround times."));

  @Override
  public List<MaritimeNewsArticle> getRecentHeadlines(String route, int maxResults) {
    return SAMPLE_NEWS.stream().limit(Math.min(maxResults, SAMPLE_NEWS.size())).toList();
  }
}
