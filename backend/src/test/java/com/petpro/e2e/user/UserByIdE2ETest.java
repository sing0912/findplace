package com.petpro.e2e.user;

import com.petpro.e2e.support.AuthTestHelper;
import com.petpro.e2e.support.BaseE2ETest;
import org.junit.jupiter.api.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class UserByIdE2ETest extends BaseE2ETest {

    private AuthTestHelper.AuthResult userAuth;
    private AuthTestHelper.AuthResult otherUserAuth;
    private AuthTestHelper.AuthResult adminAuth;

    @BeforeAll
    void setUp() {
        userAuth = authTestHelper.registerAndLogin(baseUrl());
        otherUserAuth = authTestHelper.registerAndLogin(baseUrl());
        adminAuth = adminTestHelper.createAdminAndLogin(baseUrl(), "byid-admin@test.com");
    }

    // ==================== GET /v1/users/{id} ====================

    @Test
    @Order(1)
    @DisplayName("사용자 조회 성공")
    void getUser_success() {
        ResponseEntity<String> response = getWithAuth(
                "/v1/users/" + userAuth.getUserId(), userAuth.getAccessToken());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(extractField(response.getBody(), "success")).isEqualTo("true");
        assertThat(extractField(response.getBody(), "data.id")).isEqualTo(userAuth.getUserId().toString());
        assertThat(extractField(response.getBody(), "data.email")).isEqualTo(userAuth.getEmail());
    }

    @Test
    @Order(2)
    @DisplayName("존재하지 않는 사용자 조회")
    void getUser_notFound() {
        // 관리자 토큰 사용: 일반 사용자는 @PreAuthorize에 의해 타인 ID 접근 시 403 반환
        ResponseEntity<String> response = getWithAuth("/v1/users/999999", adminAuth.getAccessToken());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(extractField(response.getBody(), "error.code")).isEqualTo("U001");
    }

    @Test
    @Order(3)
    @DisplayName("미인증으로 사용자 조회 실패")
    void getUser_unauthorized() {
        ResponseEntity<String> response = getPublic("/v1/users/" + userAuth.getUserId());

        assertThat(response.getStatusCode().value()).isIn(401, 403);
    }

    // ==================== PUT /v1/users/{id} ====================

    @Test
    @Order(4)
    @DisplayName("본인 정보 수정 성공")
    void updateUser_self_success() {
        Map<String, String> body = Map.of("name", "수정된이름");
        ResponseEntity<String> response = putWithAuth(
                "/v1/users/" + userAuth.getUserId(), body, userAuth.getAccessToken());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(extractField(response.getBody(), "success")).isEqualTo("true");
        assertThat(extractField(response.getBody(), "data.name")).isEqualTo("수정된이름");
    }

    @Test
    @Order(5)
    @DisplayName("관리자가 타인 정보 수정 성공")
    void updateUser_byAdmin_success() {
        Map<String, String> body = Map.of("name", "관리자수정");
        ResponseEntity<String> response = putWithAuth(
                "/v1/users/" + otherUserAuth.getUserId(), body, adminAuth.getAccessToken());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(extractField(response.getBody(), "data.name")).isEqualTo("관리자수정");
    }

    @Test
    @Order(6)
    @DisplayName("타인이 정보 수정 시도 - 거부")
    void updateUser_byOther_forbidden() {
        Map<String, String> body = Map.of("name", "불법수정");
        ResponseEntity<String> response = putWithAuth(
                "/v1/users/" + userAuth.getUserId(), body, otherUserAuth.getAccessToken());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    // ==================== PUT /v1/users/{id}/password ====================

    @Test
    @Order(7)
    @DisplayName("본인 비밀번호 변경 성공")
    void updatePassword_self_success() {
        AuthTestHelper.AuthResult pwUser = authTestHelper.registerAndLogin(baseUrl());

        Map<String, String> body = Map.of(
                "currentPassword", pwUser.getPassword(),
                "newPassword", "Changed1234!"
        );
        ResponseEntity<String> response = putWithAuth(
                "/v1/users/" + pwUser.getUserId() + "/password", body, pwUser.getAccessToken());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @Order(8)
    @DisplayName("타인 비밀번호 변경 시도 - 거부")
    void updatePassword_byOther_forbidden() {
        Map<String, String> body = Map.of(
                "currentPassword", "anything",
                "newPassword", "NewPass1234!"
        );
        ResponseEntity<String> response = putWithAuth(
                "/v1/users/" + userAuth.getUserId() + "/password", body, otherUserAuth.getAccessToken());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @Order(9)
    @DisplayName("비밀번호 변경 실패 - 현재 비밀번호 불일치")
    void updatePassword_wrongCurrent() {
        Map<String, String> body = Map.of(
                "currentPassword", "WrongPassword1!",
                "newPassword", "NewPass1234!"
        );
        ResponseEntity<String> response = putWithAuth(
                "/v1/users/" + userAuth.getUserId() + "/password", body, userAuth.getAccessToken());

        assertThat(response.getStatusCode().value()).isIn(400, 401);
    }
}
