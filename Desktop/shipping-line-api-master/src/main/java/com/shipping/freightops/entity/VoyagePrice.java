package com.shipping.freightops.entity;

import com.shipping.freightops.enums.ContainerSize;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

@Entity
@Table(
    name = "voyage_prices",
    uniqueConstraints = {
      @UniqueConstraint(
          name = "uc_voyage_container_size",
          columnNames = {"voyage_id", "container_size"})
    })
public class VoyagePrice extends BaseEntity {

  @NotNull
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "voyage_id", nullable = false)
  private Voyage voyage;

  @NotNull
  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private ContainerSize containerSize;

  @NotNull
  @Column(nullable = false, precision = 10, scale = 2)
  @Positive
  private BigDecimal basePriceUsd;

  public VoyagePrice() {}

  public Voyage getVoyage() {
    return voyage;
  }

  public void setVoyage(Voyage voyage) {
    this.voyage = voyage;
  }

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
