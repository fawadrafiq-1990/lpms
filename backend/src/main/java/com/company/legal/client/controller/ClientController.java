package com.company.legal.client.controller;

import com.company.legal.client.dto.ClientDTO;
import com.company.legal.client.service.ClientService;
import com.company.legal.common.dto.ApiResponse;
import com.company.legal.common.dto.PageResponse;
import com.company.legal.common.security.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for Client management.
 */
@RestController
@RequestMapping("/v1/clients")
@RequiredArgsConstructor
@Tag(name = "Clients", description = "Client management APIs")
public class ClientController {

    private final ClientService clientService;

    @PostMapping
    @Operation(summary = "Create a new client")
    public ResponseEntity<ApiResponse<ClientDTO>> createClient(
            @Valid @RequestBody ClientDTO dto) {
        ClientDTO created = clientService.createClient(dto);
        return ResponseEntity.ok(ApiResponse.success(created, "Client created successfully"));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing client")
    public ResponseEntity<ApiResponse<ClientDTO>> updateClient(
            @PathVariable String id,
            @Valid @RequestBody ClientDTO dto) {
        ClientDTO updated = clientService.updateClient(id, dto);
        return ResponseEntity.ok(ApiResponse.success(updated, "Client updated successfully"));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get client by ID")
    public ResponseEntity<ApiResponse<ClientDTO>> getClient(@PathVariable String id) {
        ClientDTO client = clientService.getClientById(id);
        return ResponseEntity.ok(ApiResponse.success(client));
    }

    @GetMapping
    @Operation(summary = "Get all clients with pagination and filters")
    public ResponseEntity<ApiResponse<PageResponse<ClientDTO>>> getClients(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String type) {
        // Get law firm ID from authenticated user context
        String lawFirmId = getLawFirmIdFromAuth();
        PageResponse<ClientDTO> clients = clientService.getClients(
                lawFirmId, page, size, search, status, type);
        return ResponseEntity.ok(ApiResponse.success(clients));
    }

    @GetMapping("/active")
    @Operation(summary = "Get all active clients")
    public ResponseEntity<ApiResponse<List<ClientDTO>>> getActiveClients() {
        String lawFirmId = getLawFirmIdFromAuth();
        List<ClientDTO> clients = clientService.getActiveClients(lawFirmId);
        return ResponseEntity.ok(ApiResponse.success(clients));
    }

    @GetMapping("/my-clients")
    @Operation(summary = "Get clients assigned to current lawyer")
    public ResponseEntity<ApiResponse<List<ClientDTO>>> getMyClients() {
        // Get lawyer ID from authenticated user context
        String lawyerId = getLawyerIdFromAuth();
        List<ClientDTO> clients = clientService.getClientsByLawyer(lawyerId);
        return ResponseEntity.ok(ApiResponse.success(clients));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a client (soft delete)")
    public ResponseEntity<ApiResponse<Void>> deleteClient(@PathVariable String id) {
        clientService.deleteClient(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Client deleted successfully"));
    }

    // Helper methods to get context from authentication
    private String getLawFirmIdFromAuth() {
        return SecurityUtils.getLawFirmId();
    }

    private String getLawyerIdFromAuth() {
        return SecurityUtils.getCurrentLawyerId();
    }
}