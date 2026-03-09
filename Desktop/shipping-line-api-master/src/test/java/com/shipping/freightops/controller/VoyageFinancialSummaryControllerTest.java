package com.shipping.freightops.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.shipping.freightops.entity.Agent;
import com.shipping.freightops.entity.Container;
import com.shipping.freightops.entity.Customer;
import com.shipping.freightops.entity.FreightOrder;
import com.shipping.freightops.entity.Port;
import com.shipping.freightops.entity.Vessel;
import com.shipping.freightops.entity.VesselOwner;
import com.shipping.freightops.entity.Voyage;
import com.shipping.freightops.entity.VoyageCost;
import com.shipping.freightops.enums.AgentType;
import com.shipping.freightops.enums.ContainerSize;
import com.shipping.freightops.enums.ContainerType;
import com.shipping.freightops.enums.OrderStatus;
import com.shipping.freightops.enums.VoyageStatus;
import com.shipping.freightops.repository.AgentRepository;
import com.shipping.freightops.repository.ContainerRepository;
import com.shipping.freightops.repository.CustomerRepository;
import com.shipping.freightops.repository.FreightOrderRepository;
import com.shipping.freightops.repository.PortRepository;
import com.shipping.freightops.repository.VesselOwnerRepository;
import com.shipping.freightops.repository.VesselRepository;
import com.shipping.freightops.repository.VoyageCostRepository;
import com.shipping.freightops.repository.VoyageRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class VoyageFinancialSummaryControllerTest {
  @Autowired private VoyageRepository voyageRepository;
  @Autowired private PortRepository portRepository;
  @Autowired private MockMvc mockMvc;
  @Autowired private VesselRepository vesselRepository;
  @Autowired private VoyageCostRepository voyageCostRepository;
  @Autowired private ContainerRepository containerRepository;
  @Autowired private FreightOrderRepository freightOrderRepository;
  @Autowired private CustomerRepository customerRepository;
  @Autowired private AgentRepository agentRepository;
  @Autowired private VesselOwnerRepository vesselOwnerRepository;

  private Vessel vessel;
  private Voyage voyage;
  private Agent agent;
  private Container container20;
  private Container container40;
  private Customer customer;

  @BeforeEach
  void setUp() {
    freightOrderRepository.deleteAll();
    voyageCostRepository.deleteAll();
    voyageRepository.deleteAll();
    vesselOwnerRepository.deleteAll();
    containerRepository.deleteAll();
    customerRepository.deleteAll();
    vesselRepository.deleteAll();
    portRepository.deleteAll();
    agentRepository.deleteAll();

    Port departurePort = portRepository.save(new Port("TGKRY", "kalgary", "Togo"));
    Port arrivalPort = portRepository.save(new Port("JPTKY", "tokyo", "Japan"));
    vessel = vesselRepository.save(new Vessel("SeeFox", "111", 4));

    Voyage createdVoyage = new Voyage();
    createdVoyage.setVoyageNumber("E-228");
    createdVoyage.setVessel(vessel);
    createdVoyage.setArrivalTime(LocalDateTime.of(2026, 11, 14, 6, 23));
    createdVoyage.setDepartureTime(LocalDateTime.now());
    createdVoyage.setDeparturePort(departurePort);
    createdVoyage.setArrivalPort(arrivalPort);
    createdVoyage.setMaxCapacityTeu(vessel.getCapacityTeu());
    createdVoyage.setBookingOpen(true);
    voyage = voyageRepository.save(createdVoyage);

    container20 =
        containerRepository.save(
            new Container("C20", ContainerSize.TWENTY_FOOT, ContainerType.DRY));
    container40 =
        containerRepository.save(new Container("C40", ContainerSize.FORTY_FOOT, ContainerType.DRY));

    Customer createdCustomer = new Customer();
    createdCustomer.setCompanyName("Test Co");
    createdCustomer.setContactName("John Doe");
    createdCustomer.setEmail("john@test.com");
    customer = customerRepository.save(createdCustomer);

    agent = new Agent();
    agent.setActive(true);
    agent.setName("Test Agent");
    agent.setEmail("agent@somewhere.com");
    agent.setType(AgentType.INTERNAL);
    agent.setCommissionPercent(BigDecimal.TEN);
    agentRepository.save(agent);
  }

  @Test
  @DisplayName("POST /api/v1/voyages/{voyageId}/costs → 201 Created")
  void addVoyageCost_returnsCreated() throws Exception {
    String body = "{\"description\":\"Fuel\",\"amountUsd\":12000.00}";

    mockMvc
        .perform(
            post("/api/v1/voyages/{voyageId}/costs", voyage.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.voyageId").value(voyage.getId()))
        .andExpect(jsonPath("$.description").value("Fuel"))
        .andExpect(jsonPath("$.amountUsd").value(12000.00));
  }

  @Test
  @DisplayName("GET /api/v1/voyages/{voyageId}/costs → 200 OK")
  void getVoyageCosts_returnsList() throws Exception {
    createAndSaveCost("Fuel", 12000);
    createAndSaveCost("Port fees", 3000);

    mockMvc
        .perform(get("/api/v1/voyages/{voyageId}/costs", voyage.getId()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$.length()").value(2))
        .andExpect(jsonPath("$[0].description").value("Fuel"))
        .andExpect(jsonPath("$[1].description").value("Port fees"));
  }

  @Test
  @DisplayName("GET /api/v1/voyages/{voyageId}/financial-summary → 200 OK for completed voyage")
  void getFinancialSummary_returnsCompletedVoyageSummary() throws Exception {
    voyage.setStatus(VoyageStatus.COMPLETED);
    voyageRepository.save(voyage);

    createAndSaveOwners();
    createAndSaveDeliveredOrders();
    createAndSaveCosts();

    mockMvc
        .perform(get("/api/v1/voyages/{voyageId}/financial-summary", voyage.getId()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.voyageNumber").value("E-228"))
        .andExpect(jsonPath("$.totalRevenueUsd").value(250000))
        .andExpect(jsonPath("$.totalCostsUsd").value(180000))
        .andExpect(jsonPath("$.netProfitUsd").value(70000))
        .andExpect(jsonPath("$.orderCount").value(2))
        .andExpect(jsonPath("$.owners.length()").value(2))
        .andExpect(jsonPath("$.owners[0].ownerName").value("Alpha Shipping Ltd"))
        .andExpect(jsonPath("$.owners[0].sharePercent").value(60))
        .andExpect(jsonPath("$.owners[0].revenueShareUsd").value(150000))
        .andExpect(jsonPath("$.owners[0].costShareUsd").value(108000))
        .andExpect(jsonPath("$.owners[0].profitShareUsd").value(42000))
        .andExpect(jsonPath("$.owners[1].ownerName").value("Beta Maritime Co"))
        .andExpect(jsonPath("$.owners[1].sharePercent").value(40))
        .andExpect(jsonPath("$.owners[1].revenueShareUsd").value(100000))
        .andExpect(jsonPath("$.owners[1].costShareUsd").value(72000))
        .andExpect(jsonPath("$.owners[1].profitShareUsd").value(28000));
  }

  @Test
  @DisplayName("GET /api/v1/voyages/{voyageId}/financial-summary → 409 for non-completed voyage")
  void getFinancialSummary_returnsConflictWhenVoyageIsNotCompleted() throws Exception {
    mockMvc
        .perform(get("/api/v1/voyages/{voyageId}/financial-summary", voyage.getId()))
        .andExpect(status().isConflict());
  }

  private void createAndSaveCost(String description, long amount) {
    VoyageCost cost = new VoyageCost();
    cost.setVoyage(voyage);
    cost.setDescription(description);
    cost.setAmountUsd(BigDecimal.valueOf(amount));
    voyageCostRepository.save(cost);
  }

  private void createAndSaveOwners() {
    VesselOwner owner1 = new VesselOwner();
    owner1.setVessel(vessel);
    owner1.setOwnerName("Alpha Shipping Ltd");
    owner1.setOwnerEmail("alpha@owners.com");
    owner1.setSharePercent(BigDecimal.valueOf(60));

    VesselOwner owner2 = new VesselOwner();
    owner2.setVessel(vessel);
    owner2.setOwnerName("Beta Maritime Co");
    owner2.setOwnerEmail("beta@owners.com");
    owner2.setSharePercent(BigDecimal.valueOf(40));

    vesselOwnerRepository.saveAll(List.of(owner1, owner2));
  }

  private void createAndSaveDeliveredOrders() {
    FreightOrder delivered1 = new FreightOrder();
    delivered1.setVoyage(voyage);
    delivered1.setContainer(container20);
    delivered1.setCustomer(customer);
    delivered1.setOrderedBy("ops");
    delivered1.setBasePriceUsd(BigDecimal.valueOf(100000));
    delivered1.setDiscountPercent(BigDecimal.ZERO);
    delivered1.setFinalPrice(BigDecimal.valueOf(100000));
    delivered1.setStatus(OrderStatus.DELIVERED);
    delivered1.setAgent(agent);

    FreightOrder delivered2 = new FreightOrder();
    delivered2.setVoyage(voyage);
    delivered2.setContainer(container40);
    delivered2.setCustomer(customer);
    delivered2.setOrderedBy("ops");
    delivered2.setBasePriceUsd(BigDecimal.valueOf(150000));
    delivered2.setDiscountPercent(BigDecimal.ZERO);
    delivered2.setFinalPrice(BigDecimal.valueOf(150000));
    delivered2.setStatus(OrderStatus.DELIVERED);
    delivered2.setAgent(agent);

    freightOrderRepository.saveAll(List.of(delivered1, delivered2));
  }

  private void createAndSaveCosts() {
    createAndSaveCost("Fuel", 100000);
    createAndSaveCost("Port fees", 80000);
  }
}
