package com.shipping.freightops.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class BarcodeServiceTest {

  @Autowired private BarcodeService barcodeService;

  @Test
  @DisplayName("generateBarcode → returns valid PNG bytes")
  void generateBarcode_returnsValidPng() {

    byte[] result = barcodeService.generateBarcode("TEST123", 300, 100);

    assertThat(result).isNotNull();
    assertThat(result.length).isGreaterThan(0);

    // PNG magic bytes: 89 50 4E 47
    assertThat(result[0]).isEqualTo((byte) 0x89);
    assertThat(result[1]).isEqualTo((byte) 0x50);
    assertThat(result[2]).isEqualTo((byte) 0x4E);
    assertThat(result[3]).isEqualTo((byte) 0x47);
  }

  @Test
  @DisplayName("generateQrCode → returns valid PNG bytes")
  void generateQrCode_returnsValidPng() {

    byte[] result = barcodeService.generateQrCode("https://example.com", 250, 250);

    assertThat(result).isNotNull();
    assertThat(result.length).isGreaterThan(0);

    assertThat(result[0]).isEqualTo((byte) 0x89);
    assertThat(result[1]).isEqualTo((byte) 0x50);
    assertThat(result[2]).isEqualTo((byte) 0x4E);
    assertThat(result[3]).isEqualTo((byte) 0x47);
  }

  @Test
  @DisplayName("generateBarcode → throws when content is null")
  void generateBarcode_whenContentNull_throws() {

    assertThatThrownBy(() -> barcodeService.generateBarcode(null, 300, 100))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  @DisplayName("generateQrCode → throws when width or height invalid")
  void generateQrCode_invalidSize_throws() {

    assertThatThrownBy(() -> barcodeService.generateQrCode("ABC", 0, 0))
        .isInstanceOf(IllegalArgumentException.class);
  }
}
