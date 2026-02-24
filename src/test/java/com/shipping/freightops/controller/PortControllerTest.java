package com.shipping.freightops.controller;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shipping.freightops.dto.CreatePortRequest;
import com.shipping.freightops.entity.Port;
import com.shipping.freightops.repository.FreightOrderRepository;
import com.shipping.freightops.repository.PortRepository;
import com.shipping.freightops.repository.VoyageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/** Integration test for {@link PortController}. */
@SpringBootTest
@AutoConfigureMockMvc
class PortControllerTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;
  @Autowired private PortRepository portRepository;
  @Autowired private FreightOrderRepository freightOrderRepository;
  @Autowired private VoyageRepository voyageRepository;

  @BeforeEach
  void setUp() {
    freightOrderRepository.deleteAll();
    voyageRepository.deleteAll();
    portRepository.deleteAll();
  }

  @Test
  @DisplayName("POST /api/v1/ports → 201 Created")
  void createPort_returnsCreated() throws Exception {
    CreatePortRequest request = new CreatePortRequest();
    request.setUnlocode("AEJEA");
    request.setName("Jebel Ali");
    request.setCountry("UAE");

    mockMvc
        .perform(
            post("/api/v1/ports")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(header().string("Location", containsString("/api/v1/ports/")))
        .andExpect(jsonPath("$.unlocode").value("AEJEA"))
        .andExpect(jsonPath("$.name").value("Jebel Ali"))
        .andExpect(jsonPath("$.country").value("UAE"))
        .andExpect(jsonPath("$.id").exists());
  }

  @Test
  @DisplayName("GET /api/v1/ports → 200 OK with list")
  void listPorts_returnsOk() throws Exception {
    mockMvc
        .perform(get("/api/v1/ports"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray());
  }

  @Test
  @DisplayName("GET /api/v1/ports/{id} when not found → 404 Not Found")
  void getById_notFound_returns404() throws Exception {
    mockMvc.perform(get("/api/v1/ports/99999")).andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("POST /api/v1/ports with duplicate unlocode → 409 Conflict")
  void createPort_duplicateUnlocode_returns409() throws Exception {
    portRepository.save(new Port("AEJEA", "Jebel Ali", "UAE"));

    CreatePortRequest request = new CreatePortRequest();
    request.setUnlocode("AEJEA");
    request.setName("Another Name");
    request.setCountry("UAE");

    mockMvc
        .perform(
            post("/api/v1/ports")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isConflict());
  }
}
