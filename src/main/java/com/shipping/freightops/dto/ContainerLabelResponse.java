package com.shipping.freightops.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ContainerLabelResponse {
  private final byte[] content;
  private final String fileName;

  public ContainerLabelResponse(byte[] content, String fileName) {
    this.content = content;
    this.fileName = fileName;
  }
}
