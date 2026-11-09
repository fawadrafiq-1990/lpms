package com.company.legal.billing.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.company.legal.billing.dto.InvoiceDTO;
import com.company.legal.billing.dto.InvoiceItemDTO;
import com.company.legal.common.dto.PageResponse;
import com.company.legal.common.exception.GlobalExceptionHandler;
import com.company.legal.common.exception.ResourceNotFoundException;
import com.company.legal.billing.service.InvoiceService;
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

@WebMvcTest(InvoiceController.class)
@Import(GlobalExceptionHandler.class)
class InvoiceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private InvoiceService invoiceService;

    private String lawFirmId;
    private InvoiceDTO invoiceDTO;
    private InvoiceDTO savedInvoiceDTO;

    @BeforeEach
    void setUp() {
        lawFirmId = "firm-1";
        invoiceDTO = InvoiceDTO.builder()
                .caseId("case-1")
                .caseNumber("CASE-001")
                .clientId("client-1")
                .clientName("Test Client")
                .invoiceDate(LocalDate.now())
                .dueDate(LocalDate.now().plusDays(30))
                .currency("USD")
                .items(List.of(InvoiceItemDTO.builder()
                        .itemNumber(1)
                        .description("Consultation")
                        .quantity(2)
                        .unit("hours")
                        .rate(new BigDecimal("250.00"))
                        .amount(new BigDecimal("500.00"))
                        .total(new BigDecimal("500.00"))
                        .build()))
                .build();

        savedInvoiceDTO = InvoiceDTO.builder()
                .id("inv-1")
                .invoiceNumber("INV-001")
                .caseId("case-1")
                .clientId("client-1")
                .clientName("Test Client")
                .subtotal(new BigDecimal("500.00"))
                .taxAmount(BigDecimal.ZERO)
                .discountAmount(BigDecimal.ZERO)
                .total(new BigDecimal("500.00"))
                .amountPaid(BigDecimal.ZERO)
                .amountOutstanding(new BigDecimal("500.00"))
                .status("DRAFT")
                .currency("USD")
                .build();
    }

    @Test
    @DisplayName("Create Invoice - Should return 201 on success")
    void createInvoice_success() throws Exception {
        when(invoiceService.createInvoice(any(InvoiceDTO.class), eq(lawFirmId))).thenReturn(savedInvoiceDTO);

        mockMvc.perform(post("/v1/invoices")
                        .header("X-Law-Firm-Id", lawFirmId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invoiceDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").value("inv-1"))
                .andExpect(jsonPath("$.data.invoiceNumber").value("INV-001"))
                .andExpect(jsonPath("$.data.status").value("DRAFT"));
    }

    @Test
    @DisplayName("Get Invoice - Should return invoice by ID")
    void getInvoice_success() throws Exception {
        when(invoiceService.getInvoiceById("inv-1")).thenReturn(savedInvoiceDTO);

        mockMvc.perform(get("/v1/invoices/inv-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.invoiceNumber").value("INV-001"))
                .andExpect(jsonPath("$.data.total").value(500.00));
    }

    @Test
    @DisplayName("Get Invoice - Should return 404 when not found")
    void getInvoice_notFound() throws Exception {
        when(invoiceService.getInvoiceById("missing"))
                .thenThrow(new ResourceNotFoundException("Invoice", "id", "missing"));

        mockMvc.perform(get("/v1/invoices/missing"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("List Invoices - Should return paginated invoices")
    void getInvoices_success() throws Exception {
        PageResponse<InvoiceDTO> pageResponse = PageResponse.<InvoiceDTO>builder()
                .content(List.of(savedInvoiceDTO))
                .page(0)
                .size(20)
                .totalElements(1)
                .totalPages(1)
                .first(true)
                .last(true)
                .empty(false)
                .build();

        when(invoiceService.getInvoices(eq(lawFirmId), eq(0), eq(20), isNull(), isNull()))
                .thenReturn(pageResponse);

        mockMvc.perform(get("/v1/invoices")
                        .param("page", "0")
                        .param("size", "20")
                        .header("X-Law-Firm-Id", lawFirmId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.totalElements").value(1));
    }

    @Test
    @DisplayName("List Invoices - Should filter by status")
    void getInvoices_filterByStatus() throws Exception {
        PageResponse<InvoiceDTO> pageResponse = PageResponse.<InvoiceDTO>builder()
                .content(List.of(savedInvoiceDTO))
                .page(0)
                .size(20)
                .totalElements(1)
                .totalPages(1)
                .first(true)
                .last(true)
                .empty(false)
                .build();

        when(invoiceService.getInvoices(eq(lawFirmId), eq(0), eq(20), eq("DRAFT"), isNull()))
                .thenReturn(pageResponse);

        mockMvc.perform(get("/v1/invoices")
                        .param("status", "DRAFT")
                        .header("X-Law-Firm-Id", lawFirmId))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Update Invoice - Should return updated invoice")
    void updateInvoice_success() throws Exception {
        InvoiceDTO updateDTO = InvoiceDTO.builder()
                .items(List.of(InvoiceItemDTO.builder()
                        .itemNumber(1)
                        .description("Updated")
                        .quantity(3)
                        .unit("hours")
                        .rate(new BigDecimal("300.00"))
                        .amount(new BigDecimal("900.00"))
                        .total(new BigDecimal("900.00"))
                        .build()))
                .build();

        InvoiceDTO updated = savedInvoiceDTO;
        updated.setTotal(new BigDecimal("900.00"));
        when(invoiceService.updateInvoice("inv-1", updateDTO)).thenReturn(updated);

        mockMvc.perform(put("/v1/invoices/inv-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(900.00));
    }

    @Test
    @DisplayName("Issue Invoice - Should transition from DRAFT to ISSUED")
    void issueInvoice_success() throws Exception {
        InvoiceDTO issuedInvoice = InvoiceDTO.builder()
                .id("inv-1")
                .invoiceNumber("INV-001")
                .status("ISSUED")
                .build();

        when(invoiceService.issueInvoice("inv-1")).thenReturn(issuedInvoice);

        mockMvc.perform(put("/v1/invoices/inv-1/issue"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("ISSUED"));
    }

    @Test
    @DisplayName("Record Payment - Should record payment amount")
    void recordPayment_success() throws Exception {
        InvoiceDTO paidInvoice = InvoiceDTO.builder()
                .id("inv-1")
                .invoiceNumber("INV-001")
                .total(new BigDecimal("500.00"))
                .amountPaid(new BigDecimal("200.00"))
                .amountOutstanding(new BigDecimal("300.00"))
                .status("PARTIALLY_PAID")
                .build();

        when(invoiceService.recordPayment(eq("inv-1"), eq(new BigDecimal("200.00")))).thenReturn(paidInvoice);

        mockMvc.perform(post("/v1/invoices/inv-1/pay")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\":200.00}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.amountPaid").value(200.00))
                .andExpect(jsonPath("$.data.status").value("PARTIALLY_PAID"));
    }

    @Test
    @DisplayName("Cancel Invoice - Should cancel invoice")
    void cancelInvoice_success() throws Exception {
        InvoiceDTO cancelled = InvoiceDTO.builder()
                .id("inv-1")
                .status("CANCELLED")
                .build();

        when(invoiceService.cancelInvoice("inv-1")).thenReturn(cancelled);

        mockMvc.perform(put("/v1/invoices/inv-1/cancel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("CANCELLED"));
    }

    @Test
    @DisplayName("Get Invoices By Case - Should return invoices for a case")
    void getInvoicesByCase_success() throws Exception {
        when(invoiceService.getInvoicesByCase("case-1"))
                .thenReturn(List.of(savedInvoiceDTO));

        mockMvc.perform(get("/v1/invoices/case/case-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @DisplayName("Get Invoices By Client - Should return invoices for a client")
    void getInvoicesByClient_success() throws Exception {
        when(invoiceService.getInvoicesByClient("client-1"))
                .thenReturn(List.of(savedInvoiceDTO));

        mockMvc.perform(get("/v1/invoices/client/client-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @DisplayName("Get Overdue Invoices - Should return overdue invoices")
    void getOverdueInvoices_success() throws Exception {
        when(invoiceService.getOverdueInvoices())
                .thenReturn(List.of(savedInvoiceDTO));

        mockMvc.perform(get("/v1/invoices/overdue"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @DisplayName("Get Outstanding Total - Should return total outstanding")
    void getOutstandingTotal_success() throws Exception {
        when(invoiceService.getTotalOutstanding(lawFirmId))
                .thenReturn(new BigDecimal("15000.00"));

        mockMvc.perform(get("/v1/invoices/outstanding-total")
                        .header("X-Law-Firm-Id", lawFirmId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(15000.00));
    }

    @Test
    @DisplayName("Get Revenue - Should return revenue in date range")
    void getRevenue_success() throws Exception {
        when(invoiceService.getRevenueInRange(eq(lawFirmId), any(), any()))
                .thenReturn(new BigDecimal("50000.00"));

        mockMvc.perform(get("/v1/invoices/revenue")
                        .param("startDate", "2024-01-01")
                        .param("endDate", "2024-12-31")
                        .header("X-Law-Firm-Id", lawFirmId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(50000.00));
    }

    @Test
    @DisplayName("Update Invoice - Should return 404 when not found")
    void updateInvoice_notFound() throws Exception {
        doThrow(new ResourceNotFoundException("Invoice", "id", "missing"))
                .when(invoiceService).updateInvoice("missing", any());

        mockMvc.perform(put("/v1/invoices/missing")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invoiceDTO)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Issue Invoice - Should return 400 when not DRAFT")
    void issueInvoice_notDraft() throws Exception {
        doThrow(new IllegalStateException("Cannot issue a non-DRAFT invoice"))
                .when(invoiceService).issueInvoice("paid-inv");

        mockMvc.perform(put("/v1/invoices/paid-inv/issue"))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Record Payment - Should return 400 for invalid amount")
    void recordPayment_invalidAmount() throws Exception {
        doThrow(new IllegalArgumentException("Payment amount must be positive"))
                .when(invoiceService).recordPayment(eq("inv-1"), any());

        mockMvc.perform(post("/v1/invoices/inv-1/pay")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\":-100.00}"))
                .andExpect(status().isBadRequest());
    }
}
