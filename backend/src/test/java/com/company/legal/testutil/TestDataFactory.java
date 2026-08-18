package com.company.legal.testutil;

import com.company.legal.auth.dto.LoginRequest;
import com.company.legal.auth.dto.RegisterRequest;
import com.company.legal.auth.dto.ResetPasswordRequest;
import com.company.legal.billing.dto.InvoiceDTO;
import com.company.legal.billing.dto.InvoiceItemDTO;
import com.company.legal.billing.dto.TimeEntryDTO;
import com.company.legal.billing.entity.Invoice;
import com.company.legal.billing.entity.InvoiceItem;
import com.company.legal.billing.entity.TimeEntry;
import com.company.legal.case_management.dto.CaseDTO;
import com.company.legal.case_management.entity.Case;
import com.company.legal.client.dto.ClientDTO;
import com.company.legal.client.entity.Client;
import com.company.legal.client.entity.ClientContact;
import com.company.legal.conflict.entity.ConflictCheck;
import com.company.legal.user.entity.Role;
import com.company.legal.user.entity.User;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.UUID;

/**
 * Factory class for creating test data objects.
 */
public class TestDataFactory {

    private static int counter = 0;

    private static String nextId() {
        return UUID.randomUUID().toString();
    }

    private static String nextSuffix() {
        return String.format("%04d", ++counter);
    }

    // --- User ---
    public static User createActiveUser() {
        User user = User.builder()
                .id(nextId())
                .email("test" + nextSuffix() + "@example.com")
                .passwordHash("$2a$12$simplehashedpassword")
                .firstName("Test")
                .lastName("User")
                .lawFirmId("firm-" + nextSuffix())
                .status(User.UserStatus.ACTIVE)
                .emailVerified(true)
                .failedLoginAttempts(0)
                .build();
        Role role = Role.builder().name("LAWYER").build();
        user.getRoles().add(role);
        return user;
    }

    public static User createInactiveUser() {
        return User.builder()
                .id(nextId())
                .email("inactive" + nextSuffix() + "@example.com")
                .passwordHash("$2a$12$simplehashedpassword")
                .firstName("Inactive")
                .lastName("User")
                .lawFirmId("firm-" + nextSuffix())
                .status(User.UserStatus.INACTIVE)
                .build();
    }

    public static User createPendingUser() {
        return User.builder()
                .id(nextId())
                .email("pending" + nextSuffix() + "@example.com")
                .passwordHash("$2a$12$simplehashedpassword")
                .firstName("Pending")
                .lastName("User")
                .lawFirmId("firm-" + nextSuffix())
                .status(User.UserStatus.PENDING)
                .build();
    }

    public static User createLockedUser() {
        User user = User.builder()
                .id(nextId())
                .email("locked" + nextSuffix() + "@example.com")
                .passwordHash("$2a$12$simplehashedpassword")
                .firstName("Locked")
                .lastName("User")
                .lawFirmId("firm-" + nextSuffix())
                .status(User.UserStatus.ACTIVE)
                .failedLoginAttempts(5)
                .lockedUntil(LocalDateTime.now().plusMinutes(30))
                .build();
        return user;
    }

    // --- Role ---
    public static Role createLawyerRole() {
        return Role.builder()
                .id(nextId())
                .name("LAWYER")
                .description("Lawyer role")
                .build();
    }

    public static Role createClientRole() {
        return Role.builder()
                .id(nextId())
                .name("CLIENT")
                .description("Client role")
                .build();
    }

    // --- Auth DTOs ---
    public static LoginRequest createLoginRequest() {
        return LoginRequest.builder()
                .email("test@example.com")
                .password("TestPass1")
                .build();
    }

    public static RegisterRequest createRegisterRequest() {
        return RegisterRequest.builder()
                .email("newuser" + nextSuffix() + "@example.com")
                .password("SecurePass1")
                .firstName("John")
                .lastName("Doe")
                .isClient(false)
                .build();
    }

