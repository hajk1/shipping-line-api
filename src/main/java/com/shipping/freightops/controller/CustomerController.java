package com.shipping.freightops.controller;

import com.shipping.freightops.dto.CreateCustomerRequest;
import com.shipping.freightops.dto.CustomerResponse;
import com.shipping.freightops.entity.Customer;
import com.shipping.freightops.service.CustomerService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/customers")
public class CustomerController {

  private final CustomerService service;

  public CustomerController(CustomerService service) {
    this.service = service;
  }

  @PostMapping
  public ResponseEntity<CustomerResponse> create(
      @Valid @RequestBody CreateCustomerRequest request) {
    Customer customer = service.createCustomer(request);
    CustomerResponse body = CustomerResponse.fromEntity(customer);
    URI location = URI.create("/api/v1/customers/" + body.getId());
    return ResponseEntity.created(location).body(body);
  }

  @GetMapping("/{id}")
  public ResponseEntity<CustomerResponse> getById(@PathVariable Long id) {
    Customer customer = service.getCustomer(id);
    return ResponseEntity.ok(CustomerResponse.fromEntity(customer));
  }

  @GetMapping
  public ResponseEntity<List<CustomerResponse>> list() {
    List<Customer> customers = service.getAllCustomers();

    List<CustomerResponse> body = customers.stream().map(CustomerResponse::fromEntity).toList();
    return ResponseEntity.ok(body);
  }
}
