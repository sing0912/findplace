# Pet (반려동물) 도메인 영구지침

## 1. 개요

반려동물(Pet) 도메인은 회원이 등록한 반려동물 정보를 관리합니다. 여러 마리 등록이 가능하며, 사망한 반려동물은 추모관과 연동됩니다.

### 1.1 도메인 범위
- 반려동물 기본 정보 관리 (이름, 종류, 품종, 생년월일, 성별 등)
- 프로필 이미지 관리 (MinIO 연동)
- 사망 처리 및 추모 기능 연동
- 사용자당 최대 10마리 등록 가능

### 1.2 주요 기능
- 반려동물 CRUD
- 프로필 이미지 업로드/삭제
- 사망 처리
- Soft Delete

---

## 2. 아키텍처

### 2.1 패키지 구조
```
domain/pet/
├── entity/
│   ├── Pet.java          # 반려동물 엔티티
│   ├── Species.java      # 종류 enum
│   └── Gender.java       # 성별 enum
├── repository/
│   └── PetRepository.java
├── service/
│   ├── PetService.java
│   └── PetImageService.java  # MinIO 연동
├── controller/
│   └── PetController.java
└── dto/
    ├── PetRequest.java   # 요청 DTO (Create, Update, Deceased)
    └── PetResponse.java  # 응답 DTO (Detail, Summary, ListDto)
```

### 2.2 엔티티 관계
```
User (1) ──────────── (N) Pet : 회원은 여러 반려동물 보유
Pet  (1) ──────────── (1) Memorial : 사망한 반려동물은 추모관 연동 (추후)
```

---

## 3. API 명세

### 3.1 사용자 API

| Method | Endpoint | 설명 | 권한 |
|--------|----------|------|------|
| GET | `/pets` | 내 반려동물 목록 | 인증 |
| GET | `/pets/{id}` | 반려동물 상세 조회 | 본인 소유 |
| POST | `/pets` | 반려동물 등록 | 인증 |
| PUT | `/pets/{id}` | 반려동물 정보 수정 | 본인 소유 |
| POST | `/pets/{id}/image` | 프로필 이미지 업로드 | 본인 소유 |
| DELETE | `/pets/{id}` | 반려동물 삭제 | 본인 소유 |
| PATCH | `/pets/{id}/deceased` | 사망 처리 | 본인 소유 |

### 3.2 응답 형식

#### 반려동물 목록 (`GET /pets`)
```json
{
  "success": true,
  "data": {
    "content": [
      {
        "id": 1,
        "name": "콩이",
        "species": "DOG",
        "speciesName": "강아지",
        "breed": "말티즈",
        "age": 4,
        "gender": "MALE",
        "isDeceased": false,
        "profileImageUrl": "..."
      }
    ],
    "totalCount": 2,
    "aliveCount": 1,
    "deceasedCount": 1
  }
}
```

#### 반려동물 상세 (`GET /pets/{id}`)
```json
{
  "success": true,
  "data": {
    "id": 1,
    "userId": 1,
    "name": "콩이",
    "species": "DOG",
    "speciesName": "강아지",
    "breed": "말티즈",
    "birthDate": "2020-03-15",
    "age": 4,
    "gender": "MALE",
    "genderName": "수컷",
    "isNeutered": true,
    "profileImageUrl": "...",
    "memo": "활발하고 사람을 좋아함",
    "isDeceased": false,
    "deceasedAt": null,
    "createdAt": "2025-01-20T10:00:00",
    "updatedAt": "2025-01-20T10:00:00"
  }
}
```

---

## 4. 데이터 모델

### 4.1 pets 테이블
```sql
CREATE TABLE pets (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    name VARCHAR(100) NOT NULL,
    species VARCHAR(20) NOT NULL,
    breed VARCHAR(100),
    birth_date DATE,
    gender VARCHAR(10),
    is_neutered BOOLEAN DEFAULT FALSE,
    profile_image_url VARCHAR(500),
    memo TEXT,
    is_deceased BOOLEAN DEFAULT FALSE,
    deceased_at DATE,
    deleted_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);
```

### 4.2 인덱스
```sql
CREATE INDEX idx_pets_user_id ON pets(user_id);
CREATE INDEX idx_pets_species ON pets(species);
CREATE INDEX idx_pets_is_deceased ON pets(is_deceased);
CREATE INDEX idx_pets_deleted_at ON pets(deleted_at) WHERE deleted_at IS NULL;
```

### 4.3 Species enum
| 값 | 한글명 |
|----|--------|
| DOG | 강아지 |
| CAT | 고양이 |
| BIRD | 새 |
| HAMSTER | 햄스터 |
| RABBIT | 토끼 |
| FISH | 물고기 |
| REPTILE | 파충류 |
| ETC | 기타 |

