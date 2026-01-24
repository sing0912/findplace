# 반려동물 (Pet)

## 개요

사용자의 반려동물 정보를 관리하는 도메인입니다.

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
| deathDate | Date | 사망일 | Nullable |
| gender | Enum | 성별 | Nullable |
| profileImageUrl | String | 프로필 이미지 | Nullable |
| memo | Text | 메모 | Nullable |
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
| ETC | 기타 |

### Gender

| 값 | 설명 |
|----|------|
| MALE | 수컷 |
| FEMALE | 암컷 |

---

## API 목록

| Method | Endpoint | 설명 | 권한 |
|--------|----------|------|------|
| GET | /api/v1/pets | 내 반려동물 목록 | 인증된 사용자 |
| POST | /api/v1/pets | 등록 | 인증된 사용자 |
| GET | /api/v1/pets/{id} | 상세 조회 | 본인 |
| PUT | /api/v1/pets/{id} | 수정 | 본인 |
| DELETE | /api/v1/pets/{id} | 삭제 | 본인 |

---

## 비즈니스 규칙

1. 반려동물은 보호자(사용자)에게 귀속
2. 본인의 반려동물만 조회/수정/삭제 가능
3. 몸무게는 장례비 계산에 사용
4. 삭제는 Soft Delete

---

## 서브 지침

| 파일 | 설명 |
|------|------|
| [crud.md](./crud.md) | CRUD 상세 |
