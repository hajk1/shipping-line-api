package com.shipping.freightops.service;

import com.shipping.freightops.entity.FreightOrder;
import com.shipping.freightops.repository.FreightOrderRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TrackingService {

  private final FreightOrderRepository orderRepository;

  public TrackingService(FreightOrderRepository orderRepository) {
    this.orderRepository = orderRepository;
  }

  @Transactional(readOnly = true)
  public FreightOrder trackOrder(Long orderId) {
    return orderRepository
        .findById(orderId)
        .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));
  }

  @Transactional(readOnly = true)
  public List<FreightOrder> trackContainer(String containerCode) {
    List<FreightOrder> orders = orderRepository.findByContainerCode(containerCode);
    if (orders.isEmpty())
      throw new IllegalArgumentException("Container not found: " + containerCode);
    return orders;
  }
}
