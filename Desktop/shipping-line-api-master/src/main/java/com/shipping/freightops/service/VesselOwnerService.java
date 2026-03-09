package com.shipping.freightops.service;

import com.shipping.freightops.dto.AddVesselOwnerRequest;
import com.shipping.freightops.entity.Vessel;
import com.shipping.freightops.entity.VesselOwner;
import com.shipping.freightops.repository.VesselOwnerRepository;
import com.shipping.freightops.repository.VesselRepository;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Handles vessel ownership operations. */
@Service
public class VesselOwnerService {

  private final VesselOwnerRepository vesselOwnerRepository;
  private final VesselRepository vesselRepository;

  public VesselOwnerService(
      VesselOwnerRepository vesselOwnerRepository, VesselRepository vesselRepository) {
    this.vesselOwnerRepository = vesselOwnerRepository;
    this.vesselRepository = vesselRepository;
  }

  @Transactional
  public VesselOwner addOwner(Long vesselId, AddVesselOwnerRequest request) {
    Vessel vessel =
        vesselRepository
            .findById(vesselId)
            .orElseThrow(() -> new IllegalArgumentException("Vessel not found: " + vesselId));

    BigDecimal currentTotal = vesselOwnerRepository.sumSharePercentByVesselId(vesselId);
    if (currentTotal.add(request.getSharePercent()).compareTo(new BigDecimal("100.00")) > 0) {
      throw new IllegalStateException("Total ownership would exceed 100%");
    }

    VesselOwner owner = new VesselOwner();
    owner.setVessel(vessel);
    owner.setOwnerName(request.getOwnerName());
    owner.setOwnerEmail(request.getOwnerEmail());
    owner.setSharePercent(request.getSharePercent());

    return vesselOwnerRepository.save(owner);
  }

  @Transactional(readOnly = true)
  public List<VesselOwner> listOwners(Long vesselId) {
    vesselRepository
        .findById(vesselId)
        .orElseThrow(() -> new IllegalArgumentException("Vessel not found: " + vesselId));
    return vesselOwnerRepository.findByVesselId(vesselId);
  }

  @Transactional
  public void removeOwner(Long vesselId, Long ownerId) {
    vesselRepository
        .findById(vesselId)
        .orElseThrow(() -> new IllegalArgumentException("Vessel not found: " + vesselId));

    VesselOwner owner =
        vesselOwnerRepository
            .findById(ownerId)
            .orElseThrow(() -> new IllegalArgumentException("Owner not found: " + ownerId));

    if (!owner.getVessel().getId().equals(vesselId)) {
      throw new IllegalArgumentException("Owner not found: " + ownerId);
    }

    vesselOwnerRepository.delete(owner);
  }
}
