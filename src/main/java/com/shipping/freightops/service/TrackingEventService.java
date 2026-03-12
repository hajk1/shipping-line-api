package com.shipping.freightops.service;
import com.shipping.freightops.entity.*;
import com.shipping.freightops.repository.TrackingEventRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TrackingEventService {
    private final TrackingEventRepository trackingEventRepository;

    public TrackingEventService(TrackingEventRepository trackingEventRepository) {
        this.trackingEventRepository = trackingEventRepository;
    }

    public TrackingEvent createEvent(TrackingEvent event){
        return trackingEventRepository.save(event);
    }
    public List<TrackingEvent> getAllEventsByOrderId(Long id){
        return trackingEventRepository.findAllByFreightOrder_Id(id);
    }
}
