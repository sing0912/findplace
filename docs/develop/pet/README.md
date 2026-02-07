# 반려동물 (Pet)

## 개요

사용자의 반려동물 정보를 관리하는 도메인입니다.
PetPro에서는 돌봄 서비스 예약 시 반려동물 정보와 성향 체크리스트를 활용합니다.

---

## 엔티티

### Pet

| 필드 | 타입 | 설명 | 제약조건 |
|------|------|------|----------|
| id | Long | PK | Auto Increment |
| userId | Long | 보호자 ID (FK) | Not Null |
| name | String | 이름 | Not Null |
| species | Enum | 종류 | Not Null |
| breed | String | 품종 | Nullable |
| weight | Decimal | 몸무게 (kg) | Not Null |
| birthDate | Date | 생년월일 | Nullable |
| gender | Enum | 성별 | Nullable |
| isNeutered | Boolean | 중성화 여부 | Not Null, Default false |
| vaccinationStatus | String | 예방접종 상태 | Nullable |
| allergies | String | 알레르기 정보 | Nullable |
| specialNotes | Text | 특이사항 | Nullable |
| profileImageUrl | String | 프로필 이미지 | Nullable |
| memo | Text | 메모 | Nullable |
| isDeceased | Boolean | 사망 여부 | Not Null, Default false |
| deceasedAt | Date | 사망일 | Nullable |
| deletedAt | DateTime | 삭제일시 (Soft Delete) | Nullable |
| createdAt | DateTime | 생성일시 | Not Null |
| updatedAt | DateTime | 수정일시 | Not Null |

### PetSpecies

| 값 | 설명 |
|----|------|
| DOG | 강아지 |
| CAT | 고양이 |
| BIRD | 새 |
| HAMSTER | 햄스터 |
| RABBIT | 토끼 |
| FISH | 물고기 |
| REPTILE | 파충류 |
| ETC | 기타 |

### Gender

| 값 | 설명 |
|----|------|
| MALE | 수컷 |
| FEMALE | 암컷 |
| UNKNOWN | 모름 |

### PetChecklist (성향 체크리스트)

| 필드 | 타입 | 설명 | 제약조건 |
|------|------|------|----------|
| id | Long | PK | Auto Increment |
| petId | Long | 반려동물 ID (FK) | Not Null, Unique |
| friendlyToStrangers | Integer | 낯선 사람에 대한 친화도 (1~5) | Not Null |
| friendlyToDogs | Integer | 다른 강아지에 대한 친화도 (1~5) | Not Null |
| friendlyToCats | Integer | 고양이에 대한 친화도 (1~5) | Not Null |
| activityLevel | Integer | 활동량 (1~5) | Not Null |
| barkingLevel | Integer | 짖음 정도 (1~5) | Not Null |
| separationAnxiety | Integer | 분리불안 정도 (1~5) | Not Null |
| houseTraining | Integer | 배변 훈련 정도 (1~5) | Not Null |
| commandTraining | Integer | 명령어 훈련 정도 (1~5) | Not Null |
| eatingHabit | String | 식사 습관 (잘 먹음/편식/소식) | Nullable |
| walkPreference | String | 산책 선호도 (좋아함/보통/싫어함) | Nullable |
| fearItems | String | 무서워하는 것 (천둥/진공청소기/불꽃 등) | Nullable |
| additionalNotes | Text | 추가 성향 메모 | Nullable |
| createdAt | DateTime | 생성일시 | Not Null |
| updatedAt | DateTime | 수정일시 | Not Null |

> 성향 체크리스트는 예약 시 시터에게 전달되어 맞춤 돌봄에 활용됩니다.

---

## 엔티티 관계

```
User (1) ──────────── (N) Pet : 회원은 여러 반려동물 보유
Pet  (1) ──────────── (0..1) PetChecklist : 반려동물은 성향 체크리스트 보유 가능
Pet  (N) ──────────── (N) Booking : 예약 시 반려동물 선택 (BookingPet N:M)
```

