package com.shipping.freightops.dto;

import com.shipping.freightops.enums.ContainerSize;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class VoyagePriceRequest {

  @NotNull private ContainerSize containerSize;

  @NotNull @Positive private BigDecimal basePriceUsd;
}
