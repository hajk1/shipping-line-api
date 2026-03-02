package com.shipping.freightops.controller;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.shipping.freightops.entity.*;
import com.shipping.freightops.enums.AgentType;
import com.shipping.freightops.enums.ContainerSize;
import com.shipping.freightops.enums.ContainerType;
import com.shipping.freightops.enums.OrderStatus;
import com.shipping.freightops.enums.VoyageStatus;
import com.shipping.freightops.repository.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class VoyageCommissionTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private FreightOrderRepository freightOrderRepository;
  @Autowired private VoyageRepository voyageRepository;
  @Autowired private AgentRepository agentRepository;
  @Autowired private ContainerRepository containerRepository;
  @Autowired private CustomerRepository customerRepository;
  @Autowired private VesselRepository vesselRepository;
  @Autowired private PortRepository portRepository;
  @Autowired private VoyagePriceRepository voyagePriceRepository;

  private Voyage completedVoyage;
  private Agent internalAgent;
  private Agent externalAgent;
  private Customer customer;

  @BeforeEach
  void setUp() {
    freightOrderRepository.deleteAll();
    agentRepository.deleteAll();
    voyagePriceRepository.deleteAll();
    voyageRepository.deleteAll();
    containerRepository.deleteAll();
    customerRepository.deleteAll();
    vesselRepository.deleteAll();
    portRepository.deleteAll();

    Port departure = portRepository.save(new Port("AEJEA", "Jebel Ali", "UAE"));
    Port arrival = portRepository.save(new Port("CNSHA", "Shanghai", "China"));
    Vessel vessel = vesselRepository.save(new Vessel("MV Commission", "1234567", 5000));

    Voyage voyage = new Voyage();
    voyage.setVoyageNumber("VOY-COMM-001");
    voyage.setVessel(vessel);
    voyage.setDeparturePort(departure);
    voyage.setArrivalPort(arrival);
    voyage.setDepartureTime(LocalDateTime.now().minusDays(20));
    voyage.setArrivalTime(LocalDateTime.now().minusDays(5));
    voyage.setStatus(VoyageStatus.COMPLETED);
    completedVoyage = voyageRepository.save(voyage);

    Agent agentA = new Agent();
    agentA.setName("Ali Hassan");
    agentA.setEmail("ali@internal.com");
    agentA.setCommissionPercent(BigDecimal.valueOf(5));
    agentA.setType(AgentType.INTERNAL);
    internalAgent = agentRepository.save(agentA);

    Agent agentB = new Agent();
    agentB.setName("FastFreight FZE");
    agentB.setEmail("ops@fastfreight.com");
    agentB.setCommissionPercent(BigDecimal.valueOf(8));
    agentB.setType(AgentType.EXTERNAL);
    externalAgent = agentRepository.save(agentB);

    Customer cust = new Customer();
    cust.setCompanyName("Test Co.");
    cust.setContactName("Jane Smith");
    cust.setEmail("jane@testco.com");
    customer = customerRepository.save(cust);
  }

  // ── Helper ──────────────────────────────────────────────────────────────

  private FreightOrder deliveredOrder(Voyage voyage, Agent agent, BigDecimal finalPrice) {
    Container container =
        containerRepository.save(
            new Container(
                "TCKU" + (int) (Math.random() * 9_000_000 + 1_000_000),
                ContainerSize.TWENTY_FOOT,
                ContainerType.DRY));
    FreightOrder order = new FreightOrder();
    order.setVoyage(voyage);
    order.setAgent(agent);
    order.setCustomer(customer);
    order.setContainer(container);
    order.setOrderedBy("ops-team");
    order.setBasePriceUsd(finalPrice);
    order.setDiscountPercent(BigDecimal.ZERO);
    order.setFinalPrice(finalPrice);
    order.setStatus(OrderStatus.DELIVERED);
    return freightOrderRepository.save(order);
  }

  // ── Happy Path ───────────────────────────────────────────────────────────

  @Test
  @DisplayName("GET /{voyageId}/commissions → 200: correct commission per agent")
  void commissions_calculatedCorrectly() throws Exception {
    // Agent A (INTERNAL, 5%): orders 1000 + 2000 = 3000 → commission 150
    deliveredOrder(completedVoyage, internalAgent, BigDecimal.valueOf(1000));
    deliveredOrder(completedVoyage, internalAgent, BigDecimal.valueOf(2000));
    // Agent B (EXTERNAL, 8%): order 5000 → commission 400
    deliveredOrder(completedVoyage, externalAgent, BigDecimal.valueOf(5000));

    mockMvc
        .perform(
            MockMvcRequestBuilders.get(
                "/api/v1/voyages/" + completedVoyage.getId() + "/commissions"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.voyageNumber").value("VOY-COMM-001"))
        .andExpect(jsonPath("$.agents.length()").value(2))
        .andExpect(jsonPath("$.totalCommissionsUsd").value(550.00));
  }

  @Test
  @DisplayName("GET /{voyageId}/commissions → 200: agent order count and value are correct")
  void commissions_agentStatsAreCorrect() throws Exception {
    deliveredOrder(completedVoyage, internalAgent, BigDecimal.valueOf(1000));
    deliveredOrder(completedVoyage, internalAgent, BigDecimal.valueOf(2000));

    mockMvc
        .perform(
            MockMvcRequestBuilders.get(
                "/api/v1/voyages/" + completedVoyage.getId() + "/commissions"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.agents[0].agentName").value("Ali Hassan"))
        .andExpect(jsonPath("$.agents[0].type").value("INTERNAL"))
        .andExpect(jsonPath("$.agents[0].commissionPercent").value(5.0))
        .andExpect(jsonPath("$.agents[0].orderCount").value(2))
        .andExpect(jsonPath("$.agents[0].totalOrderValueUsd").value(3000.00))
        .andExpect(jsonPath("$.agents[0].commissionEarnedUsd").value(150.00));
  }

  // ── Edge Cases ───────────────────────────────────────────────────────────

  @Test
  @DisplayName("GET /{voyageId}/commissions → 200: no delivered orders returns empty agents list")
  void commissions_noDeliveredOrders_returnsEmptyList() throws Exception {
    // Create a PENDING order — should be excluded
    Container container =
        containerRepository.save(
            new Container("TCKU1234567", ContainerSize.TWENTY_FOOT, ContainerType.DRY));
    FreightOrder pendingOrder = new FreightOrder();
    pendingOrder.setVoyage(completedVoyage);
    pendingOrder.setAgent(internalAgent);
    pendingOrder.setCustomer(customer);
    pendingOrder.setContainer(container);
    pendingOrder.setOrderedBy("ops-team");
    pendingOrder.setBasePriceUsd(BigDecimal.valueOf(1000));
    pendingOrder.setDiscountPercent(BigDecimal.ZERO);
    pendingOrder.setFinalPrice(BigDecimal.valueOf(1000));
    pendingOrder.setStatus(OrderStatus.PENDING);
    freightOrderRepository.save(pendingOrder);

    mockMvc
        .perform(
            MockMvcRequestBuilders.get(
                "/api/v1/voyages/" + completedVoyage.getId() + "/commissions"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.voyageNumber").value("VOY-COMM-001"))
        .andExpect(jsonPath("$.agents").isEmpty())
        .andExpect(jsonPath("$.totalCommissionsUsd").value(0.00));
  }

  @Test
  @DisplayName("GET /{voyageId}/commissions → 200: only DELIVERED orders count, others excluded")
  void commissions_excludesNonDeliveredOrders() throws Exception {
    // 1 DELIVERED order
    deliveredOrder(completedVoyage, internalAgent, BigDecimal.valueOf(1000));

    // PENDING order (should be excluded)
    Container container2 =
        containerRepository.save(
            new Container("TCKU7654321", ContainerSize.TWENTY_FOOT, ContainerType.DRY));
    FreightOrder pendingOrder = new FreightOrder();
    pendingOrder.setVoyage(completedVoyage);
    pendingOrder.setAgent(internalAgent);
    pendingOrder.setCustomer(customer);
    pendingOrder.setContainer(container2);
    pendingOrder.setOrderedBy("ops-team");
    pendingOrder.setBasePriceUsd(BigDecimal.valueOf(500));
    pendingOrder.setDiscountPercent(BigDecimal.ZERO);
    pendingOrder.setFinalPrice(BigDecimal.valueOf(500));
    pendingOrder.setStatus(OrderStatus.CANCELLED);
    freightOrderRepository.save(pendingOrder);

    mockMvc
        .perform(
            MockMvcRequestBuilders.get(
                "/api/v1/voyages/" + completedVoyage.getId() + "/commissions"))
        .andExpect(status().isOk())
        // Only the 1 delivered order counts
        .andExpect(jsonPath("$.agents[0].orderCount").value(1))
        .andExpect(jsonPath("$.agents[0].totalOrderValueUsd").value(1000.00))
        .andExpect(jsonPath("$.agents[0].commissionEarnedUsd").value(50.00))
        .andExpect(jsonPath("$.totalCommissionsUsd").value(50.00));
  }

  @Test
  @DisplayName("GET /{voyageId}/commissions → 409: voyage not COMPLETED")
  void commissions_voyageNotCompleted_returns409() throws Exception {
    Port dep2 = portRepository.save(new Port("USNYC", "New York", "USA"));
    Port arr2 = portRepository.save(new Port("GBLON", "London", "UK"));
    Vessel vessel2 = vesselRepository.save(new Vessel("MV Planned", "7654321", 3000));

    Voyage plannedVoyage = new Voyage();
    plannedVoyage.setVoyageNumber("VOY-PLAN-001");
    plannedVoyage.setVessel(vessel2);
    plannedVoyage.setDeparturePort(dep2);
    plannedVoyage.setArrivalPort(arr2);
    plannedVoyage.setDepartureTime(LocalDateTime.now().plusDays(5));
    plannedVoyage.setArrivalTime(LocalDateTime.now().plusDays(15));
    plannedVoyage.setStatus(VoyageStatus.PLANNED);
    Voyage saved = voyageRepository.save(plannedVoyage);

    mockMvc
        .perform(MockMvcRequestBuilders.get("/api/v1/voyages/" + saved.getId() + "/commissions"))
        .andExpect(status().isConflict());
  }

  @Test
  @DisplayName("GET /{voyageId}/commissions → 404: voyage not found")
  void commissions_voyageNotFound_returns404() throws Exception {
    mockMvc
        .perform(MockMvcRequestBuilders.get("/api/v1/voyages/99999/commissions"))
        .andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("GET /{voyageId}/commissions → 200: INTERNAL and EXTERNAL agents handled correctly")
  void commissions_mixedAgentTypes() throws Exception {
    deliveredOrder(completedVoyage, internalAgent, BigDecimal.valueOf(1000));
    deliveredOrder(completedVoyage, externalAgent, BigDecimal.valueOf(1000));

    mockMvc
        .perform(
            MockMvcRequestBuilders.get(
                "/api/v1/voyages/" + completedVoyage.getId() + "/commissions"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.agents.length()").value(2))
        // Internal agent: 5% of 1000 = 50
        .andExpect(jsonPath("$.agents[0].type").value("INTERNAL"))
        .andExpect(jsonPath("$.agents[0].commissionEarnedUsd").value(50.00))
        // External agent: 8% of 1000 = 80
        .andExpect(jsonPath("$.agents[1].type").value("EXTERNAL"))
        .andExpect(jsonPath("$.agents[1].commissionEarnedUsd").value(80.00))
        .andExpect(jsonPath("$.totalCommissionsUsd").value(130.00));
  }
}
