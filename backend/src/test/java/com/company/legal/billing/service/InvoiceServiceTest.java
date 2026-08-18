package com.company.legal.billing.service;

import com.company.legal.billing.dto.InvoiceDTO;
import com.company.legal.billing.dto.InvoiceItemDTO;
import com.company.legal.billing.entity.Invoice;
import com.company.legal.billing.entity.InvoiceItem;
import com.company.legal.billing.repository.InvoiceRepository;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InvoiceServiceTest {

    @Mock
    private InvoiceRepository invoiceRepository;

    @Mock
    private ClientRepository clientRepository;

    @InjectMocks
    private InvoiceService invoiceService;

    private String lawFirmId;
    private InvoiceItemDTO invoiceItemDTO;
    private InvoiceDTO invoiceDTO;

    @BeforeEach
    void setUp() {
        lawFirmId = "firm-1234";
        invoiceItemDTO = InvoiceItemDTO.builder()
                .itemNumber(1)
                .description("Legal Consultation")
                .quantity(2)
                .unit("hours")
                .rate(new BigDecimal("250.00"))
                .amount(new BigDecimal("500.00"))
                .taxRate(new BigDecimal("0"))
                .taxAmount(BigDecimal.ZERO)
                .discountAmount(BigDecimal.ZERO)
                .total(new BigDecimal("500.00"))
                .sortOrder(1)
                .build();

        invoiceDTO = InvoiceDTO.builder()
                .caseId("case-1")
                .caseNumber("CASE-001")
                .clientId("client-1")
                .clientName("Test Client")
                .invoiceDate(LocalDate.now())
                .dueDate(LocalDate.now().plusDays(30))
                .currency("USD")
                .billingModel("HOURLY")
                .items(List.of(invoiceItemDTO))
                .build();
    }

    // ========== CREATE INVOICE TESTS ==========

    @Nested
    @DisplayName("Create Invoice Tests")
    class CreateInvoiceTests {

        @Test
        @DisplayName("Should create a draft invoice with auto-generated invoice number")
        void createInvoice_success() {
            // Arrange
            when(invoiceRepository.count()).thenReturn(5L);
            when(invoiceRepository.save(any(Invoice.class))).thenAnswer(invocation -> {
                Invoice inv = invocation.getArgument(0);
                inv.setId("inv-1");
                return inv;
            });

            // Act
            InvoiceDTO result = invoiceService.createInvoice(invoiceDTO, lawFirmId);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo("inv-1");
            assertThat(result.getInvoiceNumber()).isEqualTo("INV-fr-00006");
            assertThat(result.getStatus()).isEqualTo("DRAFT");
            assertThat(result.getLawFirmId()).isEqualTo(lawFirmId);
            assertThat(result.getCurrency()).isEqualTo("USD");

            ArgumentCaptor<Invoice> captor = ArgumentCaptor.forClass(Invoice.class);
            verify(invoiceRepository).save(captor.capture());
            Invoice saved = captor.getValue();
            assertThat(saved.getItems()).hasSize(1);
            assertThat(saved.getItems().get(0).getDescription()).isEqualTo("Legal Consultation");
            assertThat(saved.getTotal()).isEqualTo(new BigDecimal("500.00"));
        }

        @Test
        @DisplayName("Should create invoice with default currency PKR")
        void createInvoice_defaultCurrency() {
            // Arrange
            InvoiceDTO dtoWithoutCurrency = InvoiceDTO.builder()
                    .caseId("case-1")
                    .clientId("client-1")
                    .invoiceDate(LocalDate.now())
                    .dueDate(LocalDate.now().plusDays(30))
                    .build();

            when(invoiceRepository.count()).thenReturn(0L);
            when(invoiceRepository.save(any(Invoice.class))).thenAnswer(invocation -> {
                Invoice inv = invocation.getArgument(0);
                inv.setId("inv-default");
                return inv;
            });

            // Act
            InvoiceDTO result = invoiceService.createInvoice(dtoWithoutCurrency, lawFirmId);

            // Assert
            assertThat(result.getCurrency()).isEqualTo("PKR");
        }

        @Test
        @DisplayName("Should set invoice status to DRAFT on creation")
        void createInvoice_statusDraft() {
            // Arrange
            when(invoiceRepository.count()).thenReturn(0L);
            when(invoiceRepository.save(any(Invoice.class))).thenAnswer(invocation -> {
                Invoice inv = invocation.getArgument(0);
                inv.setId("inv-1");
                return inv;
            });

            // Act
            InvoiceDTO result = invoiceService.createInvoice(invoiceDTO, lawFirmId);

            // Assert
            assertThat(result.getStatus()).isEqualTo("DRAFT");
        }

        @Test
        @DisplayName("Should create invoice without items")
        void createInvoice_noItems() {
            // Arrange
            InvoiceDTO emptyItemsDTO = InvoiceDTO.builder()
                    .caseId("case-1")
                    .clientId("client-1")
                    .invoiceDate(LocalDate.now())
                    .dueDate(LocalDate.now().plusDays(30))
                    .build();

            when(invoiceRepository.count()).thenReturn(0L);
            when(invoiceRepository.save(any(Invoice.class))).thenAnswer(invocation -> {
                Invoice inv = invocation.getArgument(0);
                inv.setId("inv-no-items");
                return inv;
            });

            // Act
            InvoiceDTO result = invoiceService.createInvoice(emptyItemsDTO, lawFirmId);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getItems()).isEmpty();
        }
    }

    // ========== UPDATE INVOICE TESTS ==========

    @Nested
    @DisplayName("Update Invoice Tests")
    class UpdateInvoiceTests {

        @Test
        @DisplayName("Should update a draft invoice successfully")
        void updateInvoice_draftSuccess() {
            // Arrange
            String id = "inv-1";
            Invoice draftInvoice = createDraftInvoiceWithItems();

            InvoiceDTO updateDTO = InvoiceDTO.builder()
                    .caseId("case-updated")
                    .caseNumber("CASE-UPD")
                    .clientId("client-updated")
                    .clientName("Updated Client")
                    .invoiceDate(LocalDate.now().plusDays(1))
                    .dueDate(LocalDate.now().plusDays(45))
                    .notes("Updated notes")
                    .termsAndConditions("Updated terms")
                    .items(List.of(InvoiceItemDTO.builder()
                            .itemNumber(1)
                            .description("Updated consultation")
                            .quantity(3)
                            .unit("hours")
                            .rate(new BigDecimal("300.00"))
                            .amount(new BigDecimal("900.00"))
                            .total(new BigDecimal("900.00"))
                            .build()))
                    .build();

            when(invoiceRepository.findById(id)).thenReturn(Optional.of(draftInvoice));
            when(invoiceRepository.save(any(Invoice.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            InvoiceDTO result = invoiceService.updateInvoice(id, updateDTO);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getCaseId()).isEqualTo("case-updated");
            assertThat(result.getClientName()).isEqualTo("Updated Client");
            assertThat(result.getNotes()).isEqualTo("Updated notes");
            assertThat(result.getTermsAndConditions()).isEqualTo("Updated terms");
        }

        @Test
        @DisplayName("Should throw exception when updating issued invoice")
        void updateInvoice_issuedFail() {
            // Arrange
            String id = "inv-issued";
            Invoice issuedInvoice = createIssuedInvoice();

            when(invoiceRepository.findById(id)).thenReturn(Optional.of(issuedInvoice));

            // Act & Assert
            assertThatThrownBy(() -> invoiceService.updateInvoice(id, invoiceDTO))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Cannot update invoice");

            verify(invoiceRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw exception when updating paid invoice")
        void updateInvoice_paidFail() {
            // Arrange
            String id = "inv-paid";
            Invoice paidInvoice = createPaidInvoice();

            when(invoiceRepository.findById(id)).thenReturn(Optional.of(paidInvoice));

            // Act & Assert
            assertThatThrownBy(() -> invoiceService.updateInvoice(id, invoiceDTO))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Cannot update invoice");
        }

        @Test
        @DisplayName("Should throw exception when invoice not found")
        void updateInvoice_notFound() {
            // Arrange
            when(invoiceRepository.findById("missing-id")).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> invoiceService.updateInvoice("missing-id", invoiceDTO))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Invoice");

            verify(invoiceRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should recalculate totals when updating items")
        void updateInvoice_recalculateTotals() {
            // Arrange
            String id = "inv-1";
            Invoice invoice = createDraftInvoiceWithItems();

            InvoiceDTO updateDTO = InvoiceDTO.builder()
                    .items(List.of(InvoiceItemDTO.builder()
                            .itemNumber(1)
                            .description("New item")
                            .quantity(5)
                            .unit("hours")
                            .rate(new BigDecimal("200.00"))
                            .amount(new BigDecimal("1000.00"))
                            .taxRate(new BigDecimal("10"))
                            .taxAmount(new BigDecimal("100.00"))
                            .discountAmount(new BigDecimal("50.00"))
                            .total(new BigDecimal("1050.00"))
                            .build()))
                    .build();

            when(invoiceRepository.findById(id)).thenReturn(Optional.of(invoice));
            when(invoiceRepository.save(any(Invoice.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            InvoiceDTO result = invoiceService.updateInvoice(id, updateDTO);

            // Assert
            assertThat(result.getTotal()).isEqualTo(new BigDecimal("1050.00"));
            assertThat(result.getSubtotal()).isEqualTo(new BigDecimal("1000.00"));
            assertThat(result.getTaxAmount()).isEqualTo(new BigDecimal("100.00"));
            assertThat(result.getDiscountAmount()).isEqualTo(new BigDecimal("50.00"));
        }
    }

    // ========== GET INVOICE TESTS ==========

    @Nested
    @DisplayName("Get Invoice Tests")
    class GetInvoiceTests {

        @Test
        @DisplayName("Should get invoice by ID")
        void getInvoiceById_success() {
            // Arrange
            Invoice invoice = createDraftInvoiceWithItems();
            when(invoiceRepository.findById("inv-1")).thenReturn(Optional.of(invoice));

            // Act
            InvoiceDTO result = invoiceService.getInvoiceById("inv-1");

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getInvoiceNumber()).isEqualTo("INV-001");
            assertThat(result.getStatus()).isEqualTo("DRAFT");
            assertThat(result.getItems()).hasSize(1);
        }

        @Test
        @DisplayName("Should throw exception when invoice not found")
        void getInvoiceById_notFound() {
            // Arrange
            when(invoiceRepository.findById("missing-id")).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> invoiceService.getInvoiceById("missing-id"))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    // ========== LIST INVOICES TESTS ==========

    @Nested
    @DisplayName("List Invoices Tests")
    class ListInvoicesTests {

        @Test
        @DisplayName("Should return paginated invoices for law firm")
        void getInvoices_pagination() {
            // Arrange
            List<Invoice> invoices = List.of(createDraftInvoiceWithItems());
            Page<Invoice> page = new PageImpl<>(invoices);
            when(invoiceRepository.findByLawFirmId(lawFirmId, any(Pageable.class)))
                    .thenReturn(page);

            // Act
            PageResponse<InvoiceDTO> result = invoiceService.getInvoices(lawFirmId, 0, 20, null, null);

            // Assert
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getTotalElements()).isEqualTo(1);
        }

        @Test
        @DisplayName("Should filter invoices by status")
        void getInvoices_filterByStatus() {
            // Arrange
            List<Invoice> issuedInvoices = List.of(createIssuedInvoice());
            Page<Invoice> page = new PageImpl<>(issuedInvoices);
            when(invoiceRepository.findByStatus(lawFirmId, "ISSUED", any(Pageable.class)))
                    .thenReturn(page);

            // Act
            PageResponse<InvoiceDTO> result = invoiceService.getInvoices(lawFirmId, 0, 20, "ISSUED", null);

            // Assert
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getStatus()).isEqualTo("ISSUED");
        }

        @Test
        @DisplayName("Should filter invoices by status and caseId")
        void getInvoices_filterByStatusAndCaseId() {
            // Arrange
            List<Invoice> invoices = List.of(createDraftInvoiceWithItems());
            Page<Invoice> page = new PageImpl<>(invoices);
            when(invoiceRepository.findByStatus(lawFirmId, "DRAFT", any(Pageable.class)))
                    .thenReturn(page);

            // Act
            PageResponse<InvoiceDTO> result = invoiceService.getInvoices(
                    lawFirmId, 0, 20, "DRAFT", "case-1");

            // Assert
            assertThat(result.getContent()).isNotEmpty();
        }

        @Test
        @DisplayName("Should return empty list when no invoices")
        void getInvoices_empty() {
            // Arrange
            when(invoiceRepository.findByLawFirmId(lawFirmId, any(Pageable.class)))
                    .thenReturn(Page.empty());

            // Act
            PageResponse<InvoiceDTO> result = invoiceService.getInvoices(lawFirmId, 0, 20, null, null);

            // Assert
            assertThat(result.getContent()).isEmpty();
            assertThat(result.getTotalElements()).isZero();
        }
    }

    // ========== ISSUE INVOICE TESTS ==========

    @Nested
    @DisplayName("Issue Invoice Tests")
    class IssueInvoiceTests {

        @Test
        @DisplayName("Should issue a draft invoice")
        void issueInvoice_draftToIssued() {
            // Arrange
            Invoice draftInvoice = createDraftInvoiceWithItems();
            when(invoiceRepository.findById("inv-1")).thenReturn(Optional.of(draftInvoice));
            when(invoiceRepository.save(any(Invoice.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            InvoiceDTO result = invoiceService.issueInvoice("inv-1");

            // Assert
            assertThat(result.getStatus()).isEqualTo("ISSUED");
            assertThat(result.getIssuedAt()).isNotNull();
            verify(invoiceRepository).save(any());
        }

        @Test
        @DisplayName("Should not change status if invoice is not DRAFT")
        void issueInvoice_alreadyIssued() {
            // Arrange
            Invoice issuedInvoice = createIssuedInvoice();
            when(invoiceRepository.findById("inv-issued")).thenReturn(Optional.of(issuedInvoice));

            // Act
            InvoiceDTO result = invoiceService.issueInvoice("inv-issued");

            // Assert
            assertThat(result.getStatus()).isEqualTo("ISSUED");
        }

        @Test
        @DisplayName("Should throw exception when issuing non-existent invoice")
        void issueInvoice_notFound() {
            // Arrange
            when(invoiceRepository.findById("missing-id")).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> invoiceService.issueInvoice("missing-id"))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    // ========== PAYMENT RECORDING TESTS ==========

    @Nested
    @DisplayName("Payment Recording Tests")
    class PaymentTests {

        @Test
        @DisplayName("Should record payment and transition to PAID")
        void recordPayment_fullPayment() {
            // Arrange
            Invoice invoice = createIssuedInvoice();
            invoice.setTotal(new BigDecimal("500.00"));
            invoice.setAmountPaid(BigDecimal.ZERO);
            invoice.calculateOutstanding();

            when(invoiceRepository.findById("inv-1")).thenReturn(Optional.of(invoice));
            when(invoiceRepository.save(any(Invoice.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            InvoiceDTO result = invoiceService.recordPayment("inv-1", new BigDecimal("500.00"));

            // Assert
            assertThat(result.getStatus()).isEqualTo("PAID");
            assertThat(result.getAmountPaid()).isEqualTo(new BigDecimal("500.00"));
            assertThat(result.getAmountOutstanding()).isZero();
            assertThat(result.getPaidAt()).isNotNull();
        }

        @Test
        @DisplayName("Should record partial payment and transition to PARTIALLY_PAID")
        void recordPayment_partialPayment() {
            // Arrange
            Invoice invoice = createIssuedInvoice();
            invoice.setTotal(new BigDecimal("1000.00"));
            invoice.setAmountPaid(BigDecimal.ZERO);
            invoice.calculateOutstanding();

            when(invoiceRepository.findById("inv-1")).thenReturn(Optional.of(invoice));
            when(invoiceRepository.save(any(Invoice.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            InvoiceDTO result = invoiceService.recordPayment("inv-1", new BigDecimal("300.00"));

            // Assert
            assertThat(result.getStatus()).isEqualTo("PARTIALLY_PAID");
            assertThat(result.getAmountPaid()).isEqualTo(new BigDecimal("300.00"));
            assertThat(result.getAmountOutstanding()).isEqualTo(new BigDecimal("700.00"));
        }

        @Test
        @DisplayName("Should throw exception when payment is zero or negative")
        void recordPayment_zeroPayment() {
            // Arrange
            Invoice invoice = createIssuedInvoice();
            when(invoiceRepository.findById("inv-1")).thenReturn(Optional.of(invoice));

            // Act & Assert
            assertThatThrownBy(() -> invoiceService.recordPayment("inv-1", BigDecimal.ZERO))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Payment amount must be positive");
        }

        @Test
        @DisplayName("Should throw exception when payment exceeds outstanding")
        void recordPayment_overPayment() {
            // Arrange
            Invoice invoice = createIssuedInvoice();
            invoice.setTotal(new BigDecimal("500.00"));
            invoice.setAmountPaid(new BigDecimal("200.00"));
            invoice.calculateOutstanding(); // outstanding = 300

            when(invoiceRepository.findById("inv-1")).thenReturn(Optional.of(invoice));

            // Act & Assert
            assertThatThrownBy(() -> invoiceService.recordPayment("inv-1", new BigDecimal("500.00")))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Payment exceeds outstanding");
        }

        @Test
        @DisplayName("Should throw exception when invoice not found")
        void recordPayment_notFound() {
            // Arrange
            when(invoiceRepository.findById("missing-id")).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> invoiceService.recordPayment("missing-id", new BigDecimal("100.00")))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    // ========== CANCEL INVOICE TESTS ==========

    @Nested
    @DisplayName("Cancel Invoice Tests")
    class CancelInvoiceTests {

        @Test
        @DisplayName("Should cancel a draft invoice")
        void cancelInvoice_draftSuccess() {
            // Arrange
            Invoice draftInvoice = createDraftInvoiceWithItems();
            when(invoiceRepository.findById("inv-1")).thenReturn(Optional.of(draftInvoice));
            when(invoiceRepository.save(any(Invoice.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            InvoiceDTO result = invoiceService.cancelInvoice("inv-1");

            // Assert
            assertThat(result.getStatus()).isEqualTo("CANCELLED");
        }

        @Test
        @DisplayName("Should cancel an issued invoice")
        void cancelInvoice_issuedSuccess() {
            // Arrange
            Invoice issuedInvoice = createIssuedInvoice();
            when(invoiceRepository.findById("inv-issued")).thenReturn(Optional.of(issuedInvoice));
            when(invoiceRepository.save(any(Invoice.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            InvoiceDTO result = invoiceService.cancelInvoice("inv-issued");

            // Assert
            assertThat(result.getStatus()).isEqualTo("CANCELLED");
        }

        @Test
        @DisplayName("Should throw exception when cancelling a paid invoice")
        void cancelInvoice_paidFail() {
            // Arrange
            Invoice paidInvoice = createPaidInvoice();
            when(invoiceRepository.findById("inv-paid")).thenReturn(Optional.of(paidInvoice));

            // Act & Assert
            assertThatThrownBy(() -> invoiceService.cancelInvoice("inv-paid"))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Cannot cancel a paid invoice");
        }

        @Test
        @DisplayName("Should throw exception when cancelling non-existent invoice")
        void cancelInvoice_notFound() {
            // Arrange
            when(invoiceRepository.findById("missing-id")).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> invoiceService.cancelInvoice("missing-id"))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    // ========== INVOICE QUERIES TESTS ==========

    @Nested
    @DisplayName("Invoice Query Tests")
    class InvoiceQueryTests {

        @Test
        @DisplayName("Should get invoices by case ID")
        void getInvoicesByCase_success() {
            // Arrange
            List<Invoice> invoices = List.of(createDraftInvoiceWithItems());
            when(invoiceRepository.findByCaseId("case-1")).thenReturn(invoices);

            // Act
            List<InvoiceDTO> result = invoiceService.getInvoicesByCase("case-1");

            // Assert
            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("Should get invoices by client ID")
        void getInvoicesByClient_success() {
            // Arrange
            List<Invoice> invoices = List.of(createDraftInvoiceWithItems());
            when(invoiceRepository.findByClientId("client-1")).thenReturn(invoices);

            // Act
            List<InvoiceDTO> result = invoiceService.getInvoicesByClient("client-1");

            // Assert
            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("Should get overdue invoices")
        void getOverdueInvoices_success() {
            // Arrange
            List<Invoice> overdueInvoices = List.of(createIssuedInvoice());
            when(invoiceRepository.findOverdueInvoices(any(LocalDate.class)))
                    .thenReturn(overdueInvoices);

            // Act
            List<InvoiceDTO> result = invoiceService.getOverdueInvoices();

            // Assert
            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("Should get total outstanding for law firm")
        void getTotalOutstanding_success() {
            // Arrange
            when(invoiceRepository.getTotalOutstanding(lawFirmId))
                    .thenReturn(new BigDecimal("15000.00"));

            // Act
            BigDecimal result = invoiceService.getTotalOutstanding(lawFirmId);

            // Assert
            assertThat(result).isEqualTo(new BigDecimal("15000.00"));
        }

        @Test
        @DisplayName("Should get revenue in date range")
        void getRevenueInRange_success() {
            // Arrange
            LocalDate start = LocalDate.now().minusMonths(3);
            LocalDate end = LocalDate.now();
            when(invoiceRepository.getRevenueInRange(eq(lawFirmId), eq(start), eq(end)))
                    .thenReturn(new BigDecimal("50000.00"));

            // Act
            BigDecimal result = invoiceService.getRevenueInRange(lawFirmId, start, end);

            // Assert
            assertThat(result).isEqualTo(new BigDecimal("50000.00"));
        }

        @Test
        @DisplayName("Should return empty lists for non-existent queries")
        void getInvoices_emptyResults() {
            // Arrange
            when(invoiceRepository.findByCaseId("no-case")).thenReturn(List.of());
            when(invoiceRepository.findByClientId("no-client")).thenReturn(List.of());
            when(invoiceRepository.findOverdueInvoices(any(LocalDate.class))).thenReturn(List.of());

            // Act
            assertThat(invoiceService.getInvoicesByCase("no-case")).isEmpty();
            assertThat(invoiceService.getInvoicesByClient("no-client")).isEmpty();
            assertThat(invoiceService.getOverdueInvoices()).isEmpty();
        }
    }

    // ========== HELPER METHODS ==========

    private Invoice createDraftInvoiceWithItems() {
        Invoice invoice = Invoice.builder()
                .id("inv-1")
                .invoiceNumber("INV-001")
                .lawFirmId(lawFirmId)
                .caseId("case-1")
                .clientId("client-1")
                .clientName("Test Client")
                .invoiceDate(LocalDate.now())
                .dueDate(LocalDate.now().plusDays(30))
                .currency("USD")
                .status("DRAFT")
                .billingModel("HOURLY")
                .items(new java.util.ArrayList<>())
                .build();

        InvoiceItem item = InvoiceItem.builder()
                .invoiceId(invoice.getId())
                .itemNumber(1)
                .description("Consultation")
                .quantity(2)
                .unit("hours")
                .rate(new BigDecimal("250.00"))
                .amount(new BigDecimal("500.00"))
                .total(new BigDecimal("500.00"))
                .build();
        invoice.getItems().add(item);
        invoice.calculateTotals(invoice.getItems());
        return invoice;
    }

    private Invoice createIssuedInvoice() {
        Invoice invoice = createDraftInvoiceWithItems();
        invoice.setStatus("ISSUED");
        invoice.setIssuedAt(java.time.LocalDateTime.now());
        return invoice;
    }

    private Invoice createPaidInvoice() {
        Invoice invoice = createIssuedInvoice();
        invoice.setTotal(new BigDecimal("500.00"));
        invoice.setAmountPaid(new BigDecimal("500.00"));
        invoice.setStatus("PAID");
        invoice.setPaidAt(java.time.LocalDateTime.now());
        return invoice;
    }
}