---

## API 목록

| Method | Endpoint | 설명 | 권한 |
|--------|----------|------|------|
| GET | /api/v1/pets | 내 반려동물 목록 | 인증된 사용자 |
| POST | /api/v1/pets | 등록 | 인증된 사용자 |
| GET | /api/v1/pets/{id} | 상세 조회 | 본인 |
| PUT | /api/v1/pets/{id} | 수정 | 본인 |
| DELETE | /api/v1/pets/{id} | 삭제 | 본인 |
| POST | /api/v1/pets/{id}/image | 프로필 이미지 업로드 | 본인 |
| PATCH | /api/v1/pets/{id}/deceased | 사망 처리 | 본인 |
| GET | /api/v1/pets/{id}/checklist | 성향 체크리스트 조회 | 본인 |
| POST | /api/v1/pets/{id}/checklist | 성향 체크리스트 작성 | 본인 |
| PUT | /api/v1/pets/{id}/checklist | 성향 체크리스트 수정 | 본인 |

---

## 데이터 모델

### pets 테이블
```sql
CREATE TABLE pets (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    name VARCHAR(100) NOT NULL,
    species VARCHAR(20) NOT NULL,
    breed VARCHAR(100),
    weight DECIMAL(5,2),
    birth_date DATE,
    gender VARCHAR(10),
    is_neutered BOOLEAN DEFAULT FALSE,
    vaccination_status VARCHAR(200),
    allergies VARCHAR(500),
    special_notes TEXT,
    profile_image_url VARCHAR(500),
    memo TEXT,
    is_deceased BOOLEAN DEFAULT FALSE,
    deceased_at DATE,
    deleted_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);
```

### pet_checklists 테이블
```sql
CREATE TABLE pet_checklists (
    id BIGSERIAL PRIMARY KEY,
    pet_id BIGINT NOT NULL UNIQUE REFERENCES pets(id),
    friendly_to_strangers INTEGER NOT NULL CHECK (friendly_to_strangers BETWEEN 1 AND 5),
    friendly_to_dogs INTEGER NOT NULL CHECK (friendly_to_dogs BETWEEN 1 AND 5),
    friendly_to_cats INTEGER NOT NULL CHECK (friendly_to_cats BETWEEN 1 AND 5),
    activity_level INTEGER NOT NULL CHECK (activity_level BETWEEN 1 AND 5),
    barking_level INTEGER NOT NULL CHECK (barking_level BETWEEN 1 AND 5),
    separation_anxiety INTEGER NOT NULL CHECK (separation_anxiety BETWEEN 1 AND 5),
    house_training INTEGER NOT NULL CHECK (house_training BETWEEN 1 AND 5),
    command_training INTEGER NOT NULL CHECK (command_training BETWEEN 1 AND 5),
    eating_habit VARCHAR(50),
    walk_preference VARCHAR(50),
    fear_items VARCHAR(500),
    additional_notes TEXT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);
```

### 인덱스
```sql
CREATE INDEX idx_pets_user_id ON pets(user_id);
CREATE INDEX idx_pets_species ON pets(species);
CREATE INDEX idx_pets_is_deceased ON pets(is_deceased);
CREATE INDEX idx_pets_deleted_at ON pets(deleted_at) WHERE deleted_at IS NULL;
CREATE UNIQUE INDEX idx_pet_checklists_pet_id ON pet_checklists(pet_id);
```

---

## 비즈니스 규칙

### 등록 규칙
- 사용자당 최대 10마리 등록 가능 (`PET_LIMIT_EXCEEDED`)
- 필수 필드: name, species
- birthDate는 과거 또는 오늘만 가능

### 나이 계산
```java
public Integer getAge() {
    if (birthDate == null) return null;
    return Period.between(birthDate, LocalDate.now()).getYears();
}
```

