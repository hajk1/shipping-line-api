package com.shipping.freightops.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shipping.freightops.dto.AddVesselOwnerRequest;
import com.shipping.freightops.entity.Vessel;
import com.shipping.freightops.entity.VesselOwner;
import com.shipping.freightops.repository.VesselOwnerRepository;
import com.shipping.freightops.repository.VesselRepository;
import com.shipping.freightops.repository.VoyageRepository;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/** Integration tests for {@link VesselOwnerController}. */
@SpringBootTest
@AutoConfigureMockMvc
class VesselOwnerControllerTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;
  @Autowired private VesselOwnerRepository vesselOwnerRepository;
  @Autowired private VesselRepository vesselRepository;
  @Autowired private VoyageRepository voyageRepository;

  private Vessel savedVessel;

  @BeforeEach
  void setUp() {
    voyageRepository.deleteAll();
    vesselOwnerRepository.deleteAll();
    vesselRepository.deleteAll();
    savedVessel = vesselRepository.save(new Vessel("MV Test", "1234567", 500));
  }

  // ── ADD OWNER ──

  @Test
  @DisplayName("POST /api/v1/vessels/{vesselId}/owners → 201 Created")
  void addOwner_returnsCreated() throws Exception {
    AddVesselOwnerRequest request = new AddVesselOwnerRequest();
    request.setOwnerName("Alice Corp");
    request.setOwnerEmail("alice@corp.com");
    request.setSharePercent(new BigDecimal("40.00"));

    mockMvc
        .perform(
            post("/api/v1/vessels/" + savedVessel.getId() + "/owners")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").isNumber())
        .andExpect(jsonPath("$.ownerName").value("Alice Corp"))
        .andExpect(jsonPath("$.ownerEmail").value("alice@corp.com"))
        .andExpect(jsonPath("$.sharePercent").value(40.00))
        .andExpect(jsonPath("$.createdAt").isNotEmpty())
        .andExpect(jsonPath("$.updatedAt").isNotEmpty());
  }

  @Test
  @DisplayName("POST /api/v1/vessels/{vesselId}/owners → multiple owners within 100%")
  void addOwner_multipleOwners_withinLimit() throws Exception {
    addOwnerDirectly(savedVessel, "Owner A", "a@test.com", new BigDecimal("50.00"));

    AddVesselOwnerRequest request = new AddVesselOwnerRequest();
    request.setOwnerName("Owner B");
    request.setOwnerEmail("b@test.com");
    request.setSharePercent(new BigDecimal("50.00"));

    mockMvc
        .perform(
            post("/api/v1/vessels/" + savedVessel.getId() + "/owners")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.ownerName").value("Owner B"));
  }

  @Test
  @DisplayName("POST /api/v1/vessels/{vesselId}/owners → 409 when total exceeds 100%")
  void addOwner_exceedsTotalShare_returnsConflict() throws Exception {
    addOwnerDirectly(savedVessel, "Owner A", "a@test.com", new BigDecimal("70.00"));

    AddVesselOwnerRequest request = new AddVesselOwnerRequest();
    request.setOwnerName("Owner B");
    request.setOwnerEmail("b@test.com");
    request.setSharePercent(new BigDecimal("40.00"));

    mockMvc
        .perform(
            post("/api/v1/vessels/" + savedVessel.getId() + "/owners")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isConflict());
  }

  @Test
  @DisplayName("POST /api/v1/vessels/{vesselId}/owners → 400 when sharePercent is 0")
  void addOwner_zeroShare_returnsBadRequest() throws Exception {
    AddVesselOwnerRequest request = new AddVesselOwnerRequest();
    request.setOwnerName("Owner X");
    request.setOwnerEmail("x@test.com");
    request.setSharePercent(new BigDecimal("0.00"));

    mockMvc
        .perform(
            post("/api/v1/vessels/" + savedVessel.getId() + "/owners")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("POST /api/v1/vessels/{vesselId}/owners → 400 when required fields are missing")
  void addOwner_missingFields_returnsBadRequest() throws Exception {
    AddVesselOwnerRequest request = new AddVesselOwnerRequest();

    mockMvc
        .perform(
            post("/api/v1/vessels/" + savedVessel.getId() + "/owners")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("POST /api/v1/vessels/{vesselId}/owners → 404 when vessel not found")
  void addOwner_vesselNotFound_returnsNotFound() throws Exception {
    AddVesselOwnerRequest request = new AddVesselOwnerRequest();
    request.setOwnerName("Owner X");
    request.setOwnerEmail("x@test.com");
    request.setSharePercent(new BigDecimal("10.00"));

    mockMvc
        .perform(
            post("/api/v1/vessels/9999/owners")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isNotFound());
  }

  // ── LIST OWNERS ──

  @Test
  @DisplayName("GET /api/v1/vessels/{vesselId}/owners → 200 OK with list")
  void listOwners_returnsOk() throws Exception {
    addOwnerDirectly(savedVessel, "Alice", "alice@test.com", new BigDecimal("30.00"));
    addOwnerDirectly(savedVessel, "Bob", "bob@test.com", new BigDecimal("20.00"));

    mockMvc
        .perform(get("/api/v1/vessels/" + savedVessel.getId() + "/owners"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(2));
  }

  @Test
  @DisplayName("GET /api/v1/vessels/{vesselId}/owners → 404 when vessel not found")
  void listOwners_vesselNotFound_returnsNotFound() throws Exception {
    mockMvc.perform(get("/api/v1/vessels/9999/owners")).andExpect(status().isNotFound());
  }

  // ── DELETE OWNER ──

  @Test
  @DisplayName("DELETE /api/v1/vessels/{vesselId}/owners/{ownerId} → 204 No Content")
  void removeOwner_returnsNoContent() throws Exception {
    VesselOwner owner =
        addOwnerDirectly(savedVessel, "Alice", "alice@test.com", new BigDecimal("25.00"));

    mockMvc
        .perform(delete("/api/v1/vessels/" + savedVessel.getId() + "/owners/" + owner.getId()))
        .andExpect(status().isNoContent());
  }

  @Test
  @DisplayName("DELETE /api/v1/vessels/{vesselId}/owners/{ownerId} → 404 when owner not found")
  void removeOwner_notFound_returnsNotFound() throws Exception {
    mockMvc
        .perform(delete("/api/v1/vessels/" + savedVessel.getId() + "/owners/9999"))
        .andExpect(status().isNotFound());
  }

  // ── HELPER ──

  private VesselOwner addOwnerDirectly(Vessel vessel, String name, String email, BigDecimal share) {
    VesselOwner owner = new VesselOwner();
    owner.setVessel(vessel);
    owner.setOwnerName(name);
    owner.setOwnerEmail(email);
    owner.setSharePercent(share);
    return vesselOwnerRepository.save(owner);
  }
}
