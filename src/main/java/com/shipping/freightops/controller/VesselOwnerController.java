package com.shipping.freightops.controller;

import com.shipping.freightops.dto.AddVesselOwnerRequest;
import com.shipping.freightops.dto.VesselOwnerResponse;
import com.shipping.freightops.entity.VesselOwner;
import com.shipping.freightops.service.VesselOwnerService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** REST controller for managing vessel ownership. */
@RestController
@RequestMapping("/api/v1/vessels/{vesselId}/owners")
public class VesselOwnerController {

  private final VesselOwnerService vesselOwnerService;

  public VesselOwnerController(VesselOwnerService vesselOwnerService) {
    this.vesselOwnerService = vesselOwnerService;
  }

  /** Add an owner to a vessel. */
  @PostMapping
  public ResponseEntity<VesselOwnerResponse> addOwner(
      @PathVariable Long vesselId, @Valid @RequestBody AddVesselOwnerRequest request) {
    VesselOwner owner = vesselOwnerService.addOwner(vesselId, request);
    VesselOwnerResponse body = VesselOwnerResponse.fromEntity(owner);
    URI location = URI.create("/api/v1/vessels/" + vesselId + "/owners/" + owner.getId());
    return ResponseEntity.created(location).body(body);
  }

  /** List all owners for a vessel. */
  @GetMapping
  public ResponseEntity<List<VesselOwnerResponse>> listOwners(@PathVariable Long vesselId) {
    List<VesselOwner> owners = vesselOwnerService.listOwners(vesselId);
    List<VesselOwnerResponse> body = owners.stream().map(VesselOwnerResponse::fromEntity).toList();
    return ResponseEntity.ok(body);
  }

  /** Remove an owner from a vessel. */
  @DeleteMapping("/{ownerId}")
  public ResponseEntity<Void> removeOwner(@PathVariable Long vesselId, @PathVariable Long ownerId) {
    vesselOwnerService.removeOwner(vesselId, ownerId);
    return ResponseEntity.noContent().build();
  }
}
