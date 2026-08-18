package com.company.legal.client.service;

import com.company.legal.client.dto.ClientContactDTO;
import com.company.legal.client.dto.ClientDTO;
import com.company.legal.client.entity.Client;
import com.company.legal.client.repository.ClientRepository;
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

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClientServiceTest {

    @Mock
    private ClientRepository clientRepository;

    @InjectMocks
    private ClientService clientService;

    private Client individualClient;
    private Client companyClient;
    private ClientDTO clientDTO;

    @BeforeEach
    void setUp() {
        individualClient = Client.builder()
                .id("client-1")
                .clientNumber("CL-001")
                .lawFirmId("firm-1")
                .clientType(Client.ClientType.INDIVIDUAL)
                .firstName("John")
                .lastName("Doe")
                .email("john@example.com")
                .phone("+1234567890")
                .status(Client.ClientStatus.ACTIVE)
                .riskLevel(Client.RiskLevel.LOW)
                .billingModel("HOURLY")
                .build();

        companyClient = Client.builder()
                .id("client-2")
                .clientNumber("CL-002")
                .lawFirmId("firm-1")
                .clientType(Client.ClientType.COMPANY)
                .companyName("Acme Corp")
                .email("info@acme.com")
                .status(Client.ClientStatus.ACTIVE)
                .riskLevel(Client.RiskLevel.MEDIUM)
                .build();

        clientDTO = ClientDTO.builder()
                .clientNumber("CL-NEW")
                .clientType(Client.ClientType.INDIVIDUAL)
                .firstName("Alice")
                .lastName("Johnson")
                .email("alice@example.com")
                .status(Client.ClientStatus.ACTIVE)
                .build();
    }

    // ========== CREATE CLIENT TESTS ==========

    @Nested
    @DisplayName("Create Client Tests")
    class CreateClientTests {

        @Test
        @DisplayName("Should create an individual client successfully")
        void createClient_individualSuccess() {
            // Arrange
            when(clientRepository.existsByClientNumber("CL-NEW")).thenReturn(false);
            when(clientRepository.save(any(Client.class))).thenAnswer(invocation -> {
                Client c = invocation.getArgument(0);
                c.setId("new-client-id");
                return c;
            });

            // Act
            ClientDTO result = clientService.createClient(clientDTO);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo("new-client-id");
            assertThat(result.getClientNumber()).isEqualTo("CL-NEW");
            assertThat(result.getFirstName()).isEqualTo("Alice");
            assertThat(result.getLastName()).isEqualTo("Johnson");
            assertThat(result.getClientType()).isEqualTo(Client.ClientType.INDIVIDUAL);

            ArgumentCaptor<Client> captor = ArgumentCaptor.forClass(Client.class);
            verify(clientRepository).save(captor.capture());
            Client saved = captor.getValue();
            assertThat(saved.getStatus()).isEqualTo(Client.ClientStatus.ACTIVE);
            assertThat(saved.getRiskLevel()).isEqualTo(Client.RiskLevel.LOW);
            assertThat(saved.getVip()).isFalse();
            assertThat(saved.getBillingModel()).isEqualTo("HOURLY");
        }

        @Test
        @DisplayName("Should create a company client successfully")
        void createClient_companySuccess() {
            // Arrange
            ClientDTO companyDTO = ClientDTO.builder()
                    .clientNumber("CL-COMP")
                    .clientType(Client.ClientType.COMPANY)
                    .companyName("Acme Corp")
                    .registrationNumber("REG-123")
                    .taxId("TAX-456")
                    .industry("Technology")
                    .build();

            when(clientRepository.existsByClientNumber("CL-COMP")).thenReturn(false);
            when(clientRepository.save(any(Client.class))).thenAnswer(invocation -> {
                Client c = invocation.getArgument(0);
                c.setId("comp-client-id");
                return c;
            });

            // Act
            ClientDTO result = clientService.createClient(companyDTO);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getClientType()).isEqualTo(Client.ClientType.COMPANY);
            assertThat(result.getCompanyName()).isEqualTo("Acme Corp");
            assertThat(result.getRegistrationNumber()).isEqualTo("REG-123");
        }

        @Test
        @DisplayName("Should throw exception when client number already exists")
        void createClient_duplicateClientNumber() {
            // Arrange
            when(clientRepository.existsByClientNumber("CL-NEW")).thenReturn(true);

            // Act & Assert
            assertThatThrownBy(() -> clientService.createClient(clientDTO))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Client number already exists");

            verify(clientRepository, never()).save(any());
        }
    }

    // ========== UPDATE CLIENT TESTS ==========

    @Nested
    @DisplayName("Update Client Tests")
    class UpdateClientTests {

        @Test
        @DisplayName("Should update client successfully")
        void updateClient_success() {
            // Arrange
            String id = "client-1";
            ClientDTO updateDTO = ClientDTO.builder()
                    .firstName("Bob")
                    .lastName("Brown")
                    .email("bob@example.com")
                    .phone("+9876543210")
                    .riskLevel(Client.RiskLevel.HIGH)
                    .status(Client.ClientStatus.INACTIVE)
                    .notes("Updated notes")
                    .build();

            when(clientRepository.findById(id)).thenReturn(Optional.of(individualClient));
            when(clientRepository.save(any(Client.class))).thenAnswer(invocation -> {
                Client c = invocation.getArgument(0);
                return c;
            });

            // Act
            ClientDTO result = clientService.updateClient(id, updateDTO);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getFirstName()).isEqualTo("Bob");
            assertThat(result.getLastName()).isEqualTo("Brown");
            assertThat(result.getEmail()).isEqualTo("bob@example.com");
            assertThat(result.getPhone()).isEqualTo("+9876543210");
            assertThat(result.getRiskLevel()).isEqualTo(Client.RiskLevel.HIGH);
            assertThat(result.getStatus()).isEqualTo(Client.ClientStatus.INACTIVE);
            assertThat(result.getNotes()).isEqualTo("Updated notes");

            verify(clientRepository).save(any());
        }

        @Test
        @DisplayName("Should update client contacts")
        void updateClient_withContacts() {
            // Arrange
            String id = "client-1";
            ClientDTO updateDTO = ClientDTO.builder()
                    .firstName("Updated")
                    .lastName("Client")
                    .contacts(List.of(ClientContactDTO.builder()
                            .firstName("Contact")
                            .lastName("Person")
                            .email("contact@example.com")
                            .isPrimary(true)
                            .build()))
                    .build();

            when(clientRepository.findById(id)).thenReturn(Optional.of(individualClient));
            when(clientRepository.save(any(Client.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            ClientDTO result = clientService.updateClient(id, updateDTO);

            // Assert
            assertThat(result).isNotNull();
            verify(clientRepository).save(any());
        }

        @Test
        @DisplayName("Should throw exception when client not found")
        void updateClient_notFound() {
            // Arrange
            when(clientRepository.findById("nonexistent-id")).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> clientService.updateClient("nonexistent-id", clientDTO))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Client")
                    .hasMessageContaining("nonexistent-id");

            verify(clientRepository, never()).save(any());
        }
    }

    // ========== GET CLIENT TESTS ==========

    @Nested
    @DisplayName("Get Client Tests")
    class GetClientTests {

        @Test
        @DisplayName("Should get client by ID")
        void getClientById_success() {
            // Arrange
            when(clientRepository.findById("client-1")).thenReturn(Optional.of(individualClient));

            // Act
            ClientDTO result = clientService.getClientById("client-1");

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo("client-1");
            assertThat(result.getClientNumber()).isEqualTo("CL-001");
            assertThat(result.getFirstName()).isEqualTo("John");
            assertThat(result.getLastName()).isEqualTo("Doe");
            assertThat(result.getEmail()).isEqualTo("john@example.com");
        }

        @Test
        @DisplayName("Should throw exception when client not found")
        void getClientById_notFound() {
            // Arrange
            when(clientRepository.findById("missing-id")).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> clientService.getClientById("missing-id"))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Client");
        }
    }

    // ========== LIST CLIENTS TESTS ==========

    @Nested
    @DisplayName("List Clients Tests")
    class ListClientsTests {

        @Test
        @DisplayName("Should return paginated clients for law firm")
        void getClients_withPagination() {
            // Arrange
            Page<Client> clientPage = new PageImpl<>(List.of(individualClient, companyClient));
            when(clientRepository.findByLawFirmIdAndDeletedAtIsNull("firm-1", any(Pageable.class)))
                    .thenReturn(clientPage);

            // Act
            PageResponse<ClientDTO> result = clientService.getClients("firm-1", 0, 20, null, null, null);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getTotalElements()).isEqualTo(2);
            assertThat(result.getPage()).isEqualTo(0);
            assertThat(result.getSize()).isEqualTo(20);
        }

        @Test
        @DisplayName("Should filter clients by status")
        void getClients_filterByStatus() {
            // Arrange
            Page<Client> activePage = new PageImpl<>(List.of(individualClient));
            when(clientRepository.findByLawFirmIdAndStatusAndDeletedAtIsNull(
                    eq("firm-1"), eq(Client.ClientStatus.ACTIVE), any(Pageable.class)))
                    .thenReturn(activePage);

            // Act
            PageResponse<ClientDTO> result = clientService.getClients("firm-1", 0, 20, null, "ACTIVE", null);

            // Assert
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getStatus()).isEqualTo(Client.ClientStatus.ACTIVE);
        }

        @Test
        @DisplayName("Should filter clients by type")
        void getClients_filterByType() {
            // Arrange
            Page<Client> companyPage = new PageImpl<>(List.of(companyClient));
            when(clientRepository.findByLawFirmIdAndClientType(
                    eq("firm-1"), eq(Client.ClientType.COMPANY), any(Pageable.class)))
                    .thenReturn(companyPage);

            // Act
            PageResponse<ClientDTO> result = clientService.getClients("firm-1", 0, 20, null, null, "COMPANY");

            // Assert
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getClientType()).isEqualTo(Client.ClientType.COMPANY);
        }

        @Test
        @DisplayName("Should search clients by text")
        void getClients_withSearch() {
            // Arrange
            Page<Client> searchPage = new PageImpl<>(List.of(individualClient));
            when(clientRepository.searchByLawFirm(eq("firm-1"), eq("John"), any(Pageable.class)))
                    .thenReturn(searchPage);

            // Act
            PageResponse<ClientDTO> result = clientService.getClients("firm-1", 0, 20, "John", null, null);

            // Assert
            assertThat(result.getContent()).hasSize(1);
        }

        @Test
        @DisplayName("Should return empty page when no clients found")
        void getClients_emptyResult() {
            // Arrange
            Page<Client> emptyPage = Page.empty();
            when(clientRepository.findByLawFirmIdAndDeletedAtIsNull("firm-1", any(Pageable.class)))
                    .thenReturn(emptyPage);

            // Act
            PageResponse<ClientDTO> result = clientService.getClients("firm-1", 0, 20, null, null, null);

            // Assert
            assertThat(result.getContent()).isEmpty();
            assertThat(result.getTotalElements()).isZero();
            assertThat(result.isEmpty()).isTrue();
        }

        @Test
        @DisplayName("Should use default pagination values when null passed")
        void getClients_defaultPagination() {
            // Arrange
            Page<Client> emptyPage = Page.empty();
            when(clientRepository.findByLawFirmIdAndDeletedAtIsNull("firm-1", any(Pageable.class)))
                    .thenReturn(emptyPage);

            // Act
            PageResponse<ClientDTO> result = clientService.getClients("firm-1", null, null, null, null, null);

            // Assert
            assertThat(result.getPage()).isZero();
            assertThat(result.getSize()).isEqualTo(20);
        }
    }

    // ========== DELETE CLIENT TESTS ==========

    @Nested
    @DisplayName("Delete Client Tests")
    class DeleteClientTests {

        @Test
        @DisplayName("Should soft delete client successfully")
        void deleteClient_success() {
            // Arrange
            when(clientRepository.findById("client-1")).thenReturn(Optional.of(individualClient));

            // Act
            clientService.deleteClient("client-1");

            // Assert
            assertThat(individualClient.getDeletedAt()).isNotNull();
            verify(clientRepository).save(individualClient);
        }

        @Test
        @DisplayName("Should throw exception when deleting non-existent client")
        void deleteClient_notFound() {
            // Arrange
            when(clientRepository.findById("missing-id")).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> clientService.deleteClient("missing-id"))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Client");

            verify(clientRepository, never()).save(any());
        }
    }

    // ========== ACTIVE CLIENTS TESTS ==========

    @Nested
    @DisplayName("Active Clients Tests")
    class ActiveClientsTests {

        @Test
        @DisplayName("Should return active clients for law firm")
        void getActiveClients_success() {
            // Arrange
            when(clientRepository.findActiveByLawFirmId("firm-1"))
                    .thenReturn(List.of(individualClient, companyClient));

            // Act
            List<ClientDTO> result = clientService.getActiveClients("firm-1");

            // Assert
            assertThat(result).hasSize(2);
            assertThat(result).allMatch(c -> c.getStatus() == Client.ClientStatus.ACTIVE);
        }

        @Test
        @DisplayName("Should return empty list when no active clients")
        void getActiveClients_empty() {
            // Arrange
            when(clientRepository.findActiveByLawFirmId("firm-1")).thenReturn(List.of());

            // Act
            List<ClientDTO> result = clientService.getActiveClients("firm-1");

            // Assert
            assertThat(result).isEmpty();
        }
    }

    // ========== CLIENTS BY LAWYER TESTS ==========

    @Nested
    @DisplayName("Clients By Lawyer Tests")
    class ClientsByLawyerTests {

        @Test
        @DisplayName("Should return clients assigned to a lawyer")
        void getClientsByLawyer_success() {
            // Arrange
            when(clientRepository.findByAssignedLawyerId("lawyer-1"))
                    .thenReturn(List.of(individualClient));

            // Act
            List<ClientDTO> result = clientService.getClientsByLawyer("lawyer-1");

            // Assert
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getId()).isEqualTo("client-1");
        }

        @Test
        @DisplayName("Should return empty list when no clients for lawyer")
        void getClientsByLawyer_empty() {
            // Arrange
            when(clientRepository.findByAssignedLawyerId("lawyer-no-clients"))
                    .thenReturn(List.of());

            // Act
            List<ClientDTO> result = clientService.getClientsByLawyer("lawyer-no-clients");

            // Assert
            assertThat(result).isEmpty();
        }
    }
}
