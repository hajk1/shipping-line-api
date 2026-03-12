package com.shipping.freightops.service;

import com.shipping.freightops.entity.*;
import com.shipping.freightops.repository.TrackingEventRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class TrackingEventService {
  private final TrackingEventRepository trackingEventRepository;

  public TrackingEventService(TrackingEventRepository trackingEventRepository) {
    this.trackingEventRepository = trackingEventRepository;
  }

  public TrackingEvent createEvent(TrackingEvent event) {
    return trackingEventRepository.save(event);
  }

  public List<TrackingEvent> getAllEventsByOrderId(Long id) {
    return trackingEventRepository.findAllByFreightOrder_IdOrderByCreatedAtAsc(id);
  }
}
