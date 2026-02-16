package com.shipping.freightops.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shipping.freightops.dto.CreateVoyageRequest;
import com.shipping.freightops.entity.Port;
import com.shipping.freightops.entity.Vessel;
import com.shipping.freightops.entity.Voyage;
import com.shipping.freightops.enums.VoyageStatus;
import com.shipping.freightops.repository.PortRepository;
import com.shipping.freightops.repository.VesselRepository;
import com.shipping.freightops.repository.VoyageRepository;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.time.LocalDateTime;

@SpringBootTest
@AutoConfigureMockMvc
public class VoyageControllerTest {
    @Autowired
    private VoyageRepository voyageRepository;
    @Autowired
    private  PortRepository portRepository;
    @Autowired
    private  MockMvc mockMvc;
    @Autowired
    private VesselRepository vesselRepository;
    private Vessel vessel;
    private Port arrivalPort;
    private Port departurePort;
    private Voyage voyage;
    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp(){
        voyageRepository.deleteAll();
        vesselRepository.deleteAll();
        portRepository.deleteAll();
        Port port = new Port("TGKRY","kalgary","Togo");
        Port port2 = new Port("JPTKY","tokyo","Japan");
        Vessel vessel = new Vessel("SeeFox","111",1);
        departurePort = portRepository.save(port);
        arrivalPort = portRepository.save(port2);
        this.vessel = vesselRepository.save(vessel);
        Voyage voyage = new Voyage();
        voyage.setVoyageNumber("E-228");
        voyage.setVessel(vessel);
        voyage.setArrivalTime(LocalDateTime.of(2026,11,14,6,23));
        voyage.setDepartureTime(LocalDateTime.now());
        voyage.setDeparturePort(port);
        voyage.setArrivalPort(port2);
        this.voyage = voyageRepository.save(voyage);
    }

    @Test
    @DisplayName("GET: /api/v1/voyages -> Ok")
    public void getAll() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/voyages"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$").isArray());

    }
    @Test
    @DisplayName("GET: /api/v1/voyages -> Ok")
    public void getAllByStatus() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/voyages")
                        .param("status",voyage.getStatus().toString()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$").isArray());

    }
    @Test
    @DisplayName("GET: /api/v1/voyages/{voyageId} -> Ok")
    public void getByIdFound() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/voyages/"+voyage.getId()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$").isMap());
    }
    @Test
    @DisplayName("GET: /api/v1/voyages/{voyageId} -> 404: not found")
    public void getByIdNotFound() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/voyages/"+(int)(Math.random() *1000)))
                .andExpect(MockMvcResultMatchers.status().is4xxClientError())
                .andExpect(MockMvcResultMatchers
                        .content()
                        .string(CoreMatchers.containsStringIgnoringCase("voyage not found")));
    }
    @Test
    @DisplayName("POST: /api/v1/voyages -> 201 Created")
    public void creationSuccessfully() throws Exception{
        CreateVoyageRequest voyageRequest = new CreateVoyageRequest();
        voyageRequest.setVesselId(this.vessel.getId());
        voyageRequest.setVoyageNumber("E-22I");
        voyageRequest.setDeparturePortId(this.departurePort.getId());
        voyageRequest.setArrivalPortId(this.arrivalPort.getId());
        voyageRequest.setDepartureTime(LocalDateTime.of(2026,12,12,12,0));
        voyageRequest.setArrivalTime(LocalDateTime.of(2026,12,12,22,0));
        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/voyages")
                        .contentType("application/json")
                .content(objectMapper.writeValueAsString(voyageRequest)))
                .andExpect(MockMvcResultMatchers.status().isCreated());
    }
    @Test
    @DisplayName("POST: /api/v1/voyages -> 400 BadRequest")
    public void creationFailedWithInvalidArrivalTime() throws Exception{
        CreateVoyageRequest voyageRequest = new CreateVoyageRequest();
        voyageRequest.setVesselId(this.vessel.getId());
        voyageRequest.setDeparturePortId(this.departurePort.getId());
        voyageRequest.setArrivalPortId(this.arrivalPort.getId());
        voyageRequest.setDepartureTime(LocalDateTime.of(2026,12,12,12,0));
        voyageRequest.setArrivalTime(LocalDateTime.of(2026,12,12,11,0));
        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/voyages")
                        .content(objectMapper.writeValueAsString(voyageRequest)))
                .andExpect(MockMvcResultMatchers.status().is4xxClientError());
    }
    @Test
    @DisplayName("POST: /api/v1/voyages -> 400 BadRequest")
    public void creationFailedWithInvalidDepartureTime() throws Exception{
        CreateVoyageRequest voyageRequest = new CreateVoyageRequest();
        voyageRequest.setVesselId(this.vessel.getId());
        voyageRequest.setDeparturePortId(this.departurePort.getId());
        voyageRequest.setArrivalPortId(this.arrivalPort.getId());
        voyageRequest.setDepartureTime(LocalDateTime.of(2026,2,14,12,0));
        voyageRequest.setArrivalTime(LocalDateTime.of(2026,12,12,11,0));
        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/voyages")
                        .content(objectMapper.writeValueAsString(voyageRequest)))
                .andExpect(MockMvcResultMatchers.status().is4xxClientError());
    }
    @Test
    @DisplayName("Patch: /api/v1/voyages/{voyageId}/{status}")
    public void updateStatus() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.patch("/api/v1/voyages/"
                        +this.voyage.getId().toString() + "/" +this.voyage.getStatus().name()))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

}
