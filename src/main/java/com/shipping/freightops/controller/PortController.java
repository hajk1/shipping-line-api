package com.shipping.freightops.controller;

import com.shipping.freightops.dto.CreatePortRequest;
import com.shipping.freightops.dto.PortResponse;
import com.shipping.freightops.entity.Port;
import com.shipping.freightops.service.PortService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
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

  /** Create a new port. */
  @PostMapping
  public ResponseEntity<PortResponse> create(@Valid @RequestBody CreatePortRequest request) {
    Port port = service.createPort(request);
    PortResponse body = PortResponse.fromEntity(port);
    URI location = URI.create("/api/v1/ports/" + port.getId());
    return ResponseEntity.created(location).body(body);
  }

  /** Get a single port by ID. */
  @GetMapping("/{id}")
  public ResponseEntity<PortResponse> getById(@PathVariable Long id) {
    Port port = service.getPort(id);
    return ResponseEntity.ok(PortResponse.fromEntity(port));
  }

  /** List all ports. */
  @GetMapping
  public ResponseEntity<List<PortResponse>> list() {
    List<Port> ports = service.getAllPorts();
    List<PortResponse> body = ports.stream().map(PortResponse::fromEntity).toList();
    return ResponseEntity.ok(body);
  }
}
