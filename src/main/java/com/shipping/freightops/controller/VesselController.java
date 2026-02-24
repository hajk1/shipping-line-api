package com.shipping.freightops.controller;

import com.shipping.freightops.dto.CreateVesselRequest;
import com.shipping.freightops.dto.VesselResponse;
import com.shipping.freightops.entity.Vessel;
import com.shipping.freightops.service.VesselService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/** REST controller for vessel management. */
@RestController
@RequestMapping("/api/v1/vessels")
public class VesselController {
  private final VesselService service;

  public VesselController(VesselService service) {
    this.service = service;
  }

  /** Create a new vessel. */
  @PostMapping
  public ResponseEntity<VesselResponse> create(@Valid @RequestBody CreateVesselRequest request) {
    Vessel vessel = service.createVessel(request);
    VesselResponse body = VesselResponse.fromEntity(vessel);
    URI location = URI.create("/api/v1/vessels/" + vessel.getId());
    return ResponseEntity.created(location).body(body);
  }

  /** List all vessels. */
  @GetMapping
  public ResponseEntity<List<VesselResponse>> list() {
    List<Vessel> vessels = service.getAllVessels();

    List<VesselResponse> body = vessels.stream().map(VesselResponse::fromEntity).toList();
    return ResponseEntity.ok(body);
  }

  /** Get a vessel by its ID. */
  @GetMapping("/{id}")
  public ResponseEntity<VesselResponse> getById(@PathVariable Long id) {
    Vessel vessel = service.getVessel(id);
    return ResponseEntity.ok(VesselResponse.fromEntity(vessel));
  }
}
