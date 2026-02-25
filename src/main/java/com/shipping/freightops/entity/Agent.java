package com.shipping.freightops.entity;

import com.shipping.freightops.enums.AgentType;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

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

  // Getters & Setters

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public BigDecimal getCommissionPercent() {
    return commissionPercent;
  }

  public void setCommissionPercent(BigDecimal commissionPercent) {
    this.commissionPercent = commissionPercent;
  }

  public AgentType getType() {
    return type;
  }

  public void setType(AgentType type) {
    this.type = type;
  }

  public boolean isActive() {
    return active;
  }

  public void setActive(boolean active) {
    this.active = active;
  }
}
