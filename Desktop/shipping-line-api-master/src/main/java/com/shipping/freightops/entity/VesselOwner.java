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
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

/** An owner of a vessel with a fractional share used for cost/profit splitting. */
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

  public VesselOwner() {}

  public Vessel getVessel() {
    return vessel;
  }

  public void setVessel(Vessel vessel) {
    this.vessel = vessel;
  }

  public String getOwnerName() {
    return ownerName;
  }

  public void setOwnerName(String ownerName) {
    this.ownerName = ownerName;
  }

  public String getOwnerEmail() {
    return ownerEmail;
  }

  public void setOwnerEmail(String ownerEmail) {
    this.ownerEmail = ownerEmail;
  }

  public BigDecimal getSharePercent() {
    return sharePercent;
  }

  public void setSharePercent(BigDecimal sharePercent) {
    this.sharePercent = sharePercent;
  }
}
