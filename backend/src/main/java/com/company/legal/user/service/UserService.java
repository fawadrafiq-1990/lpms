package com.company.legal.user.service;

import com.company.legal.common.dto.PageResponse;
import com.company.legal.common.exception.BaseException;
import com.company.legal.common.exception.ResourceNotFoundException;
import com.company.legal.common.security.SecurityUtils;
import com.company.legal.user.dto.CreateUserRequest;
import com.company.legal.user.dto.LawyerDTO;
import com.company.legal.user.dto.UpdateUserRequest;
import com.company.legal.user.dto.UserDTO;
import com.company.legal.user.entity.Lawyer;
import com.company.legal.user.entity.Role;
import com.company.legal.user.entity.User;
import com.company.legal.user.repository.LawyerRepository;
import com.company.legal.user.repository.RoleRepository;
import com.company.legal.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service for user and lawyer management (admin operations).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final LawyerRepository lawyerRepository;
    private final PasswordEncoder passwordEncoder;

    /** Role ids that denote a practising lawyer (and thus warrant a lawyers row). */
    private static final Set<String> LAWYER_ROLE_IDS = Set.of(
            "ROLE-LAWYER", "ROLE-SENIOR-LAWYER", "ROLE-MANAGING-PARTNER");

    @Transactional(readOnly = true)
    public PageResponse<UserDTO> getUsers(String role, Integer page, Integer size, String search) {
        String lawFirmId = SecurityUtils.getLawFirmId();
        Pageable pageable = PageRequest.of(page != null ? page : 0,
                size != null ? size : 20,
                Sort.by("createdAt").descending());

        Page<User> userPage;

        boolean hasSearch = search != null && !search.isBlank();
        boolean hasRole = role != null && !role.isBlank();

        if (hasSearch) {
            userPage = userRepository.searchByLawFirm(lawFirmId, search, pageable);
        } else if (hasRole) {
            // role filter is applied in-memory on the firm's users (role is a join)
            List<User> firmUsers = userRepository.findByLawFirmId(lawFirmId, pageable).getContent();
            List<User> filtered = firmUsers.stream()
                    .filter(u -> u.getRoles().stream()
                            .anyMatch(r -> roleMatches(r, role)))
                    .toList();
            // Re-page the filtered list
            int from = Math.min((int) pageable.getOffset(), filtered.size());
            int to = Math.min(from + pageable.getPageSize(), filtered.size());
            List<User> sub = filtered.subList(from, to);
            userPage = new org.springframework.data.domain.PageImpl<>(sub, pageable, filtered.size());
        } else {
            userPage = userRepository.findByLawFirmId(lawFirmId, pageable);
        }

        return PageResponse.from(userPage.map(this::mapToDTO));
    }

    @Transactional(readOnly = true)
    public UserDTO getUserById(String id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        return mapToDTO(user);
    }

    @Transactional
    public UserDTO createUser(CreateUserRequest request) {
        log.info("Creating user with email: {}", request.getEmail());

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BaseException("EMAIL_EXISTS", "Email already registered");
        }

        Role role = roleRepository.findById(request.getRoleId())
                .orElseThrow(() -> new BaseException("INVALID_ROLE", "Unknown role: " + request.getRoleId()));

        String lawFirmId = SecurityUtils.getLawFirmId();

        User user = User.builder()
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phone(request.getPhone())
                .lawFirmId(lawFirmId)
                .status(parseStatus(request.getStatus(), User.UserStatus.ACTIVE))
                .emailVerified(true)
                .roles(new HashSet<>(Set.of(role)))
                .build();

        user = userRepository.save(user);

        // Create a lawyer profile when the role is a lawyer role
        if (LAWYER_ROLE_IDS.contains(role.getId())) {
            Lawyer lawyer = Lawyer.builder()
                    .userId(user.getId())
                    .lawFirmId(lawFirmId)
                    .barNumber(request.getBarNumber())
                    .licenseNumber(request.getLicenseNumber())
                    .hourlyRate(request.getHourlyRate() != null ? request.getHourlyRate() : BigDecimal.ZERO)
                    .build();
            lawyerRepository.save(lawyer);
            log.info("Created lawyer profile for user: {}", user.getId());
        }

        log.info("User created: {}", user.getId());
        return mapToDTO(user);
    }

    @Transactional
    public UserDTO updateUser(String id, UpdateUserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        if (request.getFirstName() != null) user.setFirstName(request.getFirstName());
        if (request.getLastName() != null) user.setLastName(request.getLastName());
        if (request.getPhone() != null) user.setPhone(request.getPhone());
        if (request.getMobile() != null) user.setMobile(request.getMobile());
        if (request.getStatus() != null) {
            user.setStatus(User.UserStatus.valueOf(request.getStatus()));
        }
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        }
        if (request.getRoleId() != null && !request.getRoleId().isBlank()) {
            Role role = roleRepository.findById(request.getRoleId())
                    .orElseThrow(() -> new BaseException("INVALID_ROLE", "Unknown role: " + request.getRoleId()));
            user.setRoles(new HashSet<>(Set.of(role)));
        }

        user = userRepository.save(user);
        return mapToDTO(user);
    }

    @Transactional
    public void deleteUser(String id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        user.setStatus(User.UserStatus.INACTIVE);
        user.softDelete();
        userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public List<LawyerDTO> getLawyers() {
        String lawFirmId = SecurityUtils.getLawFirmId();
        List<Lawyer> lawyers = lawyerRepository.findByLawFirmId(lawFirmId);

        return lawyers.stream()
                .map(lawyer -> {
                    User user = userRepository.findById(lawyer.getUserId()).orElse(null);
                    return mapToLawyerDTO(lawyer, user);
                })
                .toList();
    }

    // ==================== Mapping ====================

    private UserDTO mapToDTO(User user) {
        Lawyer lawyer = lawyerRepository.findByUserId(user.getId()).orElse(null);
        return UserDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .middleName(user.getMiddleName())
                .preferredName(user.getPreferredName())
                .fullName(user.getFullName())
                .phone(user.getPhone())
                .mobile(user.getMobile())
                .profileImageUrl(user.getProfileImageUrl())
                .status(user.getStatus().name())
                .emailVerified(user.getEmailVerified())
                .mfaEnabled(user.getMfaEnabled())
                .lawFirmId(user.getLawFirmId())
                .branchId(user.getBranchId())
                .departmentId(user.getDepartmentId())
                .roles(user.getRoles().stream().map(Role::getName).collect(Collectors.toSet()))
                .lawyerId(lawyer != null ? lawyer.getId() : null)
                .lastLoginAt(user.getLastLoginAt())
                .createdAt(user.getCreatedAt())
                .build();
    }

    private LawyerDTO mapToLawyerDTO(Lawyer lawyer, User user) {
        return LawyerDTO.builder()
                .id(lawyer.getId())
                .userId(lawyer.getUserId())
                .email(user != null ? user.getEmail() : null)
                .firstName(user != null ? user.getFirstName() : null)
                .lastName(user != null ? user.getLastName() : null)
                .fullName(user != null ? user.getFullName() : null)
                .barNumber(lawyer.getBarNumber())
                .hourlyRate(lawyer.getHourlyRate())
                .availabilityStatus(lawyer.getAvailabilityStatus())
                .totalCases(lawyer.getTotalCases())
                .build();
    }

    private boolean roleMatches(Role role, String requested) {
        // Match by id (ROLE-LAWYER) or name (Lawyer), case-insensitive
        if (role.getId() != null && role.getId().equalsIgnoreCase(requested)) return true;
        return role.getName() != null && role.getName().equalsIgnoreCase(requested);
    }

    private User.UserStatus parseStatus(String status, User.UserStatus fallback) {
        if (status == null || status.isBlank()) return fallback;
        try {
            return User.UserStatus.valueOf(status);
        } catch (IllegalArgumentException e) {
            return fallback;
        }
    }
}
