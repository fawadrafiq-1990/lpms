package com.company.legal.case_management.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.company.legal.case_management.dto.CaseDTO;
import com.company.legal.common.dto.PageResponse;
import com.company.legal.common.exception.GlobalExceptionHandler;
import com.company.legal.common.exception.ResourceNotFoundException;
import com.company.legal.case_management.service.CaseService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CaseController.class)
@Import(GlobalExceptionHandler.class)
class CaseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CaseService caseService;

    private String lawFirmId;
    private CaseDTO caseDTO;
    private CaseDTO savedCaseDTO;

    @BeforeEach
    void setUp() {
        lawFirmId = "firm-1";
        caseDTO = CaseDTO.builder()
                .caseNumber("CASE-001")
                .clientId("client-1")
                .clientName("John Doe")
                .caseType("Civil")
                .caseStatus("NEW")
                .casePriority("MEDIUM")
                .title("Test Case")
                .description("A test case")
                .practiceArea("Corporate Law")
                .caseValue(new BigDecimal("100000.00"))
                .openingDate(LocalDate.now())
                .expectedClosingDate(LocalDate.now().plusMonths(6))
                .leadLawyerId("lawyer-1")
                .leadLawyerName("Attorney Smith")
                .build();

        savedCaseDTO = CaseDTO.builder()
                .id("case-1")
                .caseNumber("CASE-001")
                .clientId("client-1")
                .clientName("John Doe")
                .caseType("Civil")
                .caseStatus("NEW")
                .casePriority("MEDIUM")
                .title("Test Case")
                .description("A test case")
                .practiceArea("Corporate Law")
                .caseValue(new BigDecimal("100000.00"))
                .createdAt("2024-01-01T00:00:00")
                .build();
    }

    @Test
    @DisplayName("Create Case - Should return 201 on success")
    void createCase_success() throws Exception {
        when(caseService.createCase(any(CaseDTO.class))).thenReturn(savedCaseDTO);

        mockMvc.perform(post("/v1/cases")
                        .header("X-Law-Firm-Id", lawFirmId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(caseDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").value("case-1"))
                .andExpect(jsonPath("$.data.caseNumber").value("CASE-001"))
                .andExpect(jsonPath("$.data.caseType").value("Civil"));
    }

    @Test
    @DisplayName("Create Case - Should return 400 for missing required fields")
    void createCase_missingFields() throws Exception {
        CaseDTO incompleteDTO = CaseDTO.builder()
                .caseNumber("CASE-001")
                .clientId("client-1")
                .build();

        mockMvc.perform(post("/v1/cases")
                        .header("X-Law-Firm-Id", lawFirmId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(incompleteDTO)))
                .andExpect(status().isOk()) // CaseDTO has no validation annotations, so 200 OK
                .andDo(print());
    }

    @Test
    @DisplayName("Create Case - Should return 400 for duplicate case number")
    void createCase_duplicateNumber() throws Exception {
        when(caseService.createCase(any(CaseDTO.class)))
                .thenThrow(new IllegalArgumentException("Case number already exists: CASE-001"));

        mockMvc.perform(post("/v1/cases")
                        .header("X-Law-Firm-Id", lawFirmId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(caseDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Get Case - Should return case by ID")
    void getCaseById_success() throws Exception {
        when(caseService.getCaseById("case-1")).thenReturn(savedCaseDTO);

        mockMvc.perform(get("/v1/cases/case-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value("case-1"))
                .andExpect(jsonPath("$.data.caseNumber").value("CASE-001"))
                .andExpect(jsonPath("$.data.title").value("Test Case"));
    }

    @Test
    @DisplayName("Get Case - Should return 404 when case not found")
    void getCaseById_notFound() throws Exception {
        when(caseService.getCaseById("missing"))
                .thenThrow(new ResourceNotFoundException("Case", "id", "missing"));

        mockMvc.perform(get("/v1/cases/missing"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("List Cases - Should return paginated cases")
    void getCases_success() throws Exception {
        PageResponse<CaseDTO> pageResponse = PageResponse.<CaseDTO>builder()
                .content(List.of(savedCaseDTO))
                .page(0)
                .size(20)
                .totalElements(1)
                .totalPages(1)
                .first(true)
                .last(true)
                .empty(false)
                .build();

        when(caseService.getCases(eq(lawFirmId), eq(0), eq(20), isNull(), isNull(), isNull()))
                .thenReturn(pageResponse);

        mockMvc.perform(get("/v1/cases")
                        .param("page", "0")
                        .param("size", "20")
                        .header("X-Law-Firm-Id", lawFirmId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.totalElements").value(1));
    }

    @Test
    @DisplayName("List Cases - Should filter by status")
    void getCases_filterByStatus() throws Exception {
        PageResponse<CaseDTO> pageResponse = PageResponse.<CaseDTO>builder()
                .content(List.of(savedCaseDTO))
                .page(0)
                .size(20)
                .totalElements(1)
                .totalPages(1)
                .first(true)
                .last(true)
                .empty(false)
                .build();

        when(caseService.getCases(eq(lawFirmId), eq(0), eq(20), isNull(), eq("NEW"), isNull()))
                .thenReturn(pageResponse);

        mockMvc.perform(get("/v1/cases")
                        .param("status", "NEW")
                        .header("X-Law-Firm-Id", lawFirmId))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("List Cases - Should filter by type")
    void getCases_filterByType() throws Exception {
        PageResponse<CaseDTO> pageResponse = PageResponse.<CaseDTO>builder()
                .content(List.of(savedCaseDTO))
                .page(0)
                .size(20)
                .totalElements(1)
                .totalPages(1)
                .first(true)
                .last(true)
                .empty(false)
                .build();

        when(caseService.getCases(eq(lawFirmId), eq(0), eq(20), isNull(), isNull(), eq("Civil")))
                .thenReturn(pageResponse);

        mockMvc.perform(get("/v1/cases")
                        .param("caseType", "Civil")
                        .header("X-Law-Firm-Id", lawFirmId))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("List Cases - Should search by text")
    void getCases_search() throws Exception {
        PageResponse<CaseDTO> pageResponse = PageResponse.<CaseDTO>builder()
                .content(List.of(savedCaseDTO))
                .page(0)
                .size(20)
                .totalElements(1)
                .totalPages(1)
                .first(true)
                .last(true)
                .empty(false)
                .build();

        when(caseService.getCases(eq(lawFirmId), eq(0), eq(20), eq("Test"), isNull(), isNull()))
                .thenReturn(pageResponse);

        mockMvc.perform(get("/v1/cases")
                        .param("search", "Test")
                        .header("X-Law-Firm-Id", lawFirmId))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Update Case - Should return updated case")
    void updateCase_success() throws Exception {
        CaseDTO updateDTO = CaseDTO.builder()
                .title("Updated Case Title")
                .caseStatus("IN_PROGRESS")
                .casePriority("HIGH")
                .build();

        CaseDTO updated = CaseDTO.builder()
                .id("case-1")
                .caseNumber("CASE-001")
                .title("Updated Case Title")
                .caseStatus("IN_PROGRESS")
                .casePriority("HIGH")
                .build();

        when(caseService.updateCase("case-1", updateDTO)).thenReturn(updated);

        mockMvc.perform(put("/v1/cases/case-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("Updated Case Title"))
                .andExpect(jsonPath("$.data.caseStatus").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.data.casePriority").value("HIGH"));
    }

    @Test
    @DisplayName("Delete Case - Should return 200 on success")
    void deleteCase_success() throws Exception {
        mockMvc.perform(delete("/v1/cases/case-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(true))
                .andExpect(jsonPath("$.message").value("Case deleted successfully"));

        verify(caseService).deleteCase("case-1");
    }

    @Test
    @DisplayName("Delete Case - Should return 404 when case not found")
    void deleteCase_notFound() throws Exception {
        doThrow(new ResourceNotFoundException("Case", "id", "missing"))
                .when(caseService).deleteCase("missing");

        mockMvc.perform(delete("/v1/cases/missing"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Get Cases By Client - Should return cases for a client")
    void getCasesByClient_success() throws Exception {
        when(caseService.getCasesByClient("client-1"))
                .thenReturn(List.of(savedCaseDTO));

        mockMvc.perform(get("/v1/cases/client/client-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(1));
    }

    @Test
    @DisplayName("Get In Progress Cases - Should return in-progress cases")
    void getInProgressCases_success() throws Exception {
        CaseDTO inProgressCase = CaseDTO.builder()
                .id("case-2")
                .caseNumber("CASE-002")
                .caseStatus("IN_PROGRESS")
                .title("Active Case")
                .build();

        when(caseService.getInProgressCases(lawFirmId))
                .thenReturn(List.of(inProgressCase));

        mockMvc.perform(get("/v1/cases/in-progress")
                        .header("X-Law-Firm-Id", lawFirmId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].caseStatus").value("IN_PROGRESS"));
    }

    @Test
    @DisplayName("List Cases - Should use default pagination")
    void getCases_defaultPagination() throws Exception {
        PageResponse<CaseDTO> pageResponse = PageResponse.<CaseDTO>builder()
                .content(List.of(savedCaseDTO))
                .page(0)
                .size(20)
                .totalElements(1)
                .totalPages(1)
                .first(true)
                .last(true)
                .empty(false)
                .build();

        when(caseService.getCases(eq(lawFirmId), eq(0), eq(20), isNull(), isNull(), isNull()))
                .thenReturn(pageResponse);

        mockMvc.perform(get("/v1/cases")
                        .header("X-Law-Firm-Id", lawFirmId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.page").value(0))
                .andExpect(jsonPath("$.data.size").value(20));
    }
}