    public static RegisterRequest createClientRegisterRequest() {
        return RegisterRequest.builder()
                .email("client" + nextSuffix() + "@example.com")
                .password("SecurePass1")
                .firstName("Jane")
                .lastName("Smith")
                .isClient(true)
                .build();
    }

    public static ResetPasswordRequest createResetPasswordRequest() {
        return ResetPasswordRequest.builder()
                .email("test" + nextSuffix() + "@example.com")
                .token("reset-token-" + nextSuffix())
                .newPassword("NewPass1")
                .build();
    }

    // --- Client ---
    public static Client createIndividualClient() {
        Client client = Client.builder()
                .id(nextId())
                .clientNumber("CL-" + nextSuffix())
                .lawFirmId("firm-" + nextSuffix())
                .clientType(Client.ClientType.INDIVIDUAL)
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .phone("+1234567890")
                .status(Client.ClientStatus.ACTIVE)
                .riskLevel(Client.RiskLevel.LOW)
                .billingModel("HOURLY")
                .build();
        return client;
    }

    public static Client createCompanyClient() {
        Client client = Client.builder()
                .id(nextId())
                .clientNumber("CL-" + nextSuffix())
                .lawFirmId("firm-" + nextSuffix())
                .clientType(Client.ClientType.COMPANY)
                .companyName("Acme Corporation")
                .registrationNumber("REG-123456")
                .taxId("TAX-789012")
                .email("contact@acme.com")
                .industry("Technology")
                .status(Client.ClientStatus.ACTIVE)
                .riskLevel(Client.RiskLevel.MEDIUM)
                .build();
        return client;
    }

    public static Client createBlacklistedClient() {
        return Client.builder()
                .id(nextId())
                .clientNumber("CL-" + nextSuffix())
                .lawFirmId("firm-" + nextSuffix())
                .clientType(Client.ClientType.INDIVIDUAL)
                .firstName("Blacklisted")
                .lastName("Client")
                .email("blacklisted@example.com")
                .status(Client.ClientStatus.BLACKLISTED)
                .build();
    }

    public static ClientDTO createClientDTO() {
        return ClientDTO.builder()
                .clientNumber("CL-" + nextSuffix())
                .clientType(Client.ClientType.INDIVIDUAL)
                .firstName("Alice")
                .lastName("Johnson")
                .email("alice@example.com")
                .phone("+1987654321")
                .status(Client.ClientStatus.ACTIVE)
                .riskLevel(Client.RiskLevel.LOW)
                .build();
    }

    public static ClientContact createClientContact() {
        return ClientContact.builder()
                .id(nextId())
                .clientId("client-" + nextSuffix())
                .firstName("Contact")
                .lastName("Person")
                .email("contact@example.com")
                .phone("+1111111111")
                .jobTitle("Manager")
                .isPrimary(true)
                .canReceiveInvoices(true)
                .canReceiveNotifications(true)
                .build();
    }

    // --- Case ---
    public static Case createCase() {
        Case c = new Case();
        c.setId(nextId());
        c.setCaseNumber("CASE-" + nextSuffix());
        c.setLawFirmId("firm-" + nextSuffix());
        c.setClientId("client-" + nextSuffix());
        c.setClientName("John Doe");
        c.setCaseType("Civil");
        c.setCaseStatus(Case.CaseStatus.NEW.name());
        c.setCasePriority(Case.CasePriority.MEDIUM.name());
        c.setTitle("Sample Case Title");
        c.setDescription("This is a sample case description");
        c.setPracticeArea("Corporate Law");
        c.setCourtId("court-" + nextSuffix());
        c.setCourtName("Central Court");
        c.setJudgeId("judge-" + nextSuffix());
        c.setFilingNumber("FILING-" + nextSuffix());
        c.setFilingDate(LocalDate.now());
        c.setCaseValue(new BigDecimal("100000.00"));
        c.setOpeningDate(LocalDate.now());
        c.setExpectedClosingDate(LocalDate.now().plusMonths(6));
        c.setLeadLawyerId("lawyer-" + nextSuffix());
        c.setLeadLawyerName("Attorney Smith");
        c.setBillingModel("HOURLY");
        c.setTotalBilled(BigDecimal.ZERO);
        c.setTotalPaid(BigDecimal.ZERO);
        c.setTotalOutstanding(BigDecimal.ZERO);
        c.setConfidentialityLevel("INTERNAL");
        c.setRiskLevel("LOW");
        return c;
    }

