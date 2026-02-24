package com.shipping.freightops.repository;

import com.shipping.freightops.entity.Voyage;
import com.shipping.freightops.enums.VoyageStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VoyageRepository extends JpaRepository<Voyage, Long> {

  Optional<Voyage> findByVoyageNumber(String voyageNumber);

  List<Voyage> findAllByStatus(VoyageStatus status);
}
