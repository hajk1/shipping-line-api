package com.shipping.freightops.service;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.shipping.freightops.entity.FreightOrder;
import com.shipping.freightops.entity.Invoice;
import com.shipping.freightops.enums.OrderStatus;
import com.shipping.freightops.repository.FreightOrderRepository;
import com.shipping.freightops.repository.InvoiceRepository;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class InvoiceService {

  @Autowired private InvoiceRepository invoiceRepository;

  @Autowired private FreightOrderRepository freightOrderRepository;

  private static final BaseColor BRAND_TEAL = new BaseColor(95, 134, 112);
  private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

  public byte[] generateInvoice(Long orderId) throws DocumentException, FileNotFoundException {
    FreightOrder order =
        freightOrderRepository
            .findById(orderId)
            .orElseThrow(() -> new IllegalArgumentException("Order not found"));

    if (!order.getStatus().equals(OrderStatus.DELIVERED)) {
      throw new IllegalStateException("Invoice requires DELIVERED status");
    }

    // Pattern: INV-2025-00042
    String invoiceNo = String.format("INV-%d-%05d", LocalDate.now().getYear(), order.getId());

    Document document = new Document(PageSize.A4, 36, 36, 50, 36);
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    PdfWriter.getInstance(document, out);
    document.open();

    // Fonts
    Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 24, BRAND_TEAL);
    Font headBold = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, BRAND_TEAL);
    Font normal = FontFactory.getFont(FontFactory.HELVETICA, 10);
    Font whiteHead = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, BaseColor.WHITE);

    // --- 1. Header ---
    PdfPTable header = new PdfPTable(2);
    header.setWidthPercentage(100);
    header.addCell(
        getBorderlessCell(
            "APGL SHIPPING LINE\nFreight Operations",
            FontFactory.getFont(FontFactory.HELVETICA, 14)));
    PdfPCell tCell = getBorderlessCell("INVOICE", titleFont);
    tCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
    header.addCell(tCell);
    document.add(header);
    document.add(new Paragraph("\n"));

    // --- 2. Customer & Dates (Auto-generated ID & Dates) ---
    PdfPTable info = new PdfPTable(2);
    info.setWidthPercentage(100);

    // Bill To: Name, Email, Address
    PdfPTable client = new PdfPTable(1);
    client.addCell(getBorderlessCell("BILL TO", headBold));
    client.addCell(
        getBorderlessCell(
            order.getCustomer().getCompanyName(),
            FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11)));
    client.addCell(getBorderlessCell(order.getCustomer().getEmail(), normal));
    client.addCell(getBorderlessCell(order.getCustomer().getAddress(), normal));
    info.addCell(new PdfPCell(client)).setBorder(Rectangle.NO_BORDER);

    // Invoice Details: ID, Order Date, Delivery Date
    PdfPTable details = new PdfPTable(2);
    addRow(details, "Invoice Number", invoiceNo, headBold, normal);
    addRow(
        details,
        "Order Date",
        LocalDate.now().format(DATE_FMT),
        headBold,
        normal); // Remplacez par order.getCreatedAt() si dispo
    addRow(details, "Delivery Date", LocalDate.now().format(DATE_FMT), headBold, normal);
    info.addCell(new PdfPCell(details)).setBorder(Rectangle.NO_BORDER);

    document.add(info);
    document.add(new Paragraph("\n\n"));

    // --- 3. Shipment: Voyage (Ports), Container (Code, Size, Type) ---
    PdfPTable shipTable = new PdfPTable(new float[] {2, 4, 2});
    shipTable.setWidthPercentage(100);

    addStyledHead(shipTable, "Voyage Details", whiteHead);
    addStyledHead(shipTable, "Container Specifications", whiteHead);
    addStyledHead(shipTable, "Route", whiteHead);

    shipTable.addCell(new Phrase(order.getVoyage().getVoyageNumber(), normal));
    shipTable.addCell(
        new Phrase(
            order.getContainer().getContainerCode()
                + " ("
                + order.getContainer().getSize()
                + " "
                + order.getContainer().getType()
                + ")",
            normal));
    shipTable.addCell(
        new Phrase(
            order.getVoyage().getDeparturePort().getName()
                + " / "
                + order.getVoyage().getArrivalPort().getName(),
            normal));

    document.add(shipTable);
    document.add(new Paragraph("\n"));

    // --- 4. Pricing: Base, Discount %, Reason, Final Price ---
    PdfPTable priceTable = new PdfPTable(2);
    priceTable.setWidthPercentage(45);
    priceTable.setHorizontalAlignment(Element.ALIGN_RIGHT);

    addPriceRow(priceTable, "Base Price", "$" + order.getBasePriceUsd(), normal, false);
    addPriceRow(
        priceTable,
        "Discount (" + order.getDiscountPercent() + "%)",
        "-$" + order.getBasePriceUsd().subtract(order.getFinalPrice()),
        normal,
        false);

    if (order.getDiscountReason() != null && !order.getDiscountReason().isEmpty()) {
      addPriceRow(
          priceTable,
          "Discount Reason",
          order.getDiscountReason(),
          FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 9),
          false);
    }

    addPriceRow(
        priceTable,
        "FINAL PRICE (USD)",
        "$" + order.getFinalPrice(),
        FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, BRAND_TEAL),
        true);

    document.add(priceTable);

    // --- 5. Company Footer (Hardcoded) ---
    Paragraph footer =
        new Paragraph(
            "\n\n\nAPGL Operations - Marseille, France\nContact: support@apgl-shipping.com | +33 4 91 00 00 00\nRegistered Carrier 8829102",
            FontFactory.getFont(FontFactory.HELVETICA, 8, BaseColor.GRAY));
    footer.setAlignment(Element.ALIGN_CENTER);
    document.add(footer);

    document.close();

    // Logique de persistance
    invoiceRepository.save(new Invoice(order, invoiceNo));

    return out.toByteArray();
  }

  // Helpers
  private void addRow(PdfPTable t, String l, String v, Font f1, Font f2) {
    PdfPCell c1 = getBorderlessCell(l + ":", f1);
    c1.setHorizontalAlignment(Element.ALIGN_RIGHT);
    t.addCell(c1);
    PdfPCell c2 = getBorderlessCell(v, f2);
    c2.setHorizontalAlignment(Element.ALIGN_RIGHT);
    t.addCell(c2);
  }

  private void addStyledHead(PdfPTable t, String txt, Font f) {
    PdfPCell c = new PdfPCell(new Phrase(txt, f));
    c.setBackgroundColor(BRAND_TEAL);
    c.setBorder(Rectangle.NO_BORDER);
    c.setPadding(8f);
    t.addCell(c);
  }

  private void addPriceRow(PdfPTable t, String l, String v, Font f, boolean b) {
    PdfPCell c1 = getBorderlessCell(l, f);
    if (b) c1.setBorder(Rectangle.TOP);
    t.addCell(c1);
    PdfPCell c2 = getBorderlessCell(v, f);
    c2.setHorizontalAlignment(Element.ALIGN_RIGHT);
    if (b) c2.setBorder(Rectangle.TOP);
    t.addCell(c2);
  }

  private PdfPCell getBorderlessCell(String txt, Font f) {
    PdfPCell c = new PdfPCell(new Phrase(txt, f));
    c.setBorder(Rectangle.NO_BORDER);
    c.setPaddingBottom(5f);
    return c;
  }
}
