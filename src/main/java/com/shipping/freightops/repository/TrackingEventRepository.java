package com.shipping.freightops.repository;

import com.shipping.freightops.entity.TrackingEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TrackingEventRepository extends JpaRepository<TrackingEvent,Long>{
    List<TrackingEvent> findAllByFreightOrder_Id(Long id);
}
