package com.shipping.freightops.controller;

import com.shipping.freightops.dto.*;
import com.shipping.freightops.entity.Voyage;
import com.shipping.freightops.entity.VoyagePrice;
import com.shipping.freightops.enums.VoyageStatus;
import com.shipping.freightops.service.VoyageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/** REST controller for voyages. */
@RestController
@RequestMapping("/api/v1/voyages")
public class VoyageController {
  private final VoyageService voyageService;

  public VoyageController(VoyageService voyageService) {
    this.voyageService = voyageService;
  }

  @Operation(summary = "Get all voyages")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "List of voyages retrieved successfully")
  })
  @GetMapping()
  public ResponseEntity<List<VoyageResponse>> getAll() {
    List<Voyage> voyages = voyageService.getAll();
    return ResponseEntity.ok(VoyageResponse.VoyageResponses(voyages));
  }

  @Operation(summary = "Get voyage by ID")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Voyage found"),
    @ApiResponse(responseCode = "404", description = "Voyage not found")
  })
  @GetMapping("/{voyageId}")
  public ResponseEntity<VoyageResponse> getById(@PathVariable Long voyageId) {
    Voyage voyage = voyageService.getById(voyageId);
    VoyageResponse response = new VoyageResponse(voyage);
    return ResponseEntity.ok(response);
  }

  @Operation(summary = "Create a new voyage")
  @ApiResponses({
    @ApiResponse(responseCode = "201", description = "Voyage successfully created"),
    @ApiResponse(responseCode = "400", description = "Invalid input data"),
    @ApiResponse(responseCode = "404", description = "Vessel or Port not found")
  })
  @PostMapping
  public ResponseEntity<VoyageResponse> addVoyage(@RequestBody CreateVoyageRequest voyageRequest) {
    Voyage voyage = voyageService.addVoyage(voyageRequest);
    VoyageResponse response = new VoyageResponse(voyage);
    return ResponseEntity.created(URI.create("/api/v1/voyages")).body(response);
  }

  @Operation(summary = "Update voyage status")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Voyage status updated"),
    @ApiResponse(responseCode = "404", description = "Voyage not found")
  })
  @PatchMapping("/{voyageId}/{status}")
  public ResponseEntity<VoyageResponse> updateVoyage(
      @PathVariable Long voyageId, @PathVariable VoyageStatus status) {
    Voyage updatedVoyage = voyageService.updateStatus(status, voyageId);
    VoyageResponse response = new VoyageResponse(updatedVoyage);
    return ResponseEntity.ok(response);
  }

  @Operation(summary = "Get voyages by status")
  @ApiResponses({@ApiResponse(responseCode = "200", description = "Filtered voyages retrieved")})
  @GetMapping(params = "status")
  public ResponseEntity<List<VoyageResponse>> getAllByStatus(@RequestParam VoyageStatus status) {
    List<Voyage> voyages = voyageService.getAllByStatus(status);
    return ResponseEntity.ok(VoyageResponse.VoyageResponses(voyages));
  }

  @Operation(summary = "Create price for a voyage and container size")
  @ApiResponses({
    @ApiResponse(responseCode = "201", description = "Voyage price created"),
    @ApiResponse(responseCode = "400", description = "Invalid input or missing pricing data"),
    @ApiResponse(responseCode = "404", description = "Voyage not found"),
    @ApiResponse(responseCode = "409", description = "Price already exists for this container size")
  })
  @PostMapping("/{voyageId}/prices")
  public ResponseEntity<VoyagePriceResponse> setPriceForContainer(
      @PathVariable Long voyageId, @Valid @RequestBody VoyagePriceRequest voyagePriceRequest) {
    VoyagePrice voyagePrice = voyageService.createVoyagePrice(voyageId, voyagePriceRequest);
    return ResponseEntity.created(URI.create("/api/v1/voyages/" + voyageId + "/prices"))
        .body(VoyagePriceResponse.fromEntity(voyagePrice));
  }

  @Operation(summary = "Get all prices for a voyage (paginated)")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Voyage prices retrieved"),
    @ApiResponse(responseCode = "404", description = "Voyage not found")
  })
  @GetMapping("/{voyageId}/prices")
  public ResponseEntity<PageResponse<VoyagePriceResponse>> getVoyagePrices(
      @PathVariable Long voyageId, @PageableDefault(size = 20) Pageable pageable) {
    Page<VoyagePrice> voyagePrices = voyageService.getAllPricesByVoyageId(voyageId, pageable);
    Page<VoyagePriceResponse> mapped = voyagePrices.map(VoyagePriceResponse::fromEntity);
    return ResponseEntity.ok(PageResponse.from(mapped));
  }
}
