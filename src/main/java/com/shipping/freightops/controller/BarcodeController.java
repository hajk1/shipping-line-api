package com.shipping.freightops.controller;

import com.shipping.freightops.service.BarcodeService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/barcodes")
public class BarcodeController {

  private final BarcodeService barcodeService;

  public BarcodeController(BarcodeService barcodeService) {
    this.barcodeService = barcodeService;
  }

  @GetMapping(value = "/code128", produces = MediaType.IMAGE_PNG_VALUE)
  public ResponseEntity<byte[]> generateBarcode(
      @RequestParam String content, @RequestParam int width, @RequestParam int height) {
    try {
      byte[] barcodeImage = barcodeService.generateBarcode(content, width, height);
      return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG).body(barcodeImage);
    } catch (Exception e) {
      return ResponseEntity.status(500).build();
    }
  }

  @GetMapping(value = "/qr", produces = MediaType.IMAGE_PNG_VALUE)
  public ResponseEntity<byte[]> generateQrCode(
      @RequestParam String content, @RequestParam int width, @RequestParam int height) {
    try {
      byte[] qrCodeImage = barcodeService.generateQrCode(content, width, height);
      return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG).body(qrCodeImage);
    } catch (Exception e) {
      return ResponseEntity.status(500).build();
    }
  }
}
