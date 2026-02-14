package com.shipping.freightops.controller;

import com.shipping.freightops.dto.CreateFreightOrderRequest;
import com.shipping.freightops.dto.FreightOrderResponse;
import com.shipping.freightops.dto.PageResponse;
import com.shipping.freightops.entity.FreightOrder;
import com.shipping.freightops.service.FreightOrderService;
import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Sample REST controller for freight orders.
 *
 * <p>This is a fully working example — use it as a reference when building controllers for Voyage,
 * Container, Vessel, and Port.
 */
@RestController
@RequestMapping("/api/v1/freight-orders")
public class FreightOrderController {

  private final FreightOrderService service;

  public FreightOrderController(FreightOrderService service) {
    this.service = service;
  }

  /** Create a new freight order. */
  @PostMapping
  public ResponseEntity<FreightOrderResponse> create(
      @Valid @RequestBody CreateFreightOrderRequest request) {
    FreightOrder order = service.createOrder(request);
    FreightOrderResponse body = FreightOrderResponse.fromEntity(order);
    URI location = URI.create("/api/v1/freight-orders/" + order.getId());
    return ResponseEntity.created(location).body(body);
  }

  /** Get a single freight order by ID. */
  @GetMapping("/{id}")
  public ResponseEntity<FreightOrderResponse> getById(@PathVariable Long id) {
    FreightOrder order = service.getOrder(id);
    return ResponseEntity.ok(FreightOrderResponse.fromEntity(order));
  }

  /** List all freight orders, optionally filtered by voyage. */
  @GetMapping
  public ResponseEntity<PageResponse<FreightOrderResponse>> list(
      @RequestParam(required = false) Long voyageId,
      @PageableDefault(size = 20) Pageable pageable) {
    Page<FreightOrder> orders =
        (voyageId != null)
            ? service.getOrdersByVoyage(voyageId, pageable)
            : service.getAllOrders(pageable);

    Page<FreightOrderResponse> mapped = orders.map(FreightOrderResponse::fromEntity);
    return ResponseEntity.ok(PageResponse.from(mapped));
  }
}
