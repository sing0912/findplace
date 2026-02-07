# 시터 (Sitter)

## 개요

펫시터 프로필, 서비스, 자격증빙, 환경사진을 관리하는 도메인입니다.
PARTNER 역할의 사용자가 시터 프로필을 생성하고, CUSTOMER 역할의 사용자가 시터를 검색/조회합니다.

---

## 엔티티

### Partner

시터의 기본 정보. User(role=PARTNER)와 1:1 관계.

| 필드 | 타입 | 설명 | 제약조건 |
|------|------|------|----------|
| id | Long | PK | Auto Increment |
| userId | Long | 사용자 ID (FK) | Unique, Not Null |
| nickname | String | 활동 닉네임 | Not Null |
| introduction | Text | 자기소개 | Nullable |
| experience | String | 경력 요약 | Nullable |
| address | String | 활동 지역 주소 | Not Null |
| addressDetail | String | 상세 주소 | Nullable |
| latitude | Decimal | 위도 | Not Null |
| longitude | Decimal | 경도 | Not Null |
| profileImageUrl | String | 프로필 이미지 | Nullable |
| verificationStatus | Enum | 심사 상태 | Not Null |
| averageRating | Decimal | 평균 평점 (0.0~5.0) | Default 0 |
| reviewCount | Integer | 후기 수 | Default 0 |
| completedBookingCount | Integer | 완료 예약 수 | Default 0 |
| isActive | Boolean | 활동 가능 여부 | Default false |
| approvedAt | DateTime | 심사 승인일시 | Nullable |
| createdAt | DateTime | 생성일시 | Not Null |
| updatedAt | DateTime | 수정일시 | Not Null |

### VerificationStatus (심사 상태)

| 값 | 설명 |
|----|------|
| PENDING | 심사 대기 |
| REVIEWING | 심사 중 |
| APPROVED | 승인 |
| REJECTED | 반려 |
| SUSPENDED | 일시 정지 |

### SitterProfile (시터 상세 프로필)

프로필 4단계 설정의 2단계(활동 정보)에서 작성.

| 필드 | 타입 | 설명 | 제약조건 |
|------|------|------|----------|
| id | Long | PK | Auto Increment |
| partnerId | Long | 시터 ID (FK) | Unique, Not Null |
| acceptablePetTypes | String | 수용 가능 반려동물 (DOG,CAT 등) | Not Null |
| acceptablePetSizes | String | 수용 가능 크기 (SMALL,MEDIUM,LARGE) | Not Null |
| maxPetCount | Integer | 동시 돌봄 최대 마릿수 | Not Null, Default 3 |
| hasYard | Boolean | 마당 유무 | Default false |
| hasOwnPets | Boolean | 본인 반려동물 유무 | Default false |
| ownPetsDescription | String | 본인 반려동물 설명 | Nullable |
| smokingStatus | Boolean | 흡연 여부 | Default false |
| emergencyContact | String | 비상 연락처 | Nullable |
| createdAt | DateTime | 생성일시 | Not Null |
| updatedAt | DateTime | 수정일시 | Not Null |

### SitterService (서비스 유형별 요금)

| 필드 | 타입 | 설명 | 제약조건 |
|------|------|------|----------|
| id | Long | PK | Auto Increment |
| partnerId | Long | 시터 ID (FK) | Not Null |
| serviceType | Enum | 서비스 유형 | Not Null |
| basePrice | Integer | 기본 요금 (원) | Not Null |
| additionalPetPrice | Integer | 추가 마릿수 요금 | Default 0 |
| description | Text | 서비스 설명 | Nullable |
| isActive | Boolean | 서비스 활성 여부 | Default true |
| createdAt | DateTime | 생성일시 | Not Null |
| updatedAt | DateTime | 수정일시 | Not Null |

#### ServiceType (서비스 유형)

| 값 | 설명 | 단위 |
|----|------|------|
| DAY_CARE | 데이케어 (당일 돌봄) | 1일 |
| BOARDING | 위탁 돌봄 (1박 이상) | 1박 |
| WALKING | 산책 대행 | 1회 (30분) |
| HOME_VISIT | 방문 돌봄 | 1회 (1시간) |
| GROOMING | 목욕/미용 | 1회 |

### SitterDocument (자격 증빙)

