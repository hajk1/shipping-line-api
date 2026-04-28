package com.shipping.freightops.service;

import com.itextpdf.text.DocumentException;
import com.shipping.freightops.entity.FreightOrder;
import com.shipping.freightops.enums.OrderStatus;
import com.shipping.freightops.repository.FreightOrderRepository;
import java.io.FileNotFoundException;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class InvoiceEmailService {

  private final FreightOrderRepository freightOrderRepository;

  private final InvoiceService invoiceService;

  private final EmailService emailService;

  @Transactional
  public void sendInvoiceToCustomer(Long orderId) throws DocumentException, FileNotFoundException {
    FreightOrder order = getOrderOrThrow(orderId);

    validateDelivered(order);

    byte[] invoicePdf = invoiceService.generateInvoice(orderId);

    sendInvoiceEmail(order, invoicePdf);

    log.info("Invoice sent to customer: {}", order.getCustomer().getEmail());
  }

  /** Fetch order or throw if not found */
  private FreightOrder getOrderOrThrow(Long orderId) {
    return freightOrderRepository
        .findById(orderId)
        .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));
  }

  /** Ensure order is in DELIVERED status */
  private void validateDelivered(FreightOrder order) {
    if (order.getStatus() != OrderStatus.DELIVERED) {
      throw new IllegalStateException(
          "Cannot send invoice for order in "
              + order.getStatus()
              + " status. Order must be DELIVERED.");
    }
  }

  /** Build email request with subject, body, and attachment */
  private void sendInvoiceEmail(FreightOrder order, byte[] invoicePdf) {
    String invoiceNo = generateInvoiceNumber(order);
    String voyageNo = generateVoyageNumber(order);
    String subject = String.format("Invoice %s - Voyage %s", invoiceNo, voyageNo);
    String body =
        String.format(
            "Dear %s,\n\nPlease find your invoice attached. Thank you for your business.\n\nBest regards,\nAPGL Freight Operations",
            order.getCustomer().getCompanyName());

    emailService.sendEmailWithAttachment(
        order.getCustomer().getEmail(),
        subject,
        body,
        invoiceNo + ".pdf",
        invoicePdf,
        "application/pdf");
  }

  /** Generate invoice number based on year and order ID */
  private String generateInvoiceNumber(FreightOrder order) {
    return String.format("INV-%d-%05d", LocalDate.now().getYear(), order.getId());
  }

  /** Generate voyage number in format VOY-XXXXX */
  private String generateVoyageNumber(FreightOrder order) {
    return String.format("VOY-%05d", order.getVoyage().getId());
  }
}
