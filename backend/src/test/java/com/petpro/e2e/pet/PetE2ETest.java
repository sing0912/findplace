package com.petpro.e2e.pet;

import com.petpro.e2e.support.AuthTestHelper;
import com.petpro.e2e.support.BaseE2ETest;
import org.junit.jupiter.api.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class PetE2ETest extends BaseE2ETest {

    private static AuthTestHelper.AuthResult userAuth;
    private static AuthTestHelper.AuthResult otherUserAuth;
    private static Long createdPetId;
    private static Long otherPetId;

    @BeforeAll
    void setUp() {
        userAuth = authTestHelper.registerAndLogin(baseUrl());
        otherUserAuth = authTestHelper.registerAndLogin(baseUrl());
    }

    // ==================== Pet CRUD ====================

    @Test
    @Order(1)
    @DisplayName("반려동물 등록 성공")
    void createPet_success() {
        Map<String, Object> body = Map.ofEntries(
                Map.entry("name", "콩이"),
                Map.entry("species", "DOG"),
                Map.entry("breed", "말티즈"),
                Map.entry("weight", 3.5),
                Map.entry("birthDate", "2020-03-15"),
                Map.entry("gender", "MALE"),
                Map.entry("isNeutered", true),
                Map.entry("vaccinationStatus", "접종 완료"),
                Map.entry("allergies", "닭고기"),
                Map.entry("specialNotes", "활발한 성격"),
                Map.entry("memo", "테스트 메모")
        );

        ResponseEntity<String> response = postWithAuth("/v1/pets", body, userAuth.getAccessToken());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(extractField(response.getBody(), "success")).isEqualTo("true");
        assertThat(extractField(response.getBody(), "data.name")).isEqualTo("콩이");
        assertThat(extractField(response.getBody(), "data.species")).isEqualTo("DOG");
        assertThat(extractField(response.getBody(), "data.speciesName")).isEqualTo("강아지");
        assertThat(extractField(response.getBody(), "data.breed")).isEqualTo("말티즈");
        assertThat(extractField(response.getBody(), "data.vaccinationStatus")).isEqualTo("접종 완료");
        assertThat(extractField(response.getBody(), "data.allergies")).isEqualTo("닭고기");
        assertThat(extractField(response.getBody(), "data.specialNotes")).isEqualTo("활발한 성격");

        createdPetId = Long.parseLong(extractField(response.getBody(), "data.id"));
    }

    @Test
    @Order(2)
    @DisplayName("반려동물 목록 조회")
    void getMyPets_success() {
        ResponseEntity<String> response = getWithAuth("/v1/pets", userAuth.getAccessToken());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(extractField(response.getBody(), "success")).isEqualTo("true");
        assertThat(extractField(response.getBody(), "data.totalCount")).isEqualTo("1");
        assertThat(extractField(response.getBody(), "data.aliveCount")).isEqualTo("1");
        assertThat(extractField(response.getBody(), "data.deceasedCount")).isEqualTo("0");
    }

    @Test
    @Order(3)
    @DisplayName("반려동물 상세 조회")
    void getPet_success() {
        ResponseEntity<String> response = getWithAuth("/v1/pets/" + createdPetId, userAuth.getAccessToken());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(extractField(response.getBody(), "data.id")).isEqualTo(createdPetId.toString());
        assertThat(extractField(response.getBody(), "data.name")).isEqualTo("콩이");
        assertThat(extractField(response.getBody(), "data.breed")).isEqualTo("말티즈");
        assertThat(extractField(response.getBody(), "data.gender")).isEqualTo("MALE");
        assertThat(extractField(response.getBody(), "data.genderName")).isEqualTo("수컷");
    }

    @Test
    @Order(4)
    @DisplayName("반려동물 수정 성공")
    void updatePet_success() {
        Map<String, Object> body = Map.of(
                "name", "보리",
                "species", "DOG",
                "breed", "골든리트리버",
                "weight", 25.0,
                "birthDate", "2021-05-10",
                "gender", "FEMALE",
                "isNeutered", false,
                "vaccinationStatus", "1차 완료",
                "memo", "수정된 메모"
        );

        ResponseEntity<String> response = putWithAuth("/v1/pets/" + createdPetId, body, userAuth.getAccessToken());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(extractField(response.getBody(), "data.name")).isEqualTo("보리");
        assertThat(extractField(response.getBody(), "data.breed")).isEqualTo("골든리트리버");
    }

    @Test
    @Order(5)
    @DisplayName("반려동물 수정 반영 확인")
    void getPet_afterUpdate() {
        ResponseEntity<String> response = getWithAuth("/v1/pets/" + createdPetId, userAuth.getAccessToken());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(extractField(response.getBody(), "data.name")).isEqualTo("보리");
        assertThat(extractField(response.getBody(), "data.breed")).isEqualTo("골든리트리버");
        assertThat(extractField(response.getBody(), "data.gender")).isEqualTo("FEMALE");
        assertThat(extractField(response.getBody(), "data.vaccinationStatus")).isEqualTo("1차 완료");
    }

    @Test
    @Order(6)
    @DisplayName("반려동물 삭제 성공")
    void deletePet_success() {
        ResponseEntity<String> response = deleteWithAuth("/v1/pets/" + createdPetId, userAuth.getAccessToken());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(extractField(response.getBody(), "success")).isEqualTo("true");
    }

    @Test
    @Order(7)
    @DisplayName("삭제 후 빈 목록")
    void getMyPets_afterDelete() {
        ResponseEntity<String> response = getWithAuth("/v1/pets", userAuth.getAccessToken());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(extractField(response.getBody(), "data.totalCount")).isEqualTo("0");
    }

    // ==================== 권한 검증 ====================

    @Test
    @Order(8)
    @DisplayName("미인증 요청 거부")
    void createPet_unauthorized() {
        Map<String, Object> body = Map.of(
                "name", "미인증펫",
                "species", "CAT"
        );

        ResponseEntity<String> response = postPublic("/v1/pets", body);

        assertThat(response.getStatusCode().value()).isIn(401, 403);
    }

    @Test
    @Order(9)
    @DisplayName("타인 반려동물 상세 조회 거부")
    void getPet_accessDenied() {
        // otherUser가 등록
        Map<String, Object> body = Map.of(
                "name", "타인펫",
                "species", "CAT"
        );
        ResponseEntity<String> createResponse = postWithAuth("/v1/pets", body, otherUserAuth.getAccessToken());
        otherPetId = Long.parseLong(extractField(createResponse.getBody(), "data.id"));

        // userAuth로 조회 시도
        ResponseEntity<String> response = getWithAuth("/v1/pets/" + otherPetId, userAuth.getAccessToken());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @Order(10)
    @DisplayName("타인 반려동물 수정 거부")
    void updatePet_accessDenied() {
        Map<String, Object> body = Map.of(
                "name", "해킹시도",
                "species", "DOG"
        );

        ResponseEntity<String> response = putWithAuth("/v1/pets/" + otherPetId, body, userAuth.getAccessToken());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @Order(11)
    @DisplayName("타인 반려동물 삭제 거부")
    void deletePet_accessDenied() {
        ResponseEntity<String> response = deleteWithAuth("/v1/pets/" + otherPetId, userAuth.getAccessToken());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    // ==================== 등록 한도 ====================

    @Test
    @Order(12)
    @DisplayName("반려동물 등록 한도 초과 (10마리 초과)")
    void createPet_limitExceeded() {
        // otherUser는 이미 1마리 있음 (Order 9에서 등록)
        // 9마리 더 등록하여 총 10마리
        for (int i = 2; i <= 10; i++) {
            Map<String, Object> body = Map.of(
                    "name", "펫" + i,
                    "species", "DOG"
            );
            ResponseEntity<String> resp = postWithAuth("/v1/pets", body, otherUserAuth.getAccessToken());
            assertThat(resp.getStatusCode())
                    .as("등록 %d번째 실패: %s", i, resp.getBody())
                    .isEqualTo(HttpStatus.CREATED);
        }

        // 11번째 등록 시도
        Map<String, Object> body = Map.of(
                "name", "초과펫",
                "species", "DOG"
        );
        ResponseEntity<String> response = postWithAuth("/v1/pets", body, otherUserAuth.getAccessToken());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(extractField(response.getBody(), "error.code")).isEqualTo("PT002");
    }

    // ==================== 체크리스트 ====================

    @Test
    @Order(13)
    @DisplayName("성향 체크리스트 생성 성공")
    void createChecklist_success() {
        // userAuth가 새 펫 등록 (기존 것은 삭제됨)
        Map<String, Object> petBody = Map.of(
                "name", "체크리스트테스트펫",
                "species", "DOG"
        );
        ResponseEntity<String> petResp = postWithAuth("/v1/pets", petBody, userAuth.getAccessToken());
        createdPetId = Long.parseLong(extractField(petResp.getBody(), "data.id"));

        Map<String, Object> body = Map.ofEntries(
                Map.entry("friendlyToStrangers", 4),
                Map.entry("friendlyToDogs", 3),
                Map.entry("friendlyToCats", 2),
                Map.entry("activityLevel", 5),
                Map.entry("barkingLevel", 3),
                Map.entry("separationAnxiety", 2),
                Map.entry("houseTraining", 4),
                Map.entry("commandTraining", 3),
                Map.entry("eatingHabit", "잘 먹음"),
                Map.entry("walkPreference", "좋아함"),
                Map.entry("fearItems", "천둥"),
                Map.entry("additionalNotes", "활발한 성격")
        );

        ResponseEntity<String> response = postWithAuth(
                "/v1/pets/" + createdPetId + "/checklist", body, userAuth.getAccessToken());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(extractField(response.getBody(), "success")).isEqualTo("true");
        assertThat(extractField(response.getBody(), "data.friendlyToStrangers")).isEqualTo("4");
        assertThat(extractField(response.getBody(), "data.activityLevel")).isEqualTo("5");
        assertThat(extractField(response.getBody(), "data.eatingHabit")).isEqualTo("잘 먹음");
        assertThat(extractField(response.getBody(), "data.petId")).isEqualTo(createdPetId.toString());
    }

    @Test
    @Order(14)
    @DisplayName("성향 체크리스트 조회 성공")
    void getChecklist_success() {
        ResponseEntity<String> response = getWithAuth(
                "/v1/pets/" + createdPetId + "/checklist", userAuth.getAccessToken());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(extractField(response.getBody(), "data.friendlyToStrangers")).isEqualTo("4");
        assertThat(extractField(response.getBody(), "data.eatingHabit")).isEqualTo("잘 먹음");
        assertThat(extractField(response.getBody(), "data.fearItems")).isEqualTo("천둥");
    }

    @Test
    @Order(15)
    @DisplayName("성향 체크리스트 수정 성공")
    void updateChecklist_success() {
        Map<String, Object> body = Map.ofEntries(
                Map.entry("friendlyToStrangers", 5),
                Map.entry("friendlyToDogs", 5),
                Map.entry("friendlyToCats", 4),
                Map.entry("activityLevel", 3),
                Map.entry("barkingLevel", 1),
                Map.entry("separationAnxiety", 1),
                Map.entry("houseTraining", 5),
                Map.entry("commandTraining", 5),
                Map.entry("eatingHabit", "편식"),
                Map.entry("walkPreference", "보통"),
                Map.entry("fearItems", "진공청소기"),
                Map.entry("additionalNotes", "수정된 메모")
        );

        ResponseEntity<String> response = putWithAuth(
                "/v1/pets/" + createdPetId + "/checklist", body, userAuth.getAccessToken());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(extractField(response.getBody(), "data.friendlyToStrangers")).isEqualTo("5");
        assertThat(extractField(response.getBody(), "data.activityLevel")).isEqualTo("3");
        assertThat(extractField(response.getBody(), "data.eatingHabit")).isEqualTo("편식");
        assertThat(extractField(response.getBody(), "data.fearItems")).isEqualTo("진공청소기");
    }

    @Test
    @Order(16)
    @DisplayName("성향 체크리스트 중복 생성 거부")
    void createChecklist_alreadyExists() {
        Map<String, Object> body = Map.ofEntries(
                Map.entry("friendlyToStrangers", 1),
                Map.entry("friendlyToDogs", 1),
                Map.entry("friendlyToCats", 1),
                Map.entry("activityLevel", 1),
                Map.entry("barkingLevel", 1),
                Map.entry("separationAnxiety", 1),
                Map.entry("houseTraining", 1),
                Map.entry("commandTraining", 1)
        );

        ResponseEntity<String> response = postWithAuth(
                "/v1/pets/" + createdPetId + "/checklist", body, userAuth.getAccessToken());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(extractField(response.getBody(), "error.code")).isEqualTo("PT004");
    }

    @Test
    @Order(17)
    @DisplayName("타인 반려동물 체크리스트 조회 거부")
    void getChecklist_accessDenied() {
        ResponseEntity<String> response = getWithAuth(
                "/v1/pets/" + createdPetId + "/checklist", otherUserAuth.getAccessToken());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }
}
