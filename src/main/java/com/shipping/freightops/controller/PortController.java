package com.shipping.freightops.controller;

import com.shipping.freightops.dto.CreatePortRequest;
import com.shipping.freightops.dto.PortResponse;
import com.shipping.freightops.entity.Port;
import com.shipping.freightops.service.PortService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** REST controller for managing ports. */
@RestController
@RequestMapping("/api/v1/ports")
public class PortController {

  private final PortService service;

  public PortController(PortService service) {
    this.service = service;
  }

  @Operation(summary = "Create a new port")
  @ApiResponses({
    @ApiResponse(responseCode = "201", description = "Port successfully created"),
    @ApiResponse(responseCode = "400", description = "Invalid input data"),
    @ApiResponse(responseCode = "409", description = "Port with this UN/LOCODE already exists")
  })
  @PostMapping
  public ResponseEntity<PortResponse> create(@Valid @RequestBody CreatePortRequest request) {
    Port port = service.createPort(request);
    PortResponse body = PortResponse.fromEntity(port);
    URI location = URI.create("/api/v1/ports/" + port.getId());
    return ResponseEntity.created(location).body(body);
  }

  @Operation(summary = "Get port by ID")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Port found"),
    @ApiResponse(responseCode = "404", description = "Port not found")
  })
  @GetMapping("/{id}")
  public ResponseEntity<PortResponse> getById(@PathVariable Long id) {
    Port port = service.getPort(id);
    return ResponseEntity.ok(PortResponse.fromEntity(port));
  }

  @Operation(summary = "List all ports")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "List of ports retrieved successfully")
  })
  @GetMapping
  public ResponseEntity<List<PortResponse>> list() {
    List<Port> ports = service.getAllPorts();
    List<PortResponse> body = ports.stream().map(PortResponse::fromEntity).toList();
    return ResponseEntity.ok(body);
  }
}
