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

/** A scheduled trip of a vessel from one port to another. */
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

  public Voyage() {}

  public String getVoyageNumber() {
    return voyageNumber;
  }

  public void setVoyageNumber(String voyageNumber) {
    this.voyageNumber = voyageNumber;
  }

  public Vessel getVessel() {
    return vessel;
  }

  public void setVessel(Vessel vessel) {
    this.vessel = vessel;
  }

  public Port getDeparturePort() {
    return departurePort;
  }

  public void setDeparturePort(Port departurePort) {
    this.departurePort = departurePort;
  }

  public Port getArrivalPort() {
    return arrivalPort;
  }

  public void setArrivalPort(Port arrivalPort) {
    this.arrivalPort = arrivalPort;
  }

  public LocalDateTime getDepartureTime() {
    return departureTime;
  }

  public void setDepartureTime(LocalDateTime departureTime) {
    this.departureTime = departureTime;
  }

  public LocalDateTime getArrivalTime() {
    return arrivalTime;
  }

  public void setArrivalTime(LocalDateTime arrivalTime) {
    this.arrivalTime = arrivalTime;
  }

  public VoyageStatus getStatus() {
    return status;
  }

  public void setStatus(VoyageStatus status) {
    this.status = status;
  }

  public int getMaxCapacityTeu() {
    return maxCapacityTeu;
  }

  public void setMaxCapacityTeu(int maxCapacityTeu) {
    this.maxCapacityTeu = maxCapacityTeu;
  }

  public boolean isBookingOpen() {
    return bookingOpen;
  }

  public void setBookingOpen(boolean bookingOpen) {
    this.bookingOpen = bookingOpen;
  }
}
