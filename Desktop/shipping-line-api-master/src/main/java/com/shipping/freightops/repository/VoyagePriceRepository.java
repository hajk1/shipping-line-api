package com.shipping.freightops.repository;

import com.shipping.freightops.entity.Voyage;
import com.shipping.freightops.entity.VoyagePrice;
import com.shipping.freightops.enums.ContainerSize;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VoyagePriceRepository extends JpaRepository<VoyagePrice, Long> {
  Optional<VoyagePrice> findByVoyageAndContainerSize(Voyage voyage, ContainerSize containerSize);

  Page<VoyagePrice> findByVoyageId(Long voyageId, Pageable pageable);
}
