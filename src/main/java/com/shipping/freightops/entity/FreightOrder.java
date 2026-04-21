package com.shipping.freightops.entity;

import com.shipping.freightops.enums.OrderStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** A freight booking made by the internal ops team, assigning a container to a voyage. */
@Getter
@Setter
@NoArgsConstructor
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
}
