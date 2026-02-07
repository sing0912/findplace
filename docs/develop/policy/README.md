# 약관/정책 (Policy)

**최종 수정일:** 2026-02-07
**상태:** 확정
**Phase:** 5 (부가)

---

## 1. 개요

약관/정책의 버전을 관리하는 도메인입니다. 날짜 기반 버전 관리를 통해 과거 약관 이력을 보존하고, 사용자에게 현재 유효한 약관을 제공합니다.

> **참조**: 펫프렌즈 /policies/{type}/{date} 구조를 참고하여 설계하였습니다.

### 1.1 관련 도메인

| 도메인 | 관계 | 설명 |
|--------|------|------|
| auth | 참조 | 회원가입 시 약관 동의 확인 |
| user | 참조 | 마케팅 수신 동의 변경 |
| admin | 참조 | 관리자 약관 등록/수정/활성화 |

---

## 2. 엔티티

### 2.1 PolicyType (약관 유형)

| 값 | 코드 | 설명 |
|----|------|------|
| PRIVACY | PRIVACY | 개인정보처리방침 |
| TERMS_OF_USE | TERMS_OF_USE | 이용약관 |
| MARKETING | MARKETING | 마케팅 수신 동의 |
| LOCATION | LOCATION | 위치기반서비스 이용약관 |

### 2.2 Policy (약관)

| 필드 | 타입 | 설명 | 제약조건 |
|------|------|------|----------|
| id | Long | PK | Auto Increment |
| policyType | Enum | 약관 유형 | Not Null |
| version | String | 버전 (날짜 형식: 2026-02-06) | Not Null |
| title | String | 약관 제목 | Not Null, Max 200자 |
| content | Text | 약관 내용 (HTML) | Not Null |
| isActive | Boolean | 활성 여부 | Not Null, Default false |
| effectiveDate | LocalDate | 시행일 | Not Null |
| createdAt | LocalDateTime | 생성일시 | Not Null |
| updatedAt | LocalDateTime | 수정일시 | Not Null |

---

## 3. DDL

### 3.1 policies

```sql
CREATE TABLE policies (
    id              BIGSERIAL       PRIMARY KEY,
    policy_type     VARCHAR(30)     NOT NULL,
    version         VARCHAR(10)     NOT NULL,
    title           VARCHAR(200)    NOT NULL,
    content         TEXT            NOT NULL,
    is_active       BOOLEAN         NOT NULL DEFAULT FALSE,
    effective_date  DATE            NOT NULL,
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (policy_type, version)
);
```

---

## 4. 인덱스

```sql
CREATE INDEX idx_policies_type ON policies(policy_type);
CREATE INDEX idx_policies_type_active ON policies(policy_type, is_active);
CREATE INDEX idx_policies_effective_date ON policies(effective_date DESC);
CREATE UNIQUE INDEX idx_policies_type_version ON policies(policy_type, version);
```

---

## 5. 비즈니스 규칙

### 5.1 버전 관리

1. 동일 유형(policyType) + 동일 날짜(version) 조합은 유니크 (중복 등록 불가)
2. version 형식은 `YYYY-MM-DD` (예: 2026-02-06)
3. effectiveDate는 해당 약관이 법적으로 효력이 발생하는 날짜

### 5.2 활성 버전

1. 같은 policyType 내에서 `isActive = true`인 레코드는 **반드시 1개**만 존재
2. 새 버전을 활성화하면 기존 활성 버전은 자동으로 비활성화 (`isActive = false`)
3. 활성 버전이 없는 유형은 사용자에게 노출되지 않음

### 5.3 조회 규칙

1. 사용자가 `/policies/{type}` 요청 시 해당 유형의 `isActive = true` 약관을 반환
2. 사용자가 `/policies/{type}/{date}` 요청 시 해당 유형+날짜의 약관을 반환 (과거 버전 열람)
3. 버전 히스토리 조회 시 해당 유형의 모든 버전을 시행일 역순으로 반환

### 5.4 관리자 규칙

1. 약관 등록: 새 약관을 `isActive = false` 상태로 등록
2. 약관 수정: 아직 활성화되지 않은 약관만 수정 가능 (활성 약관은 수정 불가, 새 버전 등록)
3. 약관 활성화: 선택한 약관을 활성화하고 기존 활성 약관을 비활성화

---

## 6. 에러 코드

| 코드 | HTTP | 메시지 |
|------|------|--------|
| POLICY_NOT_FOUND | 404 | 약관을 찾을 수 없습니다. |
| POLICY_VERSION_NOT_FOUND | 404 | 해당 버전의 약관을 찾을 수 없습니다. |
| POLICY_VERSION_DUPLICATE | 409 | 동일 유형의 해당 날짜 약관이 이미 존재합니다. |
| POLICY_ALREADY_ACTIVE | 400 | 이미 활성화된 약관입니다. |
| POLICY_ACTIVE_CANNOT_EDIT | 400 | 활성화된 약관은 수정할 수 없습니다. 새 버전을 등록해주세요. |
| INVALID_POLICY_TYPE | 400 | 유효하지 않은 약관 유형입니다. |
| INVALID_POLICY_VERSION | 400 | 유효하지 않은 버전 형식입니다. (YYYY-MM-DD) |

---

## 7. 패키지 구조

```
backend/src/main/java/com/petpro/domain/policy/
├── entity/
│   ├── Policy.java
│   └── PolicyType.java             # Enum
├── repository/
│   └── PolicyRepository.java
├── service/
│   └── PolicyService.java
├── controller/
│   ├── PolicyController.java        # 사용자 API (Public)
│   └── AdminPolicyController.java   # 관리자 API
└── dto/
    ├── PolicyRequest.java
    ├── PolicyResponse.java
    └── PolicyVersionResponse.java
```

---

## 8. 서브 지침

| 파일 | 설명 |
|------|------|
| [api.md](./api.md) | 약관 API 상세 스펙 |
