package com.shipping.freightops.entity;

import com.shipping.freightops.enums.ContainerSize;
import com.shipping.freightops.enums.ContainerType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/** A shipping container identified by its BIC code (e.g. MSCU1234567). */
@Entity
@Table(name = "containers")
public class Container extends BaseEntity {

  /** ISO 6346 container code (owner code + serial + check digit). */
  @NotBlank
  @Column(unique = true, nullable = false, length = 11)
  private String containerCode;

  @NotNull
  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private ContainerSize size;

  @NotNull
  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private ContainerType type;

  @Column(nullable = false)
  private int teu;

  public Container() {}

  public Container(String containerCode, ContainerSize size, ContainerType type) {
    this.containerCode = containerCode;
    this.size = size;
    this.type = type;
    this.teu = size.getTeu();
  }

  public String getContainerCode() {
    return containerCode;
  }

  public void setContainerCode(String containerCode) {
    this.containerCode = containerCode;
  }

  public ContainerSize getSize() {
    return size;
  }

  public void setSize(ContainerSize size) {
    this.size = size;
    this.teu = size.getTeu();
  }

  public ContainerType getType() {
    return type;
  }

  public void setType(ContainerType type) {
    this.type = type;
  }

  public int getTeu() {
    return teu;
  }
}
