package com.shipping.freightops.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.shipping.freightops.dto.CreateFreightOrderRequest;
import com.shipping.freightops.entity.*;
import com.shipping.freightops.enums.AgentType;
import com.shipping.freightops.enums.ContainerSize;
import com.shipping.freightops.enums.ContainerType;
import com.shipping.freightops.enums.OrderStatus;
import com.shipping.freightops.repository.*;
import com.shipping.freightops.service.FreightOrderService;
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
  private Long orderId2;
  @Autowired private FreightOrderService freightOrderService;
  @Autowired private VoyagePriceRepository voyagePriceRepository;

  @BeforeEach
  void setup() {
    // Clear repositories before each test
    freightOrderRepository.deleteAll();
    voyagePriceRepository.deleteAll();
    voyageRepository.deleteAll();
    containerRepository.deleteAll();
    vesselRepository.deleteAll();
    customerRepository.deleteAll();
    portRepository.deleteAll();
    agentRepository.deleteAll();

    // Setup test data for tracking tests
    Port departurePort = portRepository.save(new Port("AEJEC", "Jebel Ali", "UAE"));
    Port arrivalPort = portRepository.save(new Port("CNSHC", "Shanghai", "China"));
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
    Port departure2 = portRepository.save(new Port("AEJEB", "Jebel Ali 2", "UAE"));
    Port arrival2 = portRepository.save(new Port("CNSHB", "Shanghai 2", "China"));
    Vessel vessel2 = vesselRepository.save(new Vessel("MV Test 2", "9999993", 3000));

    Agent agent2 = new Agent();
    agent2.setName("Test Agent 2");
    agent2.setEmail("agent2@test.com");
    agent2.setCommissionPercent(BigDecimal.valueOf(5));
    agent2.setType(AgentType.INTERNAL);
    Agent savedAgent2 = agentRepository.save(agent2);

    Voyage voyage2 = new Voyage();
    voyage2.setVoyageNumber("VOY-002");
    voyage2.setVessel(vessel2);
    voyage2.setDeparturePort(departure2);
    voyage2.setArrivalPort(arrival2);
    voyage2.setDepartureTime(LocalDateTime.now().plusDays(3));
    voyage2.setArrivalTime(LocalDateTime.now().plusDays(10));
    voyage2.setMaxCapacityTeu(vessel2.getCapacityTeu());
    voyage2.setBookingOpen(true);
    Voyage savedVoyage2 = voyageRepository.save(voyage2);

    Customer customer2 =
        customerRepository.save(new Customer("Test Customer Inc.", "John Doe", "john@test.com"));
    Container container2 =
        containerRepository.save(
            new Container("TSTU1234567", ContainerSize.TWENTY_FOOT, ContainerType.DRY));

    VoyagePrice price2 = new VoyagePrice();
    price2.setVoyage(savedVoyage2);
    price2.setContainerSize(ContainerSize.TWENTY_FOOT);
    price2.setBasePriceUsd(BigDecimal.valueOf(1000));
    voyagePriceRepository.save(price2);

    CreateFreightOrderRequest request2 = new CreateFreightOrderRequest();
    request2.setVoyageId(savedVoyage2.getId());
    request2.setContainerId(container2.getId());
    request2.setCustomerId(customer2.getId());
    request2.setAgentId(savedAgent2.getId());
    request2.setOrderedBy("ops-team");
    request2.setNotes("Audit trail starting");

    FreightOrder savedOrder2 = freightOrderService.createOrder(request2);
    this.orderId2 = savedOrder2.getId();
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

  @Test
  @DisplayName(
      "GET /api/v1/track/container/{containerCode} - container with no orders returns 200 with empty voyages")
  void testTrackContainer_containerWithNoOrders_returnsOkWithEmptyVoyages() throws Exception {
    containerRepository.save(
        new Container("ORPHAN-001", ContainerSize.TWENTY_FOOT, ContainerType.DRY));
    freightOrderRepository.deleteAll();

    mockMvc
        .perform(get("/api/v1/track/container/ORPHAN-001"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.containerCode").value("ORPHAN-001"))
        .andExpect(jsonPath("$.containerSize").value("TWENTY_FOOT"))
        .andExpect(jsonPath("$.containerType").value("DRY"))
        .andExpect(jsonPath("$.voyages").isArray())
        .andExpect(jsonPath("$.voyages").isEmpty());
  }

  @Test
  @DisplayName("GET /api/v1/track/order/{orderId} - valid order ID returns tracking details")
  void testTrackOrder_withListEvents() throws Exception {
    mockMvc
        .perform(get("/api/v1/track/order/" + orderId2))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.orderId").value(orderId2))
        .andExpect(jsonPath("$.status").value("PENDING"))
        .andExpect(jsonPath("$.voyageNumber").value("VOY-002"))
        .andExpect(jsonPath("$.containerCode").value("TSTU1234567"))
        .andExpect(jsonPath("$.events").isArray());
  }
}
