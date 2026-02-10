package com.petpro.e2e.user;

import com.petpro.e2e.support.AuthTestHelper;
import com.petpro.e2e.support.BaseE2ETest;
import org.junit.jupiter.api.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class UserAdminE2ETest extends BaseE2ETest {

    private AuthTestHelper.AuthResult userAuth;
    private AuthTestHelper.AuthResult adminAuth;
    private AuthTestHelper.AuthResult superAdminAuth;
    private Long targetUserId;

    @BeforeAll
    void setUp() {
        userAuth = authTestHelper.registerAndLogin(baseUrl());
        adminAuth = adminTestHelper.createAdminAndLogin(baseUrl(), "admin-e2e@test.com");
        superAdminAuth = adminTestHelper.createSuperAdminAndLogin(baseUrl(), "superadmin-e2e@test.com");

        // 관리자 API 테스트 대상 사용자
        AuthTestHelper.AuthResult targetUser = authTestHelper.registerAndLogin(baseUrl());
        targetUserId = targetUser.getUserId();
    }

    // ==================== POST /v1/users (사용자 생성) ====================

    @Test
    @Order(1)
    @DisplayName("관리자가 사용자 생성 성공")
    void createUser_byAdmin_success() {
        Map<String, Object> body = Map.of(
                "email", "admin-created-" + System.currentTimeMillis() + "@test.com",
                "password", "Test1234!",
                "name", "관리자생성유저"
        );
        ResponseEntity<String> response = postWithAuth("/v1/users", body, adminAuth.getAccessToken());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(extractField(response.getBody(), "success")).isEqualTo("true");
        assertThat(extractField(response.getBody(), "data.email")).contains("admin-created-");
    }

    @Test
    @Order(2)
    @DisplayName("일반 사용자가 사용자 생성 시도 - 거부")
    void createUser_byUser_forbidden() {
        Map<String, Object> body = Map.of(
                "email", "user-attempt@test.com",
                "password", "Test1234!",
                "name", "시도유저"
        );
        ResponseEntity<String> response = postWithAuth("/v1/users", body, userAuth.getAccessToken());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @Order(3)
    @DisplayName("미인증으로 사용자 생성 시도 - 거부")
    void createUser_unauthorized() {
        Map<String, Object> body = Map.of(
                "email", "noauth@test.com",
                "password", "Test1234!",
                "name", "미인증유저"
        );
        ResponseEntity<String> response = postPublic("/v1/users", body);

        assertThat(response.getStatusCode().value()).isIn(401, 403);
    }

    @Test
    @Order(4)
    @DisplayName("이메일 중복으로 사용자 생성 실패")
    void createUser_duplicateEmail() {
        Map<String, Object> body = Map.of(
                "email", userAuth.getEmail(),
                "password", "Test1234!",
                "name", "중복이메일유저"
        );
        ResponseEntity<String> response = postWithAuth("/v1/users", body, adminAuth.getAccessToken());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(extractField(response.getBody(), "error.code")).isEqualTo("U002");
    }

    // ==================== GET /v1/users (목록 조회) ====================

    @Test
    @Order(5)
    @DisplayName("관리자가 사용자 목록 조회 성공")
    void getUsers_byAdmin_success() {
        ResponseEntity<String> response = getWithAuth("/v1/users", adminAuth.getAccessToken());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(extractField(response.getBody(), "success")).isEqualTo("true");
        assertThat(extractObject(response.getBody(), "data.content")).isNotNull();
        assertThat(extractObject(response.getBody(), "data.page")).isNotNull();
    }

    @Test
    @Order(6)
    @DisplayName("일반 사용자가 목록 조회 시도 - 거부")
    void getUsers_byUser_forbidden() {
        ResponseEntity<String> response = getWithAuth("/v1/users", userAuth.getAccessToken());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    // ==================== GET /v1/users/search ====================

    @Test
    @Order(7)
    @DisplayName("관리자가 사용자 검색 성공")
    void searchUsers_byAdmin_success() {
        ResponseEntity<String> response = getWithAuth(
                "/v1/users/search?keyword=test", adminAuth.getAccessToken());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(extractField(response.getBody(), "success")).isEqualTo("true");
    }

    @Test
    @Order(8)
    @DisplayName("일반 사용자가 검색 시도 - 거부")
    void searchUsers_byUser_forbidden() {
        ResponseEntity<String> response = getWithAuth(
                "/v1/users/search?keyword=test", userAuth.getAccessToken());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    // ==================== PATCH /v1/users/{id}/status ====================

    @Test
    @Order(9)
    @DisplayName("관리자가 사용자 정지 성공")
    void changeStatus_suspend_success() {
        ResponseEntity<String> response = patchWithAuth(
                "/v1/users/" + targetUserId + "/status?status=SUSPENDED", adminAuth.getAccessToken());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(extractField(response.getBody(), "data.status")).isEqualTo("SUSPENDED");
    }

    @Test
    @Order(10)
    @DisplayName("관리자가 사용자 재활성화 성공")
    void changeStatus_activate_success() {
        ResponseEntity<String> response = patchWithAuth(
                "/v1/users/" + targetUserId + "/status?status=ACTIVE", adminAuth.getAccessToken());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(extractField(response.getBody(), "data.status")).isEqualTo("ACTIVE");
    }

    @Test
    @Order(11)
    @DisplayName("일반 사용자가 상태 변경 시도 - 거부")
    void changeStatus_byUser_forbidden() {
        ResponseEntity<String> response = patchWithAuth(
                "/v1/users/" + targetUserId + "/status?status=SUSPENDED", userAuth.getAccessToken());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    // ==================== PATCH /v1/users/{id}/role ====================

    @Test
    @Order(12)
    @DisplayName("SUPER_ADMIN이 역할 변경 성공")
    void changeRole_bySuperAdmin_success() {
        // 역할 변경 전용 사용자 생성
        AuthTestHelper.AuthResult roleTarget = authTestHelper.registerAndLogin(baseUrl());

        ResponseEntity<String> response = patchWithAuth(
                "/v1/users/" + roleTarget.getUserId() + "/role?role=ADMIN", superAdminAuth.getAccessToken());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(extractField(response.getBody(), "data.role")).isEqualTo("ADMIN");
    }

    @Test
    @Order(13)
    @DisplayName("ADMIN이 역할 변경 시도 - 거부")
    void changeRole_byAdmin_forbidden() {
        ResponseEntity<String> response = patchWithAuth(
                "/v1/users/" + targetUserId + "/role?role=ADMIN", adminAuth.getAccessToken());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @Order(14)
    @DisplayName("일반 사용자가 역할 변경 시도 - 거부")
    void changeRole_byUser_forbidden() {
        ResponseEntity<String> response = patchWithAuth(
                "/v1/users/" + targetUserId + "/role?role=ADMIN", userAuth.getAccessToken());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    // ==================== DELETE /v1/users/{id} ====================

    @Test
    @Order(15)
    @DisplayName("관리자가 사용자 소프트 삭제 성공")
    void deleteUser_byAdmin_success() {
        AuthTestHelper.AuthResult deleteTarget = authTestHelper.registerAndLogin(baseUrl());

        ResponseEntity<String> response = deleteWithAuth(
                "/v1/users/" + deleteTarget.getUserId(), adminAuth.getAccessToken());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        // 삭제 후 조회하면 404
        ResponseEntity<String> getResponse = getWithAuth(
                "/v1/users/" + deleteTarget.getUserId(), adminAuth.getAccessToken());
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @Order(16)
    @DisplayName("일반 사용자가 삭제 시도 - 거부")
    void deleteUser_byUser_forbidden() {
        ResponseEntity<String> response = deleteWithAuth(
                "/v1/users/" + targetUserId, userAuth.getAccessToken());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }
}
