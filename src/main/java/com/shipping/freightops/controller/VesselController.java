package com.shipping.freightops.controller;

import com.shipping.freightops.dto.CreateVesselRequest;
import com.shipping.freightops.dto.VesselResponse;
import com.shipping.freightops.entity.Vessel;
import com.shipping.freightops.service.VesselService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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

  @Operation(summary = "Create a new vessel")
  @ApiResponses({
    @ApiResponse(responseCode = "201", description = "Vessel successfully created"),
    @ApiResponse(responseCode = "400", description = "Invalid input data"),
    @ApiResponse(responseCode = "409", description = "Vessel with this IMO number already exists")
  })
  @PostMapping
  public ResponseEntity<VesselResponse> create(@Valid @RequestBody CreateVesselRequest request) {
    Vessel vessel = service.createVessel(request);
    VesselResponse body = VesselResponse.fromEntity(vessel);
    URI location = URI.create("/api/v1/vessels/" + vessel.getId());
    return ResponseEntity.created(location).body(body);
  }

  @Operation(summary = "List all vessels")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "List of vessels retrieved successfully")
  })
  @GetMapping
  public ResponseEntity<List<VesselResponse>> list() {
    List<Vessel> vessels = service.getAllVessels();

    List<VesselResponse> body = vessels.stream().map(VesselResponse::fromEntity).toList();
    return ResponseEntity.ok(body);
  }

  @Operation(summary = "Get vessel by ID")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Vessel found"),
    @ApiResponse(responseCode = "404", description = "Vessel not found")
  })
  @GetMapping("/{id}")
  public ResponseEntity<VesselResponse> getById(@PathVariable Long id) {
    Vessel vessel = service.getVessel(id);
    return ResponseEntity.ok(VesselResponse.fromEntity(vessel));
  }
}
