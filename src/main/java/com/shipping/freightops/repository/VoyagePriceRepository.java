package com.shipping.freightops.repository;

import com.shipping.freightops.entity.Voyage;
import com.shipping.freightops.entity.VoyagePrice;
import com.shipping.freightops.enums.ContainerSize;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface VoyagePriceRepository extends JpaRepository<VoyagePrice, Long> {
  Optional<VoyagePrice> findByVoyageAndContainerSize(Voyage voyage, ContainerSize containerSize);

  Page<VoyagePrice> findByVoyageId(Long voyageId, Pageable pageable);

  @Query(
      """
    SELECT vp FROM VoyagePrice vp
    JOIN FETCH vp.voyage v
    JOIN FETCH v.departurePort
    JOIN FETCH v.arrivalPort
    WHERE v.departurePort.id = :departurePortId
    AND v.arrivalPort.id = :arrivalPortId
    AND v.id != :excludeVoyageId
    AND vp.containerSize = :containerSize
    ORDER BY v.departureTime DESC
    """)
  List<VoyagePrice> findHistoricalPricesSameRoute(
      @Param("departurePortId") Long departurePortId,
      @Param("arrivalPortId") Long arrivalPortId,
      @Param("excludeVoyageId") Long excludeVoyageId,
      @Param("containerSize") ContainerSize containerSize,
      Pageable pageable);

  @Query(
      """
    SELECT vp FROM VoyagePrice vp
    JOIN FETCH vp.voyage v
    JOIN FETCH v.departurePort
    JOIN FETCH v.arrivalPort
    WHERE v.departurePort.id IN :departurePortIds
    AND v.arrivalPort.id IN :arrivalPortIds
    AND v.id != :excludeVoyageId
    AND vp.containerSize = :containerSize
    ORDER BY v.departureTime DESC
    """)
  List<VoyagePrice> findHistoricalPricesSimilarRoute(
      @Param("departurePortIds") List<Long> departurePortIds,
      @Param("arrivalPortIds") List<Long> arrivalPortIds,
      @Param("excludeVoyageId") Long excludeVoyageId,
      @Param("containerSize") ContainerSize containerSize,
      Pageable pageable);
}
