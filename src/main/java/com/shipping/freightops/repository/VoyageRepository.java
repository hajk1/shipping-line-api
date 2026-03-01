package com.shipping.freightops.repository;

import com.shipping.freightops.entity.Voyage;
import com.shipping.freightops.enums.VoyageStatus;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface VoyageRepository extends JpaRepository<Voyage, Long> {

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("SELECT v FROM Voyage v WHERE v.id = :id")
  Optional<Voyage> findByIdForUpdate(@Param("id") Long id);

  Optional<Voyage> findByVoyageNumber(String voyageNumber);

  List<Voyage> findAllByStatus(VoyageStatus status);
}
