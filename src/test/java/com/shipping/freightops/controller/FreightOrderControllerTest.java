package com.shipping.freightops.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shipping.freightops.dto.CreateFreightOrderRequest;
import com.shipping.freightops.entity.*;
import com.shipping.freightops.enums.ContainerSize;
import com.shipping.freightops.enums.ContainerType;
import com.shipping.freightops.enums.OrderStatus;
import com.shipping.freightops.repository.ContainerRepository;
import com.shipping.freightops.repository.CustomerRepository;
import com.shipping.freightops.repository.FreightOrderRepository;
import com.shipping.freightops.repository.PortRepository;
import com.shipping.freightops.repository.VesselRepository;
import com.shipping.freightops.repository.VoyageRepository;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
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

  private Voyage savedVoyage;
  private Container savedContainer;
  private Customer savedCustomer;

  @BeforeEach
  void setUp() {
    // Clear state between tests — children first to respect FK constraints
    freightOrderRepository.deleteAll();
    voyageRepository.deleteAll();
    containerRepository.deleteAll();
    customerRepository.deleteAll();
    vesselRepository.deleteAll();
    portRepository.deleteAll();

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
    savedVoyage = voyageRepository.save(voyage);

    savedContainer =
        containerRepository.save(
            new Container("TSTU1234567", ContainerSize.TWENTY_FOOT, ContainerType.DRY));

    Customer customer = new Customer();
    customer.setCompanyName("Test Customer Inc.");
    customer.setContactName("John Doe");
    customer.setEmail("John@testCust.com");
    savedCustomer = customerRepository.save(customer);
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
      FreightOrder order = new FreightOrder();
      order.setVoyage(savedVoyage);
      order.setContainer(savedContainer);
      order.setOrderedBy("user-" + i);
      order.setNotes("order-" + i);
      order.setStatus(OrderStatus.PENDING);
      order.setCustomer(savedCustomer);

      freightOrderRepository.save(order);
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
      FreightOrder order = new FreightOrder();
      order.setVoyage(savedVoyage);
      order.setContainer(savedContainer);
      order.setOrderedBy("user-" + i);
      order.setNotes("order-" + i);
      order.setStatus(OrderStatus.PENDING);
      order.setCustomer(savedCustomer);

      freightOrderRepository.save(order);
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
      FreightOrder order = new FreightOrder();
      order.setVoyage(savedVoyage);
      order.setContainer(savedContainer);
      order.setOrderedBy("user-" + i);
      order.setNotes("order-" + i);
      order.setStatus(OrderStatus.PENDING);
      order.setCustomer(savedCustomer);

      freightOrderRepository.save(order);
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
      FreightOrder order = new FreightOrder();
      order.setVoyage(savedVoyage);
      order.setContainer(savedContainer);
      order.setOrderedBy("user-" + i);
      order.setNotes("order-" + i);
      order.setStatus(OrderStatus.PENDING);
      order.setCustomer(savedCustomer);

      freightOrderRepository.save(order);
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
}
