package com.company.legal.case_management.controller;

import com.company.legal.case_management.dto.CaseDTO;
import com.company.legal.case_management.entity.Case;
import com.company.legal.case_management.repository.CaseRepository;
import com.company.legal.testutil.IntegrationTestBase;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class CaseControllerIntegrationTest extends IntegrationTestBase {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CaseRepository caseRepository;

    private String lawFirmId;

    @BeforeEach
    void setUp() {
        lawFirmId = "firm-case-integration";
        caseRepository.deleteAll();
    }

    @AfterEach
    void tearDown() {
        caseRepository.deleteAll();
    }

    @Test
    @Order(1)
    @DisplayName("Create case via API - should return 201")
    void createCase_viaApi() throws Exception {
        CaseDTO dto = CaseDTO.builder()
                .caseNumber("INT-CASE-001")
                .clientId("client-1")
                .clientName("Integration Client")
                .caseType("Civil")
                .caseStatus("NEW")
                .casePriority("MEDIUM")
                .title("Integration Test Case")
                .description("A case for integration testing")
                .practiceArea("Corporate Law")
                .caseValue(new BigDecimal("100000.00"))
                .openingDate(LocalDate.now())
                .expectedClosingDate(LocalDate.now().plusMonths(6))
                .leadLawyerId("lawyer-1")
                .leadLawyerName("Test Lawyer")
                .build();

        mockMvc.perform(post("/v1/cases")
                        .header("X-Law-Firm-Id", lawFirmId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.caseNumber").value("INT-CASE-001"))
                .andExpect(jsonPath("$.data.caseType").value("Civil"))
                .andExpect(jsonPath("$.data.caseStatus").value("NEW"))
                .andExpect(jsonPath("$.data.title").value("Integration Test Case"));
    }

    @Test
    @Order(2)
    @DisplayName("Get case - should return 404 for non-existent")
    void getCase_notFound() throws Exception {
        mockMvc.perform(get("/v1/cases/non-existent"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @Order(3)
    @DisplayName("List cases - should return empty initially")
    void listCases_empty() throws Exception {
        mockMvc.perform(get("/v1/cases")
                        .header("X-Law-Firm-Id", lawFirmId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.totalElements").value(0));
    }

    @Test
    @Order(4)
    @DisplayName("Full CRUD - create, get, update status, delete")
    void fullCrudCycle() throws Exception {
        CaseDTO dto = CaseDTO.builder()
                .caseNumber("CRUD-CASE-001")
                .clientId("client-1")
                .clientName("CRUD Client")
                .caseType("Criminal")
                .caseStatus("NEW")
                .title("CRUD Test Case")
                .build();

        String response = mockMvc.perform(post("/v1/cases")
                        .header("X-Law-Firm-Id", lawFirmId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String id = objectMapper.readTree(response).path("data").path("id").asText();

        // Get
        mockMvc.perform(get("/v1/cases/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(id));

        // Update status
        CaseDTO updateDto = CaseDTO.builder()
                .caseStatus("IN_PROGRESS")
                .casePriority("HIGH")
                .title("Updated Title")
                .build();

        mockMvc.perform(put("/v1/cases/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.caseStatus").value("IN_PROGRESS"));

        // Delete
        mockMvc.perform(delete("/v1/cases/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(true));
    }

    @Test
    @Order(5)
    @DisplayName("Duplicate case number - should return 400")
    void createCase_duplicateNumber() throws Exception {
        CaseDTO dto = CaseDTO.builder()
                .caseNumber("DUP-CASE-001")
                .clientId("client-1")
                .clientName("Dup Client")
                .caseType("Civil")
                .title("Dup Case")
                .build();

        mockMvc.perform(post("/v1/cases")
                        .header("X-Law-Firm-Id", lawFirmId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());

        CaseDTO dupDto = CaseDTO.builder()
                .caseNumber("DUP-CASE-001")
                .clientId("client-2")
                .clientName("Another Dup")
                .caseType("Criminal")
                .title("Dup Case 2")
                .build();

        mockMvc.perform(post("/v1/cases")
                        .header("X-Law-Firm-Id", lawFirmId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dupDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(6)
    @DisplayName("Get cases by client - should return cases")
    void getCasesByClient() throws Exception {
        CaseDTO dto = CaseDTO.builder()
                .caseNumber("BYCLIENT-001")
                .clientId("client-specific")
                .clientName("Specific Client")
                .caseType("Civil")
                .title("Client Specific Case")
                .build();

        mockMvc.perform(post("/v1/cases")
                        .header("X-Law-Firm-Id", lawFirmId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/v1/cases/client/client-specific"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(1));
    }

    @Test
    @Order(7)
    @DisplayName("Get in-progress cases - should return in-progress cases")
    void getInProgressCases() throws Exception {
        CaseDTO dto = CaseDTO.builder()
                .caseNumber("INPROG-001")
                .clientId("client-1")
                .clientName("InProgress Client")
                .caseType("Civil")
                .caseStatus("IN_PROGRESS")
                .title("InProgress Case")
                .build();

        mockMvc.perform(post("/v1/cases")
                        .header("X-Law-Firm-Id", lawFirmId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/v1/cases/in-progress")
                        .header("X-Law-Firm-Id", lawFirmId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(1));
    }
}
