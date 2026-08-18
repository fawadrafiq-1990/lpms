package com.company.legal.client.entity;

import com.company.legal.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Client entity representing individuals, companies, or organizations.
 */
@Entity
@Table(name = "clients")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Client extends BaseEntity {

    @Column(name = "client_number", nullable = false, unique = true, length = 50)
    private String clientNumber;

    @Column(name = "law_firm_id", nullable = false, length = 36)
    private String lawFirmId;

    @Enumerated(EnumType.STRING)
    @Column(name = "client_type", nullable = false, length = 50)
    private ClientType clientType;

    @Column(name = "first_name", length = 100)
    private String firstName;

    @Column(name = "last_name", length = 100)
    private String lastName;

    @Column(name = "company_name", length = 255)
    private String companyName;

    @Column(name = "registration_number", length = 100)
    private String registrationNumber;

    @Column(name = "tax_id", length = 100)
    private String taxId;

    @Column(length = 255)
    private String email;

    @Column(length = 50)
    private String phone;

    @Column(length = 50)
    private String mobile;

    @Column(length = 100)
    private String nationality;

    @Column(name = "identification_type", length = 50)
    private String identificationType;

    @Column(name = "identification_number", length = 100)
    private String identificationNumber;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(length = 20)
    private String gender;

    @Column(length = 255)
    private String website;

    @Column(length = 100)
    private String industry;

    @Column(name = "company_size", length = 50)
    private String companySize;

    @Column(name = "annual_revenue", precision = 15, scale = 2)
    private BigDecimal annualRevenue;

    @Enumerated(EnumType.STRING)
    @Column(name = "risk_level", length = 50)
    @Builder.Default
    private RiskLevel riskLevel = RiskLevel.LOW;

    @Column
    private Boolean vip = false;

    @Column(name = "credit_limit", precision = 12, scale = 2)
    private BigDecimal creditLimit;

    @Column(name = "billing_model", length = 50)
    @Builder.Default
    private String billingModel = "HOURLY";

    @Column(name = "preferred_payment_method", length = 50)
    private String preferredPaymentMethod;

    @Column(name = "billing_address_same_as_postal")
    @Builder.Default
    private Boolean billingAddressSameAsPostal = true;

    @Column(name = "billing_address_line1", length = 255)
    private String billingAddressLine1;

    @Column(name = "billing_address_line2", length = 255)
    private String billingAddressLine2;

    @Column(name = "billing_city", length = 100)
    private String billingCity;

    @Column(name = "billing_state", length = 100)
    private String billingState;

    @Column(name = "billing_country", length = 100)
    private String billingCountry;

    @Column(name = "billing_postal_code", length = 20)
    private String billingPostalCode;

    @Column(name = "address_line1", length = 255)
    private String addressLine1;

    @Column(name = "address_line2", length = 255)
    private String addressLine2;

    @Column(length = 100)
    private String city;

    @Column(length = 100)
    private String state;

    @Column(length = 100)
    private String country;

    @Column(name = "postal_code", length = 20)
    private String postalCode;

    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    @Builder.Default
    private ClientStatus status = ClientStatus.ACTIVE;

    @Column(name = "referred_by", length = 255)
    private String referredBy;

    @Column(length = 100)
    private String source;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(columnDefinition = "JSON")
    private String preferences;

    @Column(name = "assigned_lawyer_id", length = 36)
    private String assignedLawyerId;

    @Column(name = "assigned_lawyer_name", length = 255)
    private String assignedLawyerName;

    public enum ClientType {
        INDIVIDUAL, COMPANY, GOVERNMENT, ORGANIZATION
    }

    public enum ClientStatus {
        ACTIVE, INACTIVE, PENDING, BLACKLISTED
    }

    public enum RiskLevel {
        LOW, MEDIUM, HIGH
    }

    public String getFullName() {
        if (clientType == ClientType.INDIVIDUAL) {
            return firstName + " " + lastName;
        }
        return companyName;
    }
}