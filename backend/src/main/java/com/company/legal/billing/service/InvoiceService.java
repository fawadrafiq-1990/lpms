package com.company.legal.billing.service;

import com.company.legal.billing.dto.InvoiceDTO;
import com.company.legal.billing.dto.InvoiceItemDTO;
import com.company.legal.billing.entity.Invoice;
import com.company.legal.billing.entity.InvoiceItem;
import com.company.legal.billing.repository.InvoiceRepository;
import com.company.legal.client.repository.ClientRepository;
import com.company.legal.common.dto.PageResponse;
import com.company.legal.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for invoice management.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final ClientRepository clientRepository;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    /**
     * Create a draft invoice with line items.
     */
    @Transactional
    public InvoiceDTO createInvoice(InvoiceDTO dto, String lawFirmId) {
        log.info("Creating draft invoice for case: {}", dto.getCaseId());
        String invoiceNumber = generateInvoiceNumber(lawFirmId);
        Invoice invoice = mapToEntity(dto, lawFirmId, invoiceNumber);
        invoice.setStatus("DRAFT");
        invoice.setIssuedAt(LocalDateTime.now());
        invoice = invoiceRepository.save(invoice);
        return mapToDTO(invoice);
    }

    /**
     * Update an existing invoice.
     */
    @Transactional
    public InvoiceDTO updateInvoice(String id, InvoiceDTO dto) {
        log.info("Updating invoice: {}", id);
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice", "id", id));
        if (isFinalInvoice(invoice.getStatus())) {
            throw new IllegalStateException("Cannot update invoice with status: " + invoice.getStatus());
        }
        invoice.setCaseId(dto.getCaseId());
        invoice.setCaseNumber(dto.getCaseNumber());
        invoice.setClientId(dto.getClientId());
        invoice.setClientName(dto.getClientName());
        invoice.setInvoiceDate(dto.getInvoiceDate());
        invoice.setDueDate(dto.getDueDate());
        invoice.setBillingPeriodStart(dto.getBillingPeriodStart());
        invoice.setBillingPeriodEnd(dto.getBillingPeriodEnd());
        invoice.setCurrency(dto.getCurrency() != null ? dto.getCurrency() : "PKR");
        invoice.setNotes(dto.getNotes());
        invoice.setTermsAndConditions(dto.getTermsAndConditions());
        invoice.setPaymentInstructions(dto.getPaymentInstructions());
        invoice.setBillingModel(dto.getBillingModel());
        if (dto.getItems() != null && !dto.getItems().isEmpty()) {
            updateInvoiceItems(invoice, dto.getItems());
            invoice.calculateTotals(invoice.getItems());
        }
        invoice = invoiceRepository.save(invoice);
        return mapToDTO(invoice);
    }

    /**
     * Get invoice by ID.
     */
    @Transactional(readOnly = true)
    public InvoiceDTO getInvoiceById(String id) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice", "id", id));
        return mapToDTO(invoice);
    }

    /**
     * Get paginated invoices with filters.
     */
    @Transactional(readOnly = true)
    public PageResponse<InvoiceDTO> getInvoices(String lawFirmId, Integer page, Integer size,
                                                 String status, String caseId) {
        log.info("Fetching invoices for firm: {}", lawFirmId);
        Pageable pageable = PageRequest.of(page != null ? page : 0,
                                           size != null ? size : 20,
                                           Sort.by("createdAt").descending());
        List<InvoiceDTO> result;
        if (status != null && !status.isEmpty()) {
            result = invoiceRepository.findByStatus(lawFirmId, status, pageable)
                    .map(this::mapToDTO).getContent();
        } else {
            List<Invoice> all = invoiceRepository.findByLawFirmId(lawFirmId, pageable).getContent();
            if (caseId != null && !caseId.isEmpty()) {
                all = all.stream().filter(inv -> caseId.equals(inv.getCaseId())).collect(Collectors.toList());
            }
            result = all.stream().map(this::mapToDTO).collect(Collectors.toList());
        }
        return PageResponse.<InvoiceDTO>builder()
                .content(result)
                .page(page != null ? page : 0)
                .size(size != null ? size : 20)
                .totalElements((long) result.size())
                .totalPages(1)
                .first(true)
                .last(true)
                .empty(result.isEmpty())
                .build();
    }

    /**
     * Get invoices for a specific case.
     */
    @Transactional(readOnly = true)
    public List<InvoiceDTO> getInvoicesByCase(String caseId) {
        return invoiceRepository.findByCaseId(caseId).stream()
                .sorted(Comparator.comparing(Invoice::getInvoiceDate))
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get invoices for a specific client.
     */
    @Transactional(readOnly = true)
    public List<InvoiceDTO> getInvoicesByClient(String clientId) {
        return invoiceRepository.findByClientId(clientId).stream()
                .sorted(Comparator.comparing(Invoice::getInvoiceDate))
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Issue an invoice (DRAFT -> ISSUED).
     */
    @Transactional
    public InvoiceDTO issueInvoice(String id) {
        log.info("Issuing invoice: {}", id);
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice", "id", id));
        if ("DRAFT".equals(invoice.getStatus())) {
            invoice.setStatus("ISSUED");
            invoice = invoiceRepository.save(invoice);
        }
        return mapToDTO(invoice);
    }

    /**
     * Record a payment against an invoice.
     */
    @Transactional
    public InvoiceDTO recordPayment(String id, BigDecimal paymentAmount) {
        log.info("Recording payment of {} for invoice: {}", paymentAmount, id);
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice", "id", id));
        if (paymentAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Payment amount must be positive");
        }
        if (invoice.getAmountOutstanding().subtract(paymentAmount).compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Payment exceeds outstanding amount");
        }
        invoice.setAmountPaid(invoice.getAmountPaid().add(paymentAmount));
        invoice.setAmountOutstanding(invoice.getAmountOutstanding().subtract(paymentAmount));
        if (invoice.getAmountOutstanding().compareTo(BigDecimal.ZERO) == 0) {
            invoice.setStatus("PAID");
            invoice.setPaidAt(LocalDateTime.now());
        } else {
            invoice.setStatus("PARTIALLY_PAID");
        }
        invoice = invoiceRepository.save(invoice);
        return mapToDTO(invoice);
    }

    /**
     * Cancel an invoice.
     */
    @Transactional
    public InvoiceDTO cancelInvoice(String id) {
        log.info("Cancelling invoice: {}", id);
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice", "id", id));
        if ("PAID".equals(invoice.getStatus())) {
            throw new IllegalStateException("Cannot cancel a paid invoice");
        }
        invoice.setStatus("CANCELLED");
        invoice = invoiceRepository.save(invoice);
        return mapToDTO(invoice);
    }

    /**
     * Get overdue invoices.
     */
    @Transactional(readOnly = true)
    public List<InvoiceDTO> getOverdueInvoices() {
        return invoiceRepository.findOverdueInvoices(LocalDate.now()).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get total outstanding amount for a firm.
     */
    @Transactional(readOnly = true)
    public BigDecimal getTotalOutstanding(String lawFirmId) {
        return invoiceRepository.getTotalOutstanding(lawFirmId);
    }

    /**
     * Get revenue in a date range.
     */
    @Transactional(readOnly = true)
    public BigDecimal getRevenueInRange(String lawFirmId, LocalDate start, LocalDate end) {
        return invoiceRepository.getRevenueInRange(lawFirmId, start, end);
    }

    // --- Private Helpers ---

    private Invoice mapToEntity(InvoiceDTO dto, String lawFirmId, String invoiceNumber) {
        return Invoice.builder()
                .invoiceNumber(invoiceNumber)
                .lawFirmId(lawFirmId)
                .caseId(dto.getCaseId())
                .caseNumber(dto.getCaseNumber())
                .clientId(dto.getClientId())
                .clientName(dto.getClientName())
                .invoiceDate(dto.getInvoiceDate())
                .dueDate(dto.getDueDate())
                .billingPeriodStart(dto.getBillingPeriodStart())
                .billingPeriodEnd(dto.getBillingPeriodEnd())
                .currency(dto.getCurrency() != null ? dto.getCurrency() : "PKR")
                .notes(dto.getNotes())
                .termsAndConditions(dto.getTermsAndConditions())
                .paymentInstructions(dto.getPaymentInstructions())
                .billingModel(dto.getBillingModel())
                .build();
    }

    private InvoiceDTO mapToDTO(Invoice invoice) {
        List<InvoiceItemDTO> items = invoice.getItems().stream()
                .map(this::toItemDTO)
                .collect(Collectors.toList());
        return InvoiceDTO.builder()
                .id(invoice.getId())
                .invoiceNumber(invoice.getInvoiceNumber())
                .lawFirmId(invoice.getLawFirmId())
                .caseId(invoice.getCaseId())
                .caseNumber(invoice.getCaseNumber())
                .clientId(invoice.getClientId())
                .clientName(invoice.getClientName())
                .invoiceDate(invoice.getInvoiceDate())
                .dueDate(invoice.getDueDate())
                .billingPeriodStart(invoice.getBillingPeriodStart())
                .billingPeriodEnd(invoice.getBillingPeriodEnd())
                .subtotal(invoice.getSubtotal())
                .taxAmount(invoice.getTaxAmount())
                .discountAmount(invoice.getDiscountAmount())
                .total(invoice.getTotal())
                .amountPaid(invoice.getAmountPaid())
                .amountOutstanding(invoice.getAmountOutstanding())
                .currency(invoice.getCurrency())
                .status(invoice.getStatus())
                .billingModel(invoice.getBillingModel())
                .notes(invoice.getNotes())
                .termsAndConditions(invoice.getTermsAndConditions())
                .paymentInstructions(invoice.getPaymentInstructions())
                .issuedAt(invoice.getIssuedAt())
                .paidAt(invoice.getPaidAt())
                .createdAt(invoice.getCreatedAt() != null ? invoice.getCreatedAt().format(DATE_FORMATTER) : null)
                .updatedAt(invoice.getUpdatedAt() != null ? invoice.getUpdatedAt().format(DATE_FORMATTER) : null)
                .items(items)
                .build();
    }

    private InvoiceItemDTO toItemDTO(InvoiceItem item) {
        return InvoiceItemDTO.builder()
                .id(item.getId())
                .invoiceId(item.getInvoiceId())
                .itemNumber(item.getItemNumber())
                .description(item.getDescription())
                .quantity(item.getQuantity())
                .unit(item.getUnit())
                .rate(item.getRate())
                .amount(item.getAmount())
                .timeEntryId(item.getTimeEntryId())
                .expenseId(item.getExpenseId())
                .taxRate(item.getTaxRate())
                .taxAmount(item.getTaxAmount())
                .discountAmount(item.getDiscountAmount())
                .total(item.getTotal())
                .sortOrder(item.getSortOrder())
                .notes(item.getNotes())
                .build();
    }

    private void updateInvoiceItems(Invoice invoice, List<InvoiceItemDTO> newItems) {
        invoice.getItems().clear();
        for (int i = 0; i < newItems.size(); i++) {
            InvoiceItemDTO itemDto = newItems.get(i);
            InvoiceItem item = InvoiceItem.builder()
                    .invoiceId(invoice.getId())
                    .itemNumber(itemDto.getItemNumber())
                    .description(itemDto.getDescription())
                    .quantity(itemDto.getQuantity())
                    .unit(itemDto.getUnit())
                    .rate(itemDto.getRate())
                    .amount(itemDto.getAmount())
                    .timeEntryId(itemDto.getTimeEntryId())
                    .expenseId(itemDto.getExpenseId())
                    .taxRate(itemDto.getTaxRate())
                    .taxAmount(itemDto.getTaxAmount())
                    .discountAmount(itemDto.getDiscountAmount())
                    .total(itemDto.getTotal())
                    .sortOrder(itemDto.getSortOrder() != null ? itemDto.getSortOrder() : i + 1)
                    .notes(itemDto.getNotes())
                    .build();
            item.calculateTotal();
            invoice.getItems().add(item);
        }
    }

    private String generateInvoiceNumber(String lawFirmId) {
        String prefix = lawFirmId.substring(0, Math.min(4, lawFirmId.length()));
        long count = invoiceRepository.count();
        return String.format("INV-%s-%05d", prefix, count + 1);
    }

    private boolean isFinalInvoice(String status) {
        return "ISSUED".equals(status) || "PARTIALLY_PAID".equals(status)
                || "PAID".equals(status) || "OVERDUE".equals(status) || "CANCELLED".equals(status);
    }
}