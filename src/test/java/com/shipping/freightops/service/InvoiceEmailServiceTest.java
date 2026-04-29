package com.shipping.freightops.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.shipping.freightops.entity.Agent;
import com.shipping.freightops.entity.Container;
import com.shipping.freightops.entity.Customer;
import com.shipping.freightops.entity.FreightOrder;
import com.shipping.freightops.entity.Port;
import com.shipping.freightops.entity.Vessel;
import com.shipping.freightops.entity.Voyage;
import com.shipping.freightops.enums.AgentType;
import com.shipping.freightops.enums.ContainerSize;
import com.shipping.freightops.enums.ContainerType;
import com.shipping.freightops.enums.OrderStatus;
import com.shipping.freightops.repository.FreightOrderRepository;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class InvoiceEmailServiceTest {

  @Mock private InvoiceService invoiceService;

  @Mock private FreightOrderRepository freightOrderRepository;

  @Mock private EmailService emailService;

  @InjectMocks private InvoiceEmailService invoiceEmailService;

  @Nested
  @DisplayName("sendInvoiceToCustomer")
  class SendInvoiceToCustomer {

    @Test
    @DisplayName("sends invoice PDF to customer email when order DELIVERED")
    void sendsInvoiceWhenDelivered() throws Exception {
      FreightOrder order = buildDeliveredOrder();
      byte[] pdfBytes = {0x25, 0x50, 0x44, 0x46}; // PDF magic bytes

      when(freightOrderRepository.findById(1L)).thenReturn(Optional.of(order));
      when(invoiceService.generateInvoice(1L)).thenReturn(pdfBytes);

      invoiceEmailService.sendInvoiceToCustomer(1L);

      ArgumentCaptor<String> toCaptor = ArgumentCaptor.forClass(String.class);
      ArgumentCaptor<String> subjectCaptor = ArgumentCaptor.forClass(String.class);
      ArgumentCaptor<String> bodyCaptor = ArgumentCaptor.forClass(String.class);
      ArgumentCaptor<String> attachmentNameCaptor = ArgumentCaptor.forClass(String.class);
      ArgumentCaptor<byte[]> contentCaptor = ArgumentCaptor.forClass(byte[].class);
      ArgumentCaptor<String> mimeTypeCaptor = ArgumentCaptor.forClass(String.class);

      verify(emailService)
          .sendEmailWithAttachment(
              toCaptor.capture(),
              subjectCaptor.capture(),
              bodyCaptor.capture(),
              attachmentNameCaptor.capture(),
              contentCaptor.capture(),
              mimeTypeCaptor.capture());

      assertThat(toCaptor.getValue()).isEqualTo("customer@example.com");
      assertThat(subjectCaptor.getValue()).contains("Invoice").contains("Voyage");
      assertThat(bodyCaptor.getValue()).contains("Acme Corp");
      assertThat(attachmentNameCaptor.getValue()).endsWith(".pdf");
      assertThat(mimeTypeCaptor.getValue()).isEqualTo("application/pdf");
      assertThat(contentCaptor.getValue()).isNotEmpty();
    }

    @Test
    @DisplayName("throws when order not found")
    void throwsWhenOrderNotFound() {
      when(freightOrderRepository.findById(99L)).thenReturn(Optional.empty());

      assertThatThrownBy(() -> invoiceEmailService.sendInvoiceToCustomer(99L))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("Order not found");
    }

    @Test
    @DisplayName("throws when order not in DELIVERED status")
    void throwsWhenNotDelivered() {
      FreightOrder order = buildDeliveredOrder();
      order.setStatus(OrderStatus.IN_TRANSIT);
      when(freightOrderRepository.findById(1L)).thenReturn(Optional.of(order));

      assertThatThrownBy(() -> invoiceEmailService.sendInvoiceToCustomer(1L))
          .isInstanceOf(IllegalStateException.class)
          .hasMessageContaining("Cannot send invoice");
    }
  }

  // ── HELPERS ────────────────────────────────────────────────

  private FreightOrder buildDeliveredOrder() {
    Customer customer = new Customer();
    customer.setId(1L);
    customer.setCompanyName("Acme Corp");
    customer.setEmail("customer@example.com");
    customer.setAddress("123 Main St");

    Port port = new Port();
    port.setId(1L);
    port.setName("Port of Hamburg");

    Vessel vessel = new Vessel();
    vessel.setId(1L);
    vessel.setName("Test Vessel");
    vessel.setImoNumber("1234567");
    vessel.setCapacityTeu(1000);

    Voyage voyage = new Voyage();
    voyage.setId(1L);
    voyage.setVoyageNumber("V001");
    voyage.setDeparturePort(port);
    voyage.setArrivalPort(port);
    voyage.setVessel(vessel);

    Container container = new Container();
    container.setId(1L);
    container.setContainerCode("TEST123456");
    container.setSize(ContainerSize.TWENTY_FOOT);
    container.setType(ContainerType.DRY);

    Agent agent = new Agent();
    agent.setId(1L);
    agent.setName("Test Agent");
    agent.setEmail("agent@example.com");
    agent.setType(AgentType.INTERNAL);

    FreightOrder order = new FreightOrder();
    order.setId(1L);
    order.setCustomer(customer);
    order.setVoyage(voyage);
    order.setContainer(container);
    order.setAgent(agent);
    order.setOrderedBy("ops-team");
    order.setStatus(OrderStatus.DELIVERED);
    order.setBasePriceUsd(new BigDecimal("1000.00"));
    order.setFinalPrice(new BigDecimal("1000.00"));
    order.setDiscountPercent(BigDecimal.ZERO);

    return order;
  }
}
