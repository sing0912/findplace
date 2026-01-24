# 데이터베이스 컨벤션

## 개요

PostgreSQL 데이터베이스 설계 및 사용에 관한 컨벤션입니다.

---

## Master-Slave 구성

### 구조

```
┌─────────────────────┐
│  PostgreSQL Master  │  ← CUD (Create, Update, Delete)
│     Port: 5432      │
└──────────┬──────────┘
           │ Streaming Replication
     ┌─────┴─────┐
     ▼           ▼
┌─────────┐ ┌─────────┐
│ Slave 1 │ │ Slave 2 │  ← R (Read Only)
│  :5433  │ │  :5434  │
└─────────┘ └─────────┘
```

### 라우팅 규칙

| 어노테이션 | 대상 DB | 용도 |
|------------|---------|------|
| `@Transactional` | Master | INSERT, UPDATE, DELETE |
| `@Transactional(readOnly = true)` | Slave | SELECT |

### Spring 설정

```java
@Service
public class UserService {

    // Master DB 사용
    @Transactional
    public User createUser(UserCreateRequest request) {
        // INSERT 쿼리
    }

    // Slave DB 사용 (Round Robin)
    @Transactional(readOnly = true)
    public User getUser(Long id) {
        // SELECT 쿼리
    }
}
```

---

## 네이밍 규칙

### 테이블

- snake_case 사용
- 복수형 사용
- 접두어 없음

```sql
-- Good
users
products
order_items

-- Bad
tbl_user
User
orderItems
```

### 컬럼

- snake_case 사용
- 의미 있는 이름

```sql
-- Good
created_at
updated_at
user_id
is_active

-- Bad
createdAt
crtDt
uid
active
```

### 인덱스

```
idx_{table}_{column(s)}
```

예시:
```sql
idx_users_email
idx_orders_user_id_created_at
```

### 제약조건

```
{table}_{type}_{column(s)}
```

| 타입 | 예시 |
|------|------|
| pk | users_pk_id |
| fk | orders_fk_user_id |
| uq | users_uq_email |
| ck | orders_ck_status |

---

## 공통 컬럼

### 모든 테이블 필수 컬럼

```sql
CREATE TABLE example (
    id BIGSERIAL PRIMARY KEY,
    -- 비즈니스 컬럼들...
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_by BIGINT
);
```

### Soft Delete (선택)

```sql
deleted_at TIMESTAMP WITH TIME ZONE,
deleted_by BIGINT
```

---

## 데이터 타입

### 권장 타입

| 용도 | 타입 | 비고 |
|------|------|------|
| PK | BIGSERIAL | Auto increment |
| 문자열 (가변) | VARCHAR(n) | 길이 제한 필요 시 |
| 문자열 (무제한) | TEXT | 길이 제한 불필요 시 |
| 정수 | INTEGER, BIGINT | |
| 소수 | NUMERIC(p,s) | 금액 등 정밀도 필요 시 |
| 불리언 | BOOLEAN | |
| 날짜 | DATE | |
| 시간 | TIME | |
| 날짜시간 | TIMESTAMP WITH TIME ZONE | |
| JSON | JSONB | 구조화된 데이터 |
| UUID | UUID | 외부 노출 ID |
| Enum | VARCHAR + CHECK | 또는 별도 테이블 |

### 금액 처리

```sql
-- 금액은 항상 NUMERIC 사용 (부동소수점 오차 방지)
price NUMERIC(15, 2) NOT NULL,
total_amount NUMERIC(15, 2) NOT NULL
```

---

## 인덱스 전략

### 생성 기준

1. **FK 컬럼**: 항상 인덱스
2. **자주 검색되는 컬럼**: WHERE 절에 자주 사용
3. **정렬 컬럼**: ORDER BY에 자주 사용
4. **유니크 제약**: 자동 인덱스 생성

### 복합 인덱스

```sql
-- 순서 중요: 선택도가 높은 컬럼 먼저
CREATE INDEX idx_orders_user_id_status ON orders (user_id, status);
```

### Partial Index

```sql
-- 특정 조건만 인덱싱
CREATE INDEX idx_orders_pending ON orders (created_at)
WHERE status = 'PENDING';
```

---

## 외래키 (FK)

### 명명 규칙

```sql
CONSTRAINT {table}_fk_{column} FOREIGN KEY ({column}) REFERENCES {ref_table}(id)
```

### 삭제 정책

| 정책 | 사용 케이스 |
|------|-------------|
| RESTRICT | 기본값, 참조 있으면 삭제 불가 |
| CASCADE | 부모 삭제 시 자식도 삭제 |
| SET NULL | 부모 삭제 시 NULL로 설정 |

```sql
-- 예시: 사용자 삭제 시 주문은 유지하되 user_id는 NULL
ALTER TABLE orders
ADD CONSTRAINT orders_fk_user_id
FOREIGN KEY (user_id) REFERENCES users(id)
ON DELETE SET NULL;
```

---

## Enum 처리

### 방법 1: VARCHAR + CHECK

```sql
status VARCHAR(20) NOT NULL,
CONSTRAINT orders_ck_status CHECK (status IN ('PENDING', 'PAID', 'SHIPPED', 'DELIVERED', 'CANCELLED'))
```

### 방법 2: 별도 테이블 (권장 - 확장성)

```sql
CREATE TABLE order_statuses (
    code VARCHAR(20) PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    sort_order INTEGER NOT NULL
);

-- orders 테이블에서 FK로 참조
status VARCHAR(20) NOT NULL REFERENCES order_statuses(code)
```

---

## 마이그레이션

### 도구

- Flyway 사용

### 파일 명명

```
V{version}__{description}.sql
```

예시:
```
V1__create_users_table.sql
V2__create_products_table.sql
V3__add_phone_to_users.sql
```

### 규칙

1. **한 번 적용된 마이그레이션은 수정 금지**
2. **롤백 스크립트 준비** (가능한 경우)
3. **대용량 테이블 변경 시 주의** (락 발생)

---

## 성능 고려사항

### 대용량 테이블

1. **파티셔닝**: 날짜 기반 파티션
2. **아카이빙**: 오래된 데이터 별도 보관
3. **배치 처리**: 대량 INSERT/UPDATE는 배치로

### 쿼리 최적화

1. **EXPLAIN ANALYZE 사용**
2. **N+1 문제 주의**: JOIN 또는 배치 쿼리 사용
3. **SELECT * 지양**: 필요한 컬럼만 조회

### 커넥션 풀

```yaml
# HikariCP 설정
spring:
  datasource:
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      idle-timeout: 300000
      connection-timeout: 20000
```

---

## 백업 전략

### 일일 백업

- 매일 새벽 3시 전체 백업 (pg_dump)
- 보관 기간: 30일

### WAL 아카이빙

- Point-in-Time Recovery 지원
- 연속적인 WAL 파일 아카이빙

### 복구 테스트

- 월 1회 복구 테스트 수행
