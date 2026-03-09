package com.shipping.freightops.service;

import com.shipping.freightops.dto.BookingStatusUpdateRequest;
import com.shipping.freightops.dto.CreateVoyageCostRequest;
import com.shipping.freightops.dto.CreateVoyageRequest;
import com.shipping.freightops.dto.FinancialSummaryResponse;
import com.shipping.freightops.dto.OwnerFinancialShareResponse;
import com.shipping.freightops.dto.VoyagePriceRequest;
import com.shipping.freightops.entity.*;
import com.shipping.freightops.enums.OrderStatus;
import com.shipping.freightops.enums.VoyageStatus;
import com.shipping.freightops.repository.*;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class VoyageService {
  private final VoyageRepository voyageRepository;
  private final VesselRepository vesselRepository;
  private final PortRepository portRepository;
  private final VoyagePriceRepository voyagePriceRepository;
  private final FreightOrderRepository orderRepository;
  private final VoyageCostRepository voyageCostRepository;
  private final VesselOwnerRepository vesselOwnerRepository;

  private Voyage mapCreateVoyageRequestToVoyage(CreateVoyageRequest voyageRequest) {
    Voyage voyage = new Voyage();
    if (Objects.equals(voyageRequest.getDeparturePortId(), voyageRequest.getArrivalPortId()))
      throw new IllegalArgumentException("arrival portId must be different from departure port");
    // check for vessel,port ids in voyage payload;
    Vessel vessel =
        vesselRepository
            .findById(voyageRequest.getVesselId())
            .orElseThrow(() -> new IllegalArgumentException(("Vessel not found")));
    Port arrivalPort =
        portRepository
            .findById(voyageRequest.getArrivalPortId())
            .orElseThrow(() -> new IllegalArgumentException("Arrival port not found "));
    Port departurePort =
        portRepository
            .findById(voyageRequest.getDeparturePortId())
            .orElseThrow(() -> new IllegalArgumentException("departure port not found"));
    // check for departure time is in future
    if (!voyageRequest.getDepartureTime().isAfter(LocalDateTime.now()))
      throw new IllegalArgumentException("Departure date must be in future");
    // check for arrival time is after daparture time
    if (voyageRequest.getArrivalTime().isBefore(voyageRequest.getDepartureTime()))
      throw new IllegalArgumentException("arrival date must be after departure date");
    voyage.setVoyageNumber(voyageRequest.getVoyageNumber());
    voyage.setVessel(vessel);
    voyage.setArrivalPort(arrivalPort);
    voyage.setDeparturePort(departurePort);
    voyage.setDepartureTime(voyageRequest.getDepartureTime());
    voyage.setArrivalTime(voyageRequest.getArrivalTime());
    voyage.setBookingOpen(true);
    voyage.setMaxCapacityTeu(vessel.getCapacityTeu());
    return voyage;
  }

  public VoyageService(
      VoyageRepository voyageRepository,
      VesselRepository vesselRepository,
      PortRepository portRepository,
      VoyagePriceRepository voyagePriceRepository,
      FreightOrderRepository orderRepository,
      VoyageCostRepository voyageCostRepository,
      VesselOwnerRepository vesselOwnerRepository) {
    this.voyageRepository = voyageRepository;
    this.vesselRepository = vesselRepository;
    this.portRepository = portRepository;
    this.voyagePriceRepository = voyagePriceRepository;
    this.orderRepository = orderRepository;
    this.voyageCostRepository = voyageCostRepository;
    this.vesselOwnerRepository = vesselOwnerRepository;
  }

  public List<Voyage> getAll() {
    return voyageRepository.findAll();
  }

  public List<Voyage> getAllByStatus(VoyageStatus status) {
    return voyageRepository.findAllByStatus(status);
  }

  public Voyage getById(Long id) {
    return voyageRepository
        .findById(id)
        .orElseThrow(() -> new IllegalArgumentException("Voyage not found"));
  }

  @Transactional
  public Voyage addVoyage(@Valid CreateVoyageRequest voyageRequest) {
    Voyage voyage = mapCreateVoyageRequestToVoyage(voyageRequest);
    return voyageRepository.save(voyage);
  }

  public Voyage updateStatus(VoyageStatus status, Long voyageId) {
    Voyage voyage =
        voyageRepository
            .findById(voyageId)
            .orElseThrow(() -> new IllegalArgumentException("voyage not found"));
    voyage.setStatus(status);
    return voyageRepository.save(voyage);
  }

  public void delete(Long voyageId) {
    boolean exists = voyageRepository.existsById(voyageId);
    if (!exists) throw new IllegalArgumentException("Voyage not found");
    voyageRepository.deleteById(voyageId);
  }

  @Transactional
  public VoyagePrice createVoyagePrice(
      Long voyageId, @Valid VoyagePriceRequest voyagePriceRequest) {
    Voyage voyage =
        voyageRepository
            .findById(voyageId)
            .orElseThrow(() -> new IllegalArgumentException("Voyage not found"));
    Optional<VoyagePrice> existingCombi =
        voyagePriceRepository.findByVoyageAndContainerSize(
            voyage, voyagePriceRequest.getContainerSize());
    if (existingCombi.isPresent())
      throw new IllegalStateException(
          "Price already exists for this container size on this voyage");

    VoyagePrice voyagePrice = new VoyagePrice();
    voyagePrice.setVoyage(voyage);
    voyagePrice.setContainerSize(voyagePriceRequest.getContainerSize());
    voyagePrice.setBasePriceUsd(voyagePriceRequest.getBasePriceUsd());

    return voyagePriceRepository.save(voyagePrice);
  }

  @Transactional(readOnly = true)
  public Page<VoyagePrice> getAllPricesByVoyageId(Long voyageId, Pageable pageable) {
    if (!voyageRepository.existsById(voyageId)) {
      throw new IllegalArgumentException("Voyage not found");
    }
    return voyagePriceRepository.findByVoyageId(voyageId, pageable);
  }

  @Transactional(readOnly = true)
  public List<FreightOrder> getActiveOrdersForVoyage(Long voyageId) {
    return orderRepository.findByVoyageIdAndStatusIn(
        voyageId, List.of(OrderStatus.PENDING, OrderStatus.CONFIRMED, OrderStatus.IN_TRANSIT));
  }

  public int calculateCurrentLoadTeu(List<FreightOrder> orders) {
    return orders.stream().mapToInt(order -> order.getContainer().getSize().getTeu()).sum();
  }

  @Transactional
  public Voyage updateBookingStatus(Long voyageId, BookingStatusUpdateRequest request) {
    Voyage voyage =
        voyageRepository
            .findById(voyageId)
            .orElseThrow(() -> new IllegalArgumentException("Voyage not found"));
    voyage.setBookingOpen(request.isBookingOpen());
    return voyageRepository.save(voyage);
  }

  @Transactional
  public VoyageCost addVoyageCost(Long voyageId, @Valid CreateVoyageCostRequest request) {
    Voyage voyage =
        voyageRepository
            .findById(voyageId)
            .orElseThrow(() -> new IllegalArgumentException("Voyage not found"));
    VoyageCost voyageCost = new VoyageCost();
    voyageCost.setVoyage(voyage);
    voyageCost.setDescription(request.getDescription());
    voyageCost.setAmountUsd(request.getAmountUsd());
    return voyageCostRepository.save(voyageCost);
  }

  @Transactional(readOnly = true)
  public List<VoyageCost> getVoyageCosts(Long voyageId) {
    if (!voyageRepository.existsById(voyageId)) {
      throw new IllegalArgumentException("Voyage not found");
    }
    return voyageCostRepository.findByVoyageIdOrderByCreatedAtAsc(voyageId);
  }

  @Transactional(readOnly = true)
  public FinancialSummaryResponse getFinancialSummary(Long voyageId) {
    Voyage voyage =
        voyageRepository
            .findById(voyageId)
            .orElseThrow(() -> new IllegalArgumentException("Voyage not found"));

    if (voyage.getStatus() != VoyageStatus.COMPLETED) {
      throw new IllegalStateException(
          "Financial summary can only be generated for completed voyages");
    }

    List<FreightOrder> deliveredOrders =
        orderRepository.findByVoyageIdAndStatus(voyageId, OrderStatus.DELIVERED);
    List<VoyageCost> costs = voyageCostRepository.findByVoyageIdOrderByCreatedAtAsc(voyageId);
    List<VesselOwner> owners = vesselOwnerRepository.findByVesselId(voyage.getVessel().getId());

    BigDecimal totalRevenue =
        deliveredOrders.stream()
            .map(FreightOrder::getFinalPrice)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    BigDecimal totalCosts =
        costs.stream().map(VoyageCost::getAmountUsd).reduce(BigDecimal.ZERO, BigDecimal::add);
    BigDecimal netProfit = totalRevenue.subtract(totalCosts);

    List<OwnerFinancialShareResponse> ownerBreakdown =
        owners.stream()
            .map(owner -> mapOwnerSummary(owner, totalRevenue, totalCosts, netProfit))
            .toList();

    return FinancialSummaryResponse.fromValues(
        voyage.getVoyageNumber(),
        totalRevenue,
        totalCosts,
        netProfit,
        deliveredOrders.size(),
        ownerBreakdown);
  }

  private OwnerFinancialShareResponse mapOwnerSummary(
      VesselOwner owner, BigDecimal totalRevenue, BigDecimal totalCosts, BigDecimal netProfit) {
    return OwnerFinancialShareResponse.fromValues(
        owner.getOwnerName(),
        owner.getSharePercent(),
        applyShare(totalRevenue, owner.getSharePercent()),
        applyShare(totalCosts, owner.getSharePercent()),
        applyShare(netProfit, owner.getSharePercent()));
  }

  private BigDecimal applyShare(BigDecimal amount, BigDecimal sharePercent) {
    return amount.multiply(sharePercent).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
  }
}
