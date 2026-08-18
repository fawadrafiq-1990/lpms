package com.company.legal.billing.service;

import com.company.legal.billing.dto.TimeEntryDTO;
import com.company.legal.billing.entity.TimeEntry;
import com.company.legal.billing.repository.TimeEntryRepository;
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
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TimeEntryServiceTest {

    @Mock
    private TimeEntryRepository timeEntryRepository;

    @Mock
    private CaseRepository caseRepository;

    @InjectMocks
    private TimeEntryService timeEntryService;

    private TimeEntry timeEntry;
    private Case testCase;
    private TimeEntryDTO timeEntryDTO;

    @BeforeEach
    void setUp() {
        testCase = Case.builder()
                .id("case-1")
                .caseNumber("CASE-001")
                .title("Test Case")
                .build();

        timeEntry = TimeEntry.builder()
                .id("te-1")
                .entryNumber("TE-0001")
                .caseId("case-1")
                .caseNumber("CASE-001")
                .userId("user-1")
                .lawyerId("lawyer-1")
                .taskId("task-1")
                .workDate(LocalDate.now())
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(11, 30))
                .description("Client consultation and document review")
                .activityType("Consultation")
                .billingRate(new BigDecimal("250.00"))
                .hourlyRate(new BigDecimal("250.00"))
                .durationMinutes(225)
                .durationHours(new BigDecimal("3.75"))
                .amount(new BigDecimal("937.50"))
                .billable(true)
                .billed(false)
                .status("DRAFT")
                .notes("Important meeting")
                .build();

        timeEntryDTO = TimeEntryDTO.builder()
                .caseId("case-1")
                .caseNumber("CASE-001")
                .userId("user-1")
                .lawyerId("lawyer-1")
                .workDate(LocalDate.now())
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(11, 0))
                .description("Meeting with client")
                .activityType("Consultation")
                .billingRate(new BigDecimal("250.00"))
                .billable(true)
                .build();
    }

    // ========== CREATE TIME ENTRY TESTS ==========

    @Nested
    @DisplayName("Create Time Entry Tests")
    class CreateTimeEntryTests {

        @Test
        @DisplayName("Should create a time entry successfully")
        void createTimeEntry_success() {
            // Arrange
            when(caseRepository.findById("case-1")).thenReturn(Optional.of(testCase));
            when(timeEntryRepository.count()).thenReturn(0L);
            when(timeEntryRepository.save(any(TimeEntry.class))).thenAnswer(invocation -> {
                TimeEntry entry = invocation.getArgument(0);
                entry.setId("new-te-1");
                return entry;
            });

            // Act
            TimeEntryDTO result = timeEntryService.createTimeEntry(timeEntryDTO);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo("new-te-1");
            assertThat(result.getEntryNumber()).startsWith("TE-");
            assertThat(result.getCaseId()).isEqualTo("case-1");
            assertThat(result.getDescription()).isEqualTo("Meeting with client");
            assertThat(result.getStatus()).isEqualTo("DRAFT");
            assertThat(result.getBillable()).isTrue();
            assertThat(result.getBilled()).isFalse();

            ArgumentCaptor<TimeEntry> captor = ArgumentCaptor.forClass(TimeEntry.class);
            verify(timeEntryRepository).save(captor.capture());
            TimeEntry saved = captor.getValue();
            assertThat(saved.getDurationMinutes()).isEqualTo(120);
            assertThat(saved.getDurationHours()).isEqualTo(new BigDecimal("2.00"));
        }

        @Test
        @DisplayName("Should calculate duration automatically")
        void createTimeEntry_calculateDuration() {
            // Arrange
            TimeEntryDTO dto = TimeEntryDTO.builder()
                    .caseId("case-1")
                    .userId("user-1")
                    .startTime(LocalTime.of(8, 0))
                    .endTime(LocalTime.of(12, 30))
                    .description("Full morning session")
                    .billingRate(new BigDecimal("300.00"))
                    .billable(true)
                    .build();

            when(caseRepository.findById("case-1")).thenReturn(Optional.of(testCase));
            when(timeEntryRepository.count()).thenReturn(0L);
            when(timeEntryRepository.save(any(TimeEntry.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            TimeEntryDTO result = timeEntryService.createTimeEntry(dto);

            // Assert
            assertThat(result.getDurationMinutes()).isEqualTo(270);
            assertThat(result.getDurationHours()).isEqualTo(new BigDecimal("4.50"));
            assertThat(result.getAmount()).isEqualTo(new BigDecimal("1350.00"));
        }

        @Test
        @DisplayName("Should calculate amount automatically from rate and duration")
        void createTimeEntry_calculateAmount() {
            // Arrange
            TimeEntryDTO dto = TimeEntryDTO.builder()
                    .caseId("case-1")
                    .userId("user-1")
                    .startTime(LocalTime.of(10, 0))
                    .endTime(LocalTime.of(11, 0))
                    .description("One hour meeting")
                    .billingRate(new BigDecimal("500.00"))
                    .billable(true)
                    .build();

            when(caseRepository.findById("case-1")).thenReturn(Optional.of(testCase));
            when(timeEntryRepository.count()).thenReturn(0L);
            when(timeEntryRepository.save(any(TimeEntry.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            TimeEntryDTO result = timeEntryService.createTimeEntry(dto);

            // Assert
            assertThat(result.getDurationHours()).isEqualTo(new BigDecimal("1.00"));
            assertThat(result.getAmount()).isEqualTo(new BigDecimal("500.00"));
        }

        @Test
        @DisplayName("Should throw exception when case not found")
        void createTimeEntry_caseNotFound() {
            // Arrange
            when(caseRepository.findById("missing-case")).thenReturn(Optional.empty());

            // Act & Assert
            timeEntryDTO.setCaseId("missing-case");
            assertThatThrownBy(() -> timeEntryService.createTimeEntry(timeEntryDTO))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Case");

            verify(timeEntryRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should handle non-billable entries correctly")
        void createTimeEntry_nonBillable() {
            // Arrange
            TimeEntryDTO dto = TimeEntryDTO.builder()
                    .caseId("case-1")
                    .userId("user-1")
                    .startTime(LocalTime.of(14, 0))
                    .endTime(LocalTime.of(15, 0))
                    .description("Internal training")
                    .billable(false)
                    .build();

            when(caseRepository.findById("case-1")).thenReturn(Optional.of(testCase));
            when(timeEntryRepository.count()).thenReturn(0L);
            when(timeEntryRepository.save(any(TimeEntry.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            TimeEntryDTO result = timeEntryService.createTimeEntry(dto);

            // Assert
            assertThat(result.getBillable()).isFalse();
        }
    }

    // ========== UPDATE TIME ENTRY TESTS ==========

    @Nested
    @DisplayName("Update Time Entry Tests")
    class UpdateTimeEntryTests {

        @Test
        @DisplayName("Should update a time entry successfully")
        void updateTimeEntry_success() {
            // Arrange
            String id = "te-1";
            TimeEntryDTO updateDTO = TimeEntryDTO.builder()
                    .description("Updated description")
                    .workDate(LocalDate.now().plusDays(1))
                    .startTime(LocalTime.of(10, 0))
                    .endTime(LocalTime.of(12, 0))
                    .activityType("Research")
                    .billingRate(new BigDecimal("300.00"))
                    .notes("Updated notes")
                    .build();

            when(timeEntryRepository.findById(id)).thenReturn(Optional.of(timeEntry));
            when(timeEntryRepository.save(any(TimeEntry.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            TimeEntryDTO result = timeEntryService.updateTimeEntry(id, updateDTO);

            // Assert
            assertThat(result.getDescription()).isEqualTo("Updated description");
            assertThat(result.getActivityType()).isEqualTo("Research");
            assertThat(result.getNotes()).isEqualTo("Updated notes");
            assertThat(result.getBillingRate()).isEqualTo(new BigDecimal("300.00"));
        }

        @Test
        @DisplayName("Should throw exception when updating a billed time entry")
        void updateTimeEntry_billedFail() {
            // Arrange
            String id = "te-billed";
            TimeEntry billedEntry = TimeEntry.builder()
                    .id(id)
                    .entryNumber("TE-0002")
                    .caseId("case-1")
                    .userId("user-1")
                    .workDate(LocalDate.now())
                    .startTime(LocalTime.of(9, 0))
                    .endTime(LocalTime.of(10, 0))
                    .description("Already billed work")
                    .billable(true)
                    .billed(true)
                    .build();

            TimeEntryDTO updateDTO = TimeEntryDTO.builder()
                    .description("Updated")
                    .build();

            when(timeEntryRepository.findById(id)).thenReturn(Optional.of(billedEntry));

            // Act & Assert
            assertThatThrownBy(() -> timeEntryService.updateTimeEntry(id, updateDTO))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Cannot update a billed time entry");

            verify(timeEntryRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should recalculate amount after update")
        void updateTimeEntry_recalculateAmount() {
            // Arrange
            TimeEntry entry = TimeEntry.builder()
                    .id("te-recalc")
                    .entryNumber("TE-0003")
                    .caseId("case-1")
                    .userId("user-1")
                    .workDate(LocalDate.now())
                    .startTime(LocalTime.of(9, 0))
                    .endTime(LocalTime.of(10, 0))
                    .description("One hour")
                    .billingRate(new BigDecimal("100.00"))
                    .durationMinutes(60)
                    .durationHours(new BigDecimal("1.00"))
                    .billable(true)
                    .billed(false)
                    .build();

            TimeEntryDTO updateDTO = TimeEntryDTO.builder()
                    .startTime(LocalTime.of(9, 0))
                    .endTime(LocalTime.of(11, 0))
                    .billingRate(new BigDecimal("200.00"))
                    .build();

            when(timeEntryRepository.findById("te-recalc")).thenReturn(Optional.of(entry));
            when(timeEntryRepository.save(any(TimeEntry.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            TimeEntryDTO result = timeEntryService.updateTimeEntry("te-recalc", updateDTO);

            // Assert
            assertThat(result.getDurationMinutes()).isEqualTo(120);
            assertThat(result.getDurationHours()).isEqualTo(new BigDecimal("2.00"));
            assertThat(result.getAmount()).isEqualTo(new BigDecimal("400.00"));
        }

        @Test
        @DisplayName("Should throw exception when time entry not found")
        void updateTimeEntry_notFound() {
            // Arrange
            when(timeEntryRepository.findById("missing-id")).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> timeEntryService.updateTimeEntry("missing-id", timeEntryDTO))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Time Entry");

            verify(timeEntryRepository, never()).save(any());
        }
    }

    // ========== GET TIME ENTRY TESTS ==========

    @Nested
    @DisplayName("Get Time Entry Tests")
    class GetTimeEntryTests {

        @Test
        @DisplayName("Should get time entry by ID")
        void getTimeEntryById_success() {
            // Arrange
            when(timeEntryRepository.findById("te-1")).thenReturn(Optional.of(timeEntry));
            when(caseRepository.findById("case-1")).thenReturn(Optional.of(testCase));

            // Act
            TimeEntryDTO result = timeEntryService.getTimeEntryById("te-1");

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getEntryNumber()).isEqualTo("TE-0001");
            assertThat(result.getCaseId()).isEqualTo("case-1");
            assertThat(result.getDescription()).isEqualTo("Client consultation and document review");
            assertThat(result.getDurationMinutes()).isEqualTo(225);
            assertThat(result.getAmount()).isEqualTo(new BigDecimal("937.50"));
        }

        @Test
        @DisplayName("Should throw exception when time entry not found")
        void getTimeEntryById_notFound() {
            // Arrange
            when(timeEntryRepository.findById("missing-id")).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> timeEntryService.getTimeEntryById("missing-id"))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    // ========== LIST TIME ENTRIES TESTS ==========

    @Nested
    @DisplayName("List Time Entries Tests")
    class ListTimeEntriesTests {

        @Test
        @DisplayName("Should get time entries by date range")
        void getTimeEntries_dateRange() {
            // Arrange
            LocalDate start = LocalDate.now().minusDays(7);
            LocalDate end = LocalDate.now();
            Page<TimeEntry> page = new PageImpl<>(List.of(timeEntry));
            when(timeEntryRepository.findByDateRange(eq(start), eq(end), any(Pageable.class)))
                    .thenReturn(page);

            // Act
            PageResponse<TimeEntryDTO> result = timeEntryService.getTimeEntries(
                    0, 20, null, null, start.toString(), end.toString());

            // Assert
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getTotalElements()).isEqualTo(1);
        }

        @Test
        @DisplayName("Should get time entries by user ID")
        void getTimeEntries_byUserId() {
            // Arrange
            Page<TimeEntry> page = new PageImpl<>(List.of(timeEntry));
            when(timeEntryRepository.findByUserId("user-1", any(Pageable.class)))
                    .thenReturn(page);

            // Act
            PageResponse<TimeEntryDTO> result = timeEntryService.getTimeEntries(
                    0, 20, "user-1", null, null, null);

            // Assert
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getUserId()).isEqualTo("user-1");
        }

        @Test
        @DisplayName("Should filter time entries by case ID")
        void getTimeEntries_byCaseId() {
            // Arrange
            List<TimeEntry> entries = List.of(timeEntry);
            when(timeEntryRepository.findByCaseId("case-1")).thenReturn(entries);

            // Act
            PageResponse<TimeEntryDTO> result = timeEntryService.getTimeEntries(
                    0, 20, null, "case-1", null, null);

            // Assert
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getCaseId()).isEqualTo("case-1");
        }

        @Test
        @DisplayName("Should filter time entries by case ID and date range")
        void getTimeEntries_byCaseIdAndDateRange() {
            // Arrange
            List<TimeEntry> entries = List.of(timeEntry);
            when(timeEntryRepository.findByCaseId("case-1")).thenReturn(entries);

            // Act
            PageResponse<TimeEntryDTO> result = timeEntryService.getTimeEntries(
                    0, 20, null, "case-1", "2024-01-01", "2024-12-31");

            // Assert
            assertThat(result.getContent()).hasSize(1);
        }

        @Test
        @DisplayName("Should return empty page when no entries found")
        void getTimeEntries_empty() {
            // Arrange
            when(timeEntryRepository.findByDateRange(any(LocalDate.class), any(LocalDate.class), any(Pageable.class)))
                    .thenReturn(Page.empty());

            // Act
            PageResponse<TimeEntryDTO> result = timeEntryService.getTimeEntries(0, 20, null, null, null, null);

            // Assert
            assertThat(result.getContent()).isEmpty();
            assertThat(result.isEmpty()).isTrue();
        }

        @Test
        @DisplayName("Should use default pagination when nulls passed")
        void getTimeEntries_defaultValues() {
            // Arrange
            when(timeEntryRepository.findByDateRange(any(LocalDate.class), any(LocalDate.class), any(Pageable.class)))
                    .thenReturn(Page.empty());

            // Act
            PageResponse<TimeEntryDTO> result = timeEntryService.getTimeEntries(
                    null, null, null, null, null, null);

            // Assert
            assertThat(result.getPage()).isZero();
            assertThat(result.getSize()).isEqualTo(20);
        }
    }

    // ========== TIME ENTRIES BY CASE TESTS ==========

    @Nested
    @DisplayName("Time Entries By Case Tests")
    class TimeEntriesByCaseTests {

        @Test
        @DisplayName("Should return time entries for a specific case")
        void getTimeEntriesByCase_success() {
            // Arrange
            when(timeEntryRepository.findByCaseId("case-1")).thenReturn(List.of(timeEntry));

            // Act
            List<TimeEntryDTO> result = timeEntryService.getTimeEntriesByCase("case-1");

            // Assert
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getCaseId()).isEqualTo("case-1");
            assertThat(result.get(0).getWorkDate()).isEqualTo(LocalDate.now());
        }

        @Test
        @DisplayName("Should return empty list when no entries for case")
        void getTimeEntriesByCase_empty() {
            // Arrange
            when(timeEntryRepository.findByCaseId("no-case")).thenReturn(List.of());

            // Act
            List<TimeEntryDTO> result = timeEntryService.getTimeEntriesByCase("no-case");

            // Assert
            assertThat(result).isEmpty();
        }
    }

    // ========== UNBILLED ENTRIES TESTS ==========

    @Nested
    @DisplayName("Unbilled Billable Entries Tests")
    class UnbilledEntriesTests {

        @Test
        @DisplayName("Should return unbilled billable entries for user")
        void getUnbilledBillableEntries_success() {
            // Arrange
            TimeEntry unbilledEntry = TimeEntry.builder()
                    .id("te-unbilled")
                    .entryNumber("TE-0010")
                    .caseId("case-1")
                    .userId("user-1")
                    .workDate(LocalDate.now())
                    .description("Work to bill")
                    .billable(true)
                    .billed(false)
                    .build();

            when(timeEntryRepository.findUnbilledBillableEntries("user-1"))
                    .thenReturn(List.of(unbilledEntry));

            // Act
            List<TimeEntryDTO> result = timeEntryService.getUnbilledBillableEntries("user-1");

            // Assert
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getBillable()).isTrue();
            assertThat(result.get(0).getBilled()).isFalse();
        }

        @Test
        @DisplayName("Should return empty list when no unbilled entries")
        void getUnbilledBillableEntries_empty() {
            // Arrange
            when(timeEntryRepository.findUnbilledBillableEntries("user-1")).thenReturn(List.of());

            // Act
            List<TimeEntryDTO> result = timeEntryService.getUnbilledBillableEntries("user-1");

            // Assert
            assertThat(result).isEmpty();
        }
    }

    // ========== MARK AS BILLED TESTS ==========

    @Nested
    @DisplayName("Mark As Billed Tests")
    class MarkAsBilledTests {

        @Test
        @DisplayName("Should mark time entries as billed")
        void markAsBilled_success() {
            // Arrange
            List<String> ids = List.of("te-1", "te-2", "te-3");

            // Act
            timeEntryService.markAsBilled(ids);

            // Assert
            verify(timeEntryRepository).markAsBilled(ids);
        }

        @Test
        @DisplayName("Should handle empty list gracefully")
        void markAsBilled_emptyList() {
            // Arrange
            List<String> ids = List.of();

            // Act
            timeEntryService.markAsBilled(ids);

            // Assert
            verify(timeEntryRepository).markAsBilled(ids);
        }
    }

    // ========== DELETE TIME ENTRY TESTS ==========

    @Nested
    @DisplayName("Delete Time Entry Tests")
    class DeleteTimeEntryTests {

        @Test
        @DisplayName("Should soft delete time entry successfully")
        void deleteTimeEntry_success() {
            // Arrange
            when(timeEntryRepository.findById("te-1")).thenReturn(Optional.of(timeEntry));

            // Act
            timeEntryService.deleteTimeEntry("te-1");

            // Assert
            assertThat(timeEntry.getDeletedAt()).isNotNull();
            verify(timeEntryRepository).save(timeEntry);
        }

        @Test
        @DisplayName("Should throw exception when deleting non-existent entry")
        void deleteTimeEntry_notFound() {
            // Arrange
            when(timeEntryRepository.findById("missing-id")).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> timeEntryService.deleteTimeEntry("missing-id"))
                    .isInstanceOf(ResourceNotFoundException.class);

            verify(timeEntryRepository, never()).save(any());
        }
    }
}
