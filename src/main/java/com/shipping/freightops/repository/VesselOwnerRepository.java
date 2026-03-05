package com.shipping.freightops.repository;

import com.shipping.freightops.entity.VesselOwner;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface VesselOwnerRepository extends JpaRepository<VesselOwner, Long> {

  List<VesselOwner> findByVesselId(Long vesselId);

  @Query("SELECT COALESCE(SUM(o.sharePercent), 0) FROM VesselOwner o WHERE o.vessel.id = :vesselId")
  BigDecimal sumSharePercentByVesselId(@Param("vesselId") Long vesselId);
}
