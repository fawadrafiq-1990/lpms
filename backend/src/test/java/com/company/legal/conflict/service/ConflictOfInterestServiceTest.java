package com.company.legal.conflict.service;

import com.company.legal.client.entity.Client;
import com.company.legal.client.repository.ClientRepository;
import com.company.legal.case_management.entity.Case;
import com.company.legal.case_management.repository.CaseRepository;
import com.company.legal.conflict.entity.ConflictCheck;
import com.company.legal.conflict.repository.ConflictCheckRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConflictOfInterestServiceTest {

    @Mock
    private ConflictCheckRepository conflictCheckRepository;

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private CaseRepository caseRepository;

    @InjectMocks
    private ConflictOfInterestService conflictService;

    private String lawFirmId;
    private Client existingClient;
    private Client prospectiveClient;
    private Case existingCase;
    private Case prospectiveCase;

    @BeforeEach
    void setUp() {
        lawFirmId = "firm-1";
        existingClient = Client.builder()
                .id("client-existing")
                .clientNumber("CL-OLD")
                .lawFirmId(lawFirmId)
                .clientType(Client.ClientType.INDIVIDUAL)
                .firstName("John")
                .lastName("Doe")
                .fullName("John Doe")
                .email("john.doe@example.com")
                .status(Client.ClientStatus.ACTIVE)
                .build();

        prospectiveClient = Client.builder()
                .id("client-prospective")
                .clientNumber("CL-NEW")
                .lawFirmId(lawFirmId)
                .clientType(Client.ClientType.INDIVIDUAL)
                .firstName("Jane")
                .lastName("Smith")
                .fullName("Jane Smith")
                .email("jane.smith@example.com")
                .status(Client.ClientStatus.ACTIVE)
                .build();

        existingCase = Case.builder()
                .id("case-existing")
                .caseNumber("CASE-001")
                .lawFirmId(lawFirmId)
                .clientId("client-existing")
                .clientName("John Doe")
                .caseType("Civil")
                .caseStatus("IN_PROGRESS")
                .build();

        prospectiveCase = Case.builder()
                .id("case-prospective")
                .caseNumber("CASE-002")
                .lawFirmId(lawFirmId)
                .clientId("client-prospective")
                .clientName("Jane Smith")
                .caseType("Civil")
                .caseStatus("NEW")
                .build();
    }

    // ========== CONFLICT DETECTION TESTS ==========

    @Nested
    @DisplayName("Conflict Detection Tests")
    class ConflictDetectionTests {

        @Test
        @DisplayName("Should detect exact name match conflict")
        void checkConflicts_exactNameMatch() {
            // Arrange
            List<Client> allClients = List.of(existingClient);
            when(clientRepository.findAll()).thenReturn(allClients);

            // Act
            List<ConflictCheck> result = conflictService.checkConflicts(
                    lawFirmId, "client-prospective", null, "John Doe");

            // Assert
            assertThat(result).isNotEmpty();
            ConflictCheck conflict = result.get(0);
            assertThat(conflict.isConflict()).isTrue();
            assertThat(conflict.getConflictType()).isEqualTo("CLIENT");
            assertThat(conflict.getSeverity()).isEqualTo("HIGH");
            assertThat(conflict.getMatchField()).isEqualTo("client_name");
            assertThat(conflict.getRelatedClientId()).isEqualTo("client-existing");
        }

        @Test
        @DisplayName("Should detect partial name match conflict")
        void checkConflicts_partialNameMatch() {
            // Arrange
            Client johnClient = Client.builder()
                    .id("client-john")
                    .lawFirmId(lawFirmId)
                    .firstName("John")
                    .lastName("Doe")
                    .fullName("John Doe")
                    .build();

            List<Client> allClients = List.of(johnClient);
            when(clientRepository.findAll()).thenReturn(allClients);

            // Act - search for "John" only
            List<ConflictCheck> result = conflictService.checkConflicts(
                    lawFirmId, "client-other", null, "John");

            // Assert
            assertThat(result).isNotEmpty();
        }

        @Test
        @DisplayName("Should detect fuzzy name match conflict")
        void checkConflicts_fuzzyNameMatch() {
            // Arrange
            Client acmeClient = Client.builder()
                    .id("client-acme")
                    .lawFirmId(lawFirmId)
                    .clientType(Client.ClientType.COMPANY)
                    .companyName("Acme Technology Solutions Inc")
                    .fullName("Acme Technology Solutions Inc")
                    .build();

            List<Client> allClients = List.of(acmeClient);
            when(clientRepository.findAll()).thenReturn(allClients);

            // Act - search similar company name
            List<ConflictCheck> result = conflictService.checkConflicts(
                    lawFirmId, "client-new", null, "Acme Tech");

            // Assert
            assertThat(result).isNotEmpty();
            ConflictCheck conflict = result.get(0);
            assertThat(conflict.isConflict()).isTrue();
            assertThat(conflict.getMatchField()).isEqualTo("client_name_fuzzy");
            assertThat(conflict.getSeverity()).isEqualTo("MEDIUM");
        }

        @Test
        @DisplayName("Should not detect conflict for different names")
        void checkConflicts_noConflict() {
            // Arrange
            Client differentClient = Client.builder()
                    .id("client-different")
                    .lawFirmId(lawFirmId)
                    .firstName("Alice")
                    .lastName("Wilson")
                    .fullName("Alice Wilson")
                    .build();

            List<Client> allClients = List.of(differentClient);
            when(clientRepository.findAll()).thenReturn(allClients);

            // Act
            List<ConflictCheck> result = conflictService.checkConflicts(
                    lawFirmId, "client-new", null, "Completely Different Name XYZ");

            // Assert
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should not report self as conflict")
        void checkConflicts_selfExcluded() {
            // Arrange
            List<Client> allClients = List.of(existingClient);
            when(clientRepository.findAll()).thenReturn(allClients);

            // Act - querying for existing client itself
            List<ConflictCheck> result = conflictService.checkConflicts(
                    lawFirmId, "client-existing", null, "John Doe");

            // Assert
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should detect email overlap conflict")
        void checkConflicts_emailOverlap() {
            // Arrange
            Client emailOverlap = Client.builder()
                    .id("client-email-same")
                    .lawFirmId(lawFirmId)
                    .firstName("Different")
                    .lastName("Person")
                    .fullName("Different Person")
                    .email("jane.smith@example.com") // Same email as prospective
                    .build();

            when(clientRepository.findAll()).thenReturn(List.of(existingClient, emailOverlap));
            when(clientRepository.findById("client-prospective")).thenReturn(Optional.of(prospectiveClient));

            // Act
            List<ConflictCheck> result = conflictService.checkConflicts(
                    lawFirmId, "client-prospective", null, null);

            // Assert
            assertThat(result).isNotEmpty();
            ConflictCheck emailConflict = result.stream()
                    .filter(c -> "email".equals(c.getMatchField()))
                    .findFirst()
                    .orElse(null);
            assertThat(emailConflict).isNotNull();
            assertThat(emailConflict.getSeverity()).isEqualTo("CRITICAL");
        }

        @Test
        @DisplayName("Should detect case-client overlap conflict")
        void checkConflicts_caseClientOverlap() {
            // Arrange
            when(clientRepository.findAll()).thenReturn(List.of(existingClient));
            when(clientRepository.findById("client-prospective")).thenReturn(Optional.of(prospectiveClient));
            when(caseRepository.findById("case-prospective")).thenReturn(Optional.of(prospectiveCase));

            // Another case with same client ID in same firm
            Case anotherProspectiveCase = Case.builder()
                    .id("case-another")
                    .lawFirmId(lawFirmId)
                    .clientId("client-prospective")
                    .clientName("Jane Smith")
                    .caseType("Criminal")
                    .build();

            when(caseRepository.findAll())
                    .thenReturn(List.of(existingCase))
                    .thenReturn(List.of(anotherProspectiveCase));

            // Act
            List<ConflictCheck> result = conflictService.checkConflicts(
                    lawFirmId, "client-prospective", "case-prospective", "Jane Smith");

            // Assert
            assertThat(result).isNotEmpty();
            ConflictCheck caseConflict = result.stream()
                    .filter(c -> "case_client_overlap".equals(c.getMatchField()))
                    .findFirst()
                    .orElse(null);
            assertThat(caseConflict).isNotNull();
            assertThat(caseConflict.getConflictType()).isEqualTo("PARTY");
            assertThat(caseConflict.getSeverity()).isEqualTo("HIGH");
        }

        @Test
        @DisplayName("Should return empty list when no conflicts found")
        void checkConflicts_cleanCheck() {
            // Arrange
            when(clientRepository.findAll()).thenReturn(List.of(existingClient));

            // Act
            List<ConflictCheck> result = conflictService.checkConflicts(
                    lawFirmId, "client-new", "case-new", "Random Name That Does Not Match");

            // Assert
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should handle null query client name gracefully")
        void checkConflicts_nullQueryName() {
            // Arrange
            when(clientRepository.findAll()).thenReturn(List.of(existingClient));

            // Act
            List<ConflictCheck> result = conflictService.checkConflicts(
                    lawFirmId, "client-new", null, null);

            // Assert
            assertThat(result).isEmpty();
        }
    }

    // ========== SAVE CONFLICT CHECK TESTS ==========

    @Nested
    @DisplayName("Save Conflict Check Tests")
    class SaveConflictCheckTests {

        @Test
        @DisplayName("Should save a conflict check result")
        void saveCheck_success() {
            // Arrange
            ConflictCheck check = ConflictCheck.builder()
                    .id("cc-1")
                    .lawFirmId(lawFirmId)
                    .queryClientName("Test Client")
                    .isConflict(true)
                    .conflictType(ConflictCheck.ConflictType.CLIENT.name())
                    .severity(ConflictCheck.Severity.HIGH.name())
                    .checkedAt(LocalDateTime.now())
                    .build();

            when(conflictCheckRepository.save(check)).thenReturn(check);

            // Act
            ConflictCheck result = conflictService.saveCheck(check);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo("cc-1");
            verify(conflictCheckRepository).save(check);
        }
    }

    // ========== CLEAR CONFLICT TESTS ==========

    @Nested
    @DisplayName("Clear Conflict Tests")
    class ClearConflictTests {

        @Test
        @DisplayName("Should clear a conflict with justification")
        void clearConflict_success() {
            // Arrange
            ConflictCheck check = ConflictCheck.builder()
                    .id("cc-1")
                    .lawFirmId(lawFirmId)
                    .isConflict(true)
                    .cleared(false)
                    .build();

            when(conflictCheckRepository.findById("cc-1")).thenReturn(Optional.of(check));
            when(conflictCheckRepository.save(any(ConflictCheck.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            ConflictCheck result = conflictService.clearConflict("cc-1", "lawyer-1", "No actual conflict");

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.isCleared()).isTrue();
            assertThat(result.getClearedByUserId()).isEqualTo("lawyer-1");
            assertThat(result.getClearedReason()).isEqualTo("No actual conflict");
        }

        @Test
        @DisplayName("Should throw exception when clearing non-existent check")
        void clearConflict_notFound() {
            // Arrange
            when(conflictCheckRepository.findById("missing-cc")).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> conflictService.clearConflict("missing-cc", "lawyer-1", "Reason"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Conflict check not found");
        }
    }

    // ========== GET ACTIVE CONFLICTS TESTS ==========

    @Nested
    @DisplayName("Get Active Conflicts Tests")
    class GetActiveConflictsTests {

        @Test
        @DisplayName("Should return active conflicts for a law firm")
        void getActiveConflicts_success() {
            // Arrange
            List<ConflictCheck> activeConflicts = List.of(
                    ConflictCheck.builder()
                            .id("cc-1")
                            .lawFirmId(lawFirmId)
                            .isConflict(true)
                            .cleared(false)
                            .checkedAt(LocalDateTime.now())
                            .build()
            );

            when(conflictCheckRepository.findActiveConflicts(lawFirmId))
                    .thenReturn(activeConflicts);

            // Act
            List<ConflictCheck> result = conflictService.getActiveConflicts(lawFirmId);

            // Assert
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getCleared()).isFalse();
        }

        @Test
        @DisplayName("Should return empty list when no active conflicts")
        void getActiveConflicts_empty() {
            // Arrange
            when(conflictCheckRepository.findActiveConflicts(lawFirmId))
                    .thenReturn(List.of());

            // Act
            List<ConflictCheck> result = conflictService.getActiveConflicts(lawFirmId);

            // Assert
            assertThat(result).isEmpty();
        }
    }

    // ========== GET CHECKS FOR CLIENT TESTS ==========

    @Nested
    @DisplayName("Get Checks For Client Tests")
    class GetChecksForClientTests {

        @Test
        @DisplayName("Should return all conflict checks for a client")
        void getChecksForClient_success() {
            // Arrange
            List<ConflictCheck> checks = List.of(
                    ConflictCheck.builder()
                            .id("cc-1")
                            .lawFirmId(lawFirmId)
                            .queryClientId("client-1")
                            .isConflict(true)
                            .checkedAt(LocalDateTime.now())
                            .build()
            );

            when(conflictCheckRepository.findByQueryClientId(lawFirmId, "client-1"))
                    .thenReturn(checks);

            // Act
            List<ConflictCheck> result = conflictService.getChecksForClient(lawFirmId, "client-1");

            // Assert
            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("Should return empty list when no checks for client")
        void getChecksForClient_empty() {
            // Arrange
            when(conflictCheckRepository.findByQueryClientId(lawFirmId, "client-none"))
                    .thenReturn(List.of());

            // Act
            List<ConflictCheck> result = conflictService.getChecksForClient(lawFirmId, "client-none");

            // Assert
            assertThat(result).isEmpty();
        }
    }

    // ========== NORMALIZE FUNCTION TESTS ==========

    @Nested
    @DisplayName("Normalization Tests")
    class NormalizationTests {

        @Test
        @DisplayName("Should normalize empty string")
        void normalize_emptyString() {
            // Using reflection would be ideal, but we test via the conflict check behavior
            // Empty string query produces no conflicts
            when(clientRepository.findAll()).thenReturn(List.of(existingClient));

            List<ConflictCheck> result = conflictService.checkConflicts(
                    lawFirmId, "client-1", null, "");

            assertThat(result).isEmpty();
        }
    }
}
