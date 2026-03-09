package com.shipping.freightops.repository;

import com.shipping.freightops.entity.Vessel;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VesselRepository extends JpaRepository<Vessel, Long> {

  Optional<Vessel> findByImoNumber(String imoNumber);
}
