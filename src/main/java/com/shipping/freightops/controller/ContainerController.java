package com.shipping.freightops.controller;

import com.shipping.freightops.dto.ContainerLabelResponse;
import com.shipping.freightops.service.ContainerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/containers")
public class ContainerController {

  private final ContainerService containerService;

  public ContainerController(ContainerService containerService) {
    this.containerService = containerService;
  }

  @Operation(
      summary = "Get PDF label for a container by ID",
      description = "Returns a PDF document containing the container label")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "PDF label successfully generated"),
    @ApiResponse(responseCode = "404", description = "Container not found"),
    @ApiResponse(responseCode = "500", description = "Error generating PDF label")
  })
  @GetMapping(value = "/{id}/label", produces = MediaType.APPLICATION_PDF_VALUE)
  public ResponseEntity<byte[]> getLabelForContainer(@PathVariable long id) {
    ContainerLabelResponse responseDto = containerService.generateContainerLabel(id);

    return ResponseEntity.ok()
        .header(
            HttpHeaders.CONTENT_DISPOSITION,
            "inline; filename=\"" + responseDto.getFileName() + "\"")
        .contentType(MediaType.APPLICATION_PDF)
        .body(responseDto.getContent());
  }
}