    public static Case createInProgressCase() {
        Case c = createCase();
        c.setCaseStatus(Case.CaseStatus.IN_PROGRESS.name());
        return c;
    }

    public static Case createClosedCase() {
        Case c = createCase();
        c.setCaseStatus(Case.CaseStatus.CLOSED.name());
        c.setActualClosingDate(LocalDate.now());
        return c;
    }

    public static CaseDTO createCaseDTO() {
        return CaseDTO.builder()
                .caseNumber("CASE-" + nextSuffix())
                .clientId("client-" + nextSuffix())
                .clientName("Client Name")
                .caseType("Civil")
                .caseStatus(Case.CaseStatus.NEW.name())
                .casePriority(Case.CasePriority.MEDIUM.name())
                .title("Test Case")
                .description("Test Description")
                .practiceArea("Corporate Law")
                .caseValue(new BigDecimal("50000.00"))
                .build();
    }

    // --- Invoice ---
    public static Invoice createDraftInvoice() {
        Invoice invoice = Invoice.builder()
                .id(nextId())
                .invoiceNumber("INV-" + nextSuffix())
                .lawFirmId("firm-" + nextSuffix())
                .caseId("case-" + nextSuffix())
                .caseNumber("CASE-0001")
                .clientId("client-" + nextSuffix())
                .clientName("Test Client")
                .invoiceDate(LocalDate.now())
                .dueDate(LocalDate.now().plusDays(30))
                .billingPeriodStart(LocalDate.now().minusMonths(1))
                .billingPeriodEnd(LocalDate.now())
                .currency("USD")
                .status("DRAFT")
                .billingModel("HOURLY")
                .notes("Test invoice")
                .items(new java.util.ArrayList<>())
                .build();
        return invoice;
    }

    public static Invoice createIssuedInvoice() {
        Invoice invoice = createDraftInvoice();
        invoice.setStatus("ISSUED");
        invoice.setIssuedAt(LocalDateTime.now());
        // Add some items
        InvoiceItem item = InvoiceItem.builder()
                .invoiceId(invoice.getId())
                .itemNumber(1)
                .description("Legal consultation")
                .quantity(2)
                .unit("hours")
                .rate(new BigDecimal("250.00"))
                .amount(new BigDecimal("500.00"))
                .taxRate(new BigDecimal("0"))
                .taxAmount(BigDecimal.ZERO)
                .total(new BigDecimal("500.00"))
                .sortOrder(1)
                .build();
        invoice.getItems().add(item);
        invoice.calculateTotals(invoice.getItems());
        return invoice;
    }

    public static Invoice createPartiallyPaidInvoice() {
        Invoice invoice = createIssuedInvoice();
        invoice.setAmountPaid(new BigDecimal("200.00"));
        invoice.setStatus("PARTIALLY_PAID");
        invoice.calculateOutstanding();
        return invoice;
    }

    public static Invoice createPaidInvoice() {
        Invoice invoice = createIssuedInvoice();
        invoice.setAmountPaid(invoice.getTotal());
        invoice.setStatus("PAID");
        invoice.setPaidAt(LocalDateTime.now());
        return invoice;
    }

    public static InvoiceDTO createInvoiceDTO() {
        InvoiceItemDTO item = InvoiceItemDTO.builder()
                .itemNumber(1)
                .description("Consultation")
                .quantity(1)
                .unit("hours")
                .rate(new BigDecimal("250.00"))
                .amount(new BigDecimal("250.00"))
                .taxRate(new BigDecimal("0"))
                .total(new BigDecimal("250.00"))
                .build();

        return InvoiceDTO.builder()
                .caseId("case-" + nextSuffix())
                .caseNumber("CASE-0001")
                .clientId("client-" + nextSuffix())
                .clientName("Test Client")
                .invoiceDate(LocalDate.now())
                .dueDate(LocalDate.now().plusDays(30))
                .currency("USD")
                .billingModel("HOURLY")
                .items(java.util.List.of(item))
                .build();
    }

