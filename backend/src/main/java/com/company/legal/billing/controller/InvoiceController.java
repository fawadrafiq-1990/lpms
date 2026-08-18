package com.company.legal.billing.controller;

import com.company.legal.billing.dto.InvoiceDTO;
import com.company.legal.billing.service.InvoiceService;
import com.company.legal.common.dto.ApiResponse;
import com.company.legal.common.dto.PageResponse;
import com.company.legal.common.security.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * REST Controller for Invoice management.
 */
@RestController
@RequestMapping("/v1/invoices")
@RequiredArgsConstructor
@Tag(name = "Invoices", description = "Invoice and billing APIs")
public class InvoiceController {

    private final InvoiceService invoiceService;

    @PostMapping
    @Operation(summary = "Create a draft invoice")
    public ResponseEntity<ApiResponse<InvoiceDTO>> createInvoice(@Valid @RequestBody InvoiceDTO dto) {
        InvoiceDTO created = invoiceService.createInvoice(dto, getLawFirmId());
        return ResponseEntity.ok(ApiResponse.success(created, "Draft invoice created successfully"));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing invoice")
    public ResponseEntity<ApiResponse<InvoiceDTO>> updateInvoice(
            @PathVariable String id,
            @Valid @RequestBody InvoiceDTO dto) {
        InvoiceDTO updated = invoiceService.updateInvoice(id, dto);
        return ResponseEntity.ok(ApiResponse.success(updated, "Invoice updated successfully"));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get invoice by ID")
    public ResponseEntity<ApiResponse<InvoiceDTO>> getInvoice(@PathVariable String id) {
        InvoiceDTO invoice = invoiceService.getInvoiceById(id);
        return ResponseEntity.ok(ApiResponse.success(invoice));
    }

    @GetMapping
    @Operation(summary = "Get invoices with pagination and filters")
    public ResponseEntity<ApiResponse<PageResponse<InvoiceDTO>>> getInvoices(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String caseId) {
        PageResponse<InvoiceDTO> invoices = invoiceService.getInvoices(getLawFirmId(), page, size, status, caseId);
        return ResponseEntity.ok(ApiResponse.success(invoices));
    }

    @GetMapping("/case/{caseId}")
    @Operation(summary = "Get all invoices for a case")
    public ResponseEntity<ApiResponse<List<InvoiceDTO>>> getInvoicesByCase(@PathVariable String caseId) {
        List<InvoiceDTO> invoices = invoiceService.getInvoicesByCase(caseId);
        return ResponseEntity.ok(ApiResponse.success(invoices));
    }

    @GetMapping("/client/{clientId}")
    @Operation(summary = "Get all invoices for a client")
    public ResponseEntity<ApiResponse<List<InvoiceDTO>>> getInvoicesByClient(@PathVariable String clientId) {
        List<InvoiceDTO> invoices = invoiceService.getInvoicesByClient(clientId);
        return ResponseEntity.ok(ApiResponse.success(invoices));
    }

    @PatchMapping("/{id}/issue")
    @Operation(summary = "Issue an invoice (DRAFT -> ISSUED)")
    public ResponseEntity<ApiResponse<InvoiceDTO>> issueInvoice(@PathVariable String id) {
        InvoiceDTO invoice = invoiceService.issueInvoice(id);
        return ResponseEntity.ok(ApiResponse.success(invoice, "Invoice issued successfully"));
    }

    @PatchMapping("/{id}/payment")
    @Operation(summary = "Record a payment against an invoice")
    public ResponseEntity<ApiResponse<InvoiceDTO>> recordPayment(
            @PathVariable String id,
            @RequestParam BigDecimal amount) {
        InvoiceDTO invoice = invoiceService.recordPayment(id, amount);
        return ResponseEntity.ok(ApiResponse.success(invoice, "Payment recorded successfully"));
    }

    @PatchMapping("/{id}/cancel")
    @Operation(summary = "Cancel an invoice")
    public ResponseEntity<ApiResponse<InvoiceDTO>> cancelInvoice(@PathVariable String id) {
        InvoiceDTO invoice = invoiceService.cancelInvoice(id);
        return ResponseEntity.ok(ApiResponse.success(invoice, "Invoice cancelled"));
    }

    @GetMapping("/overdue")
    @Operation(summary = "Get overdue invoices")
    public ResponseEntity<ApiResponse<List<InvoiceDTO>>> getOverdueInvoices() {
        List<InvoiceDTO> invoices = invoiceService.getOverdueInvoices();
        return ResponseEntity.ok(ApiResponse.success(invoices));
    }

    @GetMapping("/outstanding")
    @Operation(summary = "Get total outstanding amount for the firm")
    public ResponseEntity<ApiResponse<BigDecimal>> getTotalOutstanding() {
        BigDecimal outstanding = invoiceService.getTotalOutstanding(getLawFirmId());
        return ResponseEntity.ok(ApiResponse.success(outstanding));
    }

    @GetMapping("/revenue")
    @Operation(summary = "Get revenue in a date range")
    public ResponseEntity<ApiResponse<BigDecimal>> getRevenue(
            @RequestParam String start,
            @RequestParam String end) {
        BigDecimal revenue = invoiceService.getRevenueInRange(getLawFirmId(),
                java.time.LocalDate.parse(start), java.time.LocalDate.parse(end));
        return ResponseEntity.ok(ApiResponse.success(revenue));
    }

    private String getLawFirmId() {
        return SecurityUtils.getLawFirmId();
    }
}