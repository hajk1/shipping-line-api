package com.shipping.freightops.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shipping.freightops.dto.CreateVoyageRequest;
import com.shipping.freightops.dto.VoyagePriceRequest;
import com.shipping.freightops.entity.*;
import com.shipping.freightops.enums.ContainerSize;
import com.shipping.freightops.enums.ContainerType;
import com.shipping.freightops.repository.*;
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

  private Vessel vessel;
  private Port arrivalPort;
  private Port departurePort;
  private Voyage voyage;
  @Autowired private ObjectMapper objectMapper;

  @BeforeEach
  void setUp() {
    freightOrderRepository.deleteAll();
    voyagePriceRepository.deleteAll();
    voyageRepository.deleteAll();
    containerRepository.deleteAll();
    customerRepository.deleteAll();
    vesselRepository.deleteAll();
    portRepository.deleteAll();
    Port port = new Port("TGKRY", "kalgary", "Togo");
    Port port2 = new Port("JPTKY", "tokyo", "Japan");
    Vessel vessel = new Vessel("SeeFox", "111", 1);
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
    this.voyage = voyageRepository.save(voyage);
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
            MockMvcRequestBuilders.patch(
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
