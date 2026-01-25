# Admin User Management 도메인 영구지침

## 1. 개요

관리자 사용자 관리 기능으로 사용자 목록 조회, 상태 변경, 역할 변경, 이력 조회 등을 제공합니다.

### 1.1 주요 기능
- 사용자 목록 조회 (검색, 필터링, 페이징)
- 사용자 상세 조회
- 사용자 상태 변경 (활성화, 정지, 삭제)
- 사용자 역할 변경
- 변경 이력 조회

---

## 2. 데이터 모델

### 2.1 변경 로그 테이블 (V8 마이그레이션)

**user_status_change_logs**
```sql
CREATE TABLE user_status_change_logs (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    previous_status VARCHAR(20) NOT NULL,
    new_status VARCHAR(20) NOT NULL,
    reason TEXT,
    changed_by BIGINT REFERENCES users(id),
    changed_at TIMESTAMP NOT NULL
);
```

**user_role_change_logs**
```sql
CREATE TABLE user_role_change_logs (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    previous_role VARCHAR(20) NOT NULL,
    new_role VARCHAR(20) NOT NULL,
    reason TEXT,
    changed_by BIGINT REFERENCES users(id),
    changed_at TIMESTAMP NOT NULL
);
```

---

## 3. API 명세

### 3.1 관리자 사용자 관리 API

| Method | Endpoint | 설명 | 권한 |
|--------|----------|------|------|
| GET | /api/v1/admin/users | 사용자 목록 조회 | ADMIN |
| GET | /api/v1/admin/users/{id} | 사용자 상세 조회 | ADMIN |
| PATCH | /api/v1/admin/users/{id}/status | 상태 변경 | ADMIN |
| PATCH | /api/v1/admin/users/{id}/role | 역할 변경 | SUPER_ADMIN |
| GET | /api/v1/admin/users/{id}/status-history | 상태 변경 이력 | ADMIN |
| GET | /api/v1/admin/users/{id}/role-history | 역할 변경 이력 | ADMIN |

### 3.2 상태 변경 요청
```json
{
  "status": "SUSPENDED",
  "reason": "부적절한 행위로 인한 정지"
}
```

### 3.3 역할 변경 요청
```json
{
  "role": "COMPANY_ADMIN",
  "reason": "업체 관리자로 승격"
}
```

---

## 4. UserStatus (사용자 상태)

| 값 | 설명 | 전이 가능 상태 |
|----|------|---------------|
| ACTIVE | 활성 | INACTIVE, SUSPENDED, DELETED |
| INACTIVE | 비활성 | ACTIVE |
| SUSPENDED | 정지 | ACTIVE |
| DELETED | 삭제 | - (복구 불가) |

---

## 5. UserRole (사용자 역할)

| 값 | 설명 | 권한 |
|----|------|------|
| USER | 일반 사용자 | 기본 기능 |
| COMPANY_ADMIN | 업체 관리자 | 업체 관리 |
| SUPPLIER_ADMIN | 공급사 관리자 | 공급사 관리 |
| ADMIN | 관리자 | 사용자 관리, 데이터 관리 |
| SUPER_ADMIN | 슈퍼 관리자 | 전체 권한, 역할 변경 |

---

## 6. 비즈니스 규칙

### 6.1 상태 변경
- 변경 사유 필수 입력
- 변경 이력 자동 저장
- 본인 상태 변경 불가

### 6.2 역할 변경
- SUPER_ADMIN만 변경 가능
- 변경 사유 필수 입력
- 변경 이력 자동 저장
- 본인 역할 변경 불가

### 6.3 삭제 처리
- Soft Delete (status = DELETED)
- deletedAt, deletedBy 기록
- 복구 불가 (새로 가입 필요)

---

## 7. 구현 상태

### 7.1 완료
- 변경 로그 테이블 생성 (V8 마이그레이션)
- User 엔티티 상태/역할 변경 메서드
- UserStatusChangeLog 엔티티
- UserRoleChangeLog 엔티티
- UserStatusChangeLogRepository
- UserRoleChangeLogRepository
- AdminUserService
- AdminUserController

### 7.2 추가 구현 필요
- AuditAspect (AOP로 자동 로깅)
- 프론트엔드 관리자 페이지

---

## 8. 관련 도메인

- **User**: 대상 사용자 정보
- **Auth**: 인증 및 권한 확인
- **Batch**: 휴면 계정 처리, 삭제 계정 정리
