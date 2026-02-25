package com.shipping.freightops.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shipping.freightops.dto.BookingStatusUpdateRequest;
import com.shipping.freightops.dto.CreateVoyageRequest;
import com.shipping.freightops.dto.VoyagePriceRequest;
import com.shipping.freightops.entity.*;
import com.shipping.freightops.enums.AgentType;
import com.shipping.freightops.entity.*;
import com.shipping.freightops.enums.ContainerSize;
import com.shipping.freightops.enums.ContainerType;
import com.shipping.freightops.repository.*;
import com.shipping.freightops.enums.ContainerType;
import com.shipping.freightops.enums.OrderStatus;
import com.shipping.freightops.repository.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@SpringBootTest
@AutoConfigureMockMvc
public class VoyageControllerTest {
  @Autowired private VoyageRepository voyageRepository;
  @Autowired private PortRepository portRepository;
  @Autowired private MockMvc mockMvc;
  @Autowired private VesselRepository vesselRepository;
  @Autowired private VoyagePriceRepository voyagePriceRepository;
  @Autowired private ContainerRepository containerRepository;
  @Autowired private FreightOrderRepository freightOrderRepository;
  @Autowired private CustomerRepository customerRepository;
  @Autowired private AgentRepository agentRepository;
  @Autowired private FreightOrderRepository freightOrderRepository;
  @Autowired private ContainerRepository containerRepository;
  @Autowired private CustomerRepository customerRepository;

  private Vessel vessel;
  private Port arrivalPort;
  private Port departurePort;
  private Voyage voyage;
  private Agent agent;
  @Autowired private ObjectMapper objectMapper;
  private Container container20;
  private Container container40;
  private Customer customer;

  @BeforeEach
  void setUp() {
    freightOrderRepository.deleteAll();
    voyagePriceRepository.deleteAll();
    voyageRepository.deleteAll();
    containerRepository.deleteAll();
    customerRepository.deleteAll();
    vesselRepository.deleteAll();
    portRepository.deleteAll();
    agentRepository.deleteAll();
    containerRepository.deleteAll();
    customerRepository.deleteAll();

    Port port = new Port("TGKRY", "kalgary", "Togo");
    Port port2 = new Port("JPTKY", "tokyo", "Japan");
    Vessel vessel = new Vessel("SeeFox", "111", 4);
    departurePort = portRepository.save(port);
    arrivalPort = portRepository.save(port2);
    this.vessel = vesselRepository.save(vessel);
    Voyage voyage = new Voyage();
    voyage.setVoyageNumber("E-228");
    voyage.setVessel(vessel);
    voyage.setArrivalTime(LocalDateTime.of(2026, 11, 14, 6, 23));
    voyage.setDepartureTime(LocalDateTime.now());
    voyage.setDeparturePort(port);
    voyage.setArrivalPort(port2);
    voyage.setMaxCapacityTeu(vessel.getCapacityTeu());
    voyage.setBookingOpen(true);
    this.voyage = voyageRepository.save(voyage);

    container20 =
        containerRepository.save(
            new Container("C20", ContainerSize.TWENTY_FOOT, ContainerType.DRY));

    container40 =
        containerRepository.save(new Container("C40", ContainerSize.FORTY_FOOT, ContainerType.DRY));

    Customer c = new Customer();
    c.setCompanyName("Test Co");
    c.setContactName("John Doe");
    c.setEmail("john@test.com");
    customer = customerRepository.save(c);

    agent = new Agent();
    agent.setActive(true);
    agent.setName("Test Agent");
    agent.setEmail("agent@somewhere.com");
    agent.setType(AgentType.INTERNAL);
    agent.setCommissionPercent(BigDecimal.TEN);
    agentRepository.save(agent);
  }

