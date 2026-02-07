package com.petpro.domain.user.controller;

import com.petpro.domain.user.dto.UserRequest;
import com.petpro.domain.user.dto.UserResponse;
import com.petpro.domain.user.entity.UserRole;
import com.petpro.domain.user.entity.UserStatus;
import com.petpro.domain.user.service.UserService;
import com.petpro.global.common.response.ApiResponse;
import com.petpro.global.common.response.PageResponse;
import com.petpro.global.exception.BusinessException;
import com.petpro.global.exception.ErrorCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * 사용자 컨트롤러
 *
 * 사용자 관련 REST API 엔드포인트를 제공하는 컨트롤러
 * - 사용자 CRUD 작업
 * - 비밀번호 변경
 * - 상태/역할 변경 (관리자 전용)
 */
@Tag(name = "Users", description = "사용자 관리 API")
@RestController
@RequestMapping("/v1/users")
@RequiredArgsConstructor
public class UserController {

    private static final Set<String> ALLOWED_IMAGE_TYPES = Set.of(
            "image/jpeg", "image/png", "image/gif", "image/webp"
    );
    private static final long MAX_PROFILE_IMAGE_SIZE = 5 * 1024 * 1024; // 5MB

    private final UserService userService;

    /**
     * 사용자 생성 API
     *
     * 관리자만 직접 사용자를 생성할 수 있음
     * 일반 회원가입은 AuthController 사용
     *
     * @param request 사용자 생성 요청 DTO
     * @return 생성된 사용자 정보
     */
    @Operation(summary = "사용자 생성", description = "새로운 사용자를 생성합니다.")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse.Info>> createUser(
            @Valid @RequestBody UserRequest.Create request) {
        UserResponse.Info response = userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "사용자가 생성되었습니다."));
    }

    /**
     * 사용자 조회 API
     *
     * @param id 조회할 사용자 ID
     * @return 사용자 정보
     */
    @Operation(summary = "사용자 조회", description = "ID로 사용자를 조회합니다. (본인 또는 관리자만 접근 가능)")
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or #id == T(Long).parseLong(authentication.name)")
    public ResponseEntity<ApiResponse<UserResponse.Info>> getUser(@PathVariable Long id) {
        UserResponse.Info response = userService.getUser(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 내 정보 조회 API
     *
     * 현재 로그인한 사용자 본인의 정보를 조회
     *
     * @param userDetails Spring Security 인증 정보
     * @return 현재 사용자 정보
     */
    @Operation(summary = "내 정보 조회", description = "현재 로그인한 사용자의 정보를 조회합니다.")
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse.Info>> getMyInfo(
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        UserResponse.Info response = userService.getUser(userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 내 정보 수정 API (닉네임)
     *
     * @param userDetails Spring Security 인증 정보
     * @param request 닉네임 수정 요청 DTO
     * @return 수정된 사용자 정보
     */
    @Operation(summary = "내 정보 수정", description = "닉네임을 수정합니다.")
    @PutMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse.Info>> updateMyInfo(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody UserRequest.UpdateNickname request) {
        Long userId = Long.parseLong(userDetails.getUsername());
        UserResponse.Info response = userService.updateNickname(userId, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 회원 탈퇴 API
     *
     * 소셜 로그인 사용자는 비밀번호 없이 탈퇴 가능
     *
     * @param userDetails Spring Security 인증 정보
     * @param request 탈퇴 요청 DTO (선택적)
     * @return 성공 응답
     */
    @Operation(summary = "회원 탈퇴", description = "회원 탈퇴를 처리합니다.")
    @DeleteMapping("/me")
    public ResponseEntity<ApiResponse<Map<String, Object>>> deleteMyAccount(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody(required = false) UserRequest.DeleteAccount request) {
        Long userId = Long.parseLong(userDetails.getUsername());
        userService.deleteMyAccount(userId, request != null ? request : UserRequest.DeleteAccount.builder().build());
        return ResponseEntity.ok(ApiResponse.success(
                Map.of("success", true, "message", "회원 탈퇴가 완료되었습니다.")));
    }

    /**
     * 프로필 이미지 업로드 API
     *
     * @param userDetails Spring Security 인증 정보
     * @param file 이미지 파일
     * @return 업로드된 이미지 URL
     */
    @Operation(summary = "프로필 이미지 업로드", description = "프로필 이미지를 업로드합니다.")
    @PostMapping("/me/profile-image")
    public ResponseEntity<ApiResponse<Map<String, String>>> uploadProfileImage(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam("file") MultipartFile file) {
        Long userId = Long.parseLong(userDetails.getUsername());

        validateProfileImage(file);

        String extension = getFileExtension(file.getOriginalFilename());
        String safeFileName = UUID.randomUUID() + "." + extension;

        // TODO: 실제 파일 업로드 로직 (MinIO/S3)
        // 현재는 임시 URL 반환
        String imageUrl = "https://storage.example.com/profiles/" + userId + "/" + safeFileName;
        String savedUrl = userService.updateProfileImage(userId, imageUrl);

        return ResponseEntity.ok(ApiResponse.success(Map.of("profileImageUrl", savedUrl)));
    }

    private void validateProfileImage(MultipartFile file) {
        if (file.isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
        if (file.getSize() > MAX_PROFILE_IMAGE_SIZE) {
            throw new BusinessException(ErrorCode.FILE_TOO_LARGE);
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_IMAGE_TYPES.contains(contentType)) {
            throw new BusinessException(ErrorCode.INVALID_FILE_TYPE);
        }
    }

    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "jpg";
        }
        return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
    }

    /**
     * 내 비밀번호 변경 API
     *
     * @param userDetails Spring Security 인증 정보
     * @param request 비밀번호 변경 요청 DTO
     * @return 성공 응답
     */
    @Operation(summary = "비밀번호 변경", description = "비밀번호를 변경합니다.")
    @PutMapping("/me/password")
    public ResponseEntity<ApiResponse<Map<String, Object>>> changeMyPassword(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody UserRequest.ChangeMyPassword request) {
        Long userId = Long.parseLong(userDetails.getUsername());
        userService.changeMyPassword(userId, request);
        return ResponseEntity.ok(ApiResponse.success(
                Map.of("success", true, "message", "비밀번호가 변경되었습니다.")));
    }

    /**
     * 사용자 목록 조회 API
     *
     * 관리자 전용 - 모든 사용자 목록을 페이지네이션하여 조회
     *
     * @param pageable 페이지네이션 정보
     * @return 사용자 목록 페이지
     */
    @Operation(summary = "사용자 목록 조회", description = "사용자 목록을 페이지네이션하여 조회합니다.")
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PageResponse<UserResponse.Simple>>> getUsers(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<UserResponse.Simple> page = userService.getUsers(pageable);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.of(page)));
    }

    /**
     * 사용자 검색 API
     *
     * 관리자 전용 - 키워드로 사용자를 검색
     *
     * @param keyword 검색 키워드 (이름, 이메일)
     * @param pageable 페이지네이션 정보
     * @return 검색 결과 페이지
     */
    @Operation(summary = "사용자 검색", description = "키워드로 사용자를 검색합니다.")
    @GetMapping("/search")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PageResponse<UserResponse.Simple>>> searchUsers(
            @RequestParam String keyword,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<UserResponse.Simple> page = userService.searchUsers(keyword, pageable);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.of(page)));
    }

    /**
     * 사용자 정보 수정 API
     *
     * 본인 또는 관리자만 수정 가능
     *
     * @param id 수정할 사용자 ID
     * @param request 수정 요청 DTO
     * @param userDetails 현재 인증된 사용자 정보
     * @return 수정된 사용자 정보
     */
    @Operation(summary = "사용자 수정", description = "사용자 정보를 수정합니다.")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse.Info>> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserRequest.Update request,
            @AuthenticationPrincipal UserDetails userDetails) {
        // 본인 또는 관리자만 수정 가능
        Long currentUserId = Long.parseLong(userDetails.getUsername());
        if (!id.equals(currentUserId) && !userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("C005", "접근이 거부되었습니다."));
        }

        UserResponse.Info response = userService.updateUser(id, request);
        return ResponseEntity.ok(ApiResponse.success(response, "사용자 정보가 수정되었습니다."));
    }

    /**
     * 비밀번호 변경 API
     *
     * 본인만 변경 가능
     *
     * @param id 사용자 ID
     * @param request 비밀번호 변경 요청 DTO
     * @param userDetails 현재 인증된 사용자 정보
     * @return 성공 응답
     */
    @Operation(summary = "비밀번호 변경", description = "비밀번호를 변경합니다.")
    @PutMapping("/{id}/password")
    public ResponseEntity<ApiResponse<Void>> updatePassword(
            @PathVariable Long id,
            @Valid @RequestBody UserRequest.UpdatePassword request,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long currentUserId = Long.parseLong(userDetails.getUsername());
        if (!id.equals(currentUserId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("C005", "접근이 거부되었습니다."));
        }

        userService.updatePassword(id, request);
        return ResponseEntity.ok(ApiResponse.success(null, "비밀번호가 변경되었습니다."));
    }

    /**
     * 사용자 삭제 API (Soft Delete)
     *
     * 관리자 전용 - 사용자를 논리적으로 삭제
     *
     * @param id 삭제할 사용자 ID
     * @param userDetails 현재 인증된 사용자 정보 (삭제 처리자)
     * @return 성공 응답
     */
    @Operation(summary = "사용자 삭제", description = "사용자를 삭제합니다. (Soft Delete)")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteUser(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long deletedBy = Long.parseLong(userDetails.getUsername());
        userService.deleteUser(id, deletedBy);
        return ResponseEntity.ok(ApiResponse.success(null, "사용자가 삭제되었습니다."));
    }

    /**
     * 사용자 상태 변경 API
     *
     * 관리자 전용 - 계정 활성화/정지/삭제
     *
     * @param id 사용자 ID
     * @param status 변경할 상태
     * @return 변경된 사용자 정보
     */
    @Operation(summary = "사용자 상태 변경", description = "사용자 상태를 변경합니다.")
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse.Info>> changeStatus(
            @PathVariable Long id,
            @RequestParam UserStatus status) {
        UserResponse.Info response = userService.changeUserStatus(id, status);
        return ResponseEntity.ok(ApiResponse.success(response, "사용자 상태가 변경되었습니다."));
    }

    /**
     * 사용자 역할 변경 API
     *
     * 최고 관리자 전용 - 사용자 권한 변경
     *
     * @param id 사용자 ID
     * @param role 변경할 역할
     * @return 변경된 사용자 정보
     */
    @Operation(summary = "사용자 역할 변경", description = "사용자 역할을 변경합니다.")
    @PatchMapping("/{id}/role")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse.Info>> changeRole(
            @PathVariable Long id,
            @RequestParam UserRole role) {
        UserResponse.Info response = userService.changeUserRole(id, role);
        return ResponseEntity.ok(ApiResponse.success(response, "사용자 역할이 변경되었습니다."));
    }
}
