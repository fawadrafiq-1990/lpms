package com.company.legal.client.service;

import com.company.legal.client.dto.ClientDTO;
import com.company.legal.client.dto.ClientContactDTO;
import com.company.legal.client.entity.Client;
import com.company.legal.client.repository.ClientRepository;
import com.company.legal.common.dto.PageResponse;
import com.company.legal.common.security.SecurityUtils;
import com.company.legal.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for Client management.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ClientService {

    private final ClientRepository clientRepository;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    @Transactional
    public ClientDTO createClient(ClientDTO dto) {
        log.info("Creating client: {}", dto.getClientNumber());

        if (clientRepository.existsByClientNumber(dto.getClientNumber())) {
            throw new IllegalArgumentException("Client number already exists");
        }

        Client client = mapToEntity(dto);
        client.setLawFirmId(SecurityUtils.getLawFirmId());
        client = clientRepository.save(client);

        return mapToDTO(client);
    }

    @Transactional
    public ClientDTO updateClient(String id, ClientDTO dto) {
        log.info("Updating client: {}", id);

        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Client", "id", id));

        // Update fields
        client.setFirstName(dto.getFirstName());
        client.setLastName(dto.getLastName());
        client.setCompanyName(dto.getCompanyName());
        client.setEmail(dto.getEmail());
        client.setPhone(dto.getPhone());
        client.setMobile(dto.getMobile());
        client.setRiskLevel(dto.getRiskLevel());
        client.setStatus(dto.getStatus());
        client.setNotes(dto.getNotes());
        client.setAssignedLawyerId(dto.getAssignedLawyerId());
        client.setAssignedLawyerName(dto.getAssignedLawyerName());

        client = clientRepository.save(client);

        return mapToDTO(client);
    }

    @Transactional(readOnly = true)
    public ClientDTO getClientById(String id) {
        log.info("Fetching client by id: {}", id);

        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Client", "id", id));

        return mapToDTO(client);
    }

    @Transactional(readOnly = true)
    public PageResponse<ClientDTO> getClients(String lawFirmId, Integer page, Integer size,
                                               String search, String status, String type) {
        log.info("Fetching clients for firm: {}", lawFirmId);

        Pageable pageable = PageRequest.of(page != null ? page : 0,
                                          size != null ? size : 20,
                                          Sort.by("createdAt").descending());

        Page<Client> clientPage;

        if (search != null && !search.isEmpty()) {
            clientPage = clientRepository.searchByLawFirm(lawFirmId, search, pageable);
        } else if (status != null && !status.isEmpty()) {
            clientPage = clientRepository.findByLawFirmIdAndStatus(
                    lawFirmId, Client.ClientStatus.valueOf(status), pageable);
        } else if (type != null && !type.isEmpty()) {
            clientPage = clientRepository.findByLawFirmIdAndClientType(
                    lawFirmId, Client.ClientType.valueOf(type), pageable);
        } else {
            clientPage = clientRepository.findByLawFirmId(lawFirmId, pageable);
        }

        return PageResponse.from(clientPage.map(this::mapToDTO));
    }

    @Transactional
    public void deleteClient(String id) {
        log.info("Deleting client: {}", id);

        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Client", "id", id));

        client.softDelete();
        clientRepository.save(client);
    }

    @Transactional(readOnly = true)
    public List<ClientDTO> getActiveClients(String lawFirmId) {
        log.info("Fetching active clients for firm: {}", lawFirmId);

        return clientRepository.findActiveByLawFirmId(lawFirmId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ClientDTO> getClientsByLawyer(String lawyerId) {
        log.info("Fetching clients for lawyer: {}", lawyerId);

        return clientRepository.findByAssignedLawyerId(lawyerId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    // Private helper methods
    private Client mapToEntity(ClientDTO dto) {
        return Client.builder()
                .clientNumber(dto.getClientNumber())
                .clientType(dto.getClientType())
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .companyName(dto.getCompanyName())
                .registrationNumber(dto.getRegistrationNumber())
                .taxId(dto.getTaxId())
                .email(dto.getEmail())
                .phone(dto.getPhone())
                .mobile(dto.getMobile())
                .nationality(dto.getNationality())
                .identificationNumber(dto.getIdentificationNumber())
                .dateOfBirth(dto.getDateOfBirth())
                .gender(dto.getGender())
                .website(dto.getWebsite())
                .industry(dto.getIndustry())
                .companySize(dto.getCompanySize())
                .annualRevenue(dto.getAnnualRevenue())
                .riskLevel(dto.getRiskLevel() != null ? dto.getRiskLevel() : Client.RiskLevel.LOW)
                .vip(dto.getVip() != null ? dto.getVip() : false)
                .creditLimit(dto.getCreditLimit())
                .billingModel(dto.getBillingModel() != null ? dto.getBillingModel() : "HOURLY")
                .preferredPaymentMethod(dto.getPreferredPaymentMethod())
                .status(dto.getStatus() != null ? dto.getStatus() : Client.ClientStatus.ACTIVE)
                .referredBy(dto.getReferredBy())
                .source(dto.getSource())
                .notes(dto.getNotes())
                .assignedLawyerId(dto.getAssignedLawyerId())
                .assignedLawyerName(dto.getAssignedLawyerName())
                .build();
    }

    private ClientDTO mapToDTO(Client client) {
        return ClientDTO.builder()
                .id(client.getId())
                .clientNumber(client.getClientNumber())
                .clientType(client.getClientType())
                .firstName(client.getFirstName())
                .lastName(client.getLastName())
                .companyName(client.getCompanyName())
                .registrationNumber(client.getRegistrationNumber())
                .taxId(client.getTaxId())
                .email(client.getEmail())
                .phone(client.getPhone())
                .mobile(client.getMobile())
                .nationality(client.getNationality())
                .identificationNumber(client.getIdentificationNumber())
                .dateOfBirth(client.getDateOfBirth())
                .gender(client.getGender())
                .website(client.getWebsite())
                .industry(client.getIndustry())
                .companySize(client.getCompanySize())
                .annualRevenue(client.getAnnualRevenue())
                .riskLevel(client.getRiskLevel())
                .vip(client.getVip())
                .creditLimit(client.getCreditLimit())
                .billingModel(client.getBillingModel())
                .preferredPaymentMethod(client.getPreferredPaymentMethod())
                .status(client.getStatus())
                .referredBy(client.getReferredBy())
                .source(client.getSource())
                .notes(client.getNotes())
                .assignedLawyerId(client.getAssignedLawyerId())
                .assignedLawyerName(client.getAssignedLawyerName())
                .createdAt(client.getCreatedAt() != null ? client.getCreatedAt().format(DATE_FORMATTER) : null)
                .updatedAt(client.getUpdatedAt() != null ? client.getUpdatedAt().format(DATE_FORMATTER) : null)
                .build();
    }
}