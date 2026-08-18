package com.company.legal.case_management.service;

import com.company.legal.case_management.dto.CaseDTO;
import com.company.legal.case_management.entity.Case;
import com.company.legal.case_management.repository.CaseRepository;
import com.company.legal.common.dto.PageResponse;
import com.company.legal.common.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CaseServiceTest {

    @Mock
    private CaseRepository caseRepository;

    @InjectMocks
    private CaseService caseService;

    private Case newCase;
    private Case inProgressCase;
    private Case closedCase;
    private CaseDTO caseDTO;

    @BeforeEach
    void setUp() {
        newCase = Case.builder()
                .id("case-1")
                .caseNumber("CASE-001")
                .lawFirmId("firm-1")
                .clientId("client-1")
                .clientName("John Doe")
                .caseType("Civil")
                .caseStatus("NEW")
                .casePriority("MEDIUM")
                .title("Sample Civil Case")
                .description("A sample civil case")
                .practiceArea("Corporate Law")
                .courtId("court-1")
                .courtName("Central Court")
                .judgeId("judge-1")
                .filingNumber("FILING-001")
                .filingDate(LocalDate.now())
                .caseValue(new BigDecimal("100000.00"))
                .openingDate(LocalDate.now())
                .expectedClosingDate(LocalDate.now().plusMonths(6))
                .leadLawyerId("lawyer-1")
                .leadLawyerName("Attorney Smith")
                .billingModel("HOURLY")
                .totalBilled(BigDecimal.ZERO)
                .totalPaid(BigDecimal.ZERO)
                .totalOutstanding(BigDecimal.ZERO)
                .build();

        inProgressCase = Case.builder()
                .id("case-2")
                .caseNumber("CASE-002")
                .lawFirmId("firm-1")
                .clientId("client-2")
                .clientName("Jane Doe")
                .caseType("Criminal")
                .caseStatus("IN_PROGRESS")
                .casePriority("HIGH")
                .title("Criminal Defense Case")
                .description("A criminal defense case")
                .practiceArea("Criminal Law")
                .build();

        closedCase = Case.builder()
                .id("case-3")
                .caseNumber("CASE-003")
                .lawFirmId("firm-1")
                .clientId("client-3")
                .clientName("Bob Wilson")
                .caseType("Civil")
                .caseStatus("CLOSED")
                .actualClosingDate(LocalDate.now())
                .build();

        caseDTO = CaseDTO.builder()
                .caseNumber("CASE-NEW")
                .clientId("client-new")
                .clientName("New Client")
                .caseType("Civil")
                .caseStatus("NEW")
                .casePriority("MEDIUM")
                .title("New Case")
                .description("Description")
                .practiceArea("Corporate Law")
                .caseValue(new BigDecimal("50000.00"))
                .build();
    }

    // ========== CREATE CASE TESTS ==========

    @Nested
    @DisplayName("Create Case Tests")
    class CreateCaseTests {

        @Test
        @DisplayName("Should create a new case successfully")
        void createCase_success() {
            // Arrange
            when(caseRepository.existsByCaseNumber("CASE-NEW")).thenReturn(false);
            when(caseRepository.save(any(Case.class))).thenAnswer(invocation -> {
                Case c = invocation.getArgument(0);
                c.setId("new-case-id");
                return c;
            });

            // Act
            CaseDTO result = caseService.createCase(caseDTO);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo("new-case-id");
            assertThat(result.getCaseNumber()).isEqualTo("CASE-NEW");
            assertThat(result.getTitle()).isEqualTo("New Case");
            assertThat(result.getCaseType()).isEqualTo("Civil");
            assertThat(result.getCaseStatus()).isEqualTo("NEW");
            assertThat(result.getCasePriority()).isEqualTo("MEDIUM");

            ArgumentCaptor<Case> captor = ArgumentCaptor.forClass(Case.class);
            verify(caseRepository).save(captor.capture());
            Case saved = captor.getValue();
            assertThat(saved.getCaseStatus()).isEqualTo("NEW");
            assertThat(saved.getConfidentialityLevel()).isEqualTo("INTERNAL");
            assertThat(saved.getRiskLevel()).isEqualTo("LOW");
            assertThat(saved.getTotalBilled()).isEqualTo(BigDecimal.ZERO);
            assertThat(saved.getOpeningDate()).isEqualTo(LocalDate.now());
        }

        @Test
        @DisplayName("Should throw exception when case number already exists")
        void createCase_duplicateCaseNumber() {
            // Arrange
            when(caseRepository.existsByCaseNumber("CASE-NEW")).thenReturn(true);

            // Act & Assert
            assertThatThrownBy(() -> caseService.createCase(caseDTO))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Case number already exists");

            verify(caseRepository, never()).save(any());
        }
    }

    // ========== UPDATE CASE TESTS ==========

    @Nested
    @DisplayName("Update Case Tests")
    class UpdateCaseTests {

        @Test
        @DisplayName("Should update case successfully")
        void updateCase_success() {
            // Arrange
            String id = "case-1";
            CaseDTO updateDTO = CaseDTO.builder()
                    .title("Updated Case Title")
                    .description("Updated description")
                    .caseType("Commercial")
                    .caseStatus("IN_PROGRESS")
                    .casePriority("HIGH")
                    .practiceArea("Commercial Law")
                    .caseValue(new BigDecimal("250000.00"))
                    .build();

            when(caseRepository.findById(id)).thenReturn(Optional.of(newCase));
            when(caseRepository.save(any(Case.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            CaseDTO result = caseService.updateCase(id, updateDTO);

            // Assert
            assertThat(result.getTitle()).isEqualTo("Updated Case Title");
            assertThat(result.getDescription()).isEqualTo("Updated description");
            assertThat(result.getCaseType()).isEqualTo("Commercial");
            assertThat(result.getCaseStatus()).isEqualTo("IN_PROGRESS");
            assertThat(result.getCasePriority()).isEqualTo("HIGH");
            assertThat(result.getCaseValue()).isEqualTo(new BigDecimal("250000.00"));
            assertThat(result.getPracticeArea()).isEqualTo("Commercial Law");
        }

        @Test
        @DisplayName("Should throw exception when case not found")
        void updateCase_notFound() {
            // Arrange
            when(caseRepository.findById("missing-id")).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> caseService.updateCase("missing-id", caseDTO))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Case");

            verify(caseRepository, never()).save(any());
        }
    }

    // ========== GET CASE TESTS ==========

    @Nested
    @DisplayName("Get Case Tests")
    class GetCaseTests {

        @Test
        @DisplayName("Should get case by ID")
        void getCaseById_success() {
            // Arrange
            when(caseRepository.findById("case-1")).thenReturn(Optional.of(newCase));

            // Act
            CaseDTO result = caseService.getCaseById("case-1");

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getCaseNumber()).isEqualTo("CASE-001");
            assertThat(result.getTitle()).isEqualTo("Sample Civil Case");
            assertThat(result.getClientId()).isEqualTo("client-1");
            assertThat(result.getCaseType()).isEqualTo("Civil");
            assertThat(result.getCourtName()).isEqualTo("Central Court");
        }

        @Test
        @DisplayName("Should throw exception when case not found")
        void getCaseById_notFound() {
            // Arrange
            when(caseRepository.findById("missing-id")).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> caseService.getCaseById("missing-id"))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    // ========== LIST CASES TESTS ==========

    @Nested
    @DisplayName("List Cases Tests")
    class ListCasesTests {

        @Test
        @DisplayName("Should return paginated cases for law firm")
        void getCases_paginated() {
            // Arrange
            Page<Case> casePage = new PageImpl<>(List.of(newCase, inProgressCase, closedCase));
            when(caseRepository.findByLawFirmIdAndDeletedAtIsNull("firm-1", any(Pageable.class)))
                    .thenReturn(casePage);

            // Act
            PageResponse<CaseDTO> result = caseService.getCases("firm-1", 0, 20, null, null, null);

            // Assert
            assertThat(result.getContent()).hasSize(3);
            assertThat(result.getTotalElements()).isEqualTo(3);
        }

        @Test
        @DisplayName("Should filter cases by status")
        void getCases_filterByStatus() {
            // Arrange
            Page<Case> inProgressPage = new PageImpl<>(List.of(inProgressCase));
            when(caseRepository.findByLawFirmIdAndCaseStatusAndDeletedAtIsNull(
                    eq("firm-1"), eq("IN_PROGRESS"), any(Pageable.class)))
                    .thenReturn(inProgressPage);

            // Act
            PageResponse<CaseDTO> result = caseService.getCases("firm-1", 0, 20, null, "IN_PROGRESS", null);

            // Assert
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getCaseStatus()).isEqualTo("IN_PROGRESS");
        }

        @Test
        @DisplayName("Should search cases by text")
        void getCases_withSearch() {
            // Arrange
            Page<Case> searchPage = new PageImpl<>(List.of(newCase));
            when(caseRepository.searchByLawFirm(eq("firm-1"), eq("Civil"), any(Pageable.class)))
                    .thenReturn(searchPage);

            // Act
            PageResponse<CaseDTO> result = caseService.getCases("firm-1", 0, 20, "Civil", null, null);

            // Assert
            assertThat(result.getContent()).hasSize(1);
        }

        @Test
        @DisplayName("Should return empty when no cases found")
        void getCases_empty() {
            // Arrange
            when(caseRepository.findByLawFirmIdAndDeletedAtIsNull("firm-1", any(Pageable.class)))
                    .thenReturn(Page.empty());

            // Act
            PageResponse<CaseDTO> result = caseService.getCases("firm-1", 0, 20, null, null, null);

            // Assert
            assertThat(result.getContent()).isEmpty();
            assertThat(result.isEmpty()).isTrue();
        }

        @Test
        @DisplayName("Should use default pagination when nulls passed")
        void getCases_defaultValues() {
            // Arrange
            when(caseRepository.findByLawFirmIdAndDeletedAtIsNull("firm-1", any(Pageable.class)))
                    .thenReturn(Page.empty());

            // Act
            PageResponse<CaseDTO> result = caseService.getCases("firm-1", null, null, null, null, null);

            // Assert
            assertThat(result.getPage()).isZero();
            assertThat(result.getSize()).isEqualTo(20);
        }
    }

    // ========== DELETE CASE TESTS ==========

    @Nested
    @DisplayName("Delete Case Tests")
    class DeleteCaseTests {

        @Test
        @DisplayName("Should soft delete case successfully")
        void deleteCase_success() {
            // Arrange
            when(caseRepository.findById("case-1")).thenReturn(Optional.of(newCase));

            // Act
            caseService.deleteCase("case-1");

            // Assert
            assertThat(newCase.getDeletedAt()).isNotNull();
            verify(caseRepository).save(newCase);
        }

        @Test
        @DisplayName("Should throw exception when deleting non-existent case")
        void deleteCase_notFound() {
            // Arrange
            when(caseRepository.findById("missing-id")).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> caseService.deleteCase("missing-id"))
                    .isInstanceOf(ResourceNotFoundException.class);

            verify(caseRepository, never()).save(any());
        }
    }

    // ========== GET CASES BY CLIENT TESTS ==========

    @Nested
    @DisplayName("Get Cases By Client Tests")
    class GetCasesByClientTests {

        @Test
        @DisplayName("Should return cases for a specific client")
        void getCasesByClient_success() {
            // Arrange
            when(caseRepository.findByClientId("client-1")).thenReturn(List.of(newCase));

            // Act
            List<CaseDTO> result = caseService.getCasesByClient("client-1");

            // Assert
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getClientId()).isEqualTo("client-1");
        }

        @Test
        @DisplayName("Should return empty list when client has no cases")
        void getCasesByClient_empty() {
            // Arrange
            when(caseRepository.findByClientId("client-no-cases")).thenReturn(List.of());

            // Act
            List<CaseDTO> result = caseService.getCasesByClient("client-no-cases");

            // Assert
            assertThat(result).isEmpty();
        }
    }

    // ========== GET IN PROGRESS CASES TESTS ==========

    @Nested
    @DisplayName("Get In Progress Cases Tests")
    class GetInProgressCasesTests {

        @Test
        @DisplayName("Should return in-progress cases for a law firm")
        void getInProgressCases_success() {
            // Arrange
            Page<Case> page = new PageImpl<>(List.of(inProgressCase));
            when(caseRepository.findByLawFirmIdAndCaseStatusAndDeletedAtIsNull(
                    eq("firm-1"), eq("IN_PROGRESS"), any(Pageable.class)))
                    .thenReturn(page);

            // Act
            List<CaseDTO> result = caseService.getInProgressCases("firm-1");

            // Assert
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getCaseStatus()).isEqualTo("IN_PROGRESS");
        }

        @Test
        @DisplayName("Should return empty list when no in-progress cases")
        void getInProgressCases_empty() {
            // Arrange
            when(caseRepository.findByLawFirmIdAndCaseStatusAndDeletedAtIsNull(
                    eq("firm-1"), eq("IN_PROGRESS"), any(Pageable.class)))
                    .thenReturn(Page.empty());

            // Act
            List<CaseDTO> result = caseService.getInProgressCases("firm-1");

            // Assert
            assertThat(result).isEmpty();
        }
    }

    // ========== CASE STATUS TRANSITION TESTS ==========

    @Nested
    @DisplayName("Case Status Transition Tests")
    class StatusTransitionTests {

        @Test
        @DisplayName("Should be able to transition case from NEW to OPEN")
        void transitionNewToOpen() {
            // Arrange
            Case caseEntity = Case.builder()
                    .id("case-transition")
                    .caseNumber("CASE-TRANS")
                    .lawFirmId("firm-1")
                    .clientId("client-1")
                    .caseStatus("NEW")
                    .casePriority("MEDIUM")
                    .title("Transition Test")
                    .build();

            CaseDTO transitionDTO = CaseDTO.builder()
                    .caseStatus("OPEN")
                    .build();

            when(caseRepository.findById("case-transition")).thenReturn(Optional.of(caseEntity));
            when(caseRepository.save(any(Case.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            CaseDTO result = caseService.updateCase("case-transition", transitionDTO);

            // Assert
            assertThat(result.getCaseStatus()).isEqualTo("OPEN");
        }

        @Test
        @DisplayName("Should be able to transition case from IN_PROGRESS to CLOSED")
        void transitionInProgressToClosed() {
            // Arrange
            Case caseEntity = Case.builder()
                    .id("case-close")
                    .caseNumber("CASE-CLOSE")
                    .lawFirmId("firm-1")
                    .clientId("client-1")
                    .caseStatus("IN_PROGRESS")
                    .title("Close Test")
                    .build();

            CaseDTO closeDTO = CaseDTO.builder()
                    .caseStatus("CLOSED")
                    .build();

            when(caseRepository.findById("case-close")).thenReturn(Optional.of(caseEntity));
            when(caseRepository.save(any(Case.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            CaseDTO result = caseService.updateCase("case-close", closeDTO);

            // Assert
            assertThat(result.getCaseStatus()).isEqualTo("CLOSED");
        }
    }
}
