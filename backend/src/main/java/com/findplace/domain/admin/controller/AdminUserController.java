package com.findplace.domain.admin.controller;

import com.findplace.domain.admin.dto.*;
import com.findplace.domain.admin.service.AdminUserService;
import com.findplace.domain.user.entity.UserStatus;
import com.findplace.global.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 관리자 사용자 관리 API
 */
@RestController
@RequestMapping("/v1/admin/users")
@RequiredArgsConstructor
@Tag(name = "Admin User Management", description = "관리자 사용자 관리 API")
@SecurityRequirement(name = "bearerAuth")
public class AdminUserController {

    private final AdminUserService adminUserService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "사용자 목록 조회", description = "관리자가 사용자 목록을 조회합니다.")
    public ResponseEntity<ApiResponse<Page<AdminUserResponse>>> getUsers(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) UserStatus status,
            @PageableDefault(size = 20) Pageable pageable) {

        Page<AdminUserResponse> users;
        if (keyword != null && !keyword.isBlank()) {
            users = adminUserService.searchUsers(keyword, pageable);
        } else if (status != null) {
            users = adminUserService.getUsersByStatus(status, pageable);
        } else {
            users = adminUserService.getUsers(pageable);
        }

        return ResponseEntity.ok(ApiResponse.success(users));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "사용자 상세 조회", description = "관리자가 사용자 상세 정보를 조회합니다.")
    public ResponseEntity<ApiResponse<AdminUserResponse>> getUser(
            @Parameter(description = "사용자 ID") @PathVariable Long id) {
        AdminUserResponse user = adminUserService.getUser(id);
        return ResponseEntity.ok(ApiResponse.success(user));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "사용자 상태 변경", description = "관리자가 사용자 상태를 변경합니다.")
    public ResponseEntity<ApiResponse<AdminUserResponse>> changeStatus(
            @Parameter(description = "사용자 ID") @PathVariable Long id,
            @Valid @RequestBody StatusChangeRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        Long adminId = extractUserId(userDetails);
        AdminUserResponse user = adminUserService.changeStatus(id, request, adminId);
        return ResponseEntity.ok(ApiResponse.success(user));
    }

    @PatchMapping("/{id}/role")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "사용자 역할 변경", description = "슈퍼 관리자가 사용자 역할을 변경합니다.")
    public ResponseEntity<ApiResponse<AdminUserResponse>> changeRole(
            @Parameter(description = "사용자 ID") @PathVariable Long id,
            @Valid @RequestBody RoleChangeRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        Long adminId = extractUserId(userDetails);
        AdminUserResponse user = adminUserService.changeRole(id, request, adminId);
        return ResponseEntity.ok(ApiResponse.success(user));
    }

    @GetMapping("/{id}/status-history")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "상태 변경 이력 조회", description = "사용자의 상태 변경 이력을 조회합니다.")
    public ResponseEntity<ApiResponse<List<StatusChangeLogResponse>>> getStatusHistory(
            @Parameter(description = "사용자 ID") @PathVariable Long id) {
        List<StatusChangeLogResponse> history = adminUserService.getStatusHistory(id);
        return ResponseEntity.ok(ApiResponse.success(history));
    }

    @GetMapping("/{id}/role-history")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "역할 변경 이력 조회", description = "사용자의 역할 변경 이력을 조회합니다.")
    public ResponseEntity<ApiResponse<List<RoleChangeLogResponse>>> getRoleHistory(
            @Parameter(description = "사용자 ID") @PathVariable Long id) {
        List<RoleChangeLogResponse> history = adminUserService.getRoleHistory(id);
        return ResponseEntity.ok(ApiResponse.success(history));
    }

    private Long extractUserId(UserDetails userDetails) {
        // TODO: UserDetails에서 userId 추출 로직 구현
        return 1L; // 임시 값
    }
}
