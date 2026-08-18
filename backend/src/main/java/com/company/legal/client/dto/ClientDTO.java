package com.company.legal.client.dto;

import com.company.legal.client.entity.Client;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Data Transfer Object for Client.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClientDTO {

    private String id;

    @NotBlank(message = "Client number is required")
    private String clientNumber;

    @NotNull(message = "Client type is required")
    private Client.ClientType clientType;

    @Size(max = 100)
    private String firstName;

    @Size(max = 100)
    private String lastName;

    @Size(max = 255)
    private String companyName;

    @Size(max = 100)
    private String registrationNumber;

    @Size(max = 100)
    private String taxId;

    @Email
    @Size(max = 255)
    private String email;

    @Size(max = 50)
    private String phone;

    @Size(max = 50)
    private String mobile;

    @Size(max = 100)
    private String nationality;

    @Size(max = 100)
    private String identificationNumber;

    private LocalDate dateOfBirth;

    private String gender;

    @Size(max = 255)
    private String website;

    @Size(max = 100)
    private String industry;

    @Size(max = 50)
    private String companySize;

    private BigDecimal annualRevenue;

    private Client.RiskLevel riskLevel;

    private Boolean vip;

    private BigDecimal creditLimit;

    private String billingModel;

    private String preferredPaymentMethod;

    private String billingAddressLine1;

    private String billingAddressLine2;

    private String billingCity;

    private String billingState;

    private String billingCountry;

    private String billingPostalCode;

    private String addressLine1;

    private String addressLine2;

    private String city;

    private String state;

    private String country;

    private String postalCode;

    private Client.ClientStatus status;

    private String referredBy;

    private String source;

    private String notes;

    private String assignedLawyerId;

    private String assignedLawyerName;

    private List<ClientContactDTO> contacts;

    private String createdAt;

    private String updatedAt;
}