package com.shipping.freightops.entity;

import com.shipping.freightops.enums.OrderStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/** A freight booking made by the internal ops team, assigning a container to a voyage. */
@Entity
@Table(name = "freight_orders")
public class FreightOrder extends BaseEntity {

  @NotNull
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "voyage_id", nullable = false)
  private Voyage voyage;

  @NotNull
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "container_id", nullable = false)
  private Container container;

  /** The agent who placed this order. */
  @NotNull
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "agent_id", nullable = false)
  private Agent agent;

  @NotNull
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "customer_id", nullable = false)
  private Customer customer;

  /** Username or team identifier of whoever placed the order. */
  @NotBlank
  @Column(nullable = false)
  private String orderedBy;

  @OneToMany(mappedBy = "freightOrder", cascade = CascadeType.ALL)
  private List<TrackingEvent> events = new ArrayList<>();

  @Column(length = 500)
  private String notes;

  @NotNull
  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private OrderStatus status = OrderStatus.PENDING;

  @NotNull
  @Positive
  @Column(nullable = false, precision = 10, scale = 2)
  private BigDecimal basePriceUsd;

  @NotNull
  @DecimalMin(value = "0", inclusive = true)
  @DecimalMax(value = "100", inclusive = true)
  @Column(nullable = false, precision = 5, scale = 2)
  private BigDecimal discountPercent = BigDecimal.ZERO;

  @NotNull
  @DecimalMin(value = "0.0", inclusive = true)
  @Column(nullable = false, precision = 10, scale = 2)
  private BigDecimal finalPrice;

  @Column(nullable = true, length = 500)
  private String discountReason;

  public FreightOrder() {}

  public void setEvents(List<TrackingEvent> events) {
    this.events = events;
  }

  public List<TrackingEvent> getEvents() {
    return events;
  }

  public String getOrderedBy() {
    return orderedBy;
  }

  public void setOrderedBy(String orderedBy) {
    this.orderedBy = orderedBy;
  }

  public Customer getCustomer() {
    return customer;
  }

  public void setCustomer(Customer customer) {
    this.customer = customer;
  }

  public Voyage getVoyage() {
    return voyage;
  }

  public void setVoyage(Voyage voyage) {
    this.voyage = voyage;
  }

  public Container getContainer() {
    return container;
  }

  public void setContainer(Container container) {
    this.container = container;
  }

  public Agent getAgent() {
    return agent;
  }

  public void setAgent(Agent agent) {
    this.agent = agent;
  }

  public String getNotes() {
    return notes;
  }

  public void setNotes(String notes) {
    this.notes = notes;
  }

  public OrderStatus getStatus() {
    return status;
  }

  public void setStatus(OrderStatus status) {
    this.status = status;
  }

  public BigDecimal getBasePriceUsd() {
    return basePriceUsd;
  }

  public void setBasePriceUsd(BigDecimal basePriceUsd) {
    this.basePriceUsd = basePriceUsd;
  }

  public BigDecimal getDiscountPercent() {
    return discountPercent;
  }

  public void setDiscountPercent(BigDecimal discountPercent) {
    this.discountPercent = discountPercent;
  }

  public BigDecimal getFinalPrice() {
    return finalPrice;
  }

  public void setFinalPrice(BigDecimal finalPrice) {
    this.finalPrice = finalPrice;
  }

  public String getDiscountReason() {
    return discountReason;
  }

  public void setDiscountReason(String discountReason) {
    this.discountReason = discountReason;
  }
}
