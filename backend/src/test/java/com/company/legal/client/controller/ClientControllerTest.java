package com.company.legal.client.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.company.legal.client.dto.ClientContactDTO;
import com.company.legal.client.dto.ClientDTO;
import com.company.legal.client.entity.Client;
import com.company.legal.client.service.ClientService;
import com.company.legal.common.dto.ApiResponse;
import com.company.legal.common.dto.PageResponse;
import com.company.legal.common.exception.GlobalExceptionHandler;
import com.company.legal.common.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ClientController.class)
@Import(GlobalExceptionHandler.class)
class ClientControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ClientService clientService;

    private String lawFirmId;
    private ClientDTO clientDTO;
    private ClientDTO savedClientDTO;

    @BeforeEach
    void setUp() {
        lawFirmId = "firm-1";
        clientDTO = ClientDTO.builder()
                .clientNumber("CL-001")
                .clientType(Client.ClientType.INDIVIDUAL)
                .firstName("John")
                .lastName("Doe")
                .email("john@example.com")
                .phone("+1234567890")
                .build();

        savedClientDTO = ClientDTO.builder()
                .id("client-1")
                .clientNumber("CL-001")
                .clientType(Client.ClientType.INDIVIDUAL)
                .firstName("John")
                .lastName("Doe")
                .email("john@example.com")
                .phone("+1234567890")
                .status(Client.ClientStatus.ACTIVE)
                .riskLevel(Client.RiskLevel.LOW)
                .createdAt("2024-01-01T00:00:00")
                .build();
    }

    @Test
    @DisplayName("Create Client - Should return 201 on success")
    void createClient_success() throws Exception {
        when(clientService.createClient(any(ClientDTO.class))).thenReturn(savedClientDTO);

        mockMvc.perform(post("/v1/clients")
                        .header("X-Law-Firm-Id", lawFirmId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(clientDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").value("client-1"))
                .andExpect(jsonPath("$.data.clientNumber").value("CL-001"));
    }

    @Test
    @DisplayName("Create Client - Should return 400 for invalid request")
    void createClient_validationError() throws Exception {
        ClientDTO invalidDTO = ClientDTO.builder()
                .firstName("John")
                .build();

        mockMvc.perform(post("/v1/clients")
                        .header("X-Law-Firm-Id", lawFirmId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Get Client - Should return client by ID")
    void getClientById_success() throws Exception {
        when(clientService.getClientById("client-1")).thenReturn(savedClientDTO);

        mockMvc.perform(get("/v1/clients/client-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value("client-1"))
                .andExpect(jsonPath("$.data.firstName").value("John"));
    }

    @Test
    @DisplayName("Get Client - Should return 404 when client not found")
    void getClientById_notFound() throws Exception {
        when(clientService.getClientById("missing"))
                .thenThrow(new ResourceNotFoundException("Client", "id", "missing"));

        mockMvc.perform(get("/v1/clients/missing"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("List Clients - Should return paginated clients")
    void getClients_success() throws Exception {
        PageResponse<ClientDTO> pageResponse = PageResponse.<ClientDTO>builder()
                .content(List.of(savedClientDTO))
                .page(0)
                .size(20)
                .totalElements(1)
                .totalPages(1)
                .first(true)
                .last(true)
                .empty(false)
                .build();

        when(clientService.getClients(eq(lawFirmId), eq(0), eq(20), isNull(), isNull(), isNull()))
                .thenReturn(pageResponse);

        mockMvc.perform(get("/v1/clients")
                        .param("page", "0")
                        .param("size", "20")
                        .header("X-Law-Firm-Id", lawFirmId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.page").value(0))
                .andExpect(jsonPath("$.data.totalElements").value(1));
    }

    @Test
    @DisplayName("List Clients - Should filter by status")
    void getClients_filterByStatus() throws Exception {
        PageResponse<ClientDTO> pageResponse = PageResponse.<ClientDTO>builder()
                .content(List.of(savedClientDTO))
                .page(0)
                .size(20)
                .totalElements(1)
                .totalPages(1)
                .first(true)
                .last(true)
                .empty(false)
                .build();

        when(clientService.getClients(eq(lawFirmId), eq(0), eq(20), isNull(), eq("ACTIVE"), isNull()))
                .thenReturn(pageResponse);

        mockMvc.perform(get("/v1/clients")
                        .param("page", "0")
                        .param("size", "20")
                        .param("status", "ACTIVE")
                        .header("X-Law-Firm-Id", lawFirmId))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("List Clients - Should search by text")
    void getClients_search() throws Exception {
        PageResponse<ClientDTO> pageResponse = PageResponse.<ClientDTO>builder()
                .content(List.of(savedClientDTO))
                .page(0)
                .size(20)
                .totalElements(1)
                .totalPages(1)
                .first(true)
                .last(true)
                .empty(false)
                .build();

        when(clientService.getClients(eq(lawFirmId), eq(0), eq(20), eq("John"), isNull(), isNull()))
                .thenReturn(pageResponse);

        mockMvc.perform(get("/v1/clients")
                        .param("search", "John")
                        .header("X-Law-Firm-Id", lawFirmId))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Update Client - Should return updated client")
    void updateClient_success() throws Exception {
        ClientDTO updateDTO = ClientDTO.builder()
                .firstName("Jane")
                .lastName("Updated")
                .email("jane@example.com")
                .build();

        ClientDTO updated = ClientDTO.builder()
                .id("client-1")
                .firstName("Jane")
                .lastName("Updated")
                .email("jane@example.com")
                .clientNumber("CL-001")
                .build();

        when(clientService.updateClient("client-1", updateDTO)).thenReturn(updated);

        mockMvc.perform(put("/v1/clients/client-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.firstName").value("Jane"))
                .andExpect(jsonPath("$.data.lastName").value("Updated"));
    }

    @Test
    @DisplayName("Delete Client - Should return 200 on success")
    void deleteClient_success() throws Exception {
        mockMvc.perform(delete("/v1/clients/client-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(true))
                .andExpect(jsonPath("$.message").value("Client deleted successfully"));

        verify(clientService).deleteClient("client-1");
    }

    @Test
    @DisplayName("Delete Client - Should return 404 when client not found")
    void deleteClient_notFound() throws Exception {
        doThrow(new ResourceNotFoundException("Client", "id", "missing"))
                .when(clientService).deleteClient("missing");

        mockMvc.perform(delete("/v1/clients/missing"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Get Active Clients - Should return active clients list")
    void getActiveClients_success() throws Exception {
        when(clientService.getActiveClients(lawFirmId))
                .thenReturn(List.of(savedClientDTO));

        mockMvc.perform(get("/v1/clients/active")
                        .header("X-Law-Firm-Id", lawFirmId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(1));
    }

    @Test
    @DisplayName("Get My Clients - Should return clients assigned to user")
    void getMyClients_success() throws Exception {
        when(clientService.getClientsByLawyer("lawyer-1"))
                .thenReturn(List.of(savedClientDTO));

        mockMvc.perform(get("/v1/clients/my-clients")
                        .header("X-User-Id", "lawyer-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @DisplayName("Create Client - Should return 400 for duplicate client number")
    void createClient_duplicateNumber() throws Exception {
        when(clientService.createClient(any(ClientDTO.class)))
                .thenThrow(new IllegalArgumentException("Client number already exists: CL-001"));

        mockMvc.perform(post("/v1/clients")
                        .header("X-Law-Firm-Id", lawFirmId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(clientDTO)))
                .andExpect(status().isBadRequest());
    }
}
