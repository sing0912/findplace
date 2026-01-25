# 반려동물 도메인 설계

## 1. 개요

반려동물(Pet) 도메인은 회원이 등록한 반려동물 정보를 관리합니다. 여러 마리 등록이 가능하며, 사망한 반려동물은 추모관과 연동됩니다.

---

## 2. 엔티티 설계

### 2.1 Pet 엔티티

```
┌─────────────────────────────────────────────────────────────┐
│                           Pet                                │
├─────────────────────────────────────────────────────────────┤
│  id                  BIGINT PK AUTO_INCREMENT               │
│  userId              BIGINT FK NOT NULL (→ User)            │
│                                                              │
│  [기본 정보]                                                  │
│  name                VARCHAR(100) NOT NULL                  │
│  species             VARCHAR(20) NOT NULL                   │
│  breed               VARCHAR(100)                           │
│  birthDate           DATE                                   │
│  gender              VARCHAR(10)                            │
│  isNeutered          BOOLEAN DEFAULT FALSE                  │
│                                                              │
│  [프로필]                                                     │
│  profileImageUrl     VARCHAR(500)                           │
│  memo                TEXT                                   │
│                                                              │
│  [사망 정보]                                                  │
│  isDeceased          BOOLEAN DEFAULT FALSE                  │
│  deceasedAt          DATE                                   │
│                                                              │
│  [Soft Delete]                                               │
│  deletedAt           TIMESTAMP                              │
│                                                              │
│  [Audit]                                                     │
│  createdAt           TIMESTAMP NOT NULL                     │
│  updatedAt           TIMESTAMP NOT NULL                     │
└─────────────────────────────────────────────────────────────┘
```

### 2.2 Species (종류) 열거형

| 값 | 설명 | 아이콘 |
|----|------|--------|
| DOG | 강아지 | 🐕 |
| CAT | 고양이 | 🐈 |
| BIRD | 새 | 🐦 |
| HAMSTER | 햄스터 | 🐹 |
| RABBIT | 토끼 | 🐰 |
| FISH | 물고기 | 🐟 |
| REPTILE | 파충류 | 🦎 |
| ETC | 기타 | 🐾 |

### 2.3 Gender (성별) 열거형

| 값 | 설명 |
|----|------|
| MALE | 수컷 |
| FEMALE | 암컷 |
| UNKNOWN | 모름 |

---

## 3. API 설계

### 3.1 반려동물 API

| Method | Endpoint | 설명 | 권한 |
|--------|----------|------|------|
| GET | /pets | 내 반려동물 목록 | 인증 |
| GET | /pets/{id} | 반려동물 상세 조회 | 본인 소유 |
| POST | /pets | 반려동물 등록 | 인증 |
| PUT | /pets/{id} | 반려동물 정보 수정 | 본인 소유 |
| DELETE | /pets/{id} | 반려동물 삭제 | 본인 소유 |
| PATCH | /pets/{id}/deceased | 사망 처리 | 본인 소유 |

### 3.2 관리자 API

| Method | Endpoint | 설명 | 권한 |
|--------|----------|------|------|
| GET | /admin/users/{userId}/pets | 특정 회원의 반려동물 목록 | ADMIN |

---

## 4. 요청/응답 DTO

### 4.1 반려동물 등록 요청

```json
{
  "name": "콩이",
  "species": "DOG",
  "breed": "말티즈",
  "birthDate": "2020-03-15",
  "gender": "MALE",
  "isNeutered": true,
  "memo": "활발하고 사람을 좋아함"
}
```

### 4.2 반려동물 응답

```json
{
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
  "profileImageUrl": "https://storage.findplace.com/pets/1.jpg",
  "memo": "활발하고 사람을 좋아함",
  "isDeceased": false,
  "deceasedAt": null,
  "createdAt": "2025-01-20T10:00:00",
  "updatedAt": "2025-01-20T10:00:00"
}
```

### 4.3 사망 처리 요청

```json
{
  "deceasedAt": "2025-01-15"
}
```

### 4.4 반려동물 목록 응답

```json
{
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
      "profileImageUrl": "https://storage.findplace.com/pets/1.jpg"
    },
    {
      "id": 2,
      "name": "보리",
      "species": "DOG",
      "speciesName": "강아지",
      "breed": "골든리트리버",
      "age": null,
      "gender": "MALE",
      "isDeceased": true,
      "deceasedAt": "2023-05-20",
      "profileImageUrl": "https://storage.findplace.com/pets/2.jpg"
    }
  ],
  "totalCount": 2,
  "aliveCount": 1,
  "deceasedCount": 1
}
```

