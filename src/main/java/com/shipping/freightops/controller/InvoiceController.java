package com.shipping.freightops.controller;

import com.itextpdf.text.DocumentException;
import com.shipping.freightops.service.InvoiceEmailService;
import com.shipping.freightops.service.InvoiceService;
import java.io.FileNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/invoices")
public class InvoiceController {

  @Autowired private InvoiceService invoiceService;

  @Autowired private InvoiceEmailService invoiceEmailService;

  @GetMapping("/{orderId}")
  public ResponseEntity<byte[]> getInvoice(@PathVariable Long orderId)
      throws DocumentException, FileNotFoundException {
    byte[] pdfBytes = invoiceService.generateInvoice(orderId);

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_PDF);
    headers.setContentDisposition(
        ContentDisposition.attachment().filename("invoice-" + orderId + ".pdf").build());
    headers.setContentLength(pdfBytes.length);

    return ResponseEntity.ok().headers(headers).body(pdfBytes);
  }

  @PostMapping("/{orderId}/send")
  public ResponseEntity<String> sendInvoiceToCustomer(@PathVariable Long orderId)
      throws DocumentException, FileNotFoundException {
    invoiceEmailService.sendInvoiceToCustomer(orderId);
    return ResponseEntity.ok("Invoice sent successfully to customer email");
  }
}
