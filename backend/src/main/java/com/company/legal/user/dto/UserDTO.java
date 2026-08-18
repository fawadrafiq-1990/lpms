package com.company.legal.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * User representation returned by the user-management API.
 * Never includes the password hash.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {

    private String id;
    private String email;
    private String firstName;
    private String lastName;
    private String middleName;
    private String preferredName;
    private String fullName;
    private String phone;
    private String mobile;
    private String profileImageUrl;
    private String status;
    private Boolean emailVerified;
    private Boolean mfaEnabled;
    private String lawFirmId;
    private String branchId;
    private String departmentId;
    private Set<String> roles;
    private String lawyerId;
    private LocalDateTime lastLoginAt;
    private LocalDateTime createdAt;
}
