package com.company.legal.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request body for an admin creating a new user (e.g. a lawyer or staff member).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateUserRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;

    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    private String phone;

    /** Role id, e.g. ROLE-LAWYER, ROLE-PARALEGAL, ROLE-FIRM-ADMIN. */
    @NotBlank(message = "Role is required")
    private String roleId;

    /** Optional status; defaults to ACTIVE. */
    private String status;

    /** Optional lawyer profile fields (used when the role is a lawyer role). */
    private String barNumber;
    private String licenseNumber;
    private java.math.BigDecimal hourlyRate;
}
