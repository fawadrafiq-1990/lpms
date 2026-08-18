package com.company.legal.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request body for updating an existing user. All fields optional; the password
 * is only changed when provided.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserRequest {

    private String firstName;
    private String lastName;
    private String phone;
    private String mobile;
    private String status;
    private String roleId;
    /** Set to update the password; leave null to keep the existing hash. */
    private String password;
}
