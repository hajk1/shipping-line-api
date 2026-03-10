package com.shipping.freightops.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.shipping.freightops.entity.Container;
import com.shipping.freightops.enums.ContainerSize;
import com.shipping.freightops.enums.ContainerType;
import com.shipping.freightops.repository.ContainerRepository;
import com.shipping.freightops.service.ContainerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
public class ContainerControllerTest {
  @Autowired private MockMvc mockMvc;
  @Autowired private ContainerService containerService;
  @Autowired private ContainerRepository containerRepository;

  private Container savedContainer;

  @BeforeEach
  void setUp() {
    savedContainer =
        containerRepository.save(
            new Container("TSTU1234567", ContainerSize.TWENTY_FOOT, ContainerType.DRY));
  }

  @Test
  @DisplayName("GET /api/v1/containers/{id}/label → returns valid PDF")
  void getContainerLabel_returnsValidPdf() throws Exception {

    mockMvc
        .perform(get("/api/v1/containers/{id}/label", savedContainer.getId()))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_PDF))
        .andExpect(
            result -> {
              byte[] bytes = result.getResponse().getContentAsByteArray();
              String header = new String(bytes, 0, 4);
              assertEquals("%PDF", header);
            });
  }
}
