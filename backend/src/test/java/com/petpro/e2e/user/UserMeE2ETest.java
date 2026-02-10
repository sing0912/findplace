package com.petpro.e2e.user;

import com.petpro.e2e.support.AuthTestHelper;
import com.petpro.e2e.support.BaseE2ETest;
import org.junit.jupiter.api.*;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class UserMeE2ETest extends BaseE2ETest {

    private AuthTestHelper.AuthResult userAuth;
    private AuthTestHelper.AuthResult otherUserAuth;

    @BeforeAll
    void setUp() {
        userAuth = authTestHelper.registerAndLogin(baseUrl());
        otherUserAuth = authTestHelper.registerAndLogin(baseUrl());
    }

    // ==================== GET /v1/users/me ====================

    @Test
    @Order(1)
    @DisplayName("내 정보 조회 성공")
    void getMyInfo_success() {
        ResponseEntity<String> response = getWithAuth("/v1/users/me", userAuth.getAccessToken());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(extractField(response.getBody(), "success")).isEqualTo("true");
        assertThat(extractField(response.getBody(), "data.email")).isEqualTo(userAuth.getEmail());
        assertThat(extractField(response.getBody(), "data.role")).isEqualTo("CUSTOMER");
        assertThat(extractField(response.getBody(), "data.status")).isEqualTo("ACTIVE");
    }

    @Test
    @Order(2)
    @DisplayName("내 정보 조회 미인증 시 실패")
    void getMyInfo_unauthorized() {
        ResponseEntity<String> response = getPublic("/v1/users/me");

        assertThat(response.getStatusCode().value()).isIn(401, 403);
    }

    // ==================== PUT /v1/users/me (닉네임 수정) ====================

    @Test
    @Order(3)
    @DisplayName("닉네임 수정 성공")
    void updateNickname_success() {
        Map<String, String> body = Map.of("nickname", "새닉네임" + System.currentTimeMillis());
        ResponseEntity<String> response = putWithAuth("/v1/users/me", body, userAuth.getAccessToken());

        assertThat(response.getStatusCode())
                .as("PUT /me response: %s", response.getBody())
                .isEqualTo(HttpStatus.OK);
        assertThat(extractField(response.getBody(), "success")).isEqualTo("true");
        assertThat(extractField(response.getBody(), "data.nickname")).startsWith("새닉네임");
    }

    @Test
    @Order(4)
    @DisplayName("닉네임 빈값 수정 실패")
    void updateNickname_emptyValue_fail() {
        Map<String, String> body = Map.of("nickname", "");
        ResponseEntity<String> response = putWithAuth("/v1/users/me", body, userAuth.getAccessToken());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @Order(5)
    @DisplayName("닉네임 중복 수정 실패")
    void updateNickname_duplicate_fail() {
        // otherUserAuth의 닉네임을 먼저 확인
        ResponseEntity<String> otherInfo = getWithAuth("/v1/users/me", otherUserAuth.getAccessToken());
        String otherNickname = extractField(otherInfo.getBody(), "data.nickname");

        Map<String, String> body = Map.of("nickname", otherNickname);
        ResponseEntity<String> response = putWithAuth("/v1/users/me", body, userAuth.getAccessToken());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(extractField(response.getBody(), "error.code")).isEqualTo("U007");
    }

    // ==================== POST /v1/users/me/profile-image ====================

    @Test
    @Order(6)
    @DisplayName("프로필 이미지 업로드 성공")
    void uploadProfileImage_success() {
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        // PNG magic bytes (0x89 0x50 0x4E 0x47) + padding
        byte[] pngContent = new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A};
        body.add("file", new ByteArrayResource(pngContent) {
            @Override
            public String getFilename() {
                return "test-image.png";
            }
        });

        ResponseEntity<String> response = postMultipartWithAuth(
                "/v1/users/me/profile-image", body, userAuth.getAccessToken());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(extractField(response.getBody(), "success")).isEqualTo("true");
        assertThat(extractField(response.getBody(), "data.profileImageUrl")).isNotNull();
    }

    // ==================== PUT /v1/users/me/password ====================

    @Test
    @Order(7)
    @DisplayName("비밀번호 변경 성공")
    void changeMyPassword_success() {
        // 전용 사용자 생성 (비밀번호 변경 후 기존 토큰이 무효화될 수 있으므로)
        AuthTestHelper.AuthResult pwUser = authTestHelper.registerAndLogin(baseUrl());

        Map<String, String> body = Map.of(
                "currentPassword", pwUser.getPassword(),
                "newPassword", "NewPass1234!"
        );
        ResponseEntity<String> response = putWithAuth("/v1/users/me/password", body, pwUser.getAccessToken());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(extractField(response.getBody(), "success")).isEqualTo("true");
    }

    @Test
    @Order(8)
    @DisplayName("비밀번호 변경 실패 - 현재 비밀번호 불일치")
    void changeMyPassword_wrongCurrentPassword() {
        Map<String, String> body = Map.of(
                "currentPassword", "WrongPassword1!",
                "newPassword", "NewPass1234!"
        );
        ResponseEntity<String> response = putWithAuth("/v1/users/me/password", body, userAuth.getAccessToken());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(extractField(response.getBody(), "error.code")).isEqualTo("U004");
    }

    @Test
    @Order(9)
    @DisplayName("비밀번호 변경 실패 - 취약한 비밀번호")
    void changeMyPassword_weakPassword() {
        Map<String, String> body = Map.of(
                "currentPassword", userAuth.getPassword(),
                "newPassword", "weak"
        );
        ResponseEntity<String> response = putWithAuth("/v1/users/me/password", body, userAuth.getAccessToken());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    // ==================== DELETE /v1/users/me (회원 탈퇴) ====================

    @Test
    @Order(10)
    @DisplayName("회원 탈퇴 실패 - 비밀번호 불일치")
    void deleteMyAccount_wrongPassword() {
        AuthTestHelper.AuthResult deleteUser = authTestHelper.registerAndLogin(baseUrl());

        Map<String, String> body = Map.of("password", "WrongPassword1!");
        ResponseEntity<String> response = deleteWithAuthAndBody(
                "/v1/users/me", body, deleteUser.getAccessToken());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(extractField(response.getBody(), "error.code")).isEqualTo("U004");
    }

    @Test
    @Order(11)
    @DisplayName("회원 탈퇴 성공")
    void deleteMyAccount_success() {
        AuthTestHelper.AuthResult deleteUser = authTestHelper.registerAndLogin(baseUrl());

        Map<String, String> body = Map.of("password", deleteUser.getPassword());
        ResponseEntity<String> response = deleteWithAuthAndBody(
                "/v1/users/me", body, deleteUser.getAccessToken());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }
}
