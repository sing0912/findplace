# 공급사 (Supplier)

## 개요

용품/굿즈를 공급하는 공급사 관리 도메인입니다.
공급사는 상품 등록, 주문 관리, 배송 처리, 정산 조회 기능을 수행합니다.

---

## 엔티티

### Supplier

| 필드 | 타입 | 설명 | 제약조건 |
|------|------|------|----------|
| id | Long | PK | Auto Increment |
| name | String | 공급사명 | Not Null |
| businessNumber | String | 사업자번호 | Unique, Not Null |
| representativeName | String | 대표자명 | Not Null |
| address | String | 주소 | Not Null |
| phone | String | 대표 전화번호 | Not Null |
| email | String | 대표 이메일 | Not Null |
| bankCode | String | 은행 코드 | Not Null |
| bankAccount | String | 계좌번호 | Not Null |
| bankHolder | String | 예금주 | Not Null |
| settlementCycle | Enum | 정산 주기 | Not Null |
| commissionRate | Decimal | 수수료율 (%) | Not Null |
| status | Enum | 상태 | Not Null |
| approvedAt | DateTime | 승인일시 | Nullable |
| createdAt | DateTime | 생성일시 | Not Null |
| updatedAt | DateTime | 수정일시 | Not Null |

### SupplierStatus

| 값 | 설명 |
|----|------|
| PENDING | 승인 대기 |
| APPROVED | 승인됨 |
| REJECTED | 거절됨 |
| SUSPENDED | 정지됨 |

### SettlementCycle

| 값 | 설명 |
|----|------|
| WEEKLY | 주간 정산 |
| BIWEEKLY | 격주 정산 |
| MONTHLY | 월간 정산 |

---

## API 목록

| Method | Endpoint | 설명 | 권한 |
|--------|----------|------|------|
| GET | /api/v1/suppliers | 목록 조회 | PLATFORM_ADMIN |
| POST | /api/v1/suppliers | 등록 신청 | 인증된 사용자 |
| GET | /api/v1/suppliers/{id} | 상세 조회 | 본인 또는 ADMIN |
| PUT | /api/v1/suppliers/{id} | 수정 | 본인 또는 ADMIN |
| DELETE | /api/v1/suppliers/{id} | 삭제 | PLATFORM_ADMIN |
| PUT | /api/v1/suppliers/{id}/status | 상태 변경 (승인/거절) | PLATFORM_ADMIN |
| GET | /api/v1/suppliers/{id}/products | 공급사 상품 목록 | SUPPLIER_ADMIN (본인) |
| GET | /api/v1/suppliers/{id}/orders | 공급사 주문 목록 | SUPPLIER_ADMIN (본인) |
| GET | /api/v1/suppliers/{id}/inventory | 공급사 재고 목록 | SUPPLIER_ADMIN (본인) |
| GET | /api/v1/suppliers/{id}/settlements | 공급사 정산 목록 | SUPPLIER_ADMIN (본인) |

---

## 공급사 어드민 기능

### 대시보드
- 오늘의 신규 주문
- 배송 대기 건수
- 미정산 금액
- 재고 부족 알림

### 상품 관리
- 상품 등록/수정/삭제
- 상품 상태 관리 (판매중/품절/숨김)
- 가격 및 재고 관리

### 주문 관리
- 주문 목록 조회
- 주문 상태 변경
- 배송 처리

### 정산 관리
- 정산 내역 조회
- 정산 상세 확인
- 정산 명세서 다운로드

---

## 비즈니스 규칙

1. 공급사 등록 시 PENDING 상태로 생성
2. PLATFORM_ADMIN 승인 후 APPROVED 상태로 변경
3. APPROVED 상태에서만 상품 등록/판매 가능
4. 사업자번호는 시스템 전체에서 고유해야 함
5. 수수료율은 0~100% 범위
6. 정산은 정산 주기에 따라 자동 생성

---

## 서브 지침

| 파일 | 설명 |
|------|------|
| [registration.md](./registration.md) | 공급사 등록 |
| [approval.md](./approval.md) | 승인 프로세스 |
| [product/](./product/) | 상품 관리 |
| [settlement/](./settlement/) | 정산 관리 |
