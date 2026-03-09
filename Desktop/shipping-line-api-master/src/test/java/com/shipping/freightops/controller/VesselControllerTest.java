package com.shipping.freightops.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shipping.freightops.dto.CreateVesselRequest;
import com.shipping.freightops.entity.Vessel;
import com.shipping.freightops.repository.VesselRepository;
import com.shipping.freightops.repository.VoyageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Integration test for {@link VesselController}.
 *
 * <p>Uses H2 in-memory DB (see src/test/resources/application.properties). This is a good reference
 * for writing additional controller tests.
 */
@SpringBootTest
@AutoConfigureMockMvc
public class VesselControllerTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private VoyageRepository voyageRepository;
  @Autowired private VesselRepository vesselRepository;
  @Autowired private ObjectMapper objectMapper;

  private Vessel savedVessel;

  @BeforeEach
  void setUp() {
    voyageRepository.deleteAll();
    vesselRepository.deleteAll();
    Vessel vessel = new Vessel("MV Test", "9999999", 3000);
    savedVessel = vesselRepository.save(vessel);
  }

  @Test
  @DisplayName("POST /api/v1/vessels → 201 Created")
  void createVessel_returnsCreated() throws Exception {
    CreateVesselRequest request = new CreateVesselRequest();
    request.setName(savedVessel.getName());
    request.setCapacityTeu(savedVessel.getCapacityTeu());
    request.setImoNumber("8888888");

    mockMvc
        .perform(
            post("/api/v1/vessels")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.name").value("MV Test"))
        .andExpect(jsonPath("$.imoNumber").value("8888888"))
        .andExpect(jsonPath("$.capacityTeu").value(3000));
  }

  @Test
  @DisplayName("POST /api/v1/vessels → 409 Conflict")
  void createVessel_returnsConflict() throws Exception {
    CreateVesselRequest request = new CreateVesselRequest();
    request.setName(savedVessel.getName());
    request.setCapacityTeu(savedVessel.getCapacityTeu());
    request.setImoNumber(savedVessel.getImoNumber());

    mockMvc
        .perform(
            post("/api/v1/vessels")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isConflict());
  }

  @Test
  @DisplayName("POST /api/v1/vessels with missing name field → 400 Bad Request")
  void createVessel_returnsBadRequest() throws Exception {
    CreateVesselRequest request = new CreateVesselRequest();
    request.setCapacityTeu(savedVessel.getCapacityTeu());
    request.setImoNumber(savedVessel.getImoNumber());

    mockMvc
        .perform(
            post("/api/v1/vessels")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("POST /api/v1/vessels with invalid ImoNumber → 400 Bad Request")
  void createVessel_invalidImoNumber_returnsBadRequest() throws Exception {
    CreateVesselRequest request = new CreateVesselRequest();
    request.setName(savedVessel.getName());
    request.setCapacityTeu(savedVessel.getCapacityTeu());
    request.setImoNumber("123456");

    mockMvc
        .perform(
            post("/api/v1/vessels")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("GET /api/v1/vessels → 200 OK with list")
  void listVessels_returnsOk() throws Exception {
    mockMvc
        .perform(get("/api/v1/vessels"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray());
  }
}
