package com.company.legal.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

/**
 * User info DTO for auth response.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserInfoDto {
    private String id;
    private String email;
    private String firstName;
    private String lastName;
    private String fullName;
    private String profileImageUrl;
    private String status;
    private Set<String> roles;
    private String lawFirmId;
    private String branchId;
}