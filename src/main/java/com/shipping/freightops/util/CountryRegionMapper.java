package com.shipping.freightops.util;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Maps countries to shipping regions for "similar routes" logic. Countries not in the map default
 * to a region derived from the country name (e.g. "Unknown" or the country itself).
 */
public final class CountryRegionMapper {

  private static final Map<String, String> COUNTRY_TO_REGION =
      Map.ofEntries(
          // Asia
          Map.entry("Japan", "Asia"),
          Map.entry("China", "Asia"),
          Map.entry("South Korea", "Asia"),
          Map.entry("Singapore", "Asia"),
          Map.entry("Hong Kong", "Asia"),
          Map.entry("Taiwan", "Asia"),
          Map.entry("Malaysia", "Asia"),
          Map.entry("Thailand", "Asia"),
          Map.entry("Vietnam", "Asia"),
          Map.entry("Indonesia", "Asia"),
          Map.entry("India", "Asia"),
          Map.entry("Philippines", "Asia"),
          Map.entry("Sri Lanka", "Asia"),
          Map.entry("Bangladesh", "Asia"),
          Map.entry("Pakistan", "Asia"),
          // Middle East
          Map.entry("United Arab Emirates", "Middle East"),
          Map.entry("UAE", "Middle East"),
          Map.entry("Saudi Arabia", "Middle East"),
          Map.entry("Qatar", "Middle East"),
          Map.entry("Bahrain", "Middle East"),
          Map.entry("Kuwait", "Middle East"),
          Map.entry("Oman", "Middle East"),
          Map.entry("Iran", "Middle East"),
          Map.entry("Iraq", "Middle East"),
          Map.entry("Israel", "Middle East"),
          Map.entry("Jordan", "Middle East"),
          Map.entry("Lebanon", "Middle East"),
          Map.entry("Turkey", "Middle East"),
          // Europe
          Map.entry("Germany", "Europe"),
          Map.entry("Netherlands", "Europe"),
          Map.entry("United Kingdom", "Europe"),
          Map.entry("UK", "Europe"),
          Map.entry("Belgium", "Europe"),
          Map.entry("France", "Europe"),
          Map.entry("Spain", "Europe"),
          Map.entry("Italy", "Europe"),
          Map.entry("Greece", "Europe"),
          Map.entry("Poland", "Europe"),
          Map.entry("Denmark", "Europe"),
          Map.entry("Sweden", "Europe"),
          Map.entry("Norway", "Europe"),
          Map.entry("Finland", "Europe"),
          Map.entry("Portugal", "Europe"),
          Map.entry("Ireland", "Europe"),
          Map.entry("Russia", "Europe"),
          // North America
          Map.entry("United States", "North America"),
          Map.entry("USA", "North America"),
          Map.entry("Canada", "North America"),
          Map.entry("Mexico", "North America"),
          // South America
          Map.entry("Brazil", "South America"),
          Map.entry("Argentina", "South America"),
          Map.entry("Chile", "South America"),
          Map.entry("Colombia", "South America"),
          Map.entry("Peru", "South America"),
          Map.entry("Ecuador", "South America"),
          Map.entry("Venezuela", "South America"),
          // Africa
          Map.entry("South Africa", "Africa"),
          Map.entry("Egypt", "Africa"),
          Map.entry("Morocco", "Africa"),
          Map.entry("Kenya", "Africa"),
          Map.entry("Nigeria", "Africa"),
          Map.entry("Togo", "Africa"),
          // Oceania
          Map.entry("Australia", "Oceania"),
          Map.entry("New Zealand", "Oceania"));

  private CountryRegionMapper() {}

  /**
   * Returns the region for a given country. If the country is not in the map, returns the country
   * name as the region (so same country still matches for similar routes).
   */
  public static String getRegion(String country) {
    if (country == null || country.isBlank()) {
      return "Unknown";
    }
    return COUNTRY_TO_REGION.getOrDefault(country.trim(), country.trim());
  }

  /** Returns all countries that map to the given region. */
  public static Set<String> getCountriesInRegion(String region) {
    if (region == null || region.isBlank()) {
      return Collections.emptySet();
    }
    return COUNTRY_TO_REGION.entrySet().stream()
        .filter(e -> region.equals(e.getValue()))
        .map(Map.Entry::getKey)
        .collect(Collectors.toSet());
  }

  /**
   * Returns the list of country names that map to the same region as the given country. Useful for
   * querying ports in "similar" regions.
   */
  public static List<String> getCountriesInSameRegion(String country) {
    String region = getRegion(country);
    if ("Unknown".equals(region)) {
      return List.of(country);
    }
    return Optional.ofNullable(getCountriesInRegion(region))
        .filter(s -> !s.isEmpty())
        .map(List::copyOf)
        .orElse(List.of(country));
  }
}
