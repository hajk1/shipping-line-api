package com.shipping.freightops.dto;

import com.shipping.freightops.enums.AgentType;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AgentResponse {

  private Long id;
  private String name;
  private String email;
  private BigDecimal commissionPercent;
  private AgentType type;
  private boolean active;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
}