---

## 5. 비즈니스 로직

### 5.1 반려동물 등록

```
1. 회원 인증 확인
2. 필수 필드 검증 (name, species)
3. 프로필 이미지 업로드 (MinIO)
4. Pet 엔티티 생성
5. DB 저장
```

### 5.2 나이 계산

```java
public Integer getAge() {
    if (birthDate == null) return null;
    return Period.between(birthDate, LocalDate.now()).getYears();
}
```

### 5.3 사망 처리

```
1. isDeceased = true
2. deceasedAt = 입력된 사망일
3. 추모관 연동 데이터 생성 (선택)
```

### 5.4 반려동물 삭제

```
1. Soft Delete 처리 (deletedAt = 현재 시간)
2. 목록 조회 시 제외
```

---

## 6. 유효성 검증

| 필드 | 규칙 |
|------|------|
| name | 필수, 1-100자 |
| species | 필수, enum 값 |
| breed | 선택, 최대 100자 |
| birthDate | 선택, 과거 또는 오늘 |
| gender | 선택, enum 값 |
| memo | 선택, 최대 1000자 |
| deceasedAt | isDeceased=true일 때 필수, birthDate 이후 |

---

## 7. 인덱스

```sql
CREATE INDEX idx_pets_user_id ON pets(user_id);
CREATE INDEX idx_pets_species ON pets(species);
CREATE INDEX idx_pets_is_deceased ON pets(is_deceased);
CREATE INDEX idx_pets_deleted_at ON pets(deleted_at) WHERE deleted_at IS NULL;
```

---

## 8. 연관 관계

```
User (1) ──────────── (N) Pet       : 회원은 여러 반려동물 보유
Pet  (1) ──────────── (1) Memorial  : 사망한 반려동물은 추모관 연동 (추후)
```

---

## 9. 프론트엔드 UI

### 9.1 반려동물 카드

```
┌─────────────────────┐
│ 🐕                   │
│ 콩이                 │
│ 말티즈 · 수컷        │
│ 2020.03.15 (4세)    │
│                     │
│ [수정]  [삭제]       │
└─────────────────────┘
```

### 9.2 사망한 반려동물 카드

```
┌─────────────────────┐
│ 🐕  🌈               │  ← 무지개다리 표시
│ 보리 (추모 중)        │
│ 골든리트리버 · 수컷   │
│ 2010.01.10 ~ 2023.05.20│
│                     │
│ [추모관 보기]        │
└─────────────────────┘
```

### 9.3 등록/수정 폼

```
┌─────────────────────────────────────────────────────────────┐
│  반려동물 등록                                         [X]    │
├─────────────────────────────────────────────────────────────┤
│                                                              │
│  ┌────────┐                                                  │
│  │ 사진   │  [이미지 선택]                                    │
│  └────────┘                                                  │
│                                                              │
│  이름 *        [                            ]                │
│                                                              │
│  종류 *        [강아지 ▼]                                     │
│                                                              │
│  품종          [말티즈                      ]                 │
│                                                              │
│  생년월일      [2020] 년 [03] 월 [15] 일                      │
│                                                              │
│  성별          ● 수컷  ○ 암컷  ○ 모름                         │
│                                                              │
│  중성화        ☑ 완료                                         │
│                                                              │
│  특이사항      [                            ]                 │
│               [                            ]                 │
│                                                              │
│  ☐ 무지개다리를 건넜어요                                       │
│     사망일    [2023] 년 [05] 월 [20] 일                       │
│                                                              │
│                              [취소]  [저장]                   │
└─────────────────────────────────────────────────────────────┘
```

---

## 10. 파일 업로드

### 10.1 프로필 이미지

- 저장 위치: MinIO - `findplace/pets/{petId}/profile.{ext}`
- 허용 확장자: jpg, jpeg, png, gif, webp
- 최대 크기: 5MB
- 이미지 리사이징: 400x400 (썸네일)

### 10.2 업로드 프로세스

```
1. 클라이언트 → 백엔드: 파일 업로드 요청
2. 백엔드: 확장자, 크기 검증
3. 백엔드 → MinIO: 파일 저장
4. 백엔드: URL 반환
5. Pet 엔티티 profileImageUrl 업데이트
```

---

## 11. 추후 확장 고려

- 예방접종 기록 관리
- 건강 기록 관리
- 반려동물 보험 연동
- 추모관 서비스 연동
- 반려동물 SNS 공유 기능
