package com.shipping.freightops.service;

import com.shipping.freightops.config.BookingProperties;
import com.shipping.freightops.dto.CreateFreightOrderRequest;
import com.shipping.freightops.dto.UpdateDiscountRequest;
import com.shipping.freightops.entity.*;
import com.shipping.freightops.entity.Agent;
import com.shipping.freightops.entity.Container;
import com.shipping.freightops.entity.FreightOrder;
import com.shipping.freightops.entity.Voyage;
import com.shipping.freightops.enums.ContainerSize;
import com.shipping.freightops.enums.OrderStatus;
import com.shipping.freightops.enums.VoyageStatus;
import com.shipping.freightops.exception.BadRequestException;
import com.shipping.freightops.repository.*;
import com.shipping.freightops.repository.AgentRepository;
import com.shipping.freightops.repository.ContainerRepository;
import com.shipping.freightops.repository.FreightOrderRepository;
import com.shipping.freightops.repository.VoyageRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Handles freight order creation and queries. */
@Service
public class FreightOrderService {

  private final FreightOrderRepository orderRepository;
  private final VoyageRepository voyageRepository;
  private final ContainerRepository containerRepository;
  private final AgentRepository agentRepository;
  private final CustomerRepository customerRepository;
  private final VoyagePriceRepository voyagePriceRepository;
  private final BookingProperties bookingProperties;
  private static final Logger log = LoggerFactory.getLogger(FreightOrderService.class);

  public FreightOrderService(
      FreightOrderRepository orderRepository,
      VoyageRepository voyageRepository,
      ContainerRepository containerRepository,
      AgentRepository agentRepository,
      CustomerRepository customerRepository,
      VoyagePriceRepository voyagePriceRepository,
      BookingProperties bookingProperties) {
    this.orderRepository = orderRepository;
    this.voyageRepository = voyageRepository;
    this.containerRepository = containerRepository;
    this.agentRepository = agentRepository;
    this.customerRepository = customerRepository;
    this.voyagePriceRepository = voyagePriceRepository;
    this.bookingProperties = bookingProperties;
  }

  @Transactional
  public FreightOrder createOrder(CreateFreightOrderRequest request) {
    Voyage voyage =
        voyageRepository
            .findByIdForUpdate(request.getVoyageId())
            .orElseThrow(
                () -> new IllegalArgumentException("Voyage not found: " + request.getVoyageId()));

    if (voyage.getStatus() == VoyageStatus.CANCELLED) {
      throw new IllegalStateException("Cannot book freight on a cancelled voyage");
    }

    if (!voyage.isBookingOpen())
      throw new IllegalStateException("Booking is closed for this voyage");

    Container container =
        containerRepository
            .findById(request.getContainerId())
            .orElseThrow(
                () ->
                    new IllegalArgumentException(
                        "Container not found: " + request.getContainerId()));

    Agent agent =
        agentRepository
            .findById(request.getAgentId())
            .orElseThrow(
                () -> new IllegalArgumentException("Agent not found: " + request.getAgentId()));

    if (!agent.isActive()) {
      throw new IllegalStateException("Cannot place order with inactive agent: " + agent.getId());
    }
    Customer customer =
        customerRepository
            .findById(request.getCustomerId())
            .orElseThrow(
                () ->
                    new IllegalArgumentException("Customer not found: " + request.getCustomerId()));

    ContainerSize containerSize = container.getSize();
    VoyagePrice voyagePrice =
        voyagePriceRepository
            .findByVoyageAndContainerSize(voyage, containerSize)
            .orElseThrow(
                () -> new BadRequestException("No price defined for voyage and container size"));

    validateCapacity(voyage, container);

    BigDecimal basePriceUsd = voyagePrice.getBasePriceUsd();
    BigDecimal discountPercentage =
        request.getDiscountPercent() != null ? request.getDiscountPercent() : BigDecimal.ZERO;
    BigDecimal finalPriceUsd = calculateFinalPrice(basePriceUsd, discountPercentage);

    FreightOrder order = new FreightOrder();
    order.setVoyage(voyage);
    order.setContainer(container);
    order.setAgent(agent);
    order.setCustomer(customer);
    order.setOrderedBy(request.getOrderedBy());
    order.setNotes(request.getNotes());
    order.setBasePriceUsd(basePriceUsd);
    order.setDiscountPercent(discountPercentage);
    order.setFinalPrice(finalPriceUsd);

    FreightOrder savedOrder = orderRepository.save(order);

    handleAutoCutoff(voyage);
    return savedOrder;
  }

