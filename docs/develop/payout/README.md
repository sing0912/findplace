# 정산 (Payout)

**최종 수정일:** 2026-02-07
**상태:** 확정

---

## 개요

펫시터(PARTNER)의 정산 및 수수료 관리를 담당하는 도메인입니다.
기존 settlement 도메인을 대체하며, 예약 완료 건에 대한 시터 수익 정산을 처리합니다.

---

## 엔티티

### Payout

| 필드 | 타입 | 설명 | 제약조건 |
|------|------|------|----------|
| id | Long | PK | Auto Increment |
| partnerId | Long | 시터 ID (FK → users) | Not Null |
| payoutNumber | String | 정산 번호 | Unique, Not Null |
| bookingId | Long | 예약 ID (FK → bookings) | Not Null |
| bookingAmount | BigDecimal | 예약 금액 | Not Null |
| platformFeeRate | BigDecimal | 플랫폼 수수료율 (0.00~1.00) | Not Null |
| platformFeeAmount | BigDecimal | 플랫폼 수수료 금액 | Not Null |
| payoutAmount | BigDecimal | 시터 정산 금액 | Not Null |
| status | Enum | 정산 상태 | Not Null, Default PENDING |
| processedAt | DateTime | 정산 처리일시 | Nullable |
| createdAt | DateTime | 생성일시 | Not Null |

### PayoutStatus

| 값 | 설명 |
|----|------|
| PENDING | 정산 대기 |
| PROCESSING | 정산 처리 중 |
| COMPLETED | 정산 완료 |
| FAILED | 정산 실패 |

```
상태 흐름:
  PENDING → PROCESSING → COMPLETED
                       → FAILED
```

### BankAccount

| 필드 | 타입 | 설명 | 제약조건 |
|------|------|------|----------|
| id | Long | PK | Auto Increment |
| partnerId | Long | 시터 ID (FK → users) | Not Null |
| bankName | String | 은행명 | Not Null |
| accountNumber | String | 계좌번호 (암호화 저장) | Not Null |
| accountHolder | String | 예금주 | Not Null |
| isDefault | Boolean | 기본 계좌 여부 | Default false |
| isVerified | Boolean | 계좌 인증 여부 | Default false |
| createdAt | DateTime | 생성일시 | Not Null |
| updatedAt | DateTime | 수정일시 | Not Null |

---

## 수수료 계산

### 공식

```
platformFeeAmount = bookingAmount * platformFeeRate
payoutAmount      = bookingAmount - platformFeeAmount
```

### 예시

| 항목 | 값 |
|------|-----|
| 예약 금액 (bookingAmount) | 50,000원 |
| 수수료율 (platformFeeRate) | 0.15 (15%) |
| 수수료 금액 (platformFeeAmount) | 7,500원 |
| 시터 정산 금액 (payoutAmount) | 42,500원 |

---

## 정산 주기

| 항목 | 설명 |
|------|------|
| 주기 | 주 1회 |
| 정산일 | 매주 월요일 |
| 대상 | 전주(월~일) 완료(COMPLETED) 된 예약 건 |
| 처리 방식 | 배치 스케줄러로 자동 생성 후 관리자 승인 |

### 정산 프로세스

```
1. [매주 월요일 00:00] 배치 스케줄러 실행
2. 전주 완료 예약 건 조회
3. 수수료 계산 후 Payout 레코드 생성 (PENDING)
4. 관리자가 정산 대기 목록 확인
5. 일괄 정산 처리 (PENDING → PROCESSING)
6. 은행 이체 처리
7. 이체 성공 → COMPLETED / 이체 실패 → FAILED
8. 시터에게 정산 완료 알림 발송
```

---

## DDL

### payouts 테이블

```sql
CREATE TABLE payouts (
    id              BIGSERIAL PRIMARY KEY,
    partner_id      BIGINT         NOT NULL REFERENCES users(id),
    payout_number   VARCHAR(50)    NOT NULL UNIQUE,
    booking_id      BIGINT         NOT NULL REFERENCES bookings(id),
    booking_amount  DECIMAL(12, 2) NOT NULL,
    platform_fee_rate   DECIMAL(5, 4) NOT NULL,
    platform_fee_amount DECIMAL(12, 2) NOT NULL,
    payout_amount   DECIMAL(12, 2) NOT NULL,
    status          VARCHAR(20)    NOT NULL DEFAULT 'PENDING',
    processed_at    TIMESTAMP,
    created_at      TIMESTAMP      NOT NULL DEFAULT NOW()
);
```

### bank_accounts 테이블

```sql
CREATE TABLE bank_accounts (
    id              BIGSERIAL PRIMARY KEY,
    partner_id      BIGINT       NOT NULL REFERENCES users(id),
    bank_name       VARCHAR(50)  NOT NULL,
    account_number  VARCHAR(100) NOT NULL,
    account_holder  VARCHAR(50)  NOT NULL,
    is_default      BOOLEAN      NOT NULL DEFAULT FALSE,
    is_verified     BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP    NOT NULL DEFAULT NOW()
);
```

### 인덱스

```sql
-- payouts
CREATE INDEX idx_payouts_partner_id ON payouts(partner_id);
CREATE INDEX idx_payouts_booking_id ON payouts(booking_id);
CREATE INDEX idx_payouts_status ON payouts(status);
CREATE INDEX idx_payouts_created_at ON payouts(created_at);
CREATE INDEX idx_payouts_partner_status ON payouts(partner_id, status);

-- bank_accounts
CREATE INDEX idx_bank_accounts_partner_id ON bank_accounts(partner_id);
CREATE UNIQUE INDEX idx_bank_accounts_partner_default ON bank_accounts(partner_id) WHERE is_default = TRUE;
```

---

## 패키지 구조

```
com.petpro.domain.payout/
├── controller/
│   ├── PartnerPayoutController.java    # 시터 정산 API
│   └── AdminPayoutController.java      # 관리자 정산 API
├── dto/
│   ├── PayoutRequest.java
│   ├── PayoutResponse.java
│   ├── BankAccountRequest.java
│   └── BankAccountResponse.java
├── entity/
│   ├── Payout.java
│   ├── PayoutStatus.java
│   └── BankAccount.java
├── repository/
│   ├── PayoutRepository.java
│   └── BankAccountRepository.java
├── service/
│   ├── PayoutService.java
│   ├── PayoutScheduler.java            # 주간 정산 배치
│   └── BankAccountService.java
└── exception/
    └── PayoutException.java
```

---

## 비즈니스 규칙

1. **정산 대상**: 예약 상태가 COMPLETED인 건만 정산 대상
2. **중복 방지**: 동일 bookingId에 대해 중복 정산 레코드 생성 불가
3. **기본 계좌**: 시터당 기본 계좌는 최대 1개, 새로 기본 설정 시 기존 기본 해제
4. **계좌 인증**: 미인증 계좌로는 정산 처리 불가
5. **최소 정산 금액**: 없음 (금액 무관 정산)
6. **수수료율 변경**: 관리자가 수수료율 변경 시, 이미 생성된 Payout에는 영향 없음 (생성 시점의 수수료율 적용)
7. **정산 실패 재처리**: FAILED 상태의 Payout은 관리자가 수동으로 재처리 가능
8. **계좌 삭제 제한**: 정산 대기(PENDING/PROCESSING) 중인 건이 있는 시터의 기본 계좌는 삭제 불가

---

## 서브 지침

| 파일 | 설명 |
|------|------|
| [api.md](./api.md) | API 명세 |
