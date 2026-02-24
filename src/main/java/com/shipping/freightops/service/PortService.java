package com.shipping.freightops.service;

import com.shipping.freightops.dto.CreatePortRequest;
import com.shipping.freightops.entity.Port;
import com.shipping.freightops.repository.PortRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Handles port creation and queries. */
@Service
public class PortService {

  private final PortRepository portRepository;

  public PortService(PortRepository portRepository) {
    this.portRepository = portRepository;
  }

  @Transactional
  public Port createPort(CreatePortRequest request) {
    if (portRepository.existsByUnlocode(request.getUnlocode())) {
      throw new IllegalStateException(
          "Port with unlocode already exists: " + request.getUnlocode());
    }

    Port port = new Port();
    port.setUnlocode(request.getUnlocode());
    port.setName(request.getName());
    port.setCountry(request.getCountry());
    return portRepository.save(port);
  }

  @Transactional(readOnly = true)
  public Port getPort(Long id) {
    return portRepository
        .findById(id)
        .orElseThrow(() -> new IllegalArgumentException("Port not found: " + id));
  }

  @Transactional(readOnly = true)
  public List<Port> getAllPorts() {
    return portRepository.findAll();
  }
}
