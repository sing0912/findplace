package com.petpro.domain.auth.service;

import com.petpro.domain.auth.dto.AuthRequest;
import com.petpro.domain.auth.dto.AuthResponse;
import com.petpro.domain.auth.entity.AuthProvider;
import com.petpro.domain.auth.entity.VerificationRequest;
import com.petpro.domain.auth.entity.VerificationType;
import com.petpro.domain.auth.repository.VerificationRequestRepository;
import com.petpro.domain.user.entity.User;
import com.petpro.domain.user.entity.UserRole;
import com.petpro.domain.user.entity.UserStatus;
import com.petpro.domain.user.repository.UserRepository;
import com.petpro.global.config.datasource.DataSourceContextHolder;
import com.petpro.global.config.datasource.DataSourceType;
import com.petpro.global.exception.BusinessException;
import com.petpro.global.exception.ErrorCode;
import com.petpro.global.security.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Random;

/**
 * 인증 서비스 클래스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final VerificationRequestRepository verificationRequestRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final TransactionTemplate transactionTemplate;
    private final OAuthService oAuthService;
    private final SmsService smsService;

    @Value("${app.jwt.access-expiration}")
    private long accessTokenExpiration;

    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_DATE_TIME;

    // ==================== 회원가입 ====================

    /**
     * 회원가입 처리
     */
    @Transactional
    public AuthResponse.RegisterResult register(AuthRequest.Register request) {
        // 필수 약관 동의 확인
        if (!request.isAgreeTerms() || !request.isAgreePrivacy()) {
            throw new BusinessException(ErrorCode.TERMS_NOT_AGREED);
        }

        // 이메일 중복 확인
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException(ErrorCode.DUPLICATE_EMAIL);
        }

        // 닉네임 중복 확인
        if (userRepository.existsByNickname(request.getNickname())) {
            throw new BusinessException(ErrorCode.DUPLICATE_NICKNAME);
        }

        // 비밀번호 강도 확인
        if (!isValidPassword(request.getPassword())) {
            throw new BusinessException(ErrorCode.WEAK_PASSWORD);
        }

        // 사용자 생성
        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .name(request.getName())
                .nickname(request.getNickname())
                .phone(request.getPhone())
                .provider(AuthProvider.EMAIL)
                .agreeTerms(request.isAgreeTerms())
                .agreePrivacy(request.isAgreePrivacy())
                .agreeMarketing(request.isAgreeMarketing())
                .role(UserRole.CUSTOMER)
                .status(UserStatus.ACTIVE)
                .build();

        User savedUser = userRepository.save(user);

        return AuthResponse.RegisterResult.builder()
                .id(savedUser.getId())
                .email(savedUser.getEmail())
                .nickname(savedUser.getNickname())
                .createdAt(savedUser.getCreatedAt().format(ISO_FORMATTER))
                .build();
    }

    // ==================== 로그인 ====================

    /**
     * 로그인 처리
     */
    @Transactional
    public AuthResponse.Token login(AuthRequest.Login request) {
        User user = userRepository.findByEmailAndDeletedAtIsNull(request.getEmail())
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_CREDENTIALS));

        // 계정 잠금 확인 (5회 실패 시 30분 잠금)
        if (user.isLocked()) {
            throw new BusinessException(ErrorCode.ACCOUNT_LOCKED);
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            user.recordLoginFailure();
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }

        if (!user.isActive()) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED, "계정이 비활성화되었습니다.");
        }

        // 로그인 성공 → 실패 횟수 초기화
        user.resetLoginFailure();
        user.updateLastLogin();

        String accessToken = jwtTokenProvider.createAccessToken(
                user.getId(), user.getEmail(), List.of(user.getRole().getAuthority()));
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getId());

        // Refresh Token 로테이션: 발급된 토큰을 DB에 저장
        user.updateRefreshToken(refreshToken);

        return AuthResponse.Token.of(accessToken, refreshToken, accessTokenExpiration);
    }

    /**
     * 토큰 갱신 (Refresh Token Rotation)
     * 1회용: 사용된 Refresh Token은 무효화하고 새 토큰 발급
     */
    @Transactional
    public AuthResponse.Token refresh(AuthRequest.RefreshToken request) {
        if (!jwtTokenProvider.validateToken(request.getRefreshToken())) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }

        Long userId = jwtTokenProvider.getUserId(request.getRefreshToken());
        User user = userRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (!user.isActive()) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED, "계정이 비활성화되었습니다.");
        }

        // Refresh Token 로테이션: DB에 저장된 토큰과 비교 (1회용)
        if (user.getRefreshToken() == null || !user.getRefreshToken().equals(request.getRefreshToken())) {
            // 이미 사용되었거나 탈취 의심 → 모든 토큰 무효화
            user.invalidateRefreshToken();
            throw new BusinessException(ErrorCode.INVALID_TOKEN, "이미 사용된 Refresh Token입니다.");
        }

        String accessToken = jwtTokenProvider.createAccessToken(
                user.getId(), user.getEmail(), List.of(user.getRole().getAuthority()));
        String newRefreshToken = jwtTokenProvider.createRefreshToken(user.getId());

        // 새 Refresh Token으로 교체 (이전 토큰 무효화)
        user.updateRefreshToken(newRefreshToken);

        return AuthResponse.Token.of(accessToken, newRefreshToken, accessTokenExpiration);
    }

    // ==================== 중복 확인 ====================

    /**
     * 이메일 중복 확인
     */
    @Transactional(readOnly = true)
    public AuthResponse.AvailabilityCheck checkEmail(String email) {
        if (userRepository.existsByEmail(email)) {
            return AuthResponse.AvailabilityCheck.unavailable("이미 사용 중인 이메일입니다.");
        }
        return AuthResponse.AvailabilityCheck.available();
    }

    /**
     * 닉네임 중복 확인
     */
    @Transactional(readOnly = true)
    public AuthResponse.AvailabilityCheck checkNickname(String nickname) {
        if (userRepository.existsByNickname(nickname)) {
            return AuthResponse.AvailabilityCheck.unavailable("이미 사용 중인 닉네임입니다.");
        }
        return AuthResponse.AvailabilityCheck.available();
    }

    // ==================== 아이디 찾기 ====================

    /**
     * 아이디 찾기 - 인증 요청
     */
    @Transactional
    public AuthResponse.VerificationRequestResult findIdRequest(AuthRequest.FindIdRequest request) {
        User user = userRepository.findByNameAndPhoneAndDeletedAtIsNull(request.getName(), request.getPhone())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND, "일치하는 계정을 찾을 수 없습니다."));

        String code = generateVerificationCode();
        VerificationRequest verification = VerificationRequest.builder()
                .type(VerificationType.FIND_ID)
                .phone(request.getPhone())
                .code(code)
                .userId(user.getId())
                .name(request.getName())
                .build();
        VerificationRequest saved = verificationRequestRepository.save(verification);

        smsService.sendVerificationCode(request.getPhone(), code);

        return AuthResponse.VerificationRequestResult.builder()
                .requestId(saved.getId())
                .expireAt(saved.getExpiresAt().format(ISO_FORMATTER))
                .build();
    }

    /**
     * 아이디 찾기 - 인증 확인
     */
    @Transactional
    public AuthResponse.FindIdResult findIdVerify(AuthRequest.VerifyRequest request) {
        VerificationRequest verification = verificationRequestRepository.findById(request.getRequestId())
                .orElseThrow(() -> new BusinessException(ErrorCode.VERIFICATION_NOT_FOUND));

        if (verification.getType() != VerificationType.FIND_ID) {
            throw new BusinessException(ErrorCode.VERIFICATION_NOT_FOUND);
        }

        if (verification.isExpired()) {
            throw new BusinessException(ErrorCode.VERIFICATION_EXPIRED, "인증번호가 만료되었습니다.");
        }

        if (!verification.matchCode(request.getCode())) {
            throw new BusinessException(ErrorCode.VERIFICATION_CODE_MISMATCH, "인증번호가 일치하지 않습니다.");
        }

        verification.verify();

        User user = userRepository.findByIdAndDeletedAtIsNull(verification.getUserId())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        return AuthResponse.FindIdResult.of(user.getEmail(), user.getProvider());
    }

    /**
     * 아이디 찾기 - 재전송
     */
    @Transactional
    public AuthResponse.VerificationRequestResult findIdResend(AuthRequest.ResendRequest request) {
        VerificationRequest verification = verificationRequestRepository.findById(request.getRequestId())
                .orElseThrow(() -> new BusinessException(ErrorCode.VERIFICATION_NOT_FOUND));

        if (verification.getType() != VerificationType.FIND_ID) {
            throw new BusinessException(ErrorCode.VERIFICATION_NOT_FOUND);
        }

        String newCode = generateVerificationCode();
        verification.resend(newCode);

        smsService.sendVerificationCode(verification.getPhone(), newCode);

        return AuthResponse.VerificationRequestResult.builder()
                .requestId(verification.getId())
                .expireAt(verification.getExpiresAt().format(ISO_FORMATTER))
                .build();
    }

    // ==================== 비밀번호 재설정 ====================

    /**
     * 비밀번호 재설정 - 인증 요청
     */
    @Transactional
    public AuthResponse.VerificationRequestResult resetPasswordRequest(AuthRequest.ResetPasswordRequest request) {
        User user = userRepository.findByEmailAndPhoneAndDeletedAtIsNull(request.getEmail(), request.getPhone())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND, "일치하는 계정을 찾을 수 없습니다."));

        if (user.isSocialUser()) {
            throw new BusinessException(ErrorCode.SOCIAL_LOGIN_USER);
        }

        String code = generateVerificationCode();
        VerificationRequest verification = VerificationRequest.builder()
                .type(VerificationType.RESET_PASSWORD)
                .phone(request.getPhone())
                .code(code)
                .userId(user.getId())
                .email(request.getEmail())
                .build();
        VerificationRequest saved = verificationRequestRepository.save(verification);

        smsService.sendVerificationCode(request.getPhone(), code);

        return AuthResponse.VerificationRequestResult.builder()
                .requestId(saved.getId())
                .expireAt(saved.getExpiresAt().format(ISO_FORMATTER))
                .build();
    }

    /**
     * 비밀번호 재설정 - 인증 확인
     */
    @Transactional
    public AuthResponse.PasswordResetToken resetPasswordVerify(AuthRequest.VerifyRequest request) {
        VerificationRequest verification = verificationRequestRepository.findById(request.getRequestId())
                .orElseThrow(() -> new BusinessException(ErrorCode.VERIFICATION_NOT_FOUND));

        if (verification.getType() != VerificationType.RESET_PASSWORD) {
            throw new BusinessException(ErrorCode.VERIFICATION_NOT_FOUND);
        }

        if (verification.isExpired()) {
            throw new BusinessException(ErrorCode.VERIFICATION_EXPIRED, "인증번호가 만료되었습니다.");
        }

        if (!verification.matchCode(request.getCode())) {
            throw new BusinessException(ErrorCode.VERIFICATION_CODE_MISMATCH, "인증번호가 일치하지 않습니다.");
        }

        verification.verify();
        verification.issueResetToken();

        return AuthResponse.PasswordResetToken.builder()
                .token(verification.getResetToken())
                .build();
    }

    /**
     * 비밀번호 재설정 - 변경
     */
    @Transactional
    public AuthResponse.SuccessMessage resetPasswordConfirm(AuthRequest.ResetPasswordConfirm request) {
        VerificationRequest verification = verificationRequestRepository.findByResetTokenAndVerifiedTrue(request.getToken())
                .orElseThrow(() -> new BusinessException(ErrorCode.VERIFICATION_REQUIRED));

        if (!isValidPassword(request.getNewPassword())) {
            throw new BusinessException(ErrorCode.WEAK_PASSWORD);
        }

        User user = userRepository.findByIdAndDeletedAtIsNull(verification.getUserId())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        user.updatePassword(passwordEncoder.encode(request.getNewPassword()));

        // 사용된 토큰 무효화 (삭제)
        verificationRequestRepository.delete(verification);

        return AuthResponse.SuccessMessage.of("비밀번호가 변경되었습니다.");
    }

    // ==================== 소셜 로그인 ====================

    /**
     * 소셜 로그인 처리
     * Master DB를 명시적으로 사용하여 복제 지연 문제 방지
     */
    public AuthResponse.OAuthResult oauthLogin(String provider, AuthRequest.OAuthLogin request) {
        // Master DB 사용 강제 (Slave 복제 지연 문제 방지)
        DataSourceContextHolder.setDataSourceType(DataSourceType.MASTER);
        try {
            AuthProvider authProvider;
            try {
                authProvider = AuthProvider.valueOf(provider.toUpperCase());
            } catch (IllegalArgumentException e) {
                log.warn("[OAuth 로그인] 지원하지 않는 provider: {}", provider);
                throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "지원하지 않는 OAuth 제공자입니다: " + provider);
            }

            // OAuth 서버와 통신하여 실제 사용자 정보 조회
            OAuthService.OAuthUserInfo oAuthUserInfo = oAuthService.getOAuthUserInfo(authProvider, request.getCode());

            String providerId = oAuthUserInfo.getProviderId();
            String email = oAuthUserInfo.getEmail();
            String name = oAuthUserInfo.getName();
            String profileImageUrl = oAuthUserInfo.getProfileImageUrl();

            // 닉네임: OAuth에서 받은 이름 사용, 없으면 provider_providerId 형식
            String nickname = name != null ? name : provider + "_" + (providerId != null ? providerId.substring(0, Math.min(providerId.length(), 8)) : "unknown");

            log.info("[OAuth 로그인] provider={}, email={}, name={}", provider, email, name);

            boolean isNewUser = false;
            User user = findOrCreateOAuthUser(authProvider, providerId, email, nickname, profileImageUrl);

            if (user.getCreatedAt() != null && user.getCreatedAt().equals(user.getUpdatedAt())
                    && user.getLastLoginAt() == null) {
                isNewUser = true;
            }

            // 마지막 로그인 시간 업데이트 (별도 트랜잭션)
            updateLastLoginTime(user.getId());

            String accessToken = jwtTokenProvider.createAccessToken(
                    user.getId(), user.getEmail(), List.of(user.getRole().getAuthority()));
            String refreshToken = jwtTokenProvider.createRefreshToken(user.getId());

            // Refresh Token 로테이션: 발급된 토큰을 DB에 저장
            updateRefreshTokenForUser(user.getId(), refreshToken);

            return AuthResponse.OAuthResult.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .user(AuthResponse.UserInfo.builder()
                            .id(user.getId())
                            .email(user.getEmail())
                            .nickname(user.getNickname())
                            .profileImageUrl(user.getProfileImageUrl())
                            .build())
                    .isNewUser(isNewUser)
                    .build();
        } finally {
            DataSourceContextHolder.clearDataSourceType();
        }
    }

    /**
     * 마지막 로그인 시간 업데이트
     */
    private void updateLastLoginTime(Long userId) {
        transactionTemplate.executeWithoutResult(status -> {
            userRepository.findById(userId).ifPresent(User::updateLastLogin);
        });
    }

    /**
     * OAuth 로그인 시 Refresh Token 저장 (별도 트랜잭션)
     */
    private void updateRefreshTokenForUser(Long userId, String refreshToken) {
        transactionTemplate.executeWithoutResult(status -> {
            userRepository.findById(userId).ifPresent(u -> u.updateRefreshToken(refreshToken));
        });
    }

    /**
     * OAuth 사용자 조회 또는 생성 (동시성 문제 처리)
     */
    private User findOrCreateOAuthUser(AuthProvider authProvider, String providerId, String email, String nickname, String profileImageUrl) {
        // 1. email로 기존 사용자 검색 (email이 unique이므로 먼저 검색)
        User user = userRepository.findByEmail(email).orElse(null);

        if (user != null) {
            // 기존 이메일 사용자가 있으면 provider 정보 업데이트 (트랜잭션 내에서)
            return transactionTemplate.execute(status -> {
                User existingUser = userRepository.findByEmail(email).orElse(null);
                if (existingUser != null) {
                    existingUser.updateOAuthInfo(authProvider, providerId);
                    // 프로필 이미지가 없으면 OAuth에서 받은 것으로 업데이트
                    if (existingUser.getProfileImageUrl() == null && profileImageUrl != null) {
                        existingUser.updateProfileImage(profileImageUrl);
                    }
                    return existingUser;
                }
                return null;
            });
        }

        // 2. provider + providerId로 기존 사용자 검색
        user = userRepository.findByProviderAndProviderId(authProvider, providerId).orElse(null);

        if (user != null) {
            return user;
        }

        // 3. 둘 다 없으면: 신규 사용자 생성 (별도 트랜잭션으로 처리하여 동시성 문제 대응)
        return createOAuthUserWithRetry(authProvider, providerId, email, nickname, profileImageUrl);
    }

    /**
     * OAuth 사용자 생성 (동시성 문제 시 재조회)
     */
    private User createOAuthUserWithRetry(AuthProvider authProvider, String providerId, String email, String nickname, String profileImageUrl) {
        try {
            return transactionTemplate.execute(status -> {
                User newUser = User.builder()
                        .email(email)
                        .password(passwordEncoder.encode(java.util.UUID.randomUUID().toString()))
                        .name(nickname)
                        .nickname(nickname)
                        .profileImageUrl(profileImageUrl)
                        .provider(authProvider)
                        .providerId(providerId)
                        .agreeTerms(true)
                        .agreePrivacy(true)
                        .role(UserRole.CUSTOMER)
                        .status(UserStatus.ACTIVE)
                        .build();
                return userRepository.saveAndFlush(newUser);
            });
        } catch (DataIntegrityViolationException e) {
            // 동시성 문제로 중복 생성 시도된 경우 - 다시 조회
            log.warn("[OAuth] 동시 요청으로 인한 중복 생성 시도, 재조회 수행: {}", email);
            return userRepository.findByEmail(email)
                    .orElseGet(() -> userRepository.findByProviderAndProviderId(authProvider, providerId)
                            .orElseThrow(() -> new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR,
                                    "OAuth 사용자 생성 실패")));
        }
    }

    // ==================== 유틸리티 ====================

    private boolean isValidPassword(String password) {
        if (password == null || password.length() < 8) {
            return false;
        }
        boolean hasLetter = password.matches(".*[a-zA-Z].*");
        boolean hasDigit = password.matches(".*\\d.*");
        boolean hasSpecial = password.matches(".*[@$!%*#?&].*");
        return hasLetter && hasDigit && hasSpecial;
    }

    private String generateVerificationCode() {
        Random random = new Random();
        int code = 100000 + random.nextInt(900000);
        return String.valueOf(code);
    }
}