### 삭제 규칙
- Soft Delete 적용 (deletedAt 필드)
- 프로필 이미지 있으면 MinIO에서 함께 삭제

### 접근 권한
- 본인 소유의 반려동물만 조회/수정/삭제 가능
- 타인의 반려동물 접근 시 `ACCESS_DENIED` 에러

### 성향 체크리스트 규칙
- 반려동물 1마리당 최대 1개 체크리스트 (1:1 관계)
- 수치형 항목은 1~5 범위 (1: 매우 낮음, 5: 매우 높음)
- 체크리스트 없이도 예약 가능하나, 작성 시 시터에게 자동 전달
- Upsert 패턴: 기존 체크리스트 존재 시 수정, 없으면 생성

---

## PetChecklist DTO 스펙

### PetChecklistRequest (Create/Update 공용)

```java
public record PetChecklistRequest(
    @NotNull @Min(1) @Max(5) Integer friendlyToStrangers,
    @NotNull @Min(1) @Max(5) Integer friendlyToDogs,
    @NotNull @Min(1) @Max(5) Integer friendlyToCats,
    @NotNull @Min(1) @Max(5) Integer activityLevel,
    @NotNull @Min(1) @Max(5) Integer barkingLevel,
    @NotNull @Min(1) @Max(5) Integer separationAnxiety,
    @NotNull @Min(1) @Max(5) Integer houseTraining,
    @NotNull @Min(1) @Max(5) Integer commandTraining,
    String eatingHabit,      // "잘 먹음" | "편식" | "소식"
    String walkPreference,   // "좋아함" | "보통" | "싫어함"
    String fearItems,        // 자유 입력 (최대 500자)
    String additionalNotes   // 자유 입력 (최대 1000자)
) {}
```

### PetChecklistResponse

```java
public record PetChecklistResponse(
    Long id,
    Long petId,
    Integer friendlyToStrangers,
    Integer friendlyToDogs,
    Integer friendlyToCats,
    Integer activityLevel,
    Integer barkingLevel,
    Integer separationAnxiety,
    Integer houseTraining,
    Integer commandTraining,
    String eatingHabit,
    String walkPreference,
    String fearItems,
    String additionalNotes,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
```

---

## PetChecklist Controller 엔드포인트

### GET /api/v1/pets/{id}/checklist
- **설명:** 반려동물 성향 체크리스트 조회
- **권한:** 본인 소유 펫만
- **응답:** `ApiResponse<PetChecklistResponse>`
- **에러:** `PET_NOT_FOUND`, `ACCESS_DENIED`, `CHECKLIST_NOT_FOUND`

### POST /api/v1/pets/{id}/checklist
- **설명:** 성향 체크리스트 최초 생성
- **권한:** 본인 소유 펫만
- **요청:** `PetChecklistRequest`
- **응답:** `ApiResponse<PetChecklistResponse>` (201 Created)
- **에러:** `PET_NOT_FOUND`, `ACCESS_DENIED`, `CHECKLIST_ALREADY_EXISTS`

### PUT /api/v1/pets/{id}/checklist
- **설명:** 성향 체크리스트 수정
- **권한:** 본인 소유 펫만
- **요청:** `PetChecklistRequest`
- **응답:** `ApiResponse<PetChecklistResponse>`
- **에러:** `PET_NOT_FOUND`, `ACCESS_DENIED`, `CHECKLIST_NOT_FOUND`

---

## PetChecklist Service 비즈니스 로직

```
getChecklist(petId, userId):
  1. Pet 존재 확인 → PET_NOT_FOUND
  2. 소유권 확인 → ACCESS_DENIED
  3. PetChecklist 조회 → CHECKLIST_NOT_FOUND
  4. PetChecklistResponse 반환

createChecklist(petId, userId, request):
  1. Pet 존재 확인 → PET_NOT_FOUND
  2. 소유권 확인 → ACCESS_DENIED
  3. 기존 체크리스트 존재 확인 → CHECKLIST_ALREADY_EXISTS
  4. PetChecklist 생성 및 저장
  5. PetChecklistResponse 반환

updateChecklist(petId, userId, request):
  1. Pet 존재 확인 → PET_NOT_FOUND
  2. 소유권 확인 → ACCESS_DENIED
  3. PetChecklist 조회 → CHECKLIST_NOT_FOUND
  4. 필드 업데이트 및 저장
  5. PetChecklistResponse 반환
```

