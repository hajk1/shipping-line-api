package com.shipping.freightops.controller;

import com.shipping.freightops.dto.ContainerLabelResponse;
import com.shipping.freightops.dto.ContainerResponse;
import com.shipping.freightops.dto.CreateContainerRequest;
import com.shipping.freightops.entity.Container;
import com.shipping.freightops.enums.ContainerSize;
import com.shipping.freightops.enums.ContainerType;
import com.shipping.freightops.service.ContainerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/containers")
public class ContainerController {

  private final ContainerService containerService;

  public ContainerController(ContainerService containerService) {
    this.containerService = containerService;
  }

  /** Register a new container. */
  @Operation(summary = "Register a new container")
  @ApiResponses({
    @ApiResponse(responseCode = "201", description = "Container successfully created"),
    @ApiResponse(responseCode = "400", description = "Invalid request data"),
    @ApiResponse(responseCode = "409", description = "Container code already exists")
  })
  @PostMapping
  public ResponseEntity<ContainerResponse> create(
      @Valid @RequestBody CreateContainerRequest request) {
    Container container = containerService.createContainer(request);
    ContainerResponse body = ContainerResponse.fromEntity(container);
    URI location = URI.create("/api/v1/containers/" + container.getId());
    return ResponseEntity.created(location).body(body);
  }

  /** List all containers, optionally filtered by size and/or type. */
  @Operation(summary = "List all containers with optional size and type filters")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "List of containers returned")
  })
  @GetMapping
  public ResponseEntity<List<ContainerResponse>> list(
      @RequestParam(required = false) ContainerSize size,
      @RequestParam(required = false) ContainerType type) {
    List<Container> containers = containerService.getAllContainers(size, type);
    List<ContainerResponse> body = containers.stream().map(ContainerResponse::fromEntity).toList();
    return ResponseEntity.ok(body);
  }

  /** Get a single container by ID. */
  @Operation(summary = "Get a container by ID")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Container found"),
    @ApiResponse(responseCode = "404", description = "Container not found")
  })
  @GetMapping("/{id}")
  public ResponseEntity<ContainerResponse> getById(@PathVariable Long id) {
    Container container = containerService.getContainerById(id);
    return ResponseEntity.ok(ContainerResponse.fromEntity(container));
  }

  @Operation(
      summary = "Get PDF label for a container by ID",
      description = "Returns a PDF document containing the container label")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "PDF label successfully generated"),
    @ApiResponse(responseCode = "404", description = "Container not found"),
    @ApiResponse(responseCode = "500", description = "Error generating PDF label")
  })
  @GetMapping(value = "/{id}/label")
  public ResponseEntity<byte[]> getLabelForContainer(@PathVariable long id) {
    ContainerLabelResponse responseDto = containerService.generateContainerLabel(id);

    return ResponseEntity.ok()
        .header(
            HttpHeaders.CONTENT_DISPOSITION,
            "inline; filename=\"" + responseDto.getFileName() + "\"")
        .contentType(MediaType.APPLICATION_PDF)
        .body(responseDto.getContent());
  }
}
