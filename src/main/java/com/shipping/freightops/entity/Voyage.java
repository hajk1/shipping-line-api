package com.shipping.freightops.entity;

import com.shipping.freightops.enums.VoyageStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** A scheduled trip of a vessel from one port to another. */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "voyages")
public class Voyage extends BaseEntity {

  @Column(unique = true, nullable = false)
  private String voyageNumber;

  @NotNull
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "vessel_id", nullable = false)
  private Vessel vessel;

  @NotNull
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "departure_port_id", nullable = false)
  private Port departurePort;

  @NotNull
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "arrival_port_id", nullable = false)
  private Port arrivalPort;

  @NotNull
  @Column(nullable = false)
  private LocalDateTime departureTime;

  @NotNull
  @Column(nullable = false)
  private LocalDateTime arrivalTime;

  @NotNull
  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private VoyageStatus status = VoyageStatus.PLANNED;

  @Positive
  @Column(nullable = false)
  private int maxCapacityTeu;

  @Column private boolean bookingOpen;
}
