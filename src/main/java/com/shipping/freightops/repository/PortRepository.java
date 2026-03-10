package com.shipping.freightops.repository;

import com.shipping.freightops.entity.Port;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PortRepository extends JpaRepository<Port, Long> {

  Optional<Port> findByUnlocode(String unlocode);

  boolean existsByUnlocode(String unlocode);

  List<Port> findByCountryIn(List<String> countries);
}
