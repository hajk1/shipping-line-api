package com.shipping.freightops.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shipping.freightops.dto.CreateFreightOrderRequest;
import com.shipping.freightops.dto.UpdateDiscountRequest;
import com.shipping.freightops.entity.*;
import com.shipping.freightops.enums.AgentType;
import com.shipping.freightops.enums.ContainerSize;
import com.shipping.freightops.enums.ContainerType;
import com.shipping.freightops.enums.OrderStatus;
import com.shipping.freightops.repository.*;
import com.shipping.freightops.service.FreightOrderService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration test for {@link FreightOrderController}.
 *
 * <p>Uses H2 in-memory DB (see src/test/resources/application.properties). This is a good reference
 * for writing additional controller tests.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class FreightOrderControllerTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;
  @Autowired private PortRepository portRepository;
  @Autowired private VesselRepository vesselRepository;
  @Autowired private ContainerRepository containerRepository;
  @Autowired private CustomerRepository customerRepository;
  @Autowired private VoyageRepository voyageRepository;
  @Autowired private FreightOrderRepository freightOrderRepository;
  @Autowired private VoyagePriceRepository voyagePriceRepository;
  @Autowired private FreightOrderService freightOrderService;
  @Autowired private AgentRepository agentRepository;

  private Voyage savedVoyage;
  private Container savedContainer;
  private Customer savedCustomer;
  private Agent savedAgent;
  private Long freightOrderId;

  @BeforeEach
  void setUp() {
    // Clear state between tests — children first to respect FK constraints
    freightOrderRepository.deleteAll();
    voyagePriceRepository.deleteAll();
    voyageRepository.deleteAll();
    containerRepository.deleteAll();
    customerRepository.deleteAll();
    vesselRepository.deleteAll();
    portRepository.deleteAll();
    agentRepository.deleteAll();
    Port departure = portRepository.save(new Port("AEJEA", "Jebel Ali", "UAE"));
    Port arrival = portRepository.save(new Port("CNSHA", "Shanghai", "China"));
    Vessel vessel = vesselRepository.save(new Vessel("MV Test", "9999999", 3000));

    Voyage voyage = new Voyage();
    voyage.setVoyageNumber("VOY-001");
    voyage.setVessel(vessel);
    voyage.setDeparturePort(departure);
    voyage.setArrivalPort(arrival);
    voyage.setDepartureTime(LocalDateTime.now().plusDays(3));
    voyage.setArrivalTime(LocalDateTime.now().plusDays(10));
    voyage.setMaxCapacityTeu(vessel.getCapacityTeu());
    voyage.setBookingOpen(true);
    savedVoyage = voyageRepository.save(voyage);

    savedContainer =
        containerRepository.save(
            new Container("TSTU1234567", ContainerSize.TWENTY_FOOT, ContainerType.DRY));

    Customer customer = new Customer();
    customer.setCompanyName("Test Customer Inc.");
    customer.setContactName("John Doe");
    customer.setEmail("John@testCust.com");
    savedCustomer = customerRepository.save(customer);

    VoyagePrice price = new VoyagePrice();
    price.setVoyage(savedVoyage);
    price.setContainerSize(ContainerSize.TWENTY_FOOT);
    price.setBasePriceUsd(BigDecimal.valueOf(1000));
    voyagePriceRepository.save(price);

    savedAgent = new Agent();
    savedAgent.setActive(true);
    savedAgent.setName("Test Agent");
    savedAgent.setEmail("agent@somewhere.com");
    savedAgent.setType(AgentType.INTERNAL);
    savedAgent.setCommissionPercent(BigDecimal.TEN);
    agentRepository.save(savedAgent);
    FreightOrder order = new FreightOrder();
    order.setContainer(savedContainer);
    order.setDiscountReason("random");
    order.setNotes("dsfjk");
    order.setDiscountPercent(BigDecimal.valueOf(2));
    order.setVoyage(savedVoyage);
    order.setStatus(OrderStatus.DELIVERED);
    order.setCustomer(savedCustomer);
    order.setBasePriceUsd(BigDecimal.valueOf(115));
    order.setAgent(savedAgent);
    order.setFinalPrice(BigDecimal.valueOf(126));
    order.setOrderedBy("me");
    FreightOrder savedOrder = freightOrderRepository.save(order);
    freightOrderId = savedOrder.getId();
  }

  @Test
  @DisplayName("POST /api/v1/freight-orders → 201 Created")
  void createOrder_returnsCreated() throws Exception {
    CreateFreightOrderRequest request = new CreateFreightOrderRequest();
    request.setVoyageId(savedVoyage.getId());
    request.setContainerId(savedContainer.getId());
    request.setCustomerId(savedCustomer.getId());
    request.setOrderedBy("ops-team");
    request.setNotes("Urgent delivery");
    request.setAgentId(savedAgent.getId());

    mockMvc
        .perform(
            post("/api/v1/freight-orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.voyageNumber").value("VOY-001"))
        .andExpect(jsonPath("$.containerCode").value("TSTU1234567"))
        .andExpect(jsonPath("$.customerName").value("Test Customer Inc."))
        .andExpect(jsonPath("$.customerEmail").value("John@testCust.com"))
        .andExpect(jsonPath("$.orderedBy").value("ops-team"))
        .andExpect(jsonPath("$.status").value("PENDING"));
  }

  @Test
  @DisplayName("POST /api/v1/freight-orders with missing fields → 400 Bad Request")
  void createOrder_withMissingFields_returnsBadRequest() throws Exception {
    CreateFreightOrderRequest request = new CreateFreightOrderRequest();
    // intentionally leaving required fields empty

    mockMvc
        .perform(
            post("/api/v1/freight-orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("GET /api/v1/freight-orders → 200 OK with paged result")
  void listOrders_returnsOk() throws Exception {
    int totalOrders = 25;
    int pageSize = 10;

    for (int i = 0; i < totalOrders; i++) {
      CreateFreightOrderRequest request = new CreateFreightOrderRequest();
      request.setVoyageId(savedVoyage.getId());
      request.setContainerId(savedContainer.getId());
      request.setCustomerId(savedCustomer.getId());
      request.setOrderedBy("user-" + i);
      request.setNotes("order-" + i);
      request.setAgentId(savedAgent.getId());

      freightOrderService.createOrder(request);
    }
    mockMvc
        .perform(
            get("/api/v1/freight-orders")
                .param("page", "0")
                .param("size", String.valueOf(pageSize)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray())
        .andExpect(jsonPath("$.content.length()").value(pageSize))
        .andExpect(jsonPath("$.page").value(0))
        .andExpect(jsonPath("$.size").value(pageSize))
        .andExpect(jsonPath("$.totalElements").value(totalOrders))
        .andExpect(jsonPath("$.totalPages").value(3));
  }

  @Test
  @DisplayName("GET /api/v1/freight-orders without PageSize →  200 OK with default pageSize of 20")
  void listOrders_withoutPageSize_returnsOk() throws Exception {
    int totalOrders = 25;

    for (int i = 0; i < totalOrders; i++) {
      CreateFreightOrderRequest request = new CreateFreightOrderRequest();
      request.setVoyageId(savedVoyage.getId());
      request.setContainerId(savedContainer.getId());
      request.setCustomerId(savedCustomer.getId());
      request.setOrderedBy("user-" + i);
      request.setNotes("order-" + i);
      request.setAgentId(savedAgent.getId());

      freightOrderService.createOrder(request);
    }
    mockMvc
        .perform(get("/api/v1/freight-orders").param("page", "0"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray())
        .andExpect(jsonPath("$.content.length()").value(20))
        .andExpect(jsonPath("$.page").value(0))
        .andExpect(jsonPath("$.size").value(20))
        .andExpect(jsonPath("$.totalElements").value(totalOrders))
        .andExpect(jsonPath("$.totalPages").value(2));
  }

  @Test
  @DisplayName("GET /api/v1/freight-orders without Page →  200 OK with default page of 0")
  void listOrders_withoutPage_returnsOk() throws Exception {
    int totalOrders = 25;

    for (int i = 0; i < totalOrders; i++) {
      CreateFreightOrderRequest request = new CreateFreightOrderRequest();
      request.setVoyageId(savedVoyage.getId());
      request.setContainerId(savedContainer.getId());
      request.setCustomerId(savedCustomer.getId());
      request.setOrderedBy("user-" + i);
      request.setNotes("order-" + i);
      request.setAgentId(savedAgent.getId());

      freightOrderService.createOrder(request);
    }
    mockMvc
        .perform(get("/api/v1/freight-orders"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray())
        .andExpect(jsonPath("$.content.length()").value(20))
        .andExpect(jsonPath("$.page").value(0))
        .andExpect(jsonPath("$.size").value(20))
        .andExpect(jsonPath("$.totalElements").value(totalOrders))
        .andExpect(jsonPath("$.totalPages").value(2));
  }

  @Test
  @DisplayName(
      "GET /api/v1/freight-orders pageSize bt 100 → 200 OK with default max pageSize of 100")
  void listOrders_pageSize101_returnsOk() throws Exception {
    int totalOrders = 25;

    for (int i = 0; i < totalOrders; i++) {
      CreateFreightOrderRequest request = new CreateFreightOrderRequest();
      request.setVoyageId(savedVoyage.getId());
      request.setContainerId(savedContainer.getId());
      request.setCustomerId(savedCustomer.getId());
      request.setOrderedBy("user-" + i);
      request.setNotes("order-" + i);
      request.setAgentId(savedAgent.getId());

      freightOrderService.createOrder(request);
    }
    mockMvc
        .perform(get("/api/v1/freight-orders").param("page", "0").param("size", "101"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray())
        .andExpect(jsonPath("$.content.length()").value(totalOrders))
        .andExpect(jsonPath("$.page").value(0))
        .andExpect(jsonPath("$.size").value(100))
        .andExpect(jsonPath("$.totalElements").value(totalOrders))
        .andExpect(jsonPath("$.totalPages").value(1));
  }

  @Test
  @DisplayName("PATCH /api/v1/freight-orders/{id}/discount → 200 OK")
  void updateDiscount_returnsUpdatedOrder() throws Exception {
    CreateFreightOrderRequest request = new CreateFreightOrderRequest();
    request.setVoyageId(savedVoyage.getId());
    request.setContainerId(savedContainer.getId());
    request.setCustomerId(savedCustomer.getId());
    request.setOrderedBy("ops-team");
    request.setNotes("Urgent delivery");
    request.setAgentId(savedAgent.getId());
    FreightOrder order = freightOrderService.createOrder(request);

    UpdateDiscountRequest updateDiscountRequest = new UpdateDiscountRequest();
    updateDiscountRequest.setDiscountPercent(BigDecimal.valueOf(10));
    updateDiscountRequest.setReason("Loyal customer");

    mockMvc
        .perform(
            patch("/api/v1/freight-orders/{id}/discount", order.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDiscountRequest)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.discountPercent").value(10))
        .andExpect(jsonPath("$.discountReason").value("Loyal customer"))
        .andExpect(jsonPath("$.finalPrice").value(900.00));
  }

  @Test
  @DisplayName("PATCH /api/v1/freight-orders/{id}/discount → 404 Not Found")
  void updateDiscount_orderNotFound() throws Exception {
    UpdateDiscountRequest request = new UpdateDiscountRequest();
    request.setDiscountPercent(BigDecimal.valueOf(10));
    request.setReason("Test");

    mockMvc
        .perform(
            patch("/api/v1/freight-orders/{id}/discount", 9999L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("PATCH /api/v1/freight-orders/{id}/discount → 400 Bad Request (invalid input)")
  void updateDiscount_invalidRequest() throws Exception {
    CreateFreightOrderRequest request = new CreateFreightOrderRequest();
    request.setVoyageId(savedVoyage.getId());
    request.setContainerId(savedContainer.getId());
    request.setCustomerId(savedCustomer.getId());
    request.setOrderedBy("ops-team");
    request.setNotes("Urgent delivery");
    request.setAgentId(savedAgent.getId());
    FreightOrder order = freightOrderService.createOrder(request);

    UpdateDiscountRequest updateDiscountRequest = new UpdateDiscountRequest();
    updateDiscountRequest.setDiscountPercent(BigDecimal.valueOf(150));
    updateDiscountRequest.setReason("");

    mockMvc
        .perform(
            patch("/api/v1/freight-orders/{id}/discount", order.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDiscountRequest)))
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("PATCH /api/v1/freight-orders/{id}/discount → 409 Conflict (invalid state)")
  void updateDiscount_invalidState() throws Exception {
    CreateFreightOrderRequest request = new CreateFreightOrderRequest();
    request.setVoyageId(savedVoyage.getId());
    request.setContainerId(savedContainer.getId());
    request.setCustomerId(savedCustomer.getId());
    request.setOrderedBy("ops-team");
    request.setNotes("Urgent delivery");
    request.setAgentId(savedAgent.getId());
    FreightOrder order = freightOrderService.createOrder(request);

    order.setStatus(OrderStatus.CANCELLED);
    freightOrderRepository.save(order);

    UpdateDiscountRequest updateDiscountRequest = new UpdateDiscountRequest();
    updateDiscountRequest.setDiscountPercent(BigDecimal.valueOf(10));
    updateDiscountRequest.setReason("Test");

    mockMvc
        .perform(
            patch("/api/v1/freight-orders/{id}/discount", order.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDiscountRequest)))
        .andExpect(status().isConflict());
  }

  @Test
  @DisplayName("POST /api/v1/freight-orders → 409 when booking is closed")
  void createOrder_whenBookingClosed_returnsConflict() throws Exception {

    savedVoyage.setBookingOpen(false);
    voyageRepository.save(savedVoyage);

    CreateFreightOrderRequest request = new CreateFreightOrderRequest();
    request.setVoyageId(savedVoyage.getId());
    request.setContainerId(savedContainer.getId());
    request.setCustomerId(savedCustomer.getId());
    request.setOrderedBy("ops-team");
    request.setNotes("Urgent delivery");
    request.setAgentId(savedAgent.getId());

    mockMvc
        .perform(
            post("/api/v1/freight-orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isConflict());
  }

  @Test
  @DisplayName("GET /api/v1/freight-orders/{id}/invoice → 200 OK w")
  void getFreightOrderInvoice() throws Exception {
    mockMvc
        .perform(get("/api/v1/freight-orders/" + freightOrderId.toString() + "/invoice"))
        .andExpect(status().isOk())
        .andExpect(MockMvcResultMatchers.content().string(CoreMatchers.containsString("PDF")));
  }

  @Test
  @DisplayName("GET /api/v1/freight-orders/{id}/invoice → 404 NOT_FOUND")
  void getFreightOrderInvoiceWithWrongId() throws Exception {
    mockMvc
        .perform(get("/api/v1/freight-orders/" + Long.valueOf(1) + "/invoice"))
        .andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("GET /api/v1/freight-orders/{id}/invoice → 409 Conflict")
  void getFreightOrderInvoiceWithOrderStatusNotDelivered() throws Exception {
    FreightOrder order = freightOrderRepository.getReferenceById(freightOrderId);
    order.setStatus(OrderStatus.IN_TRANSIT);
    freightOrderRepository.save(order);
    mockMvc
        .perform(get("/api/v1/freight-orders/" + freightOrderId + "/invoice"))
        .andExpect(status().isConflict());
  }
}
