package com.shipping.freightops.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** A cargo vessel that carries containers between ports. */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "vessels")
public class Vessel extends BaseEntity {

  @NotBlank
  @Column(nullable = false)
  private String name;

  /** IMO number – unique 7-digit vessel identifier. */
  @NotBlank
  @Column(unique = true, nullable = false, length = 7)
  private String imoNumber;

  /** Maximum TEU (Twenty-foot Equivalent Unit) capacity. */
  @Positive
  @Column(nullable = false)
  private int capacityTeu;

  public Vessel(String name, String imoNumber, int capacityTeu) {
    this.name = name;
    this.imoNumber = imoNumber;
    this.capacityTeu = capacityTeu;
  }
}
