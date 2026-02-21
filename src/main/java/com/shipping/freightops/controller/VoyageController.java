package com.shipping.freightops.controller;

import com.shipping.freightops.dto.*;
import com.shipping.freightops.entity.Voyage;
import com.shipping.freightops.entity.VoyagePrice;
import com.shipping.freightops.enums.VoyageStatus;
import com.shipping.freightops.service.VoyageService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/voyages")
public class VoyageController {
  private final VoyageService voyageService;

  public VoyageController(VoyageService voyageService) {
    this.voyageService = voyageService;
  }

  @GetMapping()
  public ResponseEntity<List<VoyageResponse>> getAll() {
    List<Voyage> voyages = voyageService.getAll();
    return ResponseEntity.ok(VoyageResponse.VoyageResponses(voyages));
  }

  @GetMapping("/{voyageId}")
  public ResponseEntity<VoyageResponse> getById(@PathVariable Long voyageId) {
    Voyage voyage = voyageService.getById(voyageId);
    VoyageResponse response = new VoyageResponse(voyage);
    return ResponseEntity.ok(response);
  }

  @PostMapping
  public ResponseEntity<VoyageResponse> addVoyage(@RequestBody CreateVoyageRequest voyageRequest) {
    Voyage voyage = voyageService.addVoyage(voyageRequest);
    VoyageResponse response = new VoyageResponse(voyage);
    return ResponseEntity.created(URI.create("/api/v1/voyages")).body(response);
  }

  @PatchMapping("/{voyageId}/{status}")
  public ResponseEntity<VoyageResponse> updateVoyage(
      @PathVariable Long voyageId, @PathVariable VoyageStatus status) {
    Voyage updatedVoyage = voyageService.updateStatus(status, voyageId);
    VoyageResponse response = new VoyageResponse(updatedVoyage);
    return ResponseEntity.ok(response);
  }

  @GetMapping(params = "status")
  public ResponseEntity<List<VoyageResponse>> getAllByStatus(@RequestParam VoyageStatus status) {
    List<Voyage> voyages = voyageService.getAllByStatus(status);
    return ResponseEntity.ok(VoyageResponse.VoyageResponses(voyages));
  }

  @PostMapping("/{voyageId}/prices")
  public ResponseEntity<VoyagePriceResponse> setPriceForContainer(
      @PathVariable Long voyageId, @Valid @RequestBody VoyagePriceRequest voyagePriceRequest) {
    VoyagePrice voyagePrice = voyageService.createVoyagePrice(voyageId, voyagePriceRequest);
    return ResponseEntity.ok(VoyagePriceResponse.fromEntity(voyagePrice));
  }

  @GetMapping("/{voyageId}/prices")
  public ResponseEntity<PageResponse<VoyagePriceResponse>> getVoyagePrices(
      @PathVariable Long voyageId, @PageableDefault(size = 20) Pageable pageable) {
    Page<VoyagePrice> voyagePrices = voyageService.getAllPricesByVoyageId(voyageId, pageable);
    Page<VoyagePriceResponse> mapped = voyagePrices.map(VoyagePriceResponse::fromEntity);
    return ResponseEntity.ok(PageResponse.from(mapped));
  }
}
