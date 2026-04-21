package com.shipping.freightops.dto;

import com.shipping.freightops.enums.ContainerSize;
import com.shipping.freightops.enums.ContainerType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CreateContainerRequest {

  @NotBlank(message = "Container code is required")
  @Size(min = 11, max = 11, message = "Container code must be exactly 11 characters")
  private String containerCode;

  /** Must be one of: TWENTY_FOOT, FORTY_FOOT. */
  @NotNull(message = "Container size is required")
  private ContainerSize size;

  /** Must be one of: DRY, REEFER, OPEN_TOP, FLAT_RACK, TANK. */
  @NotNull(message = "Container type is required")
  private ContainerType type;
}
