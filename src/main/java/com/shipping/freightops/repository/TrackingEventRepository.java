package com.shipping.freightops.repository;

import com.shipping.freightops.entity.TrackingEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TrackingEventRepository extends JpaRepository<TrackingEvent,Long>{
}