| 필드 | 타입 | 설명 | 제약조건 |
|------|------|------|----------|
| id | Long | PK | Auto Increment |
| partnerId | Long | 시터 ID (FK) | Not Null |
| documentType | Enum | 서류 유형 | Not Null |
| fileUrl | String | 파일 URL | Not Null |
| fileName | String | 원본 파일명 | Not Null |
| verificationStatus | Enum | 검증 상태 | Default PENDING |
| rejectionReason | String | 반려 사유 | Nullable |
| createdAt | DateTime | 생성일시 | Not Null |
| updatedAt | DateTime | 수정일시 | Not Null |

#### DocumentType (서류 유형)

| 값 | 필수 | 설명 |
|----|------|------|
| ID_CARD | 필수 | 신분증 |
| CRIMINAL_RECORD | 필수 | 범죄경력회보서 |
| PET_SITTER_CERT | 선택 | 반려동물관리사 자격증 |
| ANIMAL_CARE_CERT | 선택 | 동물돌봄전문가 자격증 |
| VET_TECH_CERT | 선택 | 동물보건사 자격증 |
| ETC | 선택 | 기타 관련 자격증 |

### SitterEnvironmentPhoto (환경 사진)

| 필드 | 타입 | 설명 | 제약조건 |
|------|------|------|----------|
| id | Long | PK | Auto Increment |
| partnerId | Long | 시터 ID (FK) | Not Null |
| imageUrl | String | 이미지 URL | Not Null |
| caption | String | 사진 설명 | Nullable |
| sortOrder | Integer | 정렬 순서 | Default 0 |
| createdAt | DateTime | 생성일시 | Not Null |

---

## 엔티티 관계

```
User(PARTNER) (1) ─── (1) Partner : 시터 기본 정보
Partner (1) ────────── (0..1) SitterProfile : 상세 프로필
Partner (1) ────────── (N) SitterService : 서비스별 요금 (1~5개)
Partner (1) ────────── (N) SitterDocument : 자격 증빙 서류 (2~10개)
Partner (1) ────────── (N) SitterEnvironmentPhoto : 환경 사진 (0~20장)
Partner (1) ────────── (N) Booking : 예약 수락/관리
Partner (1) ────────── (N) SitterReview : 후기 수신
```

---

## 프로필 4단계 설정

시터 온보딩 시 4단계로 프로필을 설정합니다.

| 단계 | 이름 | 대상 엔티티 | 필수 |
|------|------|-------------|------|
| Step 1 | 기본 정보 | Partner (nickname, address, introduction 등) | 필수 |
| Step 2 | 활동 정보 | SitterProfile (수용 가능 반려동물, 환경 정보) | 필수 |
| Step 3 | 환경 사진 | SitterEnvironmentPhoto (최소 3장) | 필수 |
| Step 4 | 서비스 & 요금 | SitterService (최소 1개 서비스) | 필수 |

---

## 비즈니스 규칙

### 시터 등록
- PARTNER 역할의 사용자만 시터 등록 가능
- 프로필 4단계 모두 완료해야 심사 요청 가능
- 필수 서류(신분증, 범죄경력회보서) 제출 필수

### 심사
- 관리자가 서류 검토 후 승인/반려/보류 처리
- 승인 시 `isActive = true`, 시터 검색에 노출
- 반려 시 사유 기입 필수, 서류 재제출 가능

### 검색 노출 조건
- `verificationStatus = APPROVED`
- `isActive = true`
- 최소 1개 이상 활성 서비스

### 환경 사진
- 최소 3장, 최대 20장
- 허용 확장자: jpg, jpeg, png, webp
- 최대 크기: 10MB/장
- 저장 경로: `petpro/sitters/{partnerId}/env_{uuid}.{ext}`

---

## 패키지 구조
```
domain/sitter/
├── entity/
│   ├── Partner.java
│   ├── SitterProfile.java
│   ├── SitterService.java
│   ├── SitterDocument.java
│   ├── SitterEnvironmentPhoto.java
│   ├── VerificationStatus.java
│   ├── ServiceType.java
│   └── DocumentType.java
├── repository/
│   ├── PartnerRepository.java
│   ├── SitterProfileRepository.java
│   ├── SitterServiceRepository.java
│   ├── SitterDocumentRepository.java
│   └── SitterEnvironmentPhotoRepository.java
├── service/
│   ├── SitterSearchService.java
│   ├── SitterProfileService.java
│   └── SitterDocumentService.java
├── controller/
│   ├── SitterSearchController.java
│   └── SitterProfileController.java
└── dto/
    ├── SitterSearchRequest.java
    ├── SitterSearchResponse.java
    ├── SitterDetailResponse.java
    ├── SitterProfileRequest.java
    └── SitterServiceRequest.java
```

