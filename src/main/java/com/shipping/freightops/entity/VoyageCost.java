package com.shipping.freightops.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

/** Cost line item associated with a voyage. */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "voyage_costs")
public class VoyageCost extends BaseEntity {

  @NotNull
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "voyage_id", nullable = false)
  @OnDelete(action = OnDeleteAction.CASCADE)
  private Voyage voyage;

  @NotBlank
  @Column(nullable = false)
  private String description;

  @NotNull
  @Positive
  @Column(nullable = false, precision = 12, scale = 2)
  private BigDecimal amountUsd;
}
