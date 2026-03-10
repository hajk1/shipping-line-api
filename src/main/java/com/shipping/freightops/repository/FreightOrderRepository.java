package com.shipping.freightops.repository;

import com.shipping.freightops.entity.FreightOrder;
import com.shipping.freightops.enums.OrderStatus;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FreightOrderRepository extends JpaRepository<FreightOrder, Long> {

  long countByVoyageId(Long voyageId);

  Page<FreightOrder> findByVoyageId(Long voyageId, Pageable pageable);

  Page<FreightOrder> findByStatus(OrderStatus status, Pageable pageable);

  List<FreightOrder> findByAgentId(Long agentId);

  Page<FreightOrder> findByOrderedBy(String orderedBy, Pageable pageable);

  List<FreightOrder> findByVoyageIdAndStatusIn(Long voyageId, List<OrderStatus> statuses);

  List<FreightOrder> findByVoyageIdAndStatus(Long voyageId, OrderStatus status);

  @Query(
      """
    SELECT COALESCE(SUM(c.teu), 0)
    FROM FreightOrder fo
    JOIN fo.container c
    WHERE fo.voyage.id = :voyageId
    """)
  int sumTeuByVoyageId(@Param("voyageId") Long voyageId);

  @Query(
      """
    SELECT f.voyage.id, COUNT(f)
    FROM FreightOrder f
    WHERE f.voyage.id IN :ids
    GROUP BY f.voyage.id
    """)
  List<Object[]> countByVoyageIds(@Param("ids") List<Long> ids);

  @Query(
      """
    SELECT fo
    FROM FreightOrder fo
    JOIN fo.container c
    WHERE c.containerCode = :containerCode
    """)
  List<FreightOrder> findByContainerCode(@Param("containerCode") String containerCode);
}
