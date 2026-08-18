package com.company.legal.user.controller;

import com.company.legal.common.dto.ApiResponse;
import com.company.legal.common.dto.PageResponse;
import com.company.legal.user.dto.CreateUserRequest;
import com.company.legal.user.dto.LawyerDTO;
import com.company.legal.user.dto.UpdateUserRequest;
import com.company.legal.user.dto.UserDTO;
import com.company.legal.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for user and lawyer management (admin operations).
 */
@RestController
@RequestMapping("/v1/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "User and lawyer management APIs")
public class UserController {

    private final UserService userService;

    @GetMapping
    @Operation(summary = "List users with pagination and optional role/search filter")
    @PreAuthorize("hasAnyRole('FIRM_ADMIN','SUPER_ADMIN','MANAGING_PARTNER')")
    public ResponseEntity<ApiResponse<PageResponse<UserDTO>>> getUsers(
            @RequestParam(required = false) String role,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) String search) {
        PageResponse<UserDTO> users = userService.getUsers(role, page, size, search);
        return ResponseEntity.ok(ApiResponse.success(users));
    }

    @GetMapping("/lawyers")
    @Operation(summary = "List lawyers in the current firm (for assignment dropdowns)")
    public ResponseEntity<ApiResponse<List<LawyerDTO>>> getLawyers() {
        List<LawyerDTO> lawyers = userService.getLawyers();
        return ResponseEntity.ok(ApiResponse.success(lawyers));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get user by ID")
    @PreAuthorize("hasAnyRole('FIRM_ADMIN','SUPER_ADMIN','MANAGING_PARTNER')")
    public ResponseEntity<ApiResponse<UserDTO>> getUser(@PathVariable String id) {
        UserDTO user = userService.getUserById(id);
        return ResponseEntity.ok(ApiResponse.success(user));
    }

    @PostMapping
    @Operation(summary = "Create a new user (e.g. a lawyer or staff member)")
    @PreAuthorize("hasAnyRole('FIRM_ADMIN','SUPER_ADMIN','MANAGING_PARTNER')")
    public ResponseEntity<ApiResponse<UserDTO>> createUser(@Valid @RequestBody CreateUserRequest request) {
        UserDTO created = userService.createUser(request);
        return ResponseEntity.ok(ApiResponse.success(created, "User created successfully"));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing user")
    @PreAuthorize("hasAnyRole('FIRM_ADMIN','SUPER_ADMIN','MANAGING_PARTNER')")
    public ResponseEntity<ApiResponse<UserDTO>> updateUser(
            @PathVariable String id,
            @Valid @RequestBody UpdateUserRequest request) {
        UserDTO updated = userService.updateUser(id, request);
        return ResponseEntity.ok(ApiResponse.success(updated, "User updated successfully"));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a user (soft delete)")
    @PreAuthorize("hasAnyRole('FIRM_ADMIN','SUPER_ADMIN','MANAGING_PARTNER')")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable String id) {
        userService.deleteUser(id);
        return ResponseEntity.ok(ApiResponse.success(null, "User deleted successfully"));
    }
}
