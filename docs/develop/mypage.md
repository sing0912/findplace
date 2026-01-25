# MyPage 도메인 영구지침

## 1. 개요

마이페이지(MyPage)는 사용자가 자신의 프로필, 반려동물, 쿠폰, 주변 장례식장 등을 관리하는 기능을 제공합니다.

### 1.1 주요 기능
- 프로필 조회/수정
- 비밀번호 변경
- 내 반려동물 관리 (Pet 도메인 연동)
- 내 쿠폰함 (Coupon 도메인 연동)
- 내 주변 장례식장 (FuneralHome + Location 연동)
- 회원 탈퇴

---

## 2. 아키텍처

### 2.1 연관 도메인
```
MyPage
├── User        프로필, 비밀번호, 주소
├── Pet         반려동물 관리
├── Coupon      쿠폰함 (별도 DB)
├── FuneralHome 주변 장례식장
└── Location    주소 좌표 변환
```

### 2.2 프론트엔드 구조 (계획)
```
frontend/src/pages/mypage/
├── MyPageMain.tsx          메인 (대시보드)
├── ProfileEditPage.tsx     프로필 수정
├── PasswordChangePage.tsx  비밀번호 변경
├── MyPetsPage.tsx          반려동물 목록
├── MyCouponsPage.tsx       쿠폰함
├── NearbyFuneralHomesPage.tsx  주변 장례식장
└── WithdrawalPage.tsx      회원 탈퇴
```

---

## 3. API 명세

### 3.1 프로필 관리

| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | /api/v1/my/profile | 내 프로필 조회 |
| PUT | /api/v1/my/profile | 프로필 수정 |
| PUT | /api/v1/my/password | 비밀번호 변경 |
| PUT | /api/v1/my/address | 주소 변경 |

### 3.2 반려동물 관리

| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | /api/v1/my/pets | 내 반려동물 목록 |
| POST | /api/v1/my/pets | 반려동물 등록 |
| GET | /api/v1/my/pets/{id} | 반려동물 상세 |
| PUT | /api/v1/my/pets/{id} | 반려동물 수정 |
| DELETE | /api/v1/my/pets/{id} | 반려동물 삭제 |

### 3.3 쿠폰함

| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | /api/v1/coupons/my | 내 쿠폰 목록 |
| GET | /api/v1/coupons/my/available | 사용 가능 쿠폰 |
| POST | /api/v1/coupons/register | 쿠폰 코드 등록 |

### 3.4 주변 장례식장

| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | /api/v1/funeral-homes/nearby | 현재 위치 기준 검색 |
| GET | /api/v1/my/address/nearby-funeral-homes | 등록 주소 기준 검색 |

### 3.5 회원 탈퇴

| Method | Endpoint | 설명 |
|--------|----------|------|
| POST | /api/v1/my/withdrawal | 회원 탈퇴 요청 |

---

## 4. User 엔티티 확장 필드

```java
// V7 마이그레이션에서 추가된 필드
private LocalDate birthDate;      // 생년월일
private String address;           // 기본 주소
private String addressDetail;     // 상세 주소
private String zipCode;           // 우편번호
private BigDecimal latitude;      // 위도
private BigDecimal longitude;     // 경도
```

---

## 5. 비즈니스 규칙

### 5.1 프로필 수정
- 이름, 전화번호, 프로필 이미지 수정 가능
- 이메일 변경 시 재인증 필요 (별도 프로세스)

### 5.2 비밀번호 변경
- 현재 비밀번호 확인 필수
- 새 비밀번호 확인 (2회 입력)
- 비밀번호 정책 준수 (최소 8자, 영문+숫자+특수문자)

### 5.3 주소 변경
- 주소 검색 → 좌표 자동 변환 (Location 서비스)
- 좌표 저장으로 주변 장례식장 검색 활용

### 5.4 회원 탈퇴
- 비밀번호 재확인 필수
- Soft Delete 처리 (status = DELETED)
- 30일 후 완전 삭제 (배치 처리)

---

## 6. 구현 상태

### 6.1 완료
- User 엔티티 확장 필드 추가 (V7 마이그레이션)
- Pet 도메인 (CRUD 완료)
- Coupon 도메인 (내 쿠폰 조회 완료)
- FuneralHome 도메인 (주변 검색 완료)
- Location 서비스 (Geocoding 완료)
- MyPageController
- UserProfileService

### 6.2 추가 구현 필요
- 프론트엔드 페이지 컴포넌트

---

## 7. 관련 도메인

- **User**: 프로필 및 인증 정보
- **Pet**: 반려동물 관리
- **Coupon**: 쿠폰함 (별도 DB)
- **FuneralHome**: 주변 장례식장
- **Location**: 주소 좌표 변환
