package com.shipping.freightops.controller;

import com.shipping.freightops.dto.CreateVoyageRequest;
import com.shipping.freightops.dto.VoyageResponse;
import com.shipping.freightops.entity.Voyage;
import com.shipping.freightops.enums.VoyageStatus;
import com.shipping.freightops.service.VoyageService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/voyages")
public class VoyageController {
    private final VoyageService voyageService;

    public VoyageController(VoyageService voyageService) {
        this.voyageService = voyageService;
    }
    @GetMapping()
    public ResponseEntity<List<VoyageResponse>> getAll(){
        List<Voyage> voyages = voyageService.getAll();
        return ResponseEntity.ok(VoyageResponse.VoyageResponses(voyages));
    }
    @GetMapping("/{voyageId}")
    public ResponseEntity<VoyageResponse> getById(@PathVariable Long voyageId){
        Voyage voyage = voyageService.getById(voyageId);
        VoyageResponse response = new VoyageResponse(voyage);
        return ResponseEntity.ok(response);
    }
    @PostMapping
    public ResponseEntity<VoyageResponse> addVoyage(@RequestBody CreateVoyageRequest voyageRequest){
        Voyage voyage = voyageService.addVoyage(voyageRequest);
        VoyageResponse response = new VoyageResponse(voyage);
        return ResponseEntity.created(URI.create("/api/v1/voyages")).body(response);
    }
    @PatchMapping("/{voyageId}/{status}")
    public ResponseEntity<VoyageResponse> updateVoyage(@PathVariable Long voyageId, @PathVariable VoyageStatus status) {
        Voyage updatedVoyage = voyageService.updateStatus(status,voyageId);
        VoyageResponse response = new VoyageResponse(updatedVoyage);
        return ResponseEntity.ok(response);
    }
    @GetMapping(params = "status")
    public ResponseEntity<List<VoyageResponse>> getAllByStatus(@RequestParam VoyageStatus status){
            List<Voyage> voyages = voyageService.getAllByStatus(status);
            return ResponseEntity.ok(VoyageResponse.VoyageResponses(voyages));
    }

}
