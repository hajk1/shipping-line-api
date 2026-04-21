package com.shipping.freightops.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

/** An owner of a vessel with a fractional share used for cost/profit splitting. */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "vessel_owners")
public class VesselOwner extends BaseEntity {

  @ManyToOne
  @JoinColumn(name = "vessel_id", nullable = false)
  @OnDelete(action = OnDeleteAction.CASCADE)
  private Vessel vessel;

  @NotBlank
  @Column(nullable = false)
  private String ownerName;

  @NotBlank
  @Email
  @Column(nullable = false)
  private String ownerEmail;

  /** Ownership share expressed as a percentage (0.01–100). */
  @NotNull
  @DecimalMin(value = "0.01")
  @DecimalMax(value = "100.00")
  @Column(nullable = false, precision = 5, scale = 2)
  private BigDecimal sharePercent;
}
