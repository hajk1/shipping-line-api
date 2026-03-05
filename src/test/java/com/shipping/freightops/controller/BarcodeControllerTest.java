package com.shipping.freightops.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.shipping.freightops.service.BarcodeService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ContextConfiguration(
    classes = {BarcodeController.class, BarcodeControllerTest.BarcodeServiceDummyConfig.class})
class BarcodeControllerTest {

  @Autowired private MockMvc mockMvc;

  // ── Dummy BarcodeService ──
  @TestConfiguration
  static class BarcodeServiceDummyConfig {

    @Bean
    public BarcodeService barcodeService() {
      return new BarcodeService() {
        @Override
        public byte[] generateBarcode(String content, int width, int height) {
          if ("FAIL".equals(content)) throw new RuntimeException("Barcode generation failed");
          return new byte[] {0x01, 0x02, 0x03};
        }

        @Override
        public byte[] generateQrCode(String content, int width, int height) {
          if ("FAIL".equals(content)) throw new RuntimeException("QR generation failed");
          return new byte[] {0x04, 0x05, 0x06};
        }
      };
    }
  }

  // ── SUCCESS CASES ──
  @Test
  @DisplayName("GET /api/v1/barcodes/code128 → 200 OK")
  void getCode128Barcode_returns200() throws Exception {
    mockMvc
        .perform(
            get("/api/v1/barcodes/code128")
                .param("content", "MSCU1234567")
                .param("width", "300")
                .param("height", "80")
                .accept(MediaType.IMAGE_PNG))
        .andExpect(status().isOk());
  }

  @Test
  @DisplayName("GET /api/v1/barcodes/qr → 200 OK")
  void getQrCode_returns200() throws Exception {
    mockMvc
        .perform(
            get("/api/v1/barcodes/qr")
                .param("content", "https://example.com/track/FO-001")
                .param("width", "250")
                .param("height", "250")
                .accept(MediaType.IMAGE_PNG))
        .andExpect(status().isOk());
  }

  // ── FAILURE CASES ──
  @Test
  @DisplayName("GET /api/v1/barcodes/code128 → 500 when service fails")
  void getCode128Barcode_serviceFails_returns500() throws Exception {
    mockMvc
        .perform(
            get("/api/v1/barcodes/code128")
                .param("content", "FAIL")
                .param("width", "300")
                .param("height", "80")
                .accept(MediaType.IMAGE_PNG))
        .andExpect(status().isInternalServerError());
  }

  @Test
  @DisplayName("GET /api/v1/barcodes/qr → 500 when service fails")
  void getQrCode_serviceFails_returns500() throws Exception {
    mockMvc
        .perform(
            get("/api/v1/barcodes/qr")
                .param("content", "FAIL")
                .param("width", "250")
                .param("height", "250")
                .accept(MediaType.IMAGE_PNG))
        .andExpect(status().isInternalServerError());
  }
}
