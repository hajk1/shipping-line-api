package com.shipping.freightops.dto;

import com.shipping.freightops.entity.Container;
import com.shipping.freightops.enums.ContainerSize;
import com.shipping.freightops.enums.ContainerType;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Read-only view of a container returned by the API. */
@Getter
@Setter
@NoArgsConstructor
public class ContainerResponse {

  private Long id;
  private String containerCode;
  private ContainerSize size;
  private ContainerType type;
  private int teu;
  private LocalDateTime createdAt;

  /** Factory method to map entity → response DTO. */
  public static ContainerResponse fromEntity(Container container) {
    ContainerResponse dto = new ContainerResponse();
    dto.id = container.getId();
    dto.containerCode = container.getContainerCode();
    dto.size = container.getSize();
    dto.type = container.getType();
    dto.teu = container.getTeu();
    dto.createdAt = container.getCreatedAt();
    return dto;
  }
}
