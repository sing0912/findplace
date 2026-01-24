package com.findplace.domain.auth.service;

import com.findplace.domain.auth.dto.AuthRequest;
import com.findplace.domain.auth.dto.AuthResponse;
import com.findplace.domain.user.entity.User;
import com.findplace.domain.user.entity.UserRole;
import com.findplace.domain.user.entity.UserStatus;
import com.findplace.domain.user.repository.UserRepository;
import com.findplace.global.exception.BusinessException;
import com.findplace.global.exception.ErrorCode;
import com.findplace.global.security.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 인증 서비스 클래스
 *
 * 사용자 인증 관련 비즈니스 로직을 처리하는 서비스
 * - 로그인: 이메일/비밀번호 검증 후 JWT 토큰 발급
 * - 회원가입: 사용자 등록 후 JWT 토큰 발급
 * - 토큰 갱신: Refresh Token으로 새로운 Access Token 발급
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    /** Access Token 만료 시간 (초) */
    @Value("${app.jwt.access-expiration}")
    private long accessTokenExpiration;

    /**
     * 로그인 처리
     *
     * 이메일과 비밀번호를 검증하고 JWT 토큰을 발급
     *
     * @param request 로그인 요청 DTO
     * @return JWT 토큰 응답
     * @throws BusinessException 인증 실패 또는 계정 비활성화 시
     */
    @Transactional
    public AuthResponse.Token login(AuthRequest.Login request) {
        // 이메일로 사용자 조회
        User user = userRepository.findByEmailAndDeletedAtIsNull(request.getEmail())
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_CREDENTIALS));

        // 비밀번호 검증
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }

        // 계정 활성화 상태 확인
        if (!user.isActive()) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED, "계정이 비활성화되었습니다.");
        }

        // 마지막 로그인 시간 갱신
        user.updateLastLogin();

        // JWT 토큰 생성
        String accessToken = jwtTokenProvider.createAccessToken(
                user.getId(),
                user.getEmail(),
                List.of(user.getRole().getAuthority())
        );
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getId());

        return AuthResponse.Token.of(accessToken, refreshToken, accessTokenExpiration);
    }

    /**
     * 회원가입 처리
     *
     * 새로운 사용자를 등록하고 JWT 토큰을 발급
     *
     * @param request 회원가입 요청 DTO
     * @return JWT 토큰 응답
     * @throws BusinessException 이메일 중복 시
     */
    @Transactional
    public AuthResponse.Token register(AuthRequest.Register request) {
        // 이메일 중복 확인
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException(ErrorCode.DUPLICATE_EMAIL);
        }

        // 사용자 생성
        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .name(request.getName())
                .phone(request.getPhone())
                .role(UserRole.USER)
                .status(UserStatus.ACTIVE)
                .build();

        User savedUser = userRepository.save(user);

        // JWT 토큰 생성
        String accessToken = jwtTokenProvider.createAccessToken(
                savedUser.getId(),
                savedUser.getEmail(),
                List.of(savedUser.getRole().getAuthority())
        );
        String refreshToken = jwtTokenProvider.createRefreshToken(savedUser.getId());

        return AuthResponse.Token.of(accessToken, refreshToken, accessTokenExpiration);
    }

    /**
     * 토큰 갱신 처리
     *
     * Refresh Token을 검증하고 새로운 Access Token을 발급
     *
     * @param request 토큰 갱신 요청 DTO
     * @return 새로운 JWT 토큰 응답
     * @throws BusinessException 토큰이 유효하지 않거나 사용자가 없을 때
     */
    @Transactional(readOnly = true)
    public AuthResponse.Token refresh(AuthRequest.RefreshToken request) {
        // Refresh Token 검증
        if (!jwtTokenProvider.validateToken(request.getRefreshToken())) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }

        // 토큰에서 사용자 ID 추출
        Long userId = jwtTokenProvider.getUserId(request.getRefreshToken());
        User user = userRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 계정 활성화 상태 확인
        if (!user.isActive()) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED, "계정이 비활성화되었습니다.");
        }

        // 새로운 JWT 토큰 생성
        String accessToken = jwtTokenProvider.createAccessToken(
                user.getId(),
                user.getEmail(),
                List.of(user.getRole().getAuthority())
        );
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getId());

        return AuthResponse.Token.of(accessToken, refreshToken, accessTokenExpiration);
    }
}
