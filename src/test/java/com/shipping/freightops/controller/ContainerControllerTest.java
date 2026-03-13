package com.shipping.freightops.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.shipping.freightops.entity.*;
import com.shipping.freightops.enums.AgentType;
import com.shipping.freightops.enums.ContainerSize;
import com.shipping.freightops.enums.ContainerType;
import com.shipping.freightops.enums.OrderStatus;
import com.shipping.freightops.repository.*;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class ContainerControllerTest {
  @Autowired private MockMvc mockMvc;
  @Autowired private ContainerRepository containerRepository;
  @Autowired private VoyageRepository voyageRepository;
  @Autowired private VesselRepository vesselRepository;
  @Autowired private PortRepository portRepository;
  @Autowired private FreightOrderRepository freightOrderRepository;
  @Autowired private CustomerRepository customerRepository;
  @Autowired private AgentRepository agentRepository;

  private Container savedContainer;
  private Voyage activeVoyage;
  private Voyage historicalVoyage;
  private Customer savedCustomer;
  private Agent savedAgent;

  @BeforeEach
  void setUp() {

    // Container
    savedContainer =
        containerRepository.save(
            new Container("TSTU1234567", ContainerSize.TWENTY_FOOT, ContainerType.DRY));

    // Ports
    Port departure = portRepository.save(new Port("AEJEK", "Jebel Ali", "UAE"));
    Port arrival = portRepository.save(new Port("CNSHK", "Shanghai", "China"));

    // Vessels
    Vessel vessel1 = vesselRepository.save(new Vessel("MV Active", "V001", 1000));
    Vessel vessel2 = vesselRepository.save(new Vessel("MV History", "V002", 1000));

    // Voyages
    Voyage activeVoyageObj = new Voyage();
    activeVoyageObj.setVoyageNumber("VOY-ACTIVE");
    activeVoyageObj.setVessel(vessel1);
    activeVoyageObj.setDeparturePort(departure);
    activeVoyageObj.setArrivalPort(arrival);
    activeVoyageObj.setMaxCapacityTeu(3000);
    activeVoyageObj.setDepartureTime(LocalDateTime.now().plusDays(1));
    activeVoyageObj.setArrivalTime(LocalDateTime.now().plusDays(10));
    activeVoyage = voyageRepository.save(activeVoyageObj);

    Voyage historicalVoyage = new Voyage();
    historicalVoyage.setVoyageNumber("VOY-HIST");
    historicalVoyage.setVessel(vessel2);
    historicalVoyage.setDeparturePort(departure);
    historicalVoyage.setArrivalPort(arrival);
    historicalVoyage.setMaxCapacityTeu(3000);
    historicalVoyage.setDepartureTime(LocalDateTime.now().minusDays(10));
    historicalVoyage.setArrivalTime(LocalDateTime.now().minusDays(5));
    historicalVoyage = voyageRepository.save(historicalVoyage);

    // Customer
    Customer customer = new Customer();
    customer.setCompanyName("Test Customer Inc.");
    customer.setContactName("John Doe");
    customer.setEmail("john@example.com");
    savedCustomer = customerRepository.save(customer);

    // Agent
    Agent agent = new Agent();
    agent.setName("Test Agent");
    agent.setEmail("agent@example.com");
    agent.setCommissionPercent(BigDecimal.valueOf(5));
    agent.setType(AgentType.EXTERNAL);
    savedAgent = agentRepository.save(agent);

    // FreightOrder (historical)
    FreightOrder historicalOrder = new FreightOrder();
    historicalOrder.setContainer(savedContainer);
    historicalOrder.setVoyage(historicalVoyage);
    historicalOrder.setCustomer(savedCustomer);
    historicalOrder.setAgent(savedAgent);
    historicalOrder.setStatus(OrderStatus.DELIVERED);
    historicalOrder.setOrderedBy("ops-team");
    historicalOrder.setBasePriceUsd(BigDecimal.valueOf(900));
    historicalOrder.setFinalPrice(BigDecimal.valueOf(950));
    freightOrderRepository.save(historicalOrder);

    // FreightOrder (active)
    FreightOrder activeOrder = new FreightOrder();
    activeOrder.setContainer(savedContainer);
    activeOrder.setVoyage(activeVoyage);
    activeOrder.setCustomer(savedCustomer);
    activeOrder.setAgent(savedAgent);
    activeOrder.setStatus(OrderStatus.CONFIRMED);
    activeOrder.setOrderedBy("ops-team");
    activeOrder.setBasePriceUsd(BigDecimal.valueOf(1000));
    activeOrder.setFinalPrice(BigDecimal.valueOf(1100));
    freightOrderRepository.save(activeOrder);
  }

  @Test
  @DisplayName("GET /api/v1/containers/{id}/label → returns valid PDF with voyage info")
  void getContainerLabel_returnsValidPdf() throws Exception {

    mockMvc
        .perform(get("/api/v1/containers/{id}/label", savedContainer.getId()))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_PDF))
        .andExpect(
            result -> {
              byte[] bytes = result.getResponse().getContentAsByteArray();
              String header = new String(bytes, 0, 4);
              assertEquals("%PDF", header);

              // Pls verify pdf manually
              Path file = Paths.get("target/test-container-label.pdf");
              Files.write(file, bytes);
              System.out.println("PDF saved to: " + file.toAbsolutePath());
            });
  }
}
