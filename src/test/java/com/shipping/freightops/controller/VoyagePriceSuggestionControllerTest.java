package com.shipping.freightops.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.shipping.freightops.entity.Port;
import com.shipping.freightops.entity.Vessel;
import com.shipping.freightops.entity.Voyage;
import com.shipping.freightops.entity.VoyagePrice;
import com.shipping.freightops.enums.ContainerSize;
import com.shipping.freightops.repository.PortRepository;
import com.shipping.freightops.repository.VesselRepository;
import com.shipping.freightops.repository.VoyagePriceRepository;
import com.shipping.freightops.repository.VoyageRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class VoyagePriceSuggestionControllerTest {

  @Autowired private VoyageRepository voyageRepository;
  @Autowired private VoyagePriceRepository voyagePriceRepository;
  @Autowired private PortRepository portRepository;
  @Autowired private VesselRepository vesselRepository;
  @Autowired private MockMvc mockMvc;

  private Voyage voyage;

  @BeforeEach
  void setUp() {
    voyagePriceRepository.deleteAll();
    voyageRepository.deleteAll();
    vesselRepository.deleteAll();
    portRepository.deleteAll();

    Port departurePort =
        portRepository.save(new Port("AEJEA", "Jebel Ali", "United Arab Emirates"));
    Port arrivalPort = portRepository.save(new Port("CNSHA", "Shanghai", "China"));
    Vessel vessel = vesselRepository.save(new Vessel("OceanStar", "IMO123", 10));

    Voyage createdVoyage = new Voyage();
    createdVoyage.setVoyageNumber("VOY-2025-010");
    createdVoyage.setVessel(vessel);
    createdVoyage.setArrivalTime(LocalDateTime.now().plusDays(14));
    createdVoyage.setDepartureTime(LocalDateTime.now().plusDays(1));
    createdVoyage.setDeparturePort(departurePort);
    createdVoyage.setArrivalPort(arrivalPort);
    createdVoyage.setMaxCapacityTeu(vessel.getCapacityTeu());
    createdVoyage.setBookingOpen(true);
    voyage = voyageRepository.save(createdVoyage);
  }

  @Test
  @DisplayName(
      "GET /api/v1/voyages/{voyageId}/price-suggestion → 200 OK with historical data returns suggestion and stats")
  void getPriceSuggestion_withHistoricalData_returnsSuggestionAndStats() throws Exception {
    Voyage pastVoyage = createPastVoyageOnSameRoute();
    VoyagePrice price = new VoyagePrice();
    price.setVoyage(pastVoyage);
    price.setContainerSize(ContainerSize.TWENTY_FOOT);
    price.setBasePriceUsd(BigDecimal.valueOf(1180));
    voyagePriceRepository.save(price);

    mockMvc
        .perform(
            get("/api/v1/voyages/{voyageId}/price-suggestion", voyage.getId())
                .param("containerSize", "TWENTY_FOOT"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.voyageNumber").value("VOY-2025-010"))
        .andExpect(jsonPath("$.route").value("Jebel Ali → Shanghai"))
        .andExpect(jsonPath("$.containerSize").value("TWENTY_FOOT"))
        .andExpect(jsonPath("$.suggestedPriceLowUsd").exists())
        .andExpect(jsonPath("$.suggestedPriceHighUsd").exists())
        .andExpect(jsonPath("$.confidence").exists())
        .andExpect(jsonPath("$.reasoning").exists())
        .andExpect(jsonPath("$.dataPoints").value(1))
        .andExpect(jsonPath("$.historicalAvgUsd").value(1180.00))
        .andExpect(jsonPath("$.historicalMinUsd").value(1180.00))
        .andExpect(jsonPath("$.historicalMaxUsd").value(1180.00));
  }

  @Test
  @DisplayName(
      "GET /api/v1/voyages/{voyageId}/price-suggestion → 200 OK with no historical data returns LOW confidence")
  void getPriceSuggestion_noHistoricalData_returnsLowConfidence() throws Exception {
    mockMvc
        .perform(
            get("/api/v1/voyages/{voyageId}/price-suggestion", voyage.getId())
                .param("containerSize", "TWENTY_FOOT"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.confidence").value("LOW"))
        .andExpect(jsonPath("$.dataPoints").value(0))
        .andExpect(
            jsonPath("$.reasoning")
                .value("No historical data for this route; please use manual pricing."));
  }

  @Test
  @DisplayName("GET /api/v1/voyages/{voyageId}/price-suggestion → 404 for invalid voyage")
  void getPriceSuggestion_invalidVoyage_returns404() throws Exception {
    mockMvc
        .perform(
            get("/api/v1/voyages/{voyageId}/price-suggestion", 99999L)
                .param("containerSize", "TWENTY_FOOT"))
        .andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("GET /api/v1/voyages/{voyageId}/price-suggestion with FORTY_FOOT")
  void getPriceSuggestion_fortyFoot_returnsSuggestion() throws Exception {
    mockMvc
        .perform(
            get("/api/v1/voyages/{voyageId}/price-suggestion", voyage.getId())
                .param("containerSize", "FORTY_FOOT"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.containerSize").value("FORTY_FOOT"))
        .andExpect(jsonPath("$.voyageNumber").value("VOY-2025-010"))
        .andExpect(jsonPath("$.route").value("Jebel Ali → Shanghai"))
        .andExpect(jsonPath("$.confidence").value("LOW"));
  }

  private Voyage createPastVoyageOnSameRoute() {
    Voyage pastVoyage = new Voyage();
    pastVoyage.setVoyageNumber("VOY-2024-099");
    pastVoyage.setVessel(voyage.getVessel());
    pastVoyage.setDeparturePort(voyage.getDeparturePort());
    pastVoyage.setArrivalPort(voyage.getArrivalPort());
    pastVoyage.setDepartureTime(LocalDateTime.now().minusMonths(2));
    pastVoyage.setArrivalTime(LocalDateTime.now().minusMonths(2).plusDays(14));
    pastVoyage.setMaxCapacityTeu(voyage.getMaxCapacityTeu());
    pastVoyage.setBookingOpen(false);
    return voyageRepository.save(pastVoyage);
  }
}
