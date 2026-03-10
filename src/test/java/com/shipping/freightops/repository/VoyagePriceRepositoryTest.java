package com.shipping.freightops.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.shipping.freightops.entity.Port;
import com.shipping.freightops.entity.Vessel;
import com.shipping.freightops.entity.Voyage;
import com.shipping.freightops.entity.VoyagePrice;
import com.shipping.freightops.enums.ContainerSize;
import com.shipping.freightops.enums.VoyageStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@DataJpaTest
class VoyagePriceRepositoryTest {

  @Autowired private VoyagePriceRepository voyagePriceRepository;
  @Autowired private VoyageRepository voyageRepository;
  @Autowired private PortRepository portRepository;
  @Autowired private VesselRepository vesselRepository;

  private Port portA;
  private Port portB;
  private Port portC;

  @BeforeEach
  void setUp() {
    voyagePriceRepository.deleteAll();
    voyageRepository.deleteAll();
    vesselRepository.deleteAll();
    portRepository.deleteAll();

    portA = portRepository.save(new Port("AAAAA", "Port A", "Country A"));
    portB = portRepository.save(new Port("BBBBB", "Port B", "Country B"));
    portC = portRepository.save(new Port("CCCCC", "Port C", "Country C"));
  }

  @Nested
  @DisplayName("findHistoricalPricesSameRoute")
  class FindHistoricalPricesSameRoute {

    @Test
    @DisplayName(
        "returns voyage prices on the same route, ordered by departure time desc, excluding given voyage id")
    void returnsSameRoutePricesOrderedAndExcludingCurrentVoyage() {
      Vessel vessel = vesselRepository.save(new Vessel("Vessel", "7654321", 200));

      Voyage current = createVoyage("CURR", vessel, portA, portB, 5);
      Voyage older = createVoyage("OLD", vessel, portA, portB, 10);
      Voyage newer = createVoyage("NEW", vessel, portA, portB, 2);
      Voyage differentRoute = createVoyage("DIFF", vessel, portA, portC, 3);

      createVoyagePrice(older, ContainerSize.TWENTY_FOOT, BigDecimal.valueOf(900));
      createVoyagePrice(newer, ContainerSize.TWENTY_FOOT, BigDecimal.valueOf(1100));
      createVoyagePrice(current, ContainerSize.TWENTY_FOOT, BigDecimal.valueOf(1000));
      createVoyagePrice(differentRoute, ContainerSize.TWENTY_FOOT, BigDecimal.valueOf(800));

      Pageable pageable = PageRequest.of(0, 10);

      List<VoyagePrice> result =
          voyagePriceRepository.findHistoricalPricesSameRoute(
              portA.getId(), portB.getId(), current.getId(), ContainerSize.TWENTY_FOOT, pageable);

      assertThat(result)
          .hasSize(2)
          .extracting(vp -> vp.getVoyage().getVoyageNumber())
          .containsExactly("NEW", "OLD");
    }

    @Test
    @DisplayName("respects pageable limit when returning historical same-route prices")
    void respectsPageableLimit() {
      Vessel vessel = vesselRepository.save(new Vessel("Vessel", "1234500", 200));

      Voyage current = createVoyage("CURR", vessel, portA, portB, 1);
      Voyage recent = createVoyage("RECENT", vessel, portA, portB, 2);
      Voyage older = createVoyage("OLDER", vessel, portA, portB, 3);

      createVoyagePrice(recent, ContainerSize.FORTY_FOOT, BigDecimal.valueOf(1500));
      createVoyagePrice(older, ContainerSize.FORTY_FOOT, BigDecimal.valueOf(1400));
      createVoyagePrice(current, ContainerSize.FORTY_FOOT, BigDecimal.valueOf(1600));

      Pageable pageable = PageRequest.of(0, 1);

      List<VoyagePrice> result =
          voyagePriceRepository.findHistoricalPricesSameRoute(
              portA.getId(), portB.getId(), current.getId(), ContainerSize.FORTY_FOOT, pageable);

      assertThat(result)
          .hasSize(1)
          .first()
          .extracting(vp -> vp.getVoyage().getVoyageNumber())
          .isEqualTo("RECENT");
    }
  }

  @Nested
  @DisplayName("findHistoricalPricesSimilarRoute")
  class FindHistoricalPricesSimilarRoute {

    @Test
    @DisplayName(
        "returns voyage prices where departure and arrival ports are within the provided sets, ordered by departure time desc")
    void returnsSimilarRoutePricesWithinPortSets() {
      Vessel vessel = vesselRepository.save(new Vessel("Vessel", "1357924", 200));

      Voyage current = createVoyage("CURR", vessel, portA, portB, 1);
      Voyage match1 = createVoyage("M1", vessel, portA, portB, 2);
      Voyage match2 = createVoyage("M2", vessel, portC, portB, 3);
      Voyage excludedByDeparture = createVoyage("EXD", vessel, portB, portC, 4);
      Voyage excludedByArrival = createVoyage("EXA", vessel, portA, portC, 5);

      createVoyagePrice(match1, ContainerSize.TWENTY_FOOT, BigDecimal.valueOf(1000));
      createVoyagePrice(match2, ContainerSize.TWENTY_FOOT, BigDecimal.valueOf(1100));
      createVoyagePrice(current, ContainerSize.TWENTY_FOOT, BigDecimal.valueOf(1200));
      createVoyagePrice(excludedByDeparture, ContainerSize.TWENTY_FOOT, BigDecimal.valueOf(1300));
      createVoyagePrice(excludedByArrival, ContainerSize.TWENTY_FOOT, BigDecimal.valueOf(1400));

      Pageable pageable = PageRequest.of(0, 10);

      List<VoyagePrice> result =
          voyagePriceRepository.findHistoricalPricesSimilarRoute(
              List.of(portA.getId(), portC.getId()),
              List.of(portB.getId()),
              current.getId(),
              ContainerSize.TWENTY_FOOT,
              pageable);

      assertThat(result)
          .hasSize(2)
          .extracting(vp -> vp.getVoyage().getVoyageNumber())
          .containsExactly("M1", "M2");
    }
  }

  private Voyage createVoyage(
      String voyageNumber, Vessel vessel, Port departure, Port arrival, int daysAgoDeparture) {
    Voyage voyage = new Voyage();
    voyage.setVoyageNumber(voyageNumber);
    voyage.setVessel(vessel);
    voyage.setDeparturePort(departure);
    voyage.setArrivalPort(arrival);
    voyage.setDepartureTime(LocalDateTime.now().minusDays(daysAgoDeparture));
    voyage.setArrivalTime(LocalDateTime.now().minusDays(daysAgoDeparture - 1L));
    voyage.setStatus(VoyageStatus.COMPLETED);
    voyage.setMaxCapacityTeu(vessel.getCapacityTeu());
    voyage.setBookingOpen(false);
    return voyageRepository.save(voyage);
  }

  private void createVoyagePrice(
      Voyage voyage, ContainerSize containerSize, BigDecimal basePriceUsd) {
    VoyagePrice price = new VoyagePrice();
    price.setVoyage(voyage);
    price.setContainerSize(containerSize);
    price.setBasePriceUsd(basePriceUsd);
    voyagePriceRepository.save(price);
  }
}