  @Transactional(readOnly = true)
  public FreightOrder getOrder(Long id) {
    return orderRepository
        .findById(id)
        .orElseThrow(() -> new IllegalArgumentException("Freight order not found: " + id));
  }

  @Transactional(readOnly = true)
  public Page<FreightOrder> getAllOrders(Pageable pageable) {
    return orderRepository.findAll(pageable);
  }

  @Transactional(readOnly = true)
  public Page<FreightOrder> getOrdersByVoyage(Long voyageId, Pageable pageable) {
    voyageRepository
        .findById(voyageId)
        .orElseThrow(() -> new IllegalArgumentException("Voyage not found"));
    return orderRepository.findByVoyageId(voyageId, pageable);
  }

  @Transactional
  public FreightOrder updateDiscount(Long id, UpdateDiscountRequest request) {
    FreightOrder order =
        orderRepository
            .findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Freight order not found: " + id));

    if (order.getStatus() == OrderStatus.CANCELLED || order.getStatus() == OrderStatus.DELIVERED)
      throw new IllegalStateException(
          "New discount cannot be applied on the cancelled or delivered freight order");

    BigDecimal discountPercentage =
        request.getDiscountPercent() != null ? request.getDiscountPercent() : BigDecimal.ZERO;

    order.setDiscountPercent(discountPercentage);
    order.setDiscountReason(request.getReason());
    order.setFinalPrice(calculateFinalPrice(order.getBasePriceUsd(), order.getDiscountPercent()));
    return orderRepository.save(order);
  }

  private BigDecimal calculateFinalPrice(BigDecimal basePriceUsd, BigDecimal discountPercent) {
    BigDecimal discount = discountPercent.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
    return basePriceUsd
        .multiply(BigDecimal.ONE.subtract(discount))
        .setScale(2, RoundingMode.HALF_UP);
  }

  private void handleAutoCutoff(Voyage voyage) {
    int currentLoadTeu = orderRepository.sumTeuByVoyageId(voyage.getId());
    int maxCapacityTeu = voyage.getMaxCapacityTeu();

    double loadFactor = (double) currentLoadTeu / maxCapacityTeu;
    double threshold = bookingProperties.getAutoCutoffPercent() / 100.0;

    if (loadFactor >= threshold && voyage.isBookingOpen()) {
      voyage.setBookingOpen(false);

      log.warn(
          "Auto cutoff triggered for voyage {}. Load factor: {} ({} / {}), threshold: {}%",
          voyage.getId(),
          loadFactor,
          currentLoadTeu,
          maxCapacityTeu,
          bookingProperties.getAutoCutoffPercent());

      voyageRepository.save(voyage);
    }
  }

  private void validateCapacity(Voyage voyage, Container container) {

    int currentLoadTeu = orderRepository.sumTeuByVoyageId(voyage.getId());
    int maxCapacityTeu = voyage.getMaxCapacityTeu();
    int remainingTeu = maxCapacityTeu - currentLoadTeu;

    int requestedTeu = container.getSize().getTeu();

    if (requestedTeu > remainingTeu) {
      throw new IllegalStateException(
          String.format(
              "Not enough capacity on voyage. Remaining capacity: %d TEU. "
                  + "Requested container (%s) requires %d TEU.",
              remainingTeu, container.getSize(), requestedTeu));
    }
  }
}