---

## PetChecklist 에러 코드

| 에러 코드 | HTTP Status | 메시지 |
|-----------|-------------|--------|
| PET_NOT_FOUND | 404 | 반려동물을 찾을 수 없습니다 |
| ACCESS_DENIED | 403 | 접근 권한이 없습니다 |
| CHECKLIST_NOT_FOUND | 404 | 성향 체크리스트를 찾을 수 없습니다 |
| CHECKLIST_ALREADY_EXISTS | 409 | 이미 성향 체크리스트가 존재합니다 |
| PET_LIMIT_EXCEEDED | 400 | 반려동물은 최대 10마리까지 등록 가능합니다 |

---

## 파일 업로드 (MinIO)

### 설정
- 저장 경로: `petpro/pets/{petId}/profile_{uuid}.{ext}`
- 허용 확장자: jpg, jpeg, png, gif, webp
- 최대 크기: 5MB

---

## 패키지 구조
```
domain/pet/
├── entity/
│   ├── Pet.java
│   ├── PetChecklist.java
│   ├── Species.java
│   └── Gender.java
├── repository/
│   ├── PetRepository.java
│   └── PetChecklistRepository.java
├── service/
│   ├── PetService.java
│   └── PetImageService.java
├── controller/
│   └── PetController.java
└── dto/
    ├── PetRequest.java
    ├── PetResponse.java
    ├── PetChecklistRequest.java
    └── PetChecklistResponse.java
```

---

## E2E 테스트

### 테스트 파일
- `e2e/pet/PetE2ETest.java`

### 테스트 시나리오 (17개)

| # | 카테고리 | 시나리오 | 검증 |
|---|----------|----------|------|
| 1 | CRUD | POST /v1/pets 등록 | 201, 필드 검증 |
| 2 | CRUD | GET /v1/pets 목록 조회 | totalCount=1 |
| 3 | CRUD | GET /v1/pets/{id} 상세 조회 | 필드 일치 |
| 4 | CRUD | PUT /v1/pets/{id} 수정 | 200, 수정 반영 |
| 5 | CRUD | GET /v1/pets/{id} 수정 반영 확인 | 수정된 값 |
| 6 | CRUD | DELETE /v1/pets/{id} 삭제 | 200 |
| 7 | CRUD | GET /v1/pets 삭제 후 빈 목록 | totalCount=0 |
| 8 | 권한 | POST /v1/pets 미인증 | 401/403 |
| 9 | 권한 | GET /v1/pets/{id} 타인 | 403 |
| 10 | 권한 | PUT /v1/pets/{id} 타인 | 403 |
| 11 | 권한 | DELETE /v1/pets/{id} 타인 | 403 |
| 12 | 한도 | 10마리 등록 후 11번째 | 400 PT002 |
| 13 | 체크리스트 | POST /v1/pets/{id}/checklist | 201 |
| 14 | 체크리스트 | GET /v1/pets/{id}/checklist | 조회 |
| 15 | 체크리스트 | PUT /v1/pets/{id}/checklist | 수정 |
| 16 | 체크리스트 | POST 중복 생성 | 409 PT004 |
| 17 | 체크리스트 | GET 타인 체크리스트 | 403 |

---

## 관련 도메인

- **User**: 반려동물 소유자 (1:N 관계)
- **Booking**: 예약 시 반려동물 선택 (N:M, BookingPet 중간 테이블)
- **Sitter**: 시터가 돌봄 시 반려동물 성향 정보 참조
