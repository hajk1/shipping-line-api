package com.shipping.freightops.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.PdfTextExtractor;
import com.shipping.freightops.entity.Agent;
import com.shipping.freightops.entity.Container;
import com.shipping.freightops.entity.Customer;
import com.shipping.freightops.entity.FreightOrder;
import com.shipping.freightops.entity.Port;
import com.shipping.freightops.entity.Vessel;
import com.shipping.freightops.entity.Voyage;
import com.shipping.freightops.entity.VoyagePrice;
import com.shipping.freightops.enums.AgentType;
import com.shipping.freightops.enums.ContainerSize;
import com.shipping.freightops.enums.ContainerType;
import com.shipping.freightops.enums.OrderStatus;
import com.shipping.freightops.enums.VoyageStatus;
import com.shipping.freightops.repository.AgentRepository;
import com.shipping.freightops.repository.ContainerRepository;
import com.shipping.freightops.repository.CustomerRepository;
import com.shipping.freightops.repository.FreightOrderRepository;
import com.shipping.freightops.repository.InvoiceRepository;
import com.shipping.freightops.repository.PortRepository;
import com.shipping.freightops.repository.VesselRepository;
import com.shipping.freightops.repository.VoyagePriceRepository;
import com.shipping.freightops.repository.VoyageRepository;
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
class InvoiceControllerTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private PortRepository portRepository;
  @Autowired private VesselRepository vesselRepository;
  @Autowired private ContainerRepository containerRepository;
  @Autowired private CustomerRepository customerRepository;
  @Autowired private VoyageRepository voyageRepository;
  @Autowired private VoyagePriceRepository voyagePriceRepository;
  @Autowired private InvoiceRepository invoiceRepository;
  @Autowired private FreightOrderRepository freightOrderRepository;
  @Autowired private AgentRepository agentRepository;

  private Long deliveredOrderId;

  @BeforeEach
  void setUp() {
    invoiceRepository.deleteAll();
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
    Vessel vessel = vesselRepository.save(new Vessel("MV Invoice", "1122334", 3000));

    Voyage voyage = new Voyage();
    voyage.setVoyageNumber("INV-VOY-001");
    voyage.setVessel(vessel);
    voyage.setDeparturePort(departure);
    voyage.setArrivalPort(arrival);
    voyage.setDepartureTime(LocalDateTime.now().minusDays(10));
    voyage.setArrivalTime(LocalDateTime.now().minusDays(3));
    voyage.setStatus(VoyageStatus.COMPLETED);
    voyage.setMaxCapacityTeu(vessel.getCapacityTeu());
    voyage.setBookingOpen(false);
    Voyage savedVoyage = voyageRepository.save(voyage);

    VoyagePrice price = new VoyagePrice();
    price.setVoyage(savedVoyage);
    price.setContainerSize(ContainerSize.TWENTY_FOOT);
    price.setBasePriceUsd(BigDecimal.valueOf(1000));
    voyagePriceRepository.save(price);

    Container container =
        containerRepository.save(
            new Container("INVT1234567", ContainerSize.TWENTY_FOOT, ContainerType.DRY));

    Customer customer = new Customer();
    customer.setCompanyName("Invoice Test Corp");
    customer.setContactName("Jane Doe");
    customer.setEmail("jane@invoicetest.com");
    customer.setAddress("123 Test Street");
    Customer savedCustomer = customerRepository.save(customer);

    Agent agent = new Agent();
    agent.setName("Invoice Agent");
    agent.setEmail("inv-agent@test.com");
    agent.setCommissionPercent(BigDecimal.valueOf(5));
    agent.setType(AgentType.INTERNAL);
    agent.setActive(true);
    agentRepository.save(agent);

    FreightOrder order = new FreightOrder();
    order.setVoyage(savedVoyage);
    order.setContainer(container);
    order.setCustomer(savedCustomer);
    order.setAgent(agent);
    order.setStatus(OrderStatus.DELIVERED);
    order.setBasePriceUsd(BigDecimal.valueOf(1000));
    order.setDiscountPercent(BigDecimal.valueOf(10));
    order.setFinalPrice(BigDecimal.valueOf(900));
    order.setDiscountReason("Loyalty discount");
    order.setOrderedBy("test-user");
    order.setNotes("test notes");
    deliveredOrderId = freightOrderRepository.save(order).getId();
  }

  @Test
  @DisplayName("GET /api/v1/freight-orders/{id}/invoice → returns valid PDF with tracking QR")
  void generateInvoice_returnsValidPdfWithQr() throws Exception {
    mockMvc
        .perform(get("/api/v1/freight-orders/{id}/invoice", deliveredOrderId))
        .andExpect(status().isOk())
        .andExpect(
            result -> {
              byte[] bytes = result.getResponse().getContentAsByteArray();
              assertTrue(bytes.length > 0, "PDF must not be empty");
              assertEquals("%PDF", new String(bytes, 0, 4), "Response must be a valid PDF");

              PdfReader reader = new PdfReader(bytes);
              String pageText = PdfTextExtractor.getTextFromPage(reader, 1);
              reader.close();

              assertTrue(
                  pageText.contains("Scan to track your shipment"),
                  "PDF must contain QR tracking label");
            });
  }
}
