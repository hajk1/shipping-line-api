package com.shipping.freightops.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.springframework.stereotype.Service;

@Service
public class BarcodeService {

  // Generates a CODE_128 barcode as a PNG byte array
  public byte[] generateBarcode(String content, int width, int height) {
    return generateImage(content, width, height, BarcodeFormat.CODE_128);
  }

  // Generates a QR code as a PNG byte array
  public byte[] generateQrCode(String content, int width, int height) {
    return generateImage(content, width, height, BarcodeFormat.QR_CODE);
  }

  // Shared private method to generate barcode/QR image
  private byte[] generateImage(String content, int width, int height, BarcodeFormat format) {
    // Validate input parameters
    if (content == null || content.isBlank()) {
      throw new IllegalArgumentException("Content must not be null or blank");
    }
    if (width <= 0 || height <= 0) {
      throw new IllegalArgumentException("Width and height must be positive");
    }

    try {
      // Encode the content to a BitMatrix using ZXing
      BitMatrix bitMatrix = new MultiFormatWriter().encode(content, format, width, height);

      // Convert the BitMatrix to a PNG byte array
      ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
      MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream);
      return outputStream.toByteArray();
    } catch (WriterException | IOException e) {
      // Throw a custom exception for clearer error reporting
      throw new BarcodeGenerationException("Failed to generate " + format.name() + " image", e);
    }
  }

  // Custom exception to indicate barcode generation failures
  public static class BarcodeGenerationException extends RuntimeException {
    public BarcodeGenerationException(String message, Throwable cause) {
      super(message, cause);
    }
  }
}
