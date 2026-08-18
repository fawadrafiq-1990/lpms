package com.company.legal.billing.controller;

import com.company.legal.billing.dto.InvoiceDTO;
import com.company.legal.billing.dto.InvoiceItemDTO;
import com.company.legal.billing.entity.Invoice;
import com.company.legal.billing.repository.InvoiceRepository;
import com.company.legal.testutil.IntegrationTestBase;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class InvoiceControllerIntegrationTest extends IntegrationTestBase {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private InvoiceRepository invoiceRepository;

    private String lawFirmId;

    @BeforeEach
    void setUp() {
        lawFirmId = "firm-invoice-integration";
        invoiceRepository.deleteAll();
    }

    @AfterEach
    void tearDown() {
        invoiceRepository.deleteAll();
    }

    @Test
    @Order(1)
    @DisplayName("Create invoice via API - should return 201 with auto-generated number")
    void createInvoice_viaApi() throws Exception {
        InvoiceDTO dto = InvoiceDTO.builder()
                .caseId("case-1")
                .caseNumber("CASE-001")
                .clientId("client-1")
                .clientName("Test Client")
                .invoiceDate(LocalDate.now())
                .dueDate(LocalDate.now().plusDays(30))
                .currency("USD")
                .items(List.of(InvoiceItemDTO.builder()
                        .itemNumber(1)
                        .description("Legal Consultation")
                        .quantity(2)
                        .unit("hours")
                        .rate(new BigDecimal("250.00"))
                        .amount(new BigDecimal("500.00"))
                        .total(new BigDecimal("500.00"))
                        .build()))
                .build();

        mockMvc.perform(post("/v1/invoices")
                        .header("X-Law-Firm-Id", lawFirmId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.invoiceNumber").exists())
                .andExpect(jsonPath("$.data.status").value("DRAFT"))
                .andExpect(jsonPath("$.data.total").value(500.00));
    }

    @Test
    @Order(2)
    @DisplayName("Get invoice - should return 404 for non-existent")
    void getInvoice_notFound() throws Exception {
        mockMvc.perform(get("/v1/invoices/non-existent"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @Order(3)
    @DisplayName("List invoices - should return empty initially")
    void listInvoices_empty() throws Exception {
        mockMvc.perform(get("/v1/invoices")
                        .header("X-Law-Firm-Id", lawFirmId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.totalElements").value(0));
    }

    @Test
    @Order(4)
    @DisplayName("Full invoice lifecycle - create, issue, pay, verify paid")
    void fullInvoiceLifecycle() throws Exception {
        // Create invoice
        InvoiceDTO dto = InvoiceDTO.builder()
                .caseId("case-1")
                .clientId("client-1")
                .clientName("Lifecycle Client")
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

        String response = mockMvc.perform(post("/v1/invoices")
                        .header("X-Law-Firm-Id", lawFirmId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String id = objectMapper.readTree(response).path("data").path("id").asText();

        // Issue the invoice
        mockMvc.perform(put("/v1/invoices/" + id + "/issue"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("ISSUED"));

        // Record partial payment
        mockMvc.perform(post("/v1/invoices/" + id + "/pay")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\": 200.00}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("PARTIALLY_PAID"))
                .andExpect(jsonPath("$.data.amountPaid").value(200.00));

        // Record remaining payment
        mockMvc.perform(post("/v1/invoices/" + id + "/pay")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\": 300.00}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("PAID"))
                .andExpect(jsonPath("$.data.amountOutstanding").value(0));
    }

    @Test
    @Order(5)
    @DisplayName("Cancel invoice - should work for DRAFT status")
    void cancelInvoice_draft() throws Exception {
        InvoiceDTO dto = InvoiceDTO.builder()
                .caseId("case-1")
                .clientId("client-1")
                .clientName("Cancel Client")
                .invoiceDate(LocalDate.now())
                .dueDate(LocalDate.now().plusDays(30))
                .build();

        String response = mockMvc.perform(post("/v1/invoices")
                        .header("X-Law-Firm-Id", lawFirmId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String id = objectMapper.readTree(response).path("data").path("id").asText();

        mockMvc.perform(put("/v1/invoices/" + id + "/cancel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("CANCELLED"));
    }

    @Test
    @Order(6)
    @DisplayName("Record payment - should reject negative amount")
    void recordPayment_negativeAmount() throws Exception {
        InvoiceDTO dto = InvoiceDTO.builder()
                .caseId("case-1")
                .clientId("client-1")
                .clientName("Pay Client")
                .invoiceDate(LocalDate.now())
                .dueDate(LocalDate.now().plusDays(30))
                .items(List.of(InvoiceItemDTO.builder()
                        .itemNumber(1)
                        .description("Service")
                        .quantity(1)
                        .rate(new BigDecimal("100.00"))
                        .amount(new BigDecimal("100.00"))
                        .total(new BigDecimal("100.00"))
                        .build()))
                .build();

        String response = mockMvc.perform(post("/v1/invoices")
                        .header("X-Law-Firm-Id", lawFirmId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String id = objectMapper.readTree(response).path("data").path("id").asText();

        // First issue it
        mockMvc.perform(put("/v1/invoices/" + id + "/issue"))
                .andExpect(status().isOk());

        // Try negative payment
        mockMvc.perform(post("/v1/invoices/" + id + "/pay")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\": -50.00}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(7)
    @DisplayName("Get invoices by case - should return invoices")
    void getInvoicesByCase() throws Exception {
        InvoiceDTO dto = InvoiceDTO.builder()
                .caseId("case-bylaw")
                .clientId("client-1")
                .clientName("ByCase Client")
                .invoiceDate(LocalDate.now())
                .dueDate(LocalDate.now().plusDays(30))
                .build();

        mockMvc.perform(post("/v1/invoices")
                        .header("X-Law-Firm-Id", lawFirmId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/v1/invoices/case/case-bylaw"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(1));
    }

    @Test
    @Order(8)
    @DisplayName("Get outstanding total - should return sum")
    void getOutstandingTotal() throws Exception {
        // Create two invoices
        for (int i = 0; i < 2; i++) {
            InvoiceDTO dto = InvoiceDTO.builder()
                    .caseId("case-" + i)
                    .clientId("client-" + i)
                    .clientName("Outstanding Client " + i)
                    .invoiceDate(LocalDate.now())
                    .dueDate(LocalDate.now().plusDays(30))
                    .items(List.of(InvoiceItemDTO.builder()
                            .itemNumber(1)
                            .description("Service")
                            .quantity(1)
                            .rate(new BigDecimal("100.00"))
                            .amount(new BigDecimal("100.00"))
                            .total(new BigDecimal("100.00"))
                            .build()))
                    .build();

            String response = mockMvc.perform(post("/v1/invoices")
                            .header("X-Law-Firm-Id", lawFirmId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andReturn().getResponse().getContentAsString();

            String id = objectMapper.readTree(response).path("data").path("id").asText();

            // Issue them
            mockMvc.perform(put("/v1/invoices/" + id + "/issue"))
                    .andExpect(status().isOk());
        }

        mockMvc.perform(get("/v1/invoices/outstanding-total")
                        .header("X-Law-Firm-Id", lawFirmId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(200.00));
    }
}
