package com.company.legal.client.controller;

import com.company.legal.client.dto.ClientDTO;
import com.company.legal.client.entity.Client;
import com.company.legal.client.repository.ClientRepository;
import com.company.legal.testutil.IntegrationTestBase;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ClientControllerIntegrationTest extends IntegrationTestBase {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ClientRepository clientRepository;

    private String lawFirmId;

    @BeforeEach
    void setUp() {
        lawFirmId = "firm-integration-test";
        clientRepository.deleteAll();
    }

    @AfterEach
    void tearDown() {
        clientRepository.deleteAll();
    }

    @Test
    @Order(1)
    @DisplayName("Create client via API - should return 201")
    void createClient_viaApi() throws Exception {
        ClientDTO dto = ClientDTO.builder()
                .clientNumber("INT-CL-001")
                .clientType(Client.ClientType.INDIVIDUAL)
                .firstName("Integration")
                .lastName("Test")
                .email("integration@test.com")
                .phone("+1234567890")
                .status(Client.ClientStatus.ACTIVE)
                .build();

        mockMvc.perform(post("/v1/clients")
                        .header("X-Law-Firm-Id", lawFirmId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.clientNumber").value("INT-CL-001"))
                .andExpect(jsonPath("$.data.firstName").value("Integration"))
                .andExpect(jsonPath("$.data.lastName").value("Test"));
    }

    @Test
    @Order(2)
    @DisplayName("Get client via API - should return 404 for non-existent")
    void getClient_notFound() throws Exception {
        mockMvc.perform(get("/v1/clients/non-existent-id"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("RESOURCE_NOT_FOUND"));
    }

    @Test
    @Order(3)
    @DisplayName("List clients via API - should return empty page initially")
    void listClients_empty() throws Exception {
        mockMvc.perform(get("/v1/clients")
                        .header("X-Law-Firm-Id", lawFirmId)
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.totalElements").value(0))
                .andExpect(jsonPath("$.data.empty").value(true));
    }

    @Test
    @Order(4)
    @DisplayName("Create client with duplicate number - should return 400")
    void createClient_duplicateNumber() throws Exception {
        ClientDTO dto = ClientDTO.builder()
                .clientNumber("DUP-CL-001")
                .clientType(Client.ClientType.INDIVIDUAL)
                .firstName("Duplicate")
                .lastName("Test")
                .email("dup@test.com")
                .status(Client.ClientStatus.ACTIVE)
                .build();

        // First create
        mockMvc.perform(post("/v1/clients")
                        .header("X-Law-Firm-Id", lawFirmId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());

        // Second create with same number
        ClientDTO dupDto = ClientDTO.builder()
                .clientNumber("DUP-CL-001")
                .clientType(Client.ClientType.INDIVIDUAL)
                .firstName("Another")
                .lastName("Duplicate")
                .email("another@test.com")
                .status(Client.ClientStatus.ACTIVE)
                .build();

        mockMvc.perform(post("/v1/clients")
                        .header("X-Law-Firm-Id", lawFirmId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dupDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(5)
    @DisplayName("Full CRUD cycle - create, get, update, delete")
    void fullCrudCycle() throws Exception {
        // Create
        ClientDTO createDto = ClientDTO.builder()
                .clientNumber("CRUD-CL-001")
                .clientType(Client.ClientType.INDIVIDUAL)
                .firstName("Create")
                .lastName("Phase")
                .email("crud@test.com")
                .status(Client.ClientStatus.ACTIVE)
                .build();

        String createResponse = mockMvc.perform(post("/v1/clients")
                        .header("X-Law-Firm-Id", lawFirmId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String id = objectMapper.readTree(createResponse).path("data").path("id").asText();

        // Get
        mockMvc.perform(get("/v1/clients/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(id))
                .andExpect(jsonPath("$.data.firstName").value("Create"));

        // Update
        ClientDTO updateDto = ClientDTO.builder()
                .firstName("Updated")
                .lastName("Name")
                .email("updated@test.com")
                .phone("+9876543210")
                .build();

        mockMvc.perform(put("/v1/clients/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.firstName").value("Updated"))
                .andExpect(jsonPath("$.data.lastName").value("Name"));

        // Delete
        mockMvc.perform(delete("/v1/clients/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(true));

        // Verify deleted (soft delete returns 404 since deletedAt is set)
        mockMvc.perform(get("/v1/clients/" + id))
                .andExpect(status().isNotFound());
    }

    @Test
    @Order(6)
    @DisplayName("Get active clients - should return active clients")
    void getActiveClients() throws Exception {
        // Create an active client
        ClientDTO dto = ClientDTO.builder()
                .clientNumber("ACTIVE-CL-001")
                .clientType(Client.ClientType.INDIVIDUAL)
                .firstName("Active")
                .lastName("Client")
                .email("active@test.com")
                .status(Client.ClientStatus.ACTIVE)
                .build();

        String response = mockMvc.perform(post("/v1/clients")
                        .header("X-Law-Firm-Id", lawFirmId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        mockMvc.perform(get("/v1/clients/active")
                        .header("X-Law-Firm-Id", lawFirmId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(1));
    }
}
