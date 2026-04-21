package com.shipping.freightops.entity;

import com.shipping.freightops.enums.AgentType;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "agents")
public class Agent extends BaseEntity {

  @NotBlank
  @Column(nullable = false)
  private String name;

  @NotBlank
  @Email
  @Column(nullable = false)
  private String email;

  @NotNull
  @DecimalMin(value = "0.0", inclusive = true)
  @DecimalMax(value = "100.0", inclusive = true)
  @Column(nullable = false)
  private BigDecimal commissionPercent;

  @NotNull
  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private AgentType type;

  @Column(nullable = false)
  private boolean active = true;
}