### 4.4 Gender enum
| 값 | 한글명 |
|----|--------|
| MALE | 수컷 |
| FEMALE | 암컷 |
| UNKNOWN | 모름 |

---

## 5. 비즈니스 규칙

### 5.1 등록 규칙
- 사용자당 최대 10마리 등록 가능 (`PET_LIMIT_EXCEEDED`)
- 필수 필드: name, species
- birthDate는 과거 또는 오늘만 가능

### 5.2 나이 계산
```java
public Integer getAge() {
    if (birthDate == null) return null;
    LocalDate endDate = isDeceased && deceasedAt != null ? deceasedAt : LocalDate.now();
    return Period.between(birthDate, endDate).getYears();
}
```

### 5.3 사망 처리 규칙
- deceasedAt은 birthDate 이후여야 함
- 사망한 반려동물은 나이를 사망일 기준으로 계산

### 5.4 삭제 규칙
- Soft Delete 적용 (deletedAt 필드)
- 프로필 이미지 있으면 MinIO에서 함께 삭제

### 5.5 접근 권한
- 본인 소유의 반려동물만 조회/수정/삭제 가능
- 타인의 반려동물 접근 시 `ACCESS_DENIED` 에러

---

## 6. 파일 업로드 (MinIO)

### 6.1 설정
- 저장 경로: `findplace/pets/{petId}/profile_{uuid}.{ext}`
- 허용 확장자: jpg, jpeg, png, gif, webp
- 최대 크기: 5MB

### 6.2 PetImageService
```java
@Service
public class PetImageService {
    public String uploadProfileImage(Long petId, MultipartFile file);
    public void deleteProfileImage(String imageUrl);
}
```

---

## 7. 프론트엔드

### 7.1 파일 구조
```
frontend/src/
├── types/pet.ts              # 타입 정의
├── api/pet.ts                # API 서비스
├── hooks/usePets.ts          # 커스텀 훅
├── components/pet/
│   ├── PetCard.tsx          # 반려동물 카드
│   └── PetForm.tsx          # 등록/수정 폼
└── pages/pet/
    └── PetListPage.tsx      # 목록 페이지
```

### 7.2 주요 훅
```typescript
// 목록 조회
const { pets, totalCount, aliveCount, deceasedCount, loading, refetch } = useMyPets();

// 상세 조회
const { pet, loading } = usePet(id);

// CRUD 작업
const { createPet, updatePet, deletePet, markAsDeceased, uploadImage } = usePetMutations();
```

### 7.3 컴포넌트 사용
```tsx
// 카드
<PetCard pet={pet} onEdit={handleEdit} onDelete={handleDelete} />

// 폼
<PetForm pet={pet} onSubmit={handleSubmit} onCancel={handleCancel} />
```

---

## 8. 테스트 가이드

### 8.1 단위 테스트
```java
// PetTest.java - 엔티티 테스트
@Test
void shouldCalculateAgeWhenBirthDateExists() {
    Pet pet = Pet.builder()
        .birthDate(LocalDate.now().minusYears(3))
        .build();
    assertThat(pet.getAge()).isEqualTo(3);
}

// PetServiceTest.java - 서비스 테스트
@Test
void shouldThrowExceptionWhenLimitExceeded() {
    given(petRepository.countByUserId(1L)).willReturn(10L);
    assertThatThrownBy(() -> petService.createPet(1L, request))
        .isInstanceOf(BusinessException.class)
        .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PET_LIMIT_EXCEEDED);
}
```

### 8.2 API 테스트
```bash
# 반려동물 등록
curl -X POST http://localhost:8080/api/pets \
  -H "Authorization: Bearer {token}" \
  -H "Content-Type: application/json" \
  -d '{"name":"콩이","species":"DOG","breed":"말티즈"}'

# 목록 조회
curl http://localhost:8080/api/pets \
  -H "Authorization: Bearer {token}"

# 이미지 업로드
curl -X POST http://localhost:8080/api/pets/1/image \
  -H "Authorization: Bearer {token}" \
  -F "file=@/path/to/image.jpg"
```

---

## 9. 트러블슈팅

### 9.1 이미지 업로드 실패
- MinIO 연결 상태 확인
- 파일 크기 (5MB 이하) 확인
- 허용 확장자 확인

### 9.2 등록 한도 에러
```
ErrorCode: PET_LIMIT_EXCEEDED
원인: 사용자당 10마리 초과
해결: 기존 반려동물 삭제 후 등록
```

### 9.3 접근 거부 에러
```
ErrorCode: ACCESS_DENIED
원인: 본인 소유가 아닌 반려동물 접근
해결: 인증 토큰의 userId와 반려동물 소유자 확인
```

---

## 10. 관련 도메인

- **User**: 반려동물 소유자 (1:N 관계)
- **Memorial**: 사망한 반려동물 추모 (추후 연동)
- **FuneralHome**: 장례 서비스 연동 가능
