package com.findplace.domain.user.controller;

import com.findplace.domain.user.dto.*;
import com.findplace.domain.user.service.UserProfileService;
import com.findplace.global.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

/**
 * 마이페이지 API
 */
@RestController
@RequestMapping("/v1/my")
@RequiredArgsConstructor
@Tag(name = "MyPage", description = "마이페이지 API")
@SecurityRequirement(name = "bearerAuth")
public class MyPageController {

    private final UserProfileService userProfileService;

    @GetMapping("/profile")
    @Operation(summary = "내 프로필 조회", description = "로그인한 사용자의 프로필을 조회합니다.")
    public ResponseEntity<ApiResponse<ProfileResponse>> getProfile(
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = extractUserId(userDetails);
        ProfileResponse profile = userProfileService.getProfile(userId);
        return ResponseEntity.ok(ApiResponse.success(profile));
    }

    @PutMapping("/profile")
    @Operation(summary = "프로필 수정", description = "프로필 정보를 수정합니다.")
    public ResponseEntity<ApiResponse<ProfileResponse>> updateProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody ProfileUpdateRequest request) {
        Long userId = extractUserId(userDetails);
        ProfileResponse profile = userProfileService.updateProfile(userId, request);
        return ResponseEntity.ok(ApiResponse.success(profile));
    }

    @PutMapping("/password")
    @Operation(summary = "비밀번호 변경", description = "비밀번호를 변경합니다.")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody PasswordChangeRequest request) {
        Long userId = extractUserId(userDetails);
        userProfileService.changePassword(userId, request);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PutMapping("/address")
    @Operation(summary = "주소 변경", description = "주소 정보를 변경합니다.")
    public ResponseEntity<ApiResponse<ProfileResponse>> updateAddress(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody AddressUpdateRequest request) {
        Long userId = extractUserId(userDetails);
        ProfileResponse profile = userProfileService.updateAddress(userId, request);
        return ResponseEntity.ok(ApiResponse.success(profile));
    }

    @PostMapping("/withdrawal")
    @Operation(summary = "회원 탈퇴", description = "회원 탈퇴를 요청합니다.")
    public ResponseEntity<ApiResponse<Void>> withdraw(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody WithdrawalRequest request) {
        Long userId = extractUserId(userDetails);
        userProfileService.withdraw(userId, request.getPassword());
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    private Long extractUserId(UserDetails userDetails) {
        // TODO: UserDetails에서 userId 추출 로직 구현
        return 1L; // 임시 값
    }
}
