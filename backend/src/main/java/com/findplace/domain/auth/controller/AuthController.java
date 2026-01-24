package com.findplace.domain.auth.controller;

import com.findplace.domain.auth.dto.AuthRequest;
import com.findplace.domain.auth.dto.AuthResponse;
import com.findplace.domain.auth.service.AuthService;
import com.findplace.global.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 인증 컨트롤러
 *
 * 사용자 인증 관련 REST API 엔드포인트를 제공하는 컨트롤러
 * - 로그인 (/auth/login)
 * - 회원가입 (/auth/register)
 * - 토큰 갱신 (/auth/refresh)
 *
 * 모든 엔드포인트는 인증 없이 접근 가능 (SecurityConfig에서 permitAll 설정)
 */
@Tag(name = "Auth", description = "인증 API")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * 로그인 API
     *
     * 이메일과 비밀번호로 로그인하여 JWT 토큰을 발급받음
     *
     * @param request 로그인 요청 DTO (이메일, 비밀번호)
     * @return JWT 토큰 (Access Token, Refresh Token)
     */
    @Operation(summary = "로그인", description = "이메일과 비밀번호로 로그인합니다.")
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse.Token>> login(
            @Valid @RequestBody AuthRequest.Login request) {
        AuthResponse.Token response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success(response, "로그인에 성공했습니다."));
    }

    /**
     * 회원가입 API
     *
     * 새로운 사용자를 등록하고 JWT 토큰을 발급받음
     *
     * @param request 회원가입 요청 DTO (이메일, 비밀번호, 이름, 전화번호)
     * @return JWT 토큰 (Access Token, Refresh Token)
     */
    @Operation(summary = "회원가입", description = "새로운 사용자를 등록합니다.")
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse.Token>> register(
            @Valid @RequestBody AuthRequest.Register request) {
        AuthResponse.Token response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "회원가입에 성공했습니다."));
    }

    /**
     * 토큰 갱신 API
     *
     * Refresh Token으로 새로운 Access Token을 발급받음
     *
     * @param request 토큰 갱신 요청 DTO (Refresh Token)
     * @return 새로운 JWT 토큰 (Access Token, Refresh Token)
     */
    @Operation(summary = "토큰 갱신", description = "Refresh Token으로 새로운 Access Token을 발급받습니다.")
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse.Token>> refresh(
            @Valid @RequestBody AuthRequest.RefreshToken request) {
        AuthResponse.Token response = authService.refresh(request);
        return ResponseEntity.ok(ApiResponse.success(response, "토큰이 갱신되었습니다."));
    }
}
