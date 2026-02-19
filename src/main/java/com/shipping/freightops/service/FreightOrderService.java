package com.shipping.freightops.service;

import com.shipping.freightops.dto.CreateFreightOrderRequest;
import com.shipping.freightops.entity.Container;
import com.shipping.freightops.entity.Customer;
import com.shipping.freightops.entity.FreightOrder;
import com.shipping.freightops.entity.Voyage;
import com.shipping.freightops.enums.VoyageStatus;
import com.shipping.freightops.repository.ContainerRepository;
import com.shipping.freightops.repository.CustomerRepository;
import com.shipping.freightops.repository.FreightOrderRepository;
import com.shipping.freightops.repository.VoyageRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Handles freight order creation and queries. */
@Service
public class FreightOrderService {

  private final FreightOrderRepository orderRepository;
  private final VoyageRepository voyageRepository;
  private final ContainerRepository containerRepository;
  private final CustomerRepository customerRepository;

  public FreightOrderService(
      FreightOrderRepository orderRepository,
      VoyageRepository voyageRepository,
      ContainerRepository containerRepository,
      CustomerRepository customerRepository) {
    this.orderRepository = orderRepository;
    this.voyageRepository = voyageRepository;
    this.containerRepository = containerRepository;
    this.customerRepository = customerRepository;
  }

  @Transactional
  public FreightOrder createOrder(CreateFreightOrderRequest request) {
    Voyage voyage =
        voyageRepository
            .findById(request.getVoyageId())
            .orElseThrow(
                () -> new IllegalArgumentException("Voyage not found: " + request.getVoyageId()));

    if (voyage.getStatus() == VoyageStatus.CANCELLED) {
      throw new IllegalStateException("Cannot book freight on a cancelled voyage");
    }

    Container container =
        containerRepository
            .findById(request.getContainerId())
            .orElseThrow(
                () ->
                    new IllegalArgumentException(
                        "Container not found: " + request.getContainerId()));

    Customer customer =
        customerRepository
            .findById(request.getCustomerId())
            .orElseThrow(
                () ->
                    new IllegalArgumentException("Customer not found: " + request.getCustomerId()));

    FreightOrder order = new FreightOrder();
    order.setVoyage(voyage);
    order.setContainer(container);
    order.setCustomer(customer);
    order.setOrderedBy(request.getOrderedBy());
    order.setNotes(request.getNotes());

    return orderRepository.save(order);
  }

  @Transactional(readOnly = true)
  public FreightOrder getOrder(Long id) {
    return orderRepository
        .findById(id)
        .orElseThrow(() -> new IllegalArgumentException("Freight order not found: " + id));
  }

  @Transactional(readOnly = true)
  public List<FreightOrder> getAllOrders() {
    return orderRepository.findAll();
  }

  @Transactional(readOnly = true)
  public List<FreightOrder> getOrdersByVoyage(Long voyageId) {
    return orderRepository.findByVoyageId(voyageId);
  }
}