    // --- TimeEntry ---
    public static TimeEntry createTimeEntry() {
        TimeEntry entry = TimeEntry.builder()
                .id(nextId())
                .entryNumber("TE-" + nextSuffix())
                .caseId("case-" + nextSuffix())
                .caseNumber("CASE-0001")
                .userId("user-" + nextSuffix())
                .lawyerId("lawyer-" + nextSuffix())
                .workDate(LocalDate.now())
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(11, 30))
                .description("Client consultation and document review")
                .activityType("Consultation")
                .billingRate(new BigDecimal("250.00"))
                .hourlyRate(new BigDecimal("250.00"))
                .billable(true)
                .billed(false)
                .status("DRAFT")
                .build();
        entry.calculateDuration();
        entry.calculateAmount();
        return entry;
    }

    public static TimeEntry createBilledTimeEntry() {
        TimeEntry entry = createTimeEntry();
        entry.setBilled(true);
        entry.setInvoiceId("invoice-" + nextSuffix());
        entry.setStatus("BILLED");
        return entry;
    }

    public static TimeEntry createNonBillableEntry() {
        TimeEntry entry = TimeEntry.builder()
                .id(nextId())
                .entryNumber("TE-" + nextSuffix())
                .caseId("case-" + nextSuffix())
                .userId("user-" + nextSuffix())
                .workDate(LocalDate.now())
                .startTime(LocalTime.of(14, 0))
                .endTime(LocalTime.of(15, 0))
                .description("Internal meeting")
                .activityType("Administration")
                .billingRate(new BigDecimal("150.00"))
                .billable(false)
                .billed(false)
                .status("DRAFT")
                .build();
        entry.calculateDuration();
        return entry;
    }

    public static TimeEntryDTO createTimeEntryDTO() {
        return TimeEntryDTO.builder()
                .caseId("case-" + nextSuffix())
                .caseNumber("CASE-0001")
                .userId("user-" + nextSuffix())
                .lawyerId("lawyer-" + nextSuffix())
                .workDate(LocalDate.now())
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(11, 0))
                .description("Test time entry")
                .activityType("Consultation")
                .billingRate(new BigDecimal("250.00"))
                .billable(true)
                .build();
    }

    // --- ConflictCheck ---
    public static ConflictCheck createConflictCheck() {
        return ConflictCheck.builder()
                .id(nextId())
                .lawFirmId("firm-" + nextSuffix())
                .queryClientName("Test Company")
                .relatedClientId("client-" + nextSuffix())
                .relatedClientName("Related Company")
                .conflictType(ConflictCheck.ConflictType.CLIENT.name())
                .severity(ConflictCheck.Severity.HIGH.name())
                .isConflict(true)
                .cleared(false)
                .checkedAt(LocalDateTime.now())
                .build();
    }

    public static ConflictCheck createClearedConflictCheck() {
        ConflictCheck cc = createConflictCheck();
        cc.setCleared(true);
        cc.setClearedByUserId("user-" + nextSuffix());
        cc.setClearedReason("No actual conflict identified");
        return cc;
    }

    // --- Helper: Set LocalDateTime on entities ---
    public static void setDates(Object entity, LocalDateTime created, LocalDateTime updated) {
        if (entity instanceof Invoice inv) {
            inv.setCreatedAt(created);
            inv.setUpdatedAt(updated);
        } else if (entity instanceof TimeEntry te) {
            te.setCreatedAt(created);
            te.setUpdatedAt(updated);
        } else if (entity instanceof Case c) {
            // BaseEntity fields are handled by JPA auditing
        }
    }
}
