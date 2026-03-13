package com.shipping.freightops.controller;

import com.itextpdf.text.DocumentException;
import com.shipping.freightops.dto.*;
import com.shipping.freightops.entity.FreightOrder;
import com.shipping.freightops.entity.TrackingEvent;
import com.shipping.freightops.repository.FreightOrderRepository;
import com.shipping.freightops.service.FreightOrderService;
import com.shipping.freightops.service.InvoiceService;
import com.shipping.freightops.service.TrackingEventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import java.io.FileNotFoundException;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;

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
  private final InvoiceService invoiceService;
  private final FreightOrderRepository freightOrderRepository;
  private final TrackingEventService trackingEventService;

  public FreightOrderController(
      FreightOrderService service,
      InvoiceService invoiceService,
      FreightOrderRepository freightOrderRepository,
      TrackingEventService trackingEventService) {
    this.service = service;
    this.invoiceService = invoiceService;
    this.freightOrderRepository = freightOrderRepository;
    this.trackingEventService = trackingEventService;
  }

  /** Create a new freight order. */
  @Operation(summary = "Create a new freight order")
  @ApiResponses({
    @ApiResponse(responseCode = "201", description = "Freight order successfully created"),
    @ApiResponse(responseCode = "400", description = "Invalid request data"),
    @ApiResponse(responseCode = "404", description = "Voyage or Container not found"),
    @ApiResponse(responseCode = "409", description = "Cannot book freight on a cancelled voyage"),
    @ApiResponse(responseCode = "409", description = "Booking is closed for this voyage")
  })
  @PostMapping
  public ResponseEntity<FreightOrderResponse> create(
      @Valid @RequestBody CreateFreightOrderRequest request) {
    FreightOrder order = service.createOrder(request);
    FreightOrderResponse body = FreightOrderResponse.fromEntity(order);
    URI location = URI.create("/api/v1/freight-orders/" + order.getId());
    return ResponseEntity.created(location).body(body);
  }

  /** Get a single freight order by ID. */
  @Operation(summary = "Get freight order by ID")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Freight order found"),
    @ApiResponse(responseCode = "404", description = "Freight order not found")
  })
  @GetMapping("/{id}")
  public ResponseEntity<FreightOrderResponse> getById(@PathVariable Long id) {
    FreightOrder order = service.getOrder(id);
    return ResponseEntity.ok(FreightOrderResponse.fromEntity(order));
  }

  /** List all freight orders, optionally filtered by voyage. */
  @Operation(summary = "List all freight orders with optional voyage filter")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Paged list of freight orders returned")
  })
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

  @GetMapping("/{id}/invoice")
  public ResponseEntity<byte[]> getFreightOrderInvoice(@PathVariable Long id) {
    try {
      byte[] pdf = invoiceService.generateInvoice(id);
      return ResponseEntity.ok(pdf);
    } catch (FileNotFoundException | DocumentException e) {
      throw new IllegalStateException();
    }
  }

  @Operation(summary = "Update discount for a freight order")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Freight order updated successfully"),
    @ApiResponse(responseCode = "400", description = "Invalid request"),
    @ApiResponse(responseCode = "404", description = "Freight order not found")
  })
  @PatchMapping("/{id}/discount")
  public ResponseEntity<FreightOrderResponse> updateDiscount(
      @PathVariable Long id, @Valid @RequestBody UpdateDiscountRequest request) {
    FreightOrder order = service.updateDiscount(id, request);
    return ResponseEntity.ok(FreightOrderResponse.fromEntity(order));
  }

  @PostMapping("/{id}/events")
  public ResponseEntity<TrackingEvent> createEvent(
      @Valid @RequestBody TrackingEventRequest eventRequest, @PathVariable Long id, Errors errors) {
    if (errors.hasErrors()) {
      throw new IllegalArgumentException("invalid data");
    }
    TrackingEvent event = new TrackingEvent();
    FreightOrder order =
        freightOrderRepository
            .findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Order not  found"));
    event.setFreightOrder(order);
    event.setDescription(eventRequest.getDescription());
    event.setEventTime(LocalDateTime.now());
    event.setEventType(eventRequest.getEventType());
    event.setLocation(eventRequest.getLocation());
    event.setDescription(eventRequest.getDescription());
    event.setPerformedBy(eventRequest.getPerformedBy());
    TrackingEvent savedEvent = trackingEventService.createEvent(event);
    return ResponseEntity.ok().body(savedEvent);
  }

  @GetMapping("/{id}/events")
  public ResponseEntity<List<TrackingEvent>> getAllEvents(@PathVariable Long id) {
    List<TrackingEvent> events = trackingEventService.getAllEventsByOrderId(id);
    return ResponseEntity.ok().body(events);
  }
}
