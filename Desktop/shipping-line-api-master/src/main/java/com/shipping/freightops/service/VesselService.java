package com.shipping.freightops.service;

import com.shipping.freightops.dto.CreateVesselRequest;
import com.shipping.freightops.entity.Vessel;
import com.shipping.freightops.repository.VesselRepository;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Handles vessel creation and queries. */
@Service
public class VesselService {

  private final VesselRepository vesselRepository;

  public VesselService(VesselRepository vesselRepository) {
    this.vesselRepository = vesselRepository;
  }

  @Transactional
  public Vessel createVessel(@Valid CreateVesselRequest request) {
    if (vesselRepository.findByImoNumber(request.getImoNumber()).isPresent()) {
      throw new IllegalStateException("IMO number already exists");
    }

    Vessel vessel = new Vessel();
    vessel.setName(request.getName());
    vessel.setCapacityTeu(request.getCapacityTeu());
    vessel.setImoNumber(request.getImoNumber());

    return vesselRepository.save(vessel);
  }

  @Transactional(readOnly = true)
  public List<Vessel> getAllVessels() {
    return vesselRepository.findAll();
  }

  @Transactional(readOnly = true)
  public Vessel getVessel(Long id) {
    return vesselRepository
        .findById(id)
        .orElseThrow(() -> new IllegalArgumentException("Vessel not found: " + id));
  }
}
