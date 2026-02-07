package com.petpro.domain.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.petpro.domain.auth.dto.AuthRequest;
import com.petpro.domain.auth.dto.AuthResponse;
import com.petpro.domain.auth.entity.AuthProvider;
import com.petpro.domain.auth.service.AuthService;
import com.petpro.global.security.jwt.JwtTokenProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("AuthController 테스트")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @Nested
    @DisplayName("회원가입")
    class Register {

        @Test
        @DisplayName("성공: 회원가입")
        void register_Success() throws Exception {
            // given
            AuthRequest.Register request = AuthRequest.Register.builder()
                    .email("test@example.com")
                    .password("password123")
                    .name("테스트")
                    .nickname("테스트닉네임")
                    .phone("01012345678")
                    .agreeTerms(true)
                    .agreePrivacy(true)
                    .build();

            AuthResponse.RegisterResult response = AuthResponse.RegisterResult.builder()
                    .id(1L)
                    .email("test@example.com")
                    .nickname("테스트닉네임")
                    .createdAt("2026-02-05T10:00:00")
                    .build();

            given(authService.register(any(AuthRequest.Register.class))).willReturn(response);

            // when & then
            mockMvc.perform(post("/v1/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.data.id").value(1))
                    .andExpect(jsonPath("$.data.email").value("test@example.com"));
        }
    }

    @Nested
    @DisplayName("로그인")
    class Login {

        @Test
        @DisplayName("성공: 로그인")
        void login_Success() throws Exception {
            // given
            AuthRequest.Login request = AuthRequest.Login.builder()
                    .email("test@example.com")
                    .password("password123")
                    .build();

            AuthResponse.Token response = AuthResponse.Token.of("accessToken", "refreshToken", 3600L);

            given(authService.login(any(AuthRequest.Login.class))).willReturn(response);

            // when & then
            mockMvc.perform(post("/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.accessToken").value("accessToken"));
        }
    }

    @Nested
    @DisplayName("로그아웃")
    class Logout {

        @Test
        @DisplayName("성공: 로그아웃")
        void logout_Success() throws Exception {
            // when & then
            mockMvc.perform(post("/v1/auth/logout"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.success").value(true));
        }
    }

    @Nested
    @DisplayName("토큰 갱신")
    class Refresh {

        @Test
        @DisplayName("성공: 토큰 갱신")
        void refresh_Success() throws Exception {
            // given
            AuthRequest.RefreshToken request = AuthRequest.RefreshToken.builder()
                    .refreshToken("refreshToken")
                    .build();

            AuthResponse.Token response = AuthResponse.Token.of("newAccessToken", "newRefreshToken", 3600L);

            given(authService.refresh(any(AuthRequest.RefreshToken.class))).willReturn(response);

            // when & then
            mockMvc.perform(post("/v1/auth/refresh")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.accessToken").value("newAccessToken"));
        }
    }

    @Nested
    @DisplayName("중복 확인")
    class CheckAvailability {

        @Test
        @DisplayName("성공: 이메일 중복 확인")
        void checkEmail_Success() throws Exception {
            // given
            given(authService.checkEmail(anyString())).willReturn(AuthResponse.AvailabilityCheck.available());

            // when & then
            mockMvc.perform(get("/v1/auth/check-email")
                            .param("email", "test@example.com"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.available").value(true));
        }

        @Test
        @DisplayName("성공: 닉네임 중복 확인")
        void checkNickname_Success() throws Exception {
            // given
            given(authService.checkNickname(anyString())).willReturn(AuthResponse.AvailabilityCheck.available());

            // when & then
            mockMvc.perform(get("/v1/auth/check-nickname")
                            .param("nickname", "테스트닉네임"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.available").value(true));
        }
    }

    @Nested
    @DisplayName("소셜 로그인")
    class OAuth {

        @Test
        @DisplayName("성공: 카카오 로그인")
        void oauthLogin_Success() throws Exception {
            // given
            AuthRequest.OAuthLogin request = AuthRequest.OAuthLogin.builder()
                    .code("auth_code")
                    .build();

            AuthResponse.OAuthResult response = AuthResponse.OAuthResult.builder()
                    .accessToken("accessToken")
                    .refreshToken("refreshToken")
                    .user(AuthResponse.UserInfo.builder()
                            .id(1L)
                            .email("user@kakao.com")
                            .nickname("kakao_user")
                            .build())
                    .isNewUser(true)
                    .build();

            given(authService.oauthLogin(anyString(), any(AuthRequest.OAuthLogin.class))).willReturn(response);

            // when & then
            mockMvc.perform(post("/v1/auth/oauth/kakao/callback")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.accessToken").value("accessToken"));
        }
    }

    @Nested
    @DisplayName("아이디 찾기")
    class FindId {

        @Test
        @DisplayName("성공: 아이디 찾기 요청")
        void findIdRequest_Success() throws Exception {
            // given
            AuthRequest.FindIdRequest request = AuthRequest.FindIdRequest.builder()
                    .name("테스트")
                    .phone("01012345678")
                    .build();

            AuthResponse.VerificationRequestResult response = AuthResponse.VerificationRequestResult.builder()
                    .requestId("uuid")
                    .expireAt("2026-02-05T10:03:00")
                    .build();

            given(authService.findIdRequest(any(AuthRequest.FindIdRequest.class))).willReturn(response);

            // when & then
            mockMvc.perform(post("/v1/auth/find-id/request")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.requestId").value("uuid"));
        }

        @Test
        @DisplayName("성공: 아이디 찾기 확인")
        void findIdVerify_Success() throws Exception {
            // given
            AuthRequest.VerifyRequest request = AuthRequest.VerifyRequest.builder()
                    .requestId("uuid")
                    .code("123456")
                    .build();

            AuthResponse.FindIdResult response = AuthResponse.FindIdResult.of("test@example.com", AuthProvider.EMAIL);

            given(authService.findIdVerify(any(AuthRequest.VerifyRequest.class))).willReturn(response);

            // when & then
            mockMvc.perform(post("/v1/auth/find-id/verify")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.email").exists());
        }

        @Test
        @DisplayName("성공: 아이디 찾기 재전송")
        void findIdResend_Success() throws Exception {
            // given
            AuthRequest.ResendRequest request = AuthRequest.ResendRequest.builder()
                    .requestId("uuid")
                    .build();

            AuthResponse.VerificationRequestResult response = AuthResponse.VerificationRequestResult.builder()
                    .requestId("uuid")
                    .expireAt("2026-02-05T10:06:00")
                    .build();

            given(authService.findIdResend(any(AuthRequest.ResendRequest.class))).willReturn(response);

            // when & then
            mockMvc.perform(post("/v1/auth/find-id/resend")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.requestId").value("uuid"));
        }
    }

    @Nested
    @DisplayName("비밀번호 재설정")
    class ResetPassword {

        @Test
        @DisplayName("성공: 비밀번호 재설정 요청")
        void resetPasswordRequest_Success() throws Exception {
            // given
            AuthRequest.ResetPasswordRequest request = AuthRequest.ResetPasswordRequest.builder()
                    .email("test@example.com")
                    .phone("01012345678")
                    .build();

            AuthResponse.VerificationRequestResult response = AuthResponse.VerificationRequestResult.builder()
                    .requestId("uuid")
                    .expireAt("2026-02-05T10:03:00")
                    .build();

            given(authService.resetPasswordRequest(any(AuthRequest.ResetPasswordRequest.class))).willReturn(response);

            // when & then
            mockMvc.perform(post("/v1/auth/reset-password/request")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.requestId").value("uuid"));
        }

        @Test
        @DisplayName("성공: 비밀번호 재설정 확인")
        void resetPasswordVerify_Success() throws Exception {
            // given
            AuthRequest.VerifyRequest request = AuthRequest.VerifyRequest.builder()
                    .requestId("uuid")
                    .code("123456")
                    .build();

            AuthResponse.PasswordResetToken response = AuthResponse.PasswordResetToken.builder()
                    .token("reset_token")
                    .build();

            given(authService.resetPasswordVerify(any(AuthRequest.VerifyRequest.class))).willReturn(response);

            // when & then
            mockMvc.perform(post("/v1/auth/reset-password/verify")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.token").value("reset_token"));
        }

        @Test
        @DisplayName("성공: 비밀번호 재설정 완료")
        void resetPasswordConfirm_Success() throws Exception {
            // given
            AuthRequest.ResetPasswordConfirm request = AuthRequest.ResetPasswordConfirm.builder()
                    .token("reset_token")
                    .newPassword("newPassword123")
                    .build();

            AuthResponse.SuccessMessage response = AuthResponse.SuccessMessage.of("비밀번호가 변경되었습니다.");

            given(authService.resetPasswordConfirm(any(AuthRequest.ResetPasswordConfirm.class))).willReturn(response);

            // when & then
            mockMvc.perform(post("/v1/auth/reset-password/confirm")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.success").value(true));
        }
    }
}
