package com.shipping.freightops.dto;

import com.shipping.freightops.enums.AgentType;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class AgentResponse {

  private Long id;
  private String name;
  private String email;
  private BigDecimal commissionPercent;
  private AgentType type;
  private boolean active;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

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

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }

  public LocalDateTime getUpdatedAt() {
    return updatedAt;
  }

  public void setUpdatedAt(LocalDateTime updatedAt) {
    this.updatedAt = updatedAt;
  }
}
