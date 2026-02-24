package com.shipping.freightops.service;

import com.shipping.freightops.dto.CreateVoyageRequest;
import com.shipping.freightops.dto.VoyagePriceRequest;
import com.shipping.freightops.entity.Port;
import com.shipping.freightops.entity.Vessel;
import com.shipping.freightops.entity.Voyage;
import com.shipping.freightops.entity.VoyagePrice;
import com.shipping.freightops.enums.VoyageStatus;
import com.shipping.freightops.repository.PortRepository;
import com.shipping.freightops.repository.VesselRepository;
import com.shipping.freightops.repository.VoyagePriceRepository;
import com.shipping.freightops.repository.VoyageRepository;
import jakarta.validation.Valid;
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
    return voyage;
  }

  public VoyageService(
      VoyageRepository voyageRepository,
      VesselRepository vesselRepository,
      PortRepository portRepository,
      VoyagePriceRepository voyagePriceRepository) {
    this.voyageRepository = voyageRepository;
    this.vesselRepository = vesselRepository;
    this.portRepository = portRepository;
    this.voyagePriceRepository = voyagePriceRepository;
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
}