---

## 데이터 모델

### partners 테이블
```sql
CREATE TABLE partners (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE REFERENCES users(id),
    nickname VARCHAR(50) NOT NULL,
    introduction TEXT,
    experience VARCHAR(200),
    address VARCHAR(300) NOT NULL,
    address_detail VARCHAR(200),
    latitude DECIMAL(10, 7) NOT NULL,
    longitude DECIMAL(10, 7) NOT NULL,
    profile_image_url VARCHAR(500),
    verification_status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    average_rating DECIMAL(2, 1) DEFAULT 0,
    review_count INTEGER DEFAULT 0,
    completed_booking_count INTEGER DEFAULT 0,
    is_active BOOLEAN DEFAULT FALSE,
    approved_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);
```

### sitter_profiles 테이블
```sql
CREATE TABLE sitter_profiles (
    id BIGSERIAL PRIMARY KEY,
    partner_id BIGINT NOT NULL UNIQUE REFERENCES partners(id),
    acceptable_pet_types VARCHAR(100) NOT NULL,
    acceptable_pet_sizes VARCHAR(100) NOT NULL,
    max_pet_count INTEGER NOT NULL DEFAULT 3,
    has_yard BOOLEAN DEFAULT FALSE,
    has_own_pets BOOLEAN DEFAULT FALSE,
    own_pets_description VARCHAR(200),
    smoking_status BOOLEAN DEFAULT FALSE,
    emergency_contact VARCHAR(20),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);
```

### sitter_services 테이블
```sql
CREATE TABLE sitter_services (
    id BIGSERIAL PRIMARY KEY,
    partner_id BIGINT NOT NULL REFERENCES partners(id),
    service_type VARCHAR(20) NOT NULL,
    base_price INTEGER NOT NULL,
    additional_pet_price INTEGER DEFAULT 0,
    description TEXT,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    UNIQUE(partner_id, service_type)
);
```

### sitter_documents 테이블
```sql
CREATE TABLE sitter_documents (
    id BIGSERIAL PRIMARY KEY,
    partner_id BIGINT NOT NULL REFERENCES partners(id),
    document_type VARCHAR(30) NOT NULL,
    file_url VARCHAR(500) NOT NULL,
    file_name VARCHAR(200) NOT NULL,
    verification_status VARCHAR(20) DEFAULT 'PENDING',
    rejection_reason VARCHAR(500),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);
```

### sitter_environment_photos 테이블
```sql
CREATE TABLE sitter_environment_photos (
    id BIGSERIAL PRIMARY KEY,
    partner_id BIGINT NOT NULL REFERENCES partners(id),
    image_url VARCHAR(500) NOT NULL,
    caption VARCHAR(200),
    sort_order INTEGER DEFAULT 0,
    created_at TIMESTAMP NOT NULL
);
```

### 인덱스
```sql
CREATE UNIQUE INDEX idx_partners_user_id ON partners(user_id);
CREATE INDEX idx_partners_verification_status ON partners(verification_status);
CREATE INDEX idx_partners_is_active ON partners(is_active);
CREATE INDEX idx_partners_location ON partners(latitude, longitude);
CREATE INDEX idx_partners_average_rating ON partners(average_rating DESC);
CREATE UNIQUE INDEX idx_sitter_profiles_partner_id ON sitter_profiles(partner_id);
CREATE INDEX idx_sitter_services_partner_id ON sitter_services(partner_id);
CREATE INDEX idx_sitter_services_type ON sitter_services(service_type);
CREATE INDEX idx_sitter_documents_partner_id ON sitter_documents(partner_id);
CREATE INDEX idx_sitter_env_photos_partner_id ON sitter_environment_photos(partner_id);
```

---

## 서브 지침

| 파일 | 설명 |
|------|------|
| [api.md](./api.md) | 시터 검색/상세/프로필 API 상세 스펙 |
| [frontend.md](./frontend.md) | 시터 검색/상세 프론트엔드 UI 지침 |

---

## 관련 도메인

- **User**: PARTNER 역할 사용자 (1:1)
- **Booking**: 예약 요청/수락 (Phase 2)
- **Review**: 시터 후기/평점 (Phase 3)
- **Availability**: 시터 일정 차단 (Phase 2)
- **Payout**: 시터 정산 (Phase 4)
