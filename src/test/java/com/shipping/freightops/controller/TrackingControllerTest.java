package com.shipping.freightops.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.shipping.freightops.entity.*;
import com.shipping.freightops.enums.AgentType;
import com.shipping.freightops.enums.ContainerSize;
import com.shipping.freightops.enums.ContainerType;
import com.shipping.freightops.enums.OrderStatus;
import com.shipping.freightops.repository.*;
import com.shipping.freightops.service.TrackingService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class TrackingControllerTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private PortRepository portRepository;
  @Autowired private VesselRepository vesselRepository;
  @Autowired private VoyageRepository voyageRepository;
  @Autowired private ContainerRepository containerRepository;
  @Autowired private CustomerRepository customerRepository;
  @Autowired private AgentRepository agentRepository;
  @Autowired private FreightOrderRepository freightOrderRepository;
  @Autowired private TrackingService trackingService;

  private Long orderId;

  @BeforeEach
  void setup() {
    // Clear repositories before each test
    freightOrderRepository.deleteAll();
    voyageRepository.deleteAll();
    containerRepository.deleteAll();
    vesselRepository.deleteAll();
    customerRepository.deleteAll();
    portRepository.deleteAll();
    agentRepository.deleteAll();

    // Setup test data for tracking tests
    Port departurePort = portRepository.save(new Port("AEJEA", "Jebel Ali", "UAE"));
    Port arrivalPort = portRepository.save(new Port("CNSHA", "Shanghai", "China"));
    Vessel vessel = vesselRepository.save(new Vessel("MV Test", "1111111", 3000));
    Agent agent = new Agent();
    agent.setName("Test Agent");
    agent.setEmail("a@agent.com");
    agent.setCommissionPercent(BigDecimal.TWO);
    agent.setType(AgentType.INTERNAL);
    agentRepository.save(agent);

    Voyage voyage = new Voyage();
    voyage.setVoyageNumber("VOY-001");
    voyage.setVessel(vessel);
    voyage.setDeparturePort(departurePort);
    voyage.setArrivalPort(arrivalPort);
    voyage.setDepartureTime(LocalDateTime.now().plusDays(3));
    voyage.setArrivalTime(LocalDateTime.now().plusDays(10));
    voyage.setMaxCapacityTeu(vessel.getCapacityTeu());
    voyage.setBookingOpen(true);
    voyageRepository.save(voyage);

    Container container =
        containerRepository.save(
            new Container("CONT-001", ContainerSize.FORTY_FOOT, ContainerType.DRY));

    Customer customer = customerRepository.save(new Customer("Company", "Contact", "c@test.com"));

    FreightOrder order = new FreightOrder();

    order.setCustomer(customer);
    order.setContainer(container);
    order.setVoyage(voyage);
    order.setAgent(agent);
    order.setStatus(OrderStatus.DELIVERED);
    order.setOrderedBy("orderedBy");
    order.setBasePriceUsd(BigDecimal.valueOf(1000));
    order.setFinalPrice(BigDecimal.valueOf(1000));
    order.setNotes("Test order notes");

    FreightOrder savedOrder = freightOrderRepository.save(order);
    orderId = savedOrder.getId();
  }

  @Test
  @DisplayName("GET /api/v1/track/order/{orderId} - valid order ID returns tracking details")
  void testTrackOrder_validId_returnsTrackingDetails() throws Exception {
    mockMvc
        .perform(get("/api/v1/track/order/" + orderId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.orderId").value(orderId))
        .andExpect(jsonPath("$.status").value("DELIVERED"))
        .andExpect(jsonPath("$.voyageNumber").value("VOY-001"))
        .andExpect(jsonPath("$.containerCode").value("CONT-001"));
  }

  @Test
  @DisplayName("GET /api/v1/track/order/{orderId} - non-existent order ID returns 404 Not Found")
  void testTrackOrder_nonExistentId_returnsNotFound() throws Exception {
    mockMvc.perform(get("/api/v1/track/order/9999")).andExpect(status().isNotFound());
  }

  @Test
  @DisplayName(
      "GET /api/v1/track/container/{containerCode} - valid container code returns tracking details")
  void testTrackContainer_validCode_returnsTrackingDetails() throws Exception {
    mockMvc
        .perform(get("/api/v1/track/container/CONT-001"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.containerCode").value("CONT-001"))
        .andExpect(jsonPath("$.containerSize").value("FORTY_FOOT"))
        .andExpect(jsonPath("$.containerType").value("DRY"))
        .andExpect(jsonPath("$.voyages").isArray())
        .andExpect(jsonPath("$.voyages[0].voyageNumber").value("VOY-001"))
        .andExpect(jsonPath("$.voyages[0].departureTime").exists())
        .andExpect(jsonPath("$.voyages[0].arrivalTime").exists())
        .andExpect(jsonPath("$.voyages[0].departureTime").isNotEmpty())
        .andExpect(jsonPath("$.voyages[0].arrivalTime").isNotEmpty());
  }

  @Test
  @DisplayName(
      "GET /api/v1/track/container/{containerCode} - non-existent container code returns 404 Not Found")
  void testTrackContainer_nonExistentCode_returnsNotFound() throws Exception {
    mockMvc.perform(get("/api/v1/track/container/UNKNOWN")).andExpect(status().isNotFound());
  }
}
