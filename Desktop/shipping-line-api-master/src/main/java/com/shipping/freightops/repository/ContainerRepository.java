package com.shipping.freightops.repository;

import com.shipping.freightops.entity.Container;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContainerRepository extends JpaRepository<Container, Long> {

  Optional<Container> findByContainerCode(String containerCode);
}
