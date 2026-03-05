package com.shipping.freightops.repository;

import com.shipping.freightops.entity.VoyageCost;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VoyageCostRepository extends JpaRepository<VoyageCost, Long> {
  List<VoyageCost> findByVoyageIdOrderByCreatedAtAsc(Long voyageId);
}
