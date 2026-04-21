package com.shipping.freightops.dto;

import com.shipping.freightops.enums.AgentType;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AgentCreateRequest {

  @NotBlank private String name;

  @NotBlank @Email private String email;

  @NotNull
  @DecimalMin("0.0")
  @DecimalMax("100.0")
  private BigDecimal commissionPercent;

  @NotNull private AgentType type;
}
