package com.shipping.freightops.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shipping.freightops.dto.CreateCustomerRequest;
import com.shipping.freightops.entity.Customer;
import com.shipping.freightops.repository.CustomerRepository;
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
class CustomerControllerTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;
  @Autowired private CustomerRepository customerRepository;

  @BeforeEach
  void setUp() {
    customerRepository.deleteAll();
  }

  @Test
  @DisplayName("POST /api/v1/customers → 201 Created")
  void createCustomer_returnsCreated() throws Exception {
    CreateCustomerRequest request = new CreateCustomerRequest();
    request.setCompanyName("Acme Corp");
    request.setContactName("John Doe");
    request.setEmail("john@acme.com");
    request.setPhone("123-456-7890");

    mockMvc
        .perform(
            post("/api/v1/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").exists())
        .andExpect(jsonPath("$.companyName").value("Acme Corp"))
        .andExpect(jsonPath("$.contactName").value("John Doe"))
        .andExpect(jsonPath("$.email").value("john@acme.com"))
        .andExpect(jsonPath("$.phone").value("123-456-7890"));
  }

  @Test
  @DisplayName("POST /api/v1/customers with invalid email → 400 Bad Request")
  void createCustomer_withInvalidEmail_returnsBadRequest() throws Exception {
    CreateCustomerRequest request = new CreateCustomerRequest();
    request.setCompanyName("Acme Corp");
    request.setContactName("John Doe");
    request.setEmail("not-a-valid-email");

    mockMvc
        .perform(
            post("/api/v1/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("POST /api/v1/customers with missing fields → 400 Bad Request")
  void createCustomer_withMissingFields_returnsBadRequest() throws Exception {
    CreateCustomerRequest request = new CreateCustomerRequest();
    // intentionally leaving required fields empty

    mockMvc
        .perform(
            post("/api/v1/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("GET /api/v1/customers/{id} → 200 OK")
  void getCustomerById_returnsOk() throws Exception {
    Customer customer = new Customer("Acme Corp", "John Doe", "john@acme.com");
    customer = customerRepository.save(customer);

    mockMvc
        .perform(get("/api/v1/customers/" + customer.getId()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(customer.getId()))
        .andExpect(jsonPath("$.companyName").value("Acme Corp"))
        .andExpect(jsonPath("$.email").value("john@acme.com"));
  }

  @Test
  @DisplayName("GET /api/v1/customers/{id} with non-existent id → 400 Bad Request")
  void getCustomerById_notFound_returnsBadRequest() throws Exception {
    mockMvc.perform(get("/api/v1/customers/999")).andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("GET /api/v1/customers → 200 OK with list")
  void listCustomers_returnsOk() throws Exception {
    customerRepository.save(new Customer("Acme Corp", "John Doe", "john@acme.com"));
    customerRepository.save(new Customer("Beta Ltd", "Jane Smith", "jane@beta.com"));

    mockMvc
        .perform(get("/api/v1/customers"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$.length()").value(2))
        .andExpect(jsonPath("$[0].companyName").value("Acme Corp"))
        .andExpect(jsonPath("$[1].companyName").value("Beta Ltd"));
  }
}