  @Test
  @DisplayName("GET: /api/v1/voyages -> Ok")
  public void getAll() throws Exception {
    mockMvc
        .perform(MockMvcRequestBuilders.get("/api/v1/voyages"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray());
  }

  @Test
  @DisplayName("GET: /api/v1/voyages -> Ok")
  public void getAllByStatus() throws Exception {
    mockMvc
        .perform(
            MockMvcRequestBuilders.get("/api/v1/voyages")
                .param("status", voyage.getStatus().toString()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray());
  }

  @Test
  @DisplayName("GET: /api/v1/voyages/{voyageId} -> Ok")
  public void getByIdFound() throws Exception {
    mockMvc
        .perform(MockMvcRequestBuilders.get("/api/v1/voyages/" + voyage.getId()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isMap());
  }

  @Test
  @DisplayName("GET: /api/v1/voyages/{voyageId} -> 404: not found")
  public void getByIdNotFound() throws Exception {
    mockMvc
        .perform(MockMvcRequestBuilders.get("/api/v1/voyages/" + (int) (Math.random() * 1000)))
        .andExpect(status().is4xxClientError())
        .andExpect(
            MockMvcResultMatchers.content()
                .string(CoreMatchers.containsStringIgnoringCase("voyage not found")));
  }

  @Test
  @DisplayName("POST: /api/v1/voyages -> 201 Created")
  public void creationSuccessfully() throws Exception {
    CreateVoyageRequest voyageRequest = new CreateVoyageRequest();
    voyageRequest.setVesselId(this.vessel.getId());
    voyageRequest.setVoyageNumber("E-22I");
    voyageRequest.setDeparturePortId(this.departurePort.getId());
    voyageRequest.setArrivalPortId(this.arrivalPort.getId());
    voyageRequest.setDepartureTime(LocalDateTime.of(2026, 12, 12, 12, 0));
    voyageRequest.setArrivalTime(LocalDateTime.of(2026, 12, 12, 22, 0));
    mockMvc
        .perform(
            post("/api/v1/voyages")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(voyageRequest)))
        .andExpect(status().isCreated());
  }

  @Test
  @DisplayName("POST: /api/v1/voyages -> 400 BadRequest")
  public void creationFailedWithInvalidArrivalTime() throws Exception {
    CreateVoyageRequest voyageRequest = new CreateVoyageRequest();
    voyageRequest.setVesselId(this.vessel.getId());
    voyageRequest.setDeparturePortId(this.departurePort.getId());
    voyageRequest.setArrivalPortId(this.arrivalPort.getId());
    voyageRequest.setDepartureTime(LocalDateTime.of(2026, 12, 12, 12, 0));
    voyageRequest.setArrivalTime(LocalDateTime.of(2026, 12, 12, 11, 0));
    mockMvc
        .perform(post("/api/v1/voyages").content(objectMapper.writeValueAsString(voyageRequest)))
        .andExpect(status().is4xxClientError());
  }

  @Test
  @DisplayName("POST: /api/v1/voyages -> 400 BadRequest")
  public void creationFailedWithInvalidDepartureTime() throws Exception {
    CreateVoyageRequest voyageRequest = new CreateVoyageRequest();
    voyageRequest.setVesselId(this.vessel.getId());
    voyageRequest.setDeparturePortId(this.departurePort.getId());
    voyageRequest.setArrivalPortId(this.arrivalPort.getId());
    voyageRequest.setDepartureTime(LocalDateTime.of(2026, 2, 14, 12, 0));
    voyageRequest.setArrivalTime(LocalDateTime.of(2026, 12, 12, 11, 0));
    mockMvc
        .perform(post("/api/v1/voyages").content(objectMapper.writeValueAsString(voyageRequest)))
        .andExpect(status().is4xxClientError());
  }

  @Test
  @DisplayName("Patch: /api/v1/voyages/{voyageId}/{status}")
  public void updateStatus() throws Exception {
    mockMvc
        .perform(
            patch(
                "/api/v1/voyages/"
                    + this.voyage.getId().toString()
                    + "/"
                    + this.voyage.getStatus().name()))
        .andExpect(status().isOk());
  }

  @Test
  @DisplayName("POST /api/v1/voyages/{voyageId}/prices → 201 Created")
  void createVoyagePrice_returnsCreated() throws Exception {
    VoyagePriceRequest request = new VoyagePriceRequest();
    request.setContainerSize(ContainerSize.FORTY_FOOT);
    request.setBasePriceUsd(BigDecimal.valueOf(1500));

    mockMvc
        .perform(
            post("/api/v1/voyages/" + voyage.getId() + "/prices")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.voyageId").value(voyage.getId()))
        .andExpect(jsonPath("$.containerSize").value("FORTY_FOOT"))
        .andExpect(jsonPath("$.basePriceUsd").value(1500));
  }

  @Test
  @DisplayName("POST /api/v1/voyages/{voyageId}/prices → 409 Conflict if price exists")
  void createVoyagePrice_returnsConflict() throws Exception {
    VoyagePrice existingPrice = new VoyagePrice();
    existingPrice.setVoyage(voyage);
    existingPrice.setContainerSize(ContainerSize.FORTY_FOOT);
    existingPrice.setBasePriceUsd(BigDecimal.valueOf(1500));
    voyagePriceRepository.save(existingPrice);

    VoyagePriceRequest request = new VoyagePriceRequest();
    request.setContainerSize(ContainerSize.FORTY_FOOT);
    request.setBasePriceUsd(BigDecimal.valueOf(2000));

    mockMvc
        .perform(
            post("/api/v1/voyages/" + voyage.getId() + "/prices")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isConflict());
  }

  @Test
  @DisplayName("POST /api/v1/voyages/{voyageId}/prices → 404 Not Found if voyage does not exist")
  void createVoyagePrice_returnsNotFound() throws Exception {

    VoyagePriceRequest request = new VoyagePriceRequest();
    request.setContainerSize(ContainerSize.TWENTY_FOOT);
    request.setBasePriceUsd(BigDecimal.valueOf(1000));

    mockMvc
        .perform(
            post("/api/v1/voyages/99999/prices")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("POST /api/v1/voyages/{id}/prices → 400 Bad Request for invalid input")
  void createVoyagePrice_returnsBadRequest() throws Exception {

    VoyagePriceRequest request = new VoyagePriceRequest();
    request.setContainerSize(null);
    request.setBasePriceUsd(BigDecimal.valueOf(-500));

    mockMvc
        .perform(
            post("/api/v1/voyages/" + voyage.getId() + "/prices")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("GET /api/v1/voyages/{voyageId}/prices → 200 OK with content")
  void getVoyagePrices_returnsPage() throws Exception {

    VoyagePrice price1 = new VoyagePrice();
    price1.setVoyage(voyage);
    price1.setContainerSize(ContainerSize.TWENTY_FOOT);
    price1.setBasePriceUsd(BigDecimal.valueOf(1000));
    voyagePriceRepository.save(price1);

    VoyagePrice price2 = new VoyagePrice();
    price2.setVoyage(voyage);
    price2.setContainerSize(ContainerSize.FORTY_FOOT);
    price2.setBasePriceUsd(BigDecimal.valueOf(1500));
    voyagePriceRepository.save(price2);

    mockMvc
        .perform(
            get("/api/v1/voyages/{id}/prices", voyage.getId())
                .param("page", "0")
                .param("size", "20"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray())
        .andExpect(jsonPath("$.content.length()").value(2))
        .andExpect(jsonPath("$.content[0].voyageId").value(voyage.getId()))
        .andExpect(jsonPath("$.content[0].basePriceUsd").value(1000))
        .andExpect(jsonPath("$.content[1].basePriceUsd").value(1500))
        .andExpect(jsonPath("$.page").value(0))
        .andExpect(jsonPath("$.size").value(20))
        .andExpect(jsonPath("$.totalElements").value(2))
        .andExpect(jsonPath("$.totalPages").value(1));
  }

  @Test
  @DisplayName("GET /api/v1/voyages/{voyageId}/prices → 200 OK with empty content")
  void getVoyagePrices_returnsEmptyPage() throws Exception {

    mockMvc
        .perform(
            get("/api/v1/voyages/{id}/prices", voyage.getId())
                .param("page", "0")
                .param("size", "20"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray())
        .andExpect(jsonPath("$.content.length()").value(0))
        .andExpect(jsonPath("$.totalElements").value(0));
  }

  @Test
  @DisplayName("GET /api/v1/voyages/{voyageId}/prices → 404 Not Found if voyage does not exist")
  void getVoyagePrices_returnsNotFound() throws Exception {

    mockMvc
        .perform(get("/api/v1/voyages/{id}/prices", 99999L).param("page", "0").param("size", "20"))
        .andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("GET /voyages/{id}/load → returns correct summary")
  void getLoadSummary_ok() throws Exception {

    FreightOrder order1 = new FreightOrder();
    order1.setVoyage(voyage);
    order1.setContainer(container20);
    order1.setCustomer(customer);
    order1.setOrderedBy("ops");
    order1.setBasePriceUsd(BigDecimal.valueOf(1000));
    order1.setDiscountPercent(BigDecimal.ZERO);
    order1.setFinalPrice(BigDecimal.valueOf(1000));
    order1.setStatus(OrderStatus.PENDING);

    FreightOrder order2 = new FreightOrder();
    order2.setVoyage(voyage);
    order2.setContainer(container40);
    order2.setCustomer(customer);
    order2.setOrderedBy("ops");
    order2.setBasePriceUsd(BigDecimal.valueOf(2000));
    order2.setDiscountPercent(BigDecimal.ZERO);
    order2.setFinalPrice(BigDecimal.valueOf(2000));
    order2.setStatus(OrderStatus.CONFIRMED);

    freightOrderRepository.saveAll(List.of(order1, order2));

    mockMvc
        .perform(get("/api/v1/voyages/" + voyage.getId() + "/load"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.voyageNumber").value("E-228"))
        .andExpect(jsonPath("$.maxCapacityTeu").value(4))
        .andExpect(jsonPath("$.currentLoadTeu").value(3))
        .andExpect(jsonPath("$.containerCount").value(2))
        .andExpect(jsonPath("$.bookingOpen").value(true))
        .andExpect(jsonPath("$.utilizationPercent").value(75.0));
  }

  @Test
  @DisplayName("GET /voyages/{id}/load → empty voyage")
  void getLoadSummary_empty() throws Exception {

    mockMvc
        .perform(get("/api/v1/voyages/" + voyage.getId() + "/load"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.voyageNumber").value("E-228"))
        .andExpect(jsonPath("$.maxCapacityTeu").value(4))
        .andExpect(jsonPath("$.currentLoadTeu").value(0))
        .andExpect(jsonPath("$.containerCount").value(0))
        .andExpect(jsonPath("$.bookingOpen").value(true))
        .andExpect(jsonPath("$.utilizationPercent").value(0.0));
  }

  @Test
  @DisplayName("GET /voyages/{id}/load → ignores cancelled orders")
  void getLoadSummary_ignoresCancelled() throws Exception {

    FreightOrder order1 = new FreightOrder();
    order1.setVoyage(voyage);
    order1.setContainer(container20);
    order1.setCustomer(customer);
    order1.setOrderedBy("ops");
    order1.setBasePriceUsd(BigDecimal.valueOf(1000));
    order1.setDiscountPercent(BigDecimal.ZERO);
    order1.setFinalPrice(BigDecimal.valueOf(1000));
    order1.setStatus(OrderStatus.CANCELLED);

    FreightOrder order2 = new FreightOrder();
    order2.setVoyage(voyage);
    order2.setContainer(container40);
    order2.setCustomer(customer);
    order2.setOrderedBy("ops");
    order2.setBasePriceUsd(BigDecimal.valueOf(2000));
    order2.setDiscountPercent(BigDecimal.ZERO);
    order2.setFinalPrice(BigDecimal.valueOf(2000));
    order2.setStatus(OrderStatus.CONFIRMED);

    freightOrderRepository.saveAll(List.of(order1, order2));

    mockMvc
        .perform(get("/api/v1/voyages/" + voyage.getId() + "/load"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.voyageNumber").value("E-228"))
        .andExpect(jsonPath("$.maxCapacityTeu").value(4))
        .andExpect(jsonPath("$.currentLoadTeu").value(2))
        .andExpect(jsonPath("$.containerCount").value(1))
        .andExpect(jsonPath("$.bookingOpen").value(true))
        .andExpect(jsonPath("$.utilizationPercent").value(50.0));
  }

  @Test
  @DisplayName("GET /voyages/{id}/load → 404")
  void getLoadSummary_notFound() throws Exception {

    mockMvc.perform(get("/api/v1/voyages/999999/load")).andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("PATCH /booking-status → closes booking")
  void updateBookingStatus_closeBooking() throws Exception {

    BookingStatusUpdateRequest request = new BookingStatusUpdateRequest();
    request.setBookingOpen(false);

    mockMvc
        .perform(
            patch("/api/v1/voyages/" + voyage.getId() + "/booking-status")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.bookingOpen").value(false));
  }

  @Test
  @DisplayName("PATCH /booking-status → opens booking")
  void updateBookingStatus_openBooking() throws Exception {
    voyage.setBookingOpen(false);
    voyageRepository.save(voyage);

    BookingStatusUpdateRequest request = new BookingStatusUpdateRequest();
    request.setBookingOpen(true);

    mockMvc
        .perform(
            patch("/api/v1/voyages/" + voyage.getId() + "/booking-status")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.bookingOpen").value(true));
  }

  @Test
  @DisplayName("PATCH /booking-status → 404 when voyage not found")
  void updateBookingStatus_notFound() throws Exception {

    BookingStatusUpdateRequest request = new BookingStatusUpdateRequest();
    request.setBookingOpen(false);

    mockMvc
        .perform(
            patch("/api/v1/voyages/999999/booking-status")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("GET /api/v1/voyages/{voyageId}/containers → 200 OK with containers")
  void getContainersByVoyageId_returnsContainers() throws Exception {
    Container container =
        containerRepository.save(
            new Container("MSCU1234567", ContainerSize.TWENTY_FOOT, ContainerType.DRY));

    Customer customer =
        customerRepository.save(new Customer("Acme Corp", "John Doe", "john@acme.com"));

    FreightOrder order = new FreightOrder();
    order.setVoyage(voyage);
    order.setContainer(container);
    order.setCustomer(customer);
    order.setOrderedBy("ops-team");
    order.setBasePriceUsd(BigDecimal.valueOf(1000));
    order.setDiscountPercent(BigDecimal.ZERO);
    order.setFinalPrice(BigDecimal.valueOf(1000));
    order.setAgent(agent);

    freightOrderRepository.save(order);

    mockMvc
        .perform(
            get("/api/v1/voyages/{voyageId}/containers", voyage.getId())
                .param("page", "0")
                .param("size", "20"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray())
        .andExpect(jsonPath("$.content.length()").value(1))
        .andExpect(jsonPath("$.content[0].containerCode").value("MSCU1234567"))
        .andExpect(jsonPath("$.content[0].containerSize").value("TWENTY_FOOT"))
        .andExpect(jsonPath("$.content[0].containerType").value("DRY"))
        .andExpect(jsonPath("$.content[0].orderedBy").value("ops-team"))
        .andExpect(jsonPath("$.content[0].orderStatus").value("PENDING"));
  }

  @Test
  @DisplayName("GET /api/v1/voyages/{voyageId}/containers → 200 OK with empty list")
  void getContainersByVoyageId_returnsEmptyList() throws Exception {
    mockMvc
        .perform(
            get("/api/v1/voyages/{voyageId}/containers", voyage.getId())
                .param("page", "0")
                .param("size", "20"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray())
        .andExpect(jsonPath("$.content.length()").value(0))
        .andExpect(jsonPath("$.totalElements").value(0));
  }

  @Test
  @DisplayName("GET /api/v1/voyages/{voyageId}/containers → 404 Not Found")
  void getContainersByVoyageId_returnsNotFound() throws Exception {
    mockMvc
        .perform(
            get("/api/v1/voyages/{voyageId}/containers", 99999L)
                .param("page", "0")
                .param("size", "20"))
        .andExpect(status().isNotFound());
  }
}
