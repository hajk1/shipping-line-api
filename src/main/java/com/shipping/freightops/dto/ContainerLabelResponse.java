package com.shipping.freightops.dto;

public class ContainerLabelResponse {
  private final byte[] content;
  private final String fileName;

  public ContainerLabelResponse(byte[] content, String fileName) {
    this.content = content;
    this.fileName = fileName;
  }

  public byte[] getContent() {
    return content;
  }

  public String getFileName() {
    return fileName;
  }
}
