package com.company.legal.client.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

/**
 * Data Transfer Object for Client Contact.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClientContactDTO {

    private String id;

    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    private String email;

    private String phone;

    private String mobile;

    private String jobTitle;

    private String department;

    private Boolean isPrimary;

    private Boolean canReceiveInvoices;

    private Boolean canReceiveNotifications;

    private String notes;
}