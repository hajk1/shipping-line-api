package com.shipping.freightops.dto;

import com.shipping.freightops.enums.ContainerSize;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public class VoyagePriceRequest {

  @NotNull private ContainerSize containerSize;

  @NotNull @Positive private BigDecimal basePriceUsd;

  public ContainerSize getContainerSize() {
    return containerSize;
  }

  public void setContainerSize(ContainerSize containerSize) {
    this.containerSize = containerSize;
  }

  public BigDecimal getBasePriceUsd() {
    return basePriceUsd;
  }

  public void setBasePriceUsd(BigDecimal basePriceUsd) {
    this.basePriceUsd = basePriceUsd;
  }
}
