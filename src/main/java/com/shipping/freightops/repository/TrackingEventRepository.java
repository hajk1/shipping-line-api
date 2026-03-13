package com.shipping.freightops.repository;

import com.shipping.freightops.entity.TrackingEvent;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TrackingEventRepository extends JpaRepository<TrackingEvent, Long> {
  List<TrackingEvent> findAllByFreightOrder_IdOrderByCreatedAtAsc(Long id);
}
