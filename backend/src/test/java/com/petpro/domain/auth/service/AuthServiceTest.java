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
import com.petpro.global.exception.BusinessException;
import com.petpro.global.exception.ErrorCode;
import com.petpro.global.security.jwt.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService 테스트")
class AuthServiceTest {

    @InjectMocks
    private AuthService authService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private VerificationRequestRepository verificationRequestRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private TransactionTemplate transactionTemplate;

    @Mock
    private OAuthService oAuthService;

    @Mock
    private SmsService smsService;

    private User testUser;

    @SuppressWarnings("unchecked")
    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(authService, "accessTokenExpiration", 3600L);

        // TransactionTemplate mock: 콜백을 즉시 실행
        lenient().when(transactionTemplate.execute(any(TransactionCallback.class))).thenAnswer(invocation -> {
            TransactionCallback<Object> callback = invocation.getArgument(0);
            return callback.doInTransaction(null);
        });

        testUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .password("encodedPassword")
                .name("테스트사용자")
                .nickname("테스트닉네임")
                .phone("01012345678")
                .provider(AuthProvider.EMAIL)
                .role(UserRole.CUSTOMER)
                .status(UserStatus.ACTIVE)
                .agreeTerms(true)
                .agreePrivacy(true)
                .build();
    }

    // ==================== 회원가입 테스트 ====================

    @Nested
    @DisplayName("회원가입")
    class Register {

        @Test
        @DisplayName("성공: 새로운 사용자 회원가입")
        void register_Success() {
            // given
            AuthRequest.Register request = AuthRequest.Register.builder()
                    .email("new@example.com")
                    .password("password123!")
                    .name("새사용자")
                    .nickname("새닉네임")
                    .phone("01098765432")
                    .agreeTerms(true)
                    .agreePrivacy(true)
                    .agreeMarketing(false)
                    .build();

            given(userRepository.existsByEmail(anyString())).willReturn(false);
            given(userRepository.existsByNickname(anyString())).willReturn(false);
            given(passwordEncoder.encode(anyString())).willReturn("encodedPassword");
            given(userRepository.save(any(User.class))).willAnswer(invocation -> {
                User user = invocation.getArgument(0);
                ReflectionTestUtils.setField(user, "id", 1L);
                ReflectionTestUtils.setField(user, "createdAt", LocalDateTime.now());
                return user;
            });

            // when
            AuthResponse.RegisterResult response = authService.register(request);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getEmail()).isEqualTo("new@example.com");
            assertThat(response.getNickname()).isEqualTo("새닉네임");
            verify(userRepository).save(any(User.class));
        }

        @Test
        @DisplayName("실패: 필수 약관 미동의")
        void register_TermsNotAgreed() {
            // given
            AuthRequest.Register request = AuthRequest.Register.builder()
                    .email("new@example.com")
                    .password("password123")
                    .name("새사용자")
                    .nickname("새닉네임")
                    .phone("01098765432")
                    .agreeTerms(false)
                    .agreePrivacy(true)
                    .build();

            // when & then
            assertThatThrownBy(() -> authService.register(request))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.TERMS_NOT_AGREED);
        }

        @Test
        @DisplayName("실패: 이메일 중복")
        void register_DuplicateEmail() {
            // given
            AuthRequest.Register request = AuthRequest.Register.builder()
                    .email("test@example.com")
                    .password("password123")
                    .name("새사용자")
                    .nickname("새닉네임")
                    .phone("01098765432")
                    .agreeTerms(true)
                    .agreePrivacy(true)
                    .build();

            given(userRepository.existsByEmail(anyString())).willReturn(true);

            // when & then
            assertThatThrownBy(() -> authService.register(request))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.DUPLICATE_EMAIL);
        }

        @Test
        @DisplayName("실패: 닉네임 중복")
        void register_DuplicateNickname() {
            // given
            AuthRequest.Register request = AuthRequest.Register.builder()
                    .email("new@example.com")
                    .password("password123")
                    .name("새사용자")
                    .nickname("테스트닉네임")
                    .phone("01098765432")
                    .agreeTerms(true)
                    .agreePrivacy(true)
                    .build();

            given(userRepository.existsByEmail(anyString())).willReturn(false);
            given(userRepository.existsByNickname(anyString())).willReturn(true);

            // when & then
            assertThatThrownBy(() -> authService.register(request))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.DUPLICATE_NICKNAME);
        }

        @Test
        @DisplayName("실패: 취약한 비밀번호")
        void register_WeakPassword() {
            // given
            AuthRequest.Register request = AuthRequest.Register.builder()
                    .email("new@example.com")
                    .password("weak")
                    .name("새사용자")
                    .nickname("새닉네임")
                    .phone("01098765432")
                    .agreeTerms(true)
                    .agreePrivacy(true)
                    .build();

            given(userRepository.existsByEmail(anyString())).willReturn(false);
            given(userRepository.existsByNickname(anyString())).willReturn(false);

            // when & then
            assertThatThrownBy(() -> authService.register(request))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.WEAK_PASSWORD);
        }
    }

    // ==================== 로그인 테스트 ====================

    @Nested
    @DisplayName("로그인")
    class Login {

        @Test
        @DisplayName("성공: 이메일/비밀번호 로그인")
        void login_Success() {
            // given
            AuthRequest.Login request = AuthRequest.Login.builder()
                    .email("test@example.com")
                    .password("password123")
                    .build();

            given(userRepository.findByEmailAndDeletedAtIsNull(anyString())).willReturn(Optional.of(testUser));
            given(passwordEncoder.matches(anyString(), anyString())).willReturn(true);
            given(jwtTokenProvider.createAccessToken(anyLong(), anyString(), anyList())).willReturn("accessToken");
            given(jwtTokenProvider.createRefreshToken(anyLong())).willReturn("refreshToken");

            // when
            AuthResponse.Token response = authService.login(request);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getAccessToken()).isEqualTo("accessToken");
            assertThat(response.getRefreshToken()).isEqualTo("refreshToken");
        }

        @Test
        @DisplayName("실패: 존재하지 않는 이메일")
        void login_UserNotFound() {
            // given
            AuthRequest.Login request = AuthRequest.Login.builder()
                    .email("notexist@example.com")
                    .password("password123")
                    .build();

            given(userRepository.findByEmailAndDeletedAtIsNull(anyString())).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> authService.login(request))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_CREDENTIALS);
        }

        @Test
        @DisplayName("실패: 비밀번호 불일치")
        void login_WrongPassword() {
            // given
            AuthRequest.Login request = AuthRequest.Login.builder()
                    .email("test@example.com")
                    .password("wrongPassword")
                    .build();

            given(userRepository.findByEmailAndDeletedAtIsNull(anyString())).willReturn(Optional.of(testUser));
            given(passwordEncoder.matches(anyString(), anyString())).willReturn(false);

            // when & then
            assertThatThrownBy(() -> authService.login(request))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_CREDENTIALS);
        }

        @Test
        @DisplayName("실패: 비활성 계정")
        void login_InactiveAccount() {
            // given
            testUser.suspend();
            AuthRequest.Login request = AuthRequest.Login.builder()
                    .email("test@example.com")
                    .password("password123")
                    .build();

            given(userRepository.findByEmailAndDeletedAtIsNull(anyString())).willReturn(Optional.of(testUser));
            given(passwordEncoder.matches(anyString(), anyString())).willReturn(true);

            // when & then
            assertThatThrownBy(() -> authService.login(request))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ACCESS_DENIED);
        }
    }

    // ==================== 토큰 갱신 테스트 ====================

    @Nested
    @DisplayName("토큰 갱신")
    class Refresh {

        @Test
        @DisplayName("성공: 토큰 갱신")
        void refresh_Success() {
            // given
            testUser.updateRefreshToken("validRefreshToken");
            AuthRequest.RefreshToken request = AuthRequest.RefreshToken.builder()
                    .refreshToken("validRefreshToken")
                    .build();

            given(jwtTokenProvider.validateToken(anyString())).willReturn(true);
            given(jwtTokenProvider.getUserId(anyString())).willReturn(1L);
            given(userRepository.findByIdAndDeletedAtIsNull(anyLong())).willReturn(Optional.of(testUser));
            given(jwtTokenProvider.createAccessToken(anyLong(), anyString(), anyList())).willReturn("newAccessToken");
            given(jwtTokenProvider.createRefreshToken(anyLong())).willReturn("newRefreshToken");

            // when
            AuthResponse.Token response = authService.refresh(request);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getAccessToken()).isEqualTo("newAccessToken");
        }

        @Test
        @DisplayName("실패: 유효하지 않은 토큰")
        void refresh_InvalidToken() {
            // given
            AuthRequest.RefreshToken request = AuthRequest.RefreshToken.builder()
                    .refreshToken("invalidToken")
                    .build();

            given(jwtTokenProvider.validateToken(anyString())).willReturn(false);

            // when & then
            assertThatThrownBy(() -> authService.refresh(request))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_TOKEN);
        }
    }

    // ==================== 중복 확인 테스트 ====================

    @Nested
    @DisplayName("중복 확인")
    class CheckAvailability {

        @Test
        @DisplayName("성공: 이메일 사용 가능")
        void checkEmail_Available() {
            // given
            given(userRepository.existsByEmail(anyString())).willReturn(false);

            // when
            AuthResponse.AvailabilityCheck response = authService.checkEmail("new@example.com");

            // then
            assertThat(response.isAvailable()).isTrue();
        }

        @Test
        @DisplayName("성공: 이메일 사용 불가")
        void checkEmail_Unavailable() {
            // given
            given(userRepository.existsByEmail(anyString())).willReturn(true);

            // when
            AuthResponse.AvailabilityCheck response = authService.checkEmail("test@example.com");

            // then
            assertThat(response.isAvailable()).isFalse();
        }

        @Test
        @DisplayName("성공: 닉네임 사용 가능")
        void checkNickname_Available() {
            // given
            given(userRepository.existsByNickname(anyString())).willReturn(false);

            // when
            AuthResponse.AvailabilityCheck response = authService.checkNickname("새닉네임");

            // then
            assertThat(response.isAvailable()).isTrue();
        }

        @Test
        @DisplayName("성공: 닉네임 사용 불가")
        void checkNickname_Unavailable() {
            // given
            given(userRepository.existsByNickname(anyString())).willReturn(true);

            // when
            AuthResponse.AvailabilityCheck response = authService.checkNickname("테스트닉네임");

            // then
            assertThat(response.isAvailable()).isFalse();
        }
    }

    // ==================== 아이디 찾기 테스트 ====================

    @Nested
    @DisplayName("아이디 찾기")
    class FindId {

        @Test
        @DisplayName("성공: 아이디 찾기 인증 요청")
        void findIdRequest_Success() {
            // given
            AuthRequest.FindIdRequest request = AuthRequest.FindIdRequest.builder()
                    .name("테스트사용자")
                    .phone("01012345678")
                    .build();

            VerificationRequest savedVerification = VerificationRequest.builder()
                    .id("test-uuid")
                    .type(VerificationType.FIND_ID)
                    .phone("01012345678")
                    .code("123456")
                    .userId(1L)
                    .expiresAt(LocalDateTime.now().plusMinutes(3))
                    .build();

            given(userRepository.findByNameAndPhoneAndDeletedAtIsNull(anyString(), anyString()))
                    .willReturn(Optional.of(testUser));
            given(verificationRequestRepository.save(any(VerificationRequest.class)))
                    .willReturn(savedVerification);

            // when
            AuthResponse.VerificationRequestResult response = authService.findIdRequest(request);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getRequestId()).isNotNull();
        }

        @Test
        @DisplayName("실패: 일치하는 계정 없음")
        void findIdRequest_UserNotFound() {
            // given
            AuthRequest.FindIdRequest request = AuthRequest.FindIdRequest.builder()
                    .name("없는사용자")
                    .phone("01099999999")
                    .build();

            given(userRepository.findByNameAndPhoneAndDeletedAtIsNull(anyString(), anyString()))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> authService.findIdRequest(request))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_NOT_FOUND);
        }

        @Test
        @DisplayName("성공: 아이디 찾기 인증 확인")
        void findIdVerify_Success() {
            // given
            VerificationRequest verification = VerificationRequest.builder()
                    .id("test-uuid")
                    .type(VerificationType.FIND_ID)
                    .phone("01012345678")
                    .code("123456")
                    .userId(1L)
                    .expiresAt(LocalDateTime.now().plusMinutes(3))
                    .build();

            AuthRequest.VerifyRequest request = AuthRequest.VerifyRequest.builder()
                    .requestId("test-uuid")
                    .code("123456")
                    .build();

            given(verificationRequestRepository.findById(anyString())).willReturn(Optional.of(verification));
            given(userRepository.findByIdAndDeletedAtIsNull(anyLong())).willReturn(Optional.of(testUser));

            // when
            AuthResponse.FindIdResult response = authService.findIdVerify(request);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getEmail()).contains("***"); // 마스킹된 이메일
        }

        @Test
        @DisplayName("실패: 인증번호 만료")
        void findIdVerify_Expired() {
            // given
            VerificationRequest verification = VerificationRequest.builder()
                    .id("test-uuid")
                    .type(VerificationType.FIND_ID)
                    .phone("01012345678")
                    .code("123456")
                    .userId(1L)
                    .expiresAt(LocalDateTime.now().minusMinutes(1))
                    .build();

            AuthRequest.VerifyRequest request = AuthRequest.VerifyRequest.builder()
                    .requestId("test-uuid")
                    .code("123456")
                    .build();

            given(verificationRequestRepository.findById(anyString())).willReturn(Optional.of(verification));

            // when & then
            assertThatThrownBy(() -> authService.findIdVerify(request))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.VERIFICATION_EXPIRED);
        }

        @Test
        @DisplayName("실패: 인증번호 불일치")
        void findIdVerify_CodeMismatch() {
            // given
            VerificationRequest verification = VerificationRequest.builder()
                    .id("test-uuid")
                    .type(VerificationType.FIND_ID)
                    .phone("01012345678")
                    .code("123456")
                    .userId(1L)
                    .expiresAt(LocalDateTime.now().plusMinutes(3))
                    .build();

            AuthRequest.VerifyRequest request = AuthRequest.VerifyRequest.builder()
                    .requestId("test-uuid")
                    .code("999999")
                    .build();

            given(verificationRequestRepository.findById(anyString())).willReturn(Optional.of(verification));

            // when & then
            assertThatThrownBy(() -> authService.findIdVerify(request))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.VERIFICATION_CODE_MISMATCH);
        }
    }

    // ==================== 비밀번호 재설정 테스트 ====================

    @Nested
    @DisplayName("비밀번호 재설정")
    class ResetPassword {

        @Test
        @DisplayName("성공: 비밀번호 재설정 인증 요청")
        void resetPasswordRequest_Success() {
            // given
            AuthRequest.ResetPasswordRequest request = AuthRequest.ResetPasswordRequest.builder()
                    .email("test@example.com")
                    .phone("01012345678")
                    .build();

            VerificationRequest savedVerification = VerificationRequest.builder()
                    .id("test-uuid")
                    .type(VerificationType.RESET_PASSWORD)
                    .phone("01012345678")
                    .code("123456")
                    .userId(1L)
                    .email("test@example.com")
                    .expiresAt(LocalDateTime.now().plusMinutes(3))
                    .build();

            given(userRepository.findByEmailAndPhoneAndDeletedAtIsNull(anyString(), anyString()))
                    .willReturn(Optional.of(testUser));
            given(verificationRequestRepository.save(any(VerificationRequest.class)))
                    .willReturn(savedVerification);

            // when
            AuthResponse.VerificationRequestResult response = authService.resetPasswordRequest(request);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getRequestId()).isNotNull();
        }

        @Test
        @DisplayName("실패: 소셜 로그인 사용자")
        void resetPasswordRequest_SocialUser() {
            // given
            User socialUser = User.builder()
                    .id(2L)
                    .email("social@kakao.com")
                    .password("encoded")
                    .name("소셜사용자")
                    .nickname("소셜닉네임")
                    .phone("01099999999")
                    .provider(AuthProvider.KAKAO)
                    .providerId("kakao_123")
                    .role(UserRole.CUSTOMER)
                    .status(UserStatus.ACTIVE)
                    .build();

            AuthRequest.ResetPasswordRequest request = AuthRequest.ResetPasswordRequest.builder()
                    .email("social@kakao.com")
                    .phone("01099999999")
                    .build();

            given(userRepository.findByEmailAndPhoneAndDeletedAtIsNull(anyString(), anyString()))
                    .willReturn(Optional.of(socialUser));

            // when & then
            assertThatThrownBy(() -> authService.resetPasswordRequest(request))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.SOCIAL_LOGIN_USER);
        }

        @Test
        @DisplayName("성공: 비밀번호 재설정 인증 확인")
        void resetPasswordVerify_Success() {
            // given
            VerificationRequest verification = VerificationRequest.builder()
                    .id("test-uuid")
                    .type(VerificationType.RESET_PASSWORD)
                    .phone("01012345678")
                    .code("123456")
                    .userId(1L)
                    .expiresAt(LocalDateTime.now().plusMinutes(3))
                    .build();

            AuthRequest.VerifyRequest request = AuthRequest.VerifyRequest.builder()
                    .requestId("test-uuid")
                    .code("123456")
                    .build();

            given(verificationRequestRepository.findById(anyString())).willReturn(Optional.of(verification));

            // when
            AuthResponse.PasswordResetToken response = authService.resetPasswordVerify(request);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getToken()).isNotNull();
        }

        @Test
        @DisplayName("성공: 비밀번호 변경")
        void resetPasswordConfirm_Success() {
            // given
            VerificationRequest verification = VerificationRequest.builder()
                    .id("test-uuid")
                    .type(VerificationType.RESET_PASSWORD)
                    .phone("01012345678")
                    .code("123456")
                    .userId(1L)
                    .resetToken("reset-token")
                    .verified(true)
                    .expiresAt(LocalDateTime.now().plusMinutes(3))
                    .build();

            AuthRequest.ResetPasswordConfirm request = AuthRequest.ResetPasswordConfirm.builder()
                    .token("reset-token")
                    .newPassword("newPassword123!")
                    .build();

            given(verificationRequestRepository.findByResetTokenAndVerifiedTrue(anyString()))
                    .willReturn(Optional.of(verification));
            given(userRepository.findByIdAndDeletedAtIsNull(anyLong())).willReturn(Optional.of(testUser));
            given(passwordEncoder.encode(anyString())).willReturn("encodedNewPassword");

            // when
            AuthResponse.SuccessMessage response = authService.resetPasswordConfirm(request);

            // then
            assertThat(response).isNotNull();
            assertThat(response.isSuccess()).isTrue();
            verify(verificationRequestRepository).delete(any(VerificationRequest.class));
        }

        @Test
        @DisplayName("실패: 취약한 새 비밀번호")
        void resetPasswordConfirm_WeakPassword() {
            // given
            VerificationRequest verification = VerificationRequest.builder()
                    .id("test-uuid")
                    .type(VerificationType.RESET_PASSWORD)
                    .phone("01012345678")
                    .code("123456")
                    .userId(1L)
                    .resetToken("reset-token")
                    .verified(true)
                    .expiresAt(LocalDateTime.now().plusMinutes(3))
                    .build();

            AuthRequest.ResetPasswordConfirm request = AuthRequest.ResetPasswordConfirm.builder()
                    .token("reset-token")
                    .newPassword("weak")
                    .build();

            given(verificationRequestRepository.findByResetTokenAndVerifiedTrue(anyString()))
                    .willReturn(Optional.of(verification));

            // when & then
            assertThatThrownBy(() -> authService.resetPasswordConfirm(request))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.WEAK_PASSWORD);
        }
    }

    // ==================== 소셜 로그인 테스트 ====================

    @Nested
    @DisplayName("소셜 로그인")
    class OAuthLogin {

        @Test
        @DisplayName("성공: 신규 소셜 로그인 사용자")
        void oauthLogin_NewUser() {
            // given
            AuthRequest.OAuthLogin request = AuthRequest.OAuthLogin.builder()
                    .code("authorization_code")
                    .build();

            OAuthService.OAuthUserInfo oAuthUserInfo = OAuthService.OAuthUserInfo.builder()
                    .provider(AuthProvider.KAKAO)
                    .providerId("kakao_123")
                    .email("kakao_user@kakao.com")
                    .name("카카오사용자")
                    .profileImageUrl(null)
                    .build();

            given(oAuthService.getOAuthUserInfo(any(AuthProvider.class), anyString())).willReturn(oAuthUserInfo);
            given(userRepository.findByEmail(anyString())).willReturn(Optional.empty());
            given(userRepository.findByProviderAndProviderId(any(AuthProvider.class), anyString()))
                    .willReturn(Optional.empty());
            given(passwordEncoder.encode(anyString())).willReturn("encodedPassword");
            given(userRepository.saveAndFlush(any(User.class))).willAnswer(invocation -> {
                User user = invocation.getArgument(0);
                ReflectionTestUtils.setField(user, "id", 1L);
                LocalDateTime now = LocalDateTime.now();
                ReflectionTestUtils.setField(user, "createdAt", now);
                ReflectionTestUtils.setField(user, "updatedAt", now);
                return user;
            });
            given(jwtTokenProvider.createAccessToken(anyLong(), anyString(), anyList())).willReturn("accessToken");
            given(jwtTokenProvider.createRefreshToken(anyLong())).willReturn("refreshToken");

            // when
            AuthResponse.OAuthResult response = authService.oauthLogin("kakao", request);

            // then
            assertThat(response).isNotNull();
            assertThat(response.isNewUser()).isTrue();
            assertThat(response.getAccessToken()).isEqualTo("accessToken");
        }

        @Test
        @DisplayName("성공: 기존 소셜 로그인 사용자")
        void oauthLogin_ExistingUser() {
            // given
            User socialUser = User.builder()
                    .id(2L)
                    .email("kakao_user@kakao.com")
                    .password("encoded")
                    .name("kakao_user")
                    .nickname("kakao_user")
                    .provider(AuthProvider.KAKAO)
                    .providerId("kakao_123")
                    .role(UserRole.CUSTOMER)
                    .status(UserStatus.ACTIVE)
                    .build();
            ReflectionTestUtils.setField(socialUser, "lastLoginAt", LocalDateTime.now().minusDays(1));

            AuthRequest.OAuthLogin request = AuthRequest.OAuthLogin.builder()
                    .code("authorization_code")
                    .build();

            OAuthService.OAuthUserInfo oAuthUserInfo = OAuthService.OAuthUserInfo.builder()
                    .provider(AuthProvider.KAKAO)
                    .providerId("kakao_123")
                    .email("kakao_user@kakao.com")
                    .name("kakao_user")
                    .profileImageUrl(null)
                    .build();

            given(oAuthService.getOAuthUserInfo(any(AuthProvider.class), anyString())).willReturn(oAuthUserInfo);
            given(userRepository.findByEmail("kakao_user@kakao.com")).willReturn(Optional.of(socialUser));
            given(jwtTokenProvider.createAccessToken(anyLong(), anyString(), anyList())).willReturn("accessToken");
            given(jwtTokenProvider.createRefreshToken(anyLong())).willReturn("refreshToken");

            // when
            AuthResponse.OAuthResult response = authService.oauthLogin("kakao", request);

            // then
            assertThat(response).isNotNull();
            assertThat(response.isNewUser()).isFalse();
        }
    }

    @Nested
    @DisplayName("아이디 찾기 - 재전송")
    class FindIdResendTest {

        @Test
        @DisplayName("성공: 인증번호 재전송")
        void findIdResend_Success() {
            // given
            AuthRequest.ResendRequest request = AuthRequest.ResendRequest.builder()
                    .requestId("request-uuid")
                    .build();

            VerificationRequest verification = VerificationRequest.builder()
                    .id("request-uuid")
                    .phone("01012345678")
                    .code("123456")
                    .type(VerificationType.FIND_ID)
                    .verified(false)
                    .build();
            ReflectionTestUtils.setField(verification, "expiresAt", LocalDateTime.now().plusMinutes(3));

            given(verificationRequestRepository.findById("request-uuid"))
                    .willReturn(Optional.of(verification));

            // when
            AuthResponse.VerificationRequestResult response = authService.findIdResend(request);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getRequestId()).isEqualTo("request-uuid");
        }

        @Test
        @DisplayName("실패: 인증 요청을 찾을 수 없음")
        void findIdResend_NotFound() {
            // given
            AuthRequest.ResendRequest request = AuthRequest.ResendRequest.builder()
                    .requestId("invalid-uuid")
                    .build();

            given(verificationRequestRepository.findById("invalid-uuid"))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> authService.findIdResend(request))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.VERIFICATION_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("비밀번호 유효성 검사 - 누락된 브랜치")
    class IsValidPasswordAdditionalTests {

        @Test
        @DisplayName("실패: 숫자만 포함 (영문 없음)")
        void register_PasswordOnlyDigits() {
            // given - 숫자만 있는 비밀번호 (8자 이상이지만 영문 없음)
            AuthRequest.Register request = AuthRequest.Register.builder()
                    .email("new@example.com")
                    .password("12345678")
                    .name("새사용자")
                    .nickname("새닉네임")
                    .phone("01098765432")
                    .agreeTerms(true)
                    .agreePrivacy(true)
                    .build();

            given(userRepository.existsByEmail(anyString())).willReturn(false);
            given(userRepository.existsByNickname(anyString())).willReturn(false);

            // when & then
            assertThatThrownBy(() -> authService.register(request))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.WEAK_PASSWORD);
        }

        @Test
        @DisplayName("실패: 영문만 포함 (숫자 없음)")
        void register_PasswordOnlyLetters() {
            // given - 영문만 있는 비밀번호 (8자 이상이지만 숫자 없음)
            AuthRequest.Register request = AuthRequest.Register.builder()
                    .email("new@example.com")
                    .password("abcdefgh")
                    .name("새사용자")
                    .nickname("새닉네임")
                    .phone("01098765432")
                    .agreeTerms(true)
                    .agreePrivacy(true)
                    .build();

            given(userRepository.existsByEmail(anyString())).willReturn(false);
            given(userRepository.existsByNickname(anyString())).willReturn(false);

            // when & then
            assertThatThrownBy(() -> authService.register(request))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.WEAK_PASSWORD);
        }

        @Test
        @DisplayName("실패: null 비밀번호")
        void resetPasswordConfirm_NullPassword() {
            // given
            VerificationRequest verification = VerificationRequest.builder()
                    .id("test-uuid")
                    .type(VerificationType.RESET_PASSWORD)
                    .phone("01012345678")
                    .code("123456")
                    .userId(1L)
                    .resetToken("reset-token")
                    .verified(true)
                    .expiresAt(LocalDateTime.now().plusMinutes(3))
                    .build();

            AuthRequest.ResetPasswordConfirm request = AuthRequest.ResetPasswordConfirm.builder()
                    .token("reset-token")
                    .newPassword(null)
                    .build();

            given(verificationRequestRepository.findByResetTokenAndVerifiedTrue(anyString()))
                    .willReturn(Optional.of(verification));

            // when & then
            assertThatThrownBy(() -> authService.resetPasswordConfirm(request))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.WEAK_PASSWORD);
        }
    }

    @Nested
    @DisplayName("회원가입 - 누락된 브랜치")
    class RegisterAdditionalTests {

        @Test
        @DisplayName("실패: 개인정보 동의 미체크")
        void register_PrivacyNotAgreed() {
            // given
            AuthRequest.Register request = AuthRequest.Register.builder()
                    .email("new@example.com")
                    .password("password123")
                    .name("새사용자")
                    .nickname("새닉네임")
                    .phone("01098765432")
                    .agreeTerms(true)
                    .agreePrivacy(false)
                    .build();

            // when & then
            assertThatThrownBy(() -> authService.register(request))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.TERMS_NOT_AGREED);
        }
    }

    @Nested
    @DisplayName("토큰 갱신 - 누락된 브랜치")
    class RefreshAdditionalTests {

        @Test
        @DisplayName("실패: 비활성 계정으로 토큰 갱신 시도")
        void refresh_InactiveUser() {
            // given
            testUser.suspend();
            AuthRequest.RefreshToken request = AuthRequest.RefreshToken.builder()
                    .refreshToken("validRefreshToken")
                    .build();

            given(jwtTokenProvider.validateToken(anyString())).willReturn(true);
            given(jwtTokenProvider.getUserId(anyString())).willReturn(1L);
            given(userRepository.findByIdAndDeletedAtIsNull(anyLong())).willReturn(Optional.of(testUser));

            // when & then
            assertThatThrownBy(() -> authService.refresh(request))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ACCESS_DENIED);
        }
    }

    @Nested
    @DisplayName("아이디 찾기 확인 - 누락된 브랜치")
    class FindIdVerifyAdditionalTests {

        @Test
        @DisplayName("실패: 잘못된 인증 타입 (RESET_PASSWORD 타입으로 아이디 찾기 시도)")
        void findIdVerify_WrongVerificationType() {
            // given
            VerificationRequest verification = VerificationRequest.builder()
                    .id("test-uuid")
                    .type(VerificationType.RESET_PASSWORD) // Wrong type
                    .phone("01012345678")
                    .code("123456")
                    .userId(1L)
                    .expiresAt(LocalDateTime.now().plusMinutes(3))
                    .build();

            AuthRequest.VerifyRequest request = AuthRequest.VerifyRequest.builder()
                    .requestId("test-uuid")
                    .code("123456")
                    .build();

            given(verificationRequestRepository.findById(anyString())).willReturn(Optional.of(verification));

            // when & then
            assertThatThrownBy(() -> authService.findIdVerify(request))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.VERIFICATION_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("비밀번호 재설정 확인 - 누락된 브랜치")
    class ResetPasswordVerifyAdditionalTests {

        @Test
        @DisplayName("실패: 잘못된 인증 타입 (FIND_ID 타입으로 비밀번호 재설정 시도)")
        void resetPasswordVerify_WrongVerificationType() {
            // given
            VerificationRequest verification = VerificationRequest.builder()
                    .id("test-uuid")
                    .type(VerificationType.FIND_ID) // Wrong type
                    .phone("01012345678")
                    .code("123456")
                    .userId(1L)
                    .expiresAt(LocalDateTime.now().plusMinutes(3))
                    .build();

            AuthRequest.VerifyRequest request = AuthRequest.VerifyRequest.builder()
                    .requestId("test-uuid")
                    .code("123456")
                    .build();

            given(verificationRequestRepository.findById(anyString())).willReturn(Optional.of(verification));

            // when & then
            assertThatThrownBy(() -> authService.resetPasswordVerify(request))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.VERIFICATION_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("아이디 찾기 재전송 - 누락된 브랜치")
    class FindIdResendAdditionalTests {

        @Test
        @DisplayName("실패: 잘못된 인증 타입 (RESET_PASSWORD 타입으로 아이디 찾기 재전송 시도)")
        void findIdResend_WrongVerificationType() {
            // given
            VerificationRequest verification = VerificationRequest.builder()
                    .id("test-uuid")
                    .type(VerificationType.RESET_PASSWORD) // Wrong type
                    .phone("01012345678")
                    .code("123456")
                    .userId(1L)
                    .expiresAt(LocalDateTime.now().plusMinutes(3))
                    .build();

            AuthRequest.ResendRequest request = AuthRequest.ResendRequest.builder()
                    .requestId("test-uuid")
                    .build();

            given(verificationRequestRepository.findById(anyString())).willReturn(Optional.of(verification));

            // when & then
            assertThatThrownBy(() -> authService.findIdResend(request))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.VERIFICATION_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("비밀번호 재설정 완료 - 누락된 브랜치")
    class ResetPasswordConfirmAdditionalTests {

        @Test
        @DisplayName("실패: 비밀번호 재설정 시 사용자 없음")
        void resetPasswordConfirm_UserNotFound() {
            // given
            VerificationRequest verification = VerificationRequest.builder()
                    .id("test-uuid")
                    .type(VerificationType.RESET_PASSWORD)
                    .phone("01012345678")
                    .code("123456")
                    .userId(999L)
                    .resetToken("reset-token")
                    .verified(true)
                    .expiresAt(LocalDateTime.now().plusMinutes(3))
                    .build();

            AuthRequest.ResetPasswordConfirm request = AuthRequest.ResetPasswordConfirm.builder()
                    .token("reset-token")
                    .newPassword("newPassword123!")
                    .build();

            given(verificationRequestRepository.findByResetTokenAndVerifiedTrue(anyString()))
                    .willReturn(Optional.of(verification));
            given(userRepository.findByIdAndDeletedAtIsNull(999L))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> authService.resetPasswordConfirm(request))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("추가 에러 케이스")
    class AdditionalErrorCases {

        @Test
        @DisplayName("실패: 토큰 갱신 - 사용자 없음")
        void refresh_UserNotFound() {
            // given
            AuthRequest.RefreshToken request = AuthRequest.RefreshToken.builder()
                    .refreshToken("refreshToken")
                    .build();

            given(jwtTokenProvider.validateToken("refreshToken")).willReturn(true);
            given(jwtTokenProvider.getUserId("refreshToken")).willReturn(999L);
            given(userRepository.findByIdAndDeletedAtIsNull(999L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> authService.refresh(request))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_NOT_FOUND);
        }

        @Test
        @DisplayName("실패: 비밀번호 재설정 확인 - 인증 요청 없음")
        void resetPasswordVerify_NotFound() {
            // given
            AuthRequest.VerifyRequest request = AuthRequest.VerifyRequest.builder()
                    .requestId("invalid-uuid")
                    .code("123456")
                    .build();

            given(verificationRequestRepository.findById("invalid-uuid"))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> authService.resetPasswordVerify(request))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.VERIFICATION_NOT_FOUND);
        }

        @Test
        @DisplayName("실패: 비밀번호 재설정 확인 - 만료됨")
        void resetPasswordVerify_Expired() {
            // given
            AuthRequest.VerifyRequest request = AuthRequest.VerifyRequest.builder()
                    .requestId("request-uuid")
                    .code("123456")
                    .build();

            VerificationRequest verification = VerificationRequest.builder()
                    .id("request-uuid")
                    .phone("01012345678")
                    .code("123456")
                    .type(VerificationType.RESET_PASSWORD)
                    .verified(false)
                    .build();
            ReflectionTestUtils.setField(verification, "expiresAt", LocalDateTime.now().minusMinutes(1));

            given(verificationRequestRepository.findById("request-uuid"))
                    .willReturn(Optional.of(verification));

            // when & then
            assertThatThrownBy(() -> authService.resetPasswordVerify(request))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.VERIFICATION_EXPIRED);
        }

        @Test
        @DisplayName("실패: 비밀번호 재설정 확인 - 코드 불일치")
        void resetPasswordVerify_CodeMismatch() {
            // given
            AuthRequest.VerifyRequest request = AuthRequest.VerifyRequest.builder()
                    .requestId("request-uuid")
                    .code("999999")
                    .build();

            VerificationRequest verification = VerificationRequest.builder()
                    .id("request-uuid")
                    .phone("01012345678")
                    .code("123456")
                    .type(VerificationType.RESET_PASSWORD)
                    .verified(false)
                    .build();
            ReflectionTestUtils.setField(verification, "expiresAt", LocalDateTime.now().plusMinutes(3));

            given(verificationRequestRepository.findById("request-uuid"))
                    .willReturn(Optional.of(verification));

            // when & then
            assertThatThrownBy(() -> authService.resetPasswordVerify(request))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.VERIFICATION_CODE_MISMATCH);
        }

        @Test
        @DisplayName("실패: 아이디 찾기 확인 - 인증 요청 없음")
        void findIdVerify_NotFound() {
            // given
            AuthRequest.VerifyRequest request = AuthRequest.VerifyRequest.builder()
                    .requestId("invalid-uuid")
                    .code("123456")
                    .build();

            given(verificationRequestRepository.findById("invalid-uuid"))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> authService.findIdVerify(request))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.VERIFICATION_NOT_FOUND);
        }

        @Test
        @DisplayName("실패: 아이디 찾기 확인 - 사용자 없음")
        void findIdVerify_UserNotFound() {
            // given
            AuthRequest.VerifyRequest request = AuthRequest.VerifyRequest.builder()
                    .requestId("request-uuid")
                    .code("123456")
                    .build();

            VerificationRequest verification = VerificationRequest.builder()
                    .id("request-uuid")
                    .phone("01012345678")
                    .code("123456")
                    .type(VerificationType.FIND_ID)
                    .verified(false)
                    .userId(999L)
                    .build();
            ReflectionTestUtils.setField(verification, "expiresAt", LocalDateTime.now().plusMinutes(3));

            given(verificationRequestRepository.findById("request-uuid"))
                    .willReturn(Optional.of(verification));
            given(userRepository.findByIdAndDeletedAtIsNull(999L))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> authService.findIdVerify(request))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_NOT_FOUND);
        }

        @Test
        @DisplayName("실패: 비밀번호 재설정 요청 - 사용자 없음")
        void resetPasswordRequest_UserNotFound() {
            // given
            AuthRequest.ResetPasswordRequest request = AuthRequest.ResetPasswordRequest.builder()
                    .email("unknown@example.com")
                    .phone("01012345678")
                    .build();

            given(userRepository.findByEmailAndPhoneAndDeletedAtIsNull("unknown@example.com", "01012345678"))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> authService.resetPasswordRequest(request))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_NOT_FOUND);
        }

        @Test
        @DisplayName("실패: 비밀번호 재설정 완료 - 인증 요청 없음 또는 미인증")
        void resetPasswordConfirm_VerificationNotFoundOrNotVerified() {
            // given
            AuthRequest.ResetPasswordConfirm request = AuthRequest.ResetPasswordConfirm.builder()
                    .token("invalid-token")
                    .newPassword("newPassword123")
                    .build();

            // findByResetTokenAndVerifiedTrue은 인증되지 않은 것과 없는 것 모두 empty 반환
            given(verificationRequestRepository.findByResetTokenAndVerifiedTrue("invalid-token"))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> authService.resetPasswordConfirm(request))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.VERIFICATION_REQUIRED);
        }
    }
}
