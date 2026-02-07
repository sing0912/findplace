# 데이터베이스 스키마 설계

## 1. 개요

PetPro 시스템은 두 개의 PostgreSQL 데이터베이스를 사용합니다.

| DB | 용도 | 포트 | 비고 |
|----|------|------|------|
| Main DB | 회원, 반려동물, 장례식장, 주문, 예약 | 5432 (Master), 5433/5434 (Slave) | Master-Slave 복제 |
| Coupon DB | 쿠폰 전용 | 5435 | 마이크로서비스 분리 대비 |

---

## 2. Main Database 스키마

### 2.1 users 테이블

```sql
CREATE TABLE users (
    id                  BIGSERIAL PRIMARY KEY,
    email               VARCHAR(255) NOT NULL UNIQUE,
    password            VARCHAR(255) NOT NULL,
    name                VARCHAR(100) NOT NULL,
    phone               VARCHAR(20),
    birth_date          DATE,

    -- 주소 정보
    address             VARCHAR(500),
    address_detail      VARCHAR(200),
    zip_code            VARCHAR(10),
    latitude            DECIMAL(10, 7),
    longitude           DECIMAL(10, 7),

    -- 역할 및 상태
    role                VARCHAR(20) DEFAULT 'USER' NOT NULL,
    status              VARCHAR(20) DEFAULT 'ACTIVE' NOT NULL,

    -- 프로필
    profile_image_url   VARCHAR(500),

    -- 로그인 정보
    last_login_at       TIMESTAMP,

    -- Soft Delete
    deleted_at          TIMESTAMP,
    deleted_by          BIGINT,

    -- Audit
    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_users_role CHECK (role IN ('USER', 'COMPANY_ADMIN', 'SUPPLIER_ADMIN', 'ADMIN', 'SUPER_ADMIN')),
    CONSTRAINT chk_users_status CHECK (status IN ('ACTIVE', 'INACTIVE', 'SUSPENDED', 'DELETED'))
);

-- 인덱스
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_status ON users(status);
CREATE INDEX idx_users_role ON users(role);
CREATE INDEX idx_users_name ON users(name);
CREATE INDEX idx_users_phone ON users(phone);
CREATE INDEX idx_users_birth_month_day ON users(EXTRACT(MONTH FROM birth_date), EXTRACT(DAY FROM birth_date));
CREATE INDEX idx_users_deleted_at ON users(deleted_at) WHERE deleted_at IS NULL;
CREATE INDEX idx_users_last_login_at ON users(last_login_at);
CREATE INDEX idx_users_created_at ON users(created_at DESC);
```

### 2.2 pets 테이블

```sql
CREATE TABLE pets (
    id                  BIGSERIAL PRIMARY KEY,
    user_id             BIGINT NOT NULL REFERENCES users(id),

    -- 기본 정보
    name                VARCHAR(100) NOT NULL,
    species             VARCHAR(20) NOT NULL,
    breed               VARCHAR(100),
    birth_date          DATE,
    gender              VARCHAR(10),
    is_neutered         BOOLEAN DEFAULT FALSE,

    -- 프로필
    profile_image_url   VARCHAR(500),
    memo                TEXT,

    -- 사망 정보
    is_deceased         BOOLEAN DEFAULT FALSE,
    deceased_at         DATE,

    -- Soft Delete
    deleted_at          TIMESTAMP,

    -- Audit
    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_pets_species CHECK (species IN ('DOG', 'CAT', 'BIRD', 'HAMSTER', 'RABBIT', 'FISH', 'REPTILE', 'ETC')),
    CONSTRAINT chk_pets_gender CHECK (gender IN ('MALE', 'FEMALE', 'UNKNOWN'))
);

-- 인덱스
CREATE INDEX idx_pets_user_id ON pets(user_id);
CREATE INDEX idx_pets_species ON pets(species);
CREATE INDEX idx_pets_is_deceased ON pets(is_deceased);
CREATE INDEX idx_pets_deleted_at ON pets(deleted_at) WHERE deleted_at IS NULL;
```

### 2.3 funeral_homes 테이블

```sql
CREATE TABLE funeral_homes (
    id                  BIGSERIAL PRIMARY KEY,

    -- 기본 정보
    name                VARCHAR(200) NOT NULL,
    road_address        VARCHAR(500),
    lot_address         VARCHAR(500),
    phone               VARCHAR(50),

    -- 지역 정보
    loc_code            VARCHAR(20),
    loc_name            VARCHAR(100),

    -- 서비스 유형
    has_crematorium     BOOLEAN DEFAULT FALSE,
    has_columbarium     BOOLEAN DEFAULT FALSE,
    has_funeral         BOOLEAN DEFAULT FALSE,

    -- 좌표
    latitude            DECIMAL(10, 7),
    longitude           DECIMAL(10, 7),
    geocoded_at         TIMESTAMP,

    -- 상태
    is_active           BOOLEAN DEFAULT TRUE,
    verified_at         TIMESTAMP,

    -- Audit
    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    synced_at           TIMESTAMP
);

-- 인덱스
CREATE INDEX idx_funeral_homes_loc_code ON funeral_homes(loc_code);
CREATE INDEX idx_funeral_homes_is_active ON funeral_homes(is_active);
CREATE INDEX idx_funeral_homes_name ON funeral_homes(name);
CREATE INDEX idx_funeral_homes_location ON funeral_homes(latitude, longitude);
CREATE INDEX idx_funeral_homes_services ON funeral_homes(has_crematorium, has_funeral, has_columbarium);
```

### 2.4 funeral_home_sync_logs 테이블

```sql
CREATE TABLE funeral_home_sync_logs (
    id                  BIGSERIAL PRIMARY KEY,
    sync_type           VARCHAR(20) NOT NULL,
    started_at          TIMESTAMP NOT NULL,
    completed_at        TIMESTAMP,
    status              VARCHAR(20) NOT NULL,
    total_count         INTEGER,
    inserted_count      INTEGER,
    updated_count       INTEGER,
    deleted_count       INTEGER,
    error_count         INTEGER,
    error_message       TEXT,

    CONSTRAINT chk_sync_type CHECK (sync_type IN ('INCREMENTAL', 'FULL')),
    CONSTRAINT chk_sync_status CHECK (status IN ('RUNNING', 'COMPLETED', 'FAILED', 'PARTIAL'))
);

CREATE INDEX idx_sync_logs_sync_type ON funeral_home_sync_logs(sync_type);
CREATE INDEX idx_sync_logs_started_at ON funeral_home_sync_logs(started_at DESC);
```

### 2.5 region_codes 테이블

```sql
CREATE TABLE region_codes (
    id                  BIGSERIAL PRIMARY KEY,
    code                VARCHAR(20) NOT NULL UNIQUE,
    name                VARCHAR(100) NOT NULL,
    type                VARCHAR(20) NOT NULL,
    parent_code         VARCHAR(20),
    sort_order          INTEGER DEFAULT 0,
    is_active           BOOLEAN DEFAULT TRUE,
    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_region_type CHECK (type IN ('METRO', 'CITY'))
);

CREATE UNIQUE INDEX idx_region_codes_code ON region_codes(code);
CREATE INDEX idx_region_codes_parent ON region_codes(parent_code);
CREATE INDEX idx_region_codes_type ON region_codes(type);
CREATE INDEX idx_region_codes_active ON region_codes(is_active);
```

### 2.6 user_status_change_logs 테이블

```sql
CREATE TABLE user_status_change_logs (
    id                  BIGSERIAL PRIMARY KEY,
    user_id             BIGINT NOT NULL,
    from_status         VARCHAR(20) NOT NULL,
    to_status           VARCHAR(20) NOT NULL,
    reason              TEXT,
    changed_by          BIGINT NOT NULL,
    changed_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_status_log_user ON user_status_change_logs(user_id);
CREATE INDEX idx_status_log_changed_at ON user_status_change_logs(changed_at DESC);
```

### 2.7 user_role_change_logs 테이블

```sql
CREATE TABLE user_role_change_logs (
    id                  BIGSERIAL PRIMARY KEY,
    user_id             BIGINT NOT NULL,
    from_role           VARCHAR(20) NOT NULL,
    to_role             VARCHAR(20) NOT NULL,
    reason              TEXT,
    changed_by          BIGINT NOT NULL,
    changed_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_role_log_user ON user_role_change_logs(user_id);
CREATE INDEX idx_role_log_changed_at ON user_role_change_logs(changed_at DESC);
```

### 2.8 batch_job_logs 테이블

```sql
CREATE TABLE batch_job_logs (
    id                  BIGSERIAL PRIMARY KEY,
    job_name            VARCHAR(100) NOT NULL,
    status              VARCHAR(20) NOT NULL,
    started_at          TIMESTAMP NOT NULL,
    completed_at        TIMESTAMP,
    processed_count     INTEGER,
    success_count       INTEGER,
    fail_count          INTEGER,
    error_message       TEXT,

    CONSTRAINT chk_batch_status CHECK (status IN ('RUNNING', 'COMPLETED', 'FAILED'))
);

CREATE INDEX idx_batch_logs_job_name ON batch_job_logs(job_name);
CREATE INDEX idx_batch_logs_started_at ON batch_job_logs(started_at DESC);
```

### 2.9 daily_statistics 테이블

```sql
CREATE TABLE daily_statistics (
    id                  BIGSERIAL PRIMARY KEY,
    stat_date           DATE NOT NULL UNIQUE,
    new_user_count      INTEGER DEFAULT 0,
    active_user_count   INTEGER DEFAULT 0,
    issued_coupon_count INTEGER DEFAULT 0,
    used_coupon_count   INTEGER DEFAULT 0,
    order_count         INTEGER DEFAULT 0,
    total_order_amount  BIGINT DEFAULT 0,
    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE UNIQUE INDEX idx_daily_stats_date ON daily_statistics(stat_date);
```

---

## 3. Coupon Database 스키마

### 3.1 coupon_types 테이블

```sql
CREATE TABLE coupon_types (
    id                  BIGSERIAL PRIMARY KEY,
    code                VARCHAR(50) NOT NULL UNIQUE,
    name                VARCHAR(200) NOT NULL,
    description         TEXT,

    -- 할인 정보
    discount_type       VARCHAR(20) NOT NULL,
    discount_value      DECIMAL(10, 2) NOT NULL,
    max_discount_amount DECIMAL(10, 2),
    min_order_amount    DECIMAL(10, 2),

    -- 발급 정보
    issue_type          VARCHAR(20) NOT NULL,
    auto_issue_event    VARCHAR(50),
    max_issue_count     INTEGER,
    current_issue_count INTEGER DEFAULT 0,

    -- 유효 기간
    valid_days          INTEGER,
    valid_from          DATE,
    valid_until         DATE,

    -- 상태
    is_active           BOOLEAN DEFAULT TRUE,

    -- Audit
    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by          BIGINT,

    CONSTRAINT chk_discount_type CHECK (discount_type IN ('FIXED', 'PERCENT', 'FREE')),
    CONSTRAINT chk_issue_type CHECK (issue_type IN ('MANUAL', 'CODE', 'AUTO'))
);

CREATE UNIQUE INDEX idx_coupon_types_code ON coupon_types(code);
CREATE INDEX idx_coupon_types_active ON coupon_types(is_active);
CREATE INDEX idx_coupon_types_auto_issue ON coupon_types(auto_issue_event) WHERE auto_issue_event IS NOT NULL;
```

### 3.2 coupon_conditions 테이블 (EAV)

```sql
CREATE TABLE coupon_conditions (
    id                  BIGSERIAL PRIMARY KEY,
    coupon_type_id      BIGINT NOT NULL REFERENCES coupon_types(id),
    condition_key       VARCHAR(100) NOT NULL,
    condition_value     VARCHAR(500) NOT NULL,
    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_coupon_conditions_type ON coupon_conditions(coupon_type_id);
CREATE INDEX idx_coupon_conditions_key ON coupon_conditions(condition_key);
```

### 3.3 coupons 테이블

```sql
CREATE TABLE coupons (
    id                  BIGSERIAL PRIMARY KEY,
    coupon_type_id      BIGINT NOT NULL REFERENCES coupon_types(id),
    coupon_code         VARCHAR(50) UNIQUE,

    -- 유효 기간
    valid_from          DATE NOT NULL,
    valid_until         DATE NOT NULL,

    -- 상태
    status              VARCHAR(20) DEFAULT 'ACTIVE' NOT NULL,

    -- Audit
    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_coupon_status CHECK (status IN ('ACTIVE', 'USED', 'EXPIRED', 'CANCELLED'))
);

CREATE INDEX idx_coupons_type ON coupons(coupon_type_id);
CREATE INDEX idx_coupons_code ON coupons(coupon_code) WHERE coupon_code IS NOT NULL;
CREATE INDEX idx_coupons_status ON coupons(status);
CREATE INDEX idx_coupons_valid_until ON coupons(valid_until);
```

### 3.4 user_coupons 테이블

```sql
CREATE TABLE user_coupons (
    id                  BIGSERIAL PRIMARY KEY,
    user_id             BIGINT NOT NULL,
    coupon_id           BIGINT NOT NULL REFERENCES coupons(id),
    coupon_type_id      BIGINT NOT NULL REFERENCES coupon_types(id),

    -- 상태
    status              VARCHAR(20) DEFAULT 'ACTIVE' NOT NULL,

    -- 시간 정보
    issued_at           TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    used_at             TIMESTAMP,
    expired_at          TIMESTAMP,

    -- 사용 정보
    used_order_id       BIGINT,

    CONSTRAINT chk_user_coupon_status CHECK (status IN ('ACTIVE', 'USED', 'EXPIRED', 'CANCELLED'))
);

CREATE INDEX idx_user_coupons_user ON user_coupons(user_id);
CREATE INDEX idx_user_coupons_coupon ON user_coupons(coupon_id);
CREATE INDEX idx_user_coupons_type ON user_coupons(coupon_type_id);
CREATE INDEX idx_user_coupons_status ON user_coupons(status);
CREATE INDEX idx_user_coupons_issued_at ON user_coupons(issued_at);
CREATE INDEX idx_user_coupons_expiry ON user_coupons(status, expired_at) WHERE status = 'ACTIVE';
```

### 3.5 coupon_usage_histories 테이블

```sql
CREATE TABLE coupon_usage_histories (
    id                  BIGSERIAL PRIMARY KEY,
    user_coupon_id      BIGINT NOT NULL REFERENCES user_coupons(id),
    user_id             BIGINT NOT NULL,
    order_id            BIGINT NOT NULL,
    order_amount        DECIMAL(10, 2) NOT NULL,
    discount_amount     DECIMAL(10, 2) NOT NULL,
    used_at             TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_usage_history_user_coupon ON coupon_usage_histories(user_coupon_id);
CREATE INDEX idx_usage_history_user ON coupon_usage_histories(user_id);
CREATE INDEX idx_usage_history_order ON coupon_usage_histories(order_id);
CREATE INDEX idx_usage_history_used_at ON coupon_usage_histories(used_at DESC);
```

---

## 4. ERD (Entity Relationship Diagram)

### 4.1 Main Database ERD

```
┌─────────────┐       ┌─────────────┐
│   users     │       │    pets     │
├─────────────┤       ├─────────────┤
│ id (PK)     │───────│ id (PK)     │
│ email       │   1:N │ user_id(FK) │
│ password    │       │ name        │
│ name        │       │ species     │
│ phone       │       │ breed       │
│ birth_date  │       │ birth_date  │
│ address     │       │ gender      │
│ latitude    │       │ is_deceased │
│ longitude   │       │ deceased_at │
│ role        │       └─────────────┘
│ status      │
│ last_login  │
│ created_at  │
│ updated_at  │
│ deleted_at  │
└─────────────┘
       │
       │ 1:N
       ▼
┌─────────────────────────┐     ┌─────────────────────────┐
│ user_status_change_logs │     │  user_role_change_logs  │
├─────────────────────────┤     ├─────────────────────────┤
│ id (PK)                 │     │ id (PK)                 │
│ user_id                 │     │ user_id                 │
│ from_status             │     │ from_role               │
│ to_status               │     │ to_role                 │
│ reason                  │     │ reason                  │
│ changed_by              │     │ changed_by              │
│ changed_at              │     │ changed_at              │
└─────────────────────────┘     └─────────────────────────┘


┌─────────────────┐       ┌─────────────────────────┐
│  funeral_homes  │       │ funeral_home_sync_logs  │
├─────────────────┤       ├─────────────────────────┤
│ id (PK)         │       │ id (PK)                 │
│ name            │       │ sync_type               │
│ road_address    │       │ status                  │
│ phone           │       │ started_at              │
│ loc_code        │───────│ completed_at            │
│ loc_name        │       │ total_count             │
│ has_crematorium │       │ inserted_count          │
│ has_funeral     │       │ updated_count           │
│ latitude        │       │ error_message           │
│ longitude       │       └─────────────────────────┘
│ is_active       │
└─────────────────┘
       │
       │ N:1
       ▼
┌─────────────────┐
│  region_codes   │
├─────────────────┤
│ id (PK)         │
│ code (UK)       │───┐
│ name            │   │ self-reference
│ type            │   │ (parent_code)
│ parent_code     │◄──┘
│ sort_order      │
│ is_active       │
└─────────────────┘
```

### 4.2 Coupon Database ERD

```
┌─────────────────┐       ┌─────────────────────┐
│  coupon_types   │       │  coupon_conditions  │
├─────────────────┤       ├─────────────────────┤
│ id (PK)         │───────│ id (PK)             │
│ code (UK)       │   1:N │ coupon_type_id (FK) │
│ name            │       │ condition_key       │
│ discount_type   │       │ condition_value     │
│ discount_value  │       └─────────────────────┘
│ issue_type      │
│ auto_issue_event│
│ valid_days      │
│ is_active       │
└─────────────────┘
       │
       │ 1:N
       ▼
┌─────────────────┐       ┌─────────────────┐
│    coupons      │       │  user_coupons   │
├─────────────────┤       ├─────────────────┤
│ id (PK)         │───────│ id (PK)         │
│ coupon_type_id  │   1:N │ user_id         │
│ coupon_code(UK) │       │ coupon_id (FK)  │
│ valid_from      │       │ coupon_type_id  │
│ valid_until     │       │ status          │
│ status          │       │ issued_at       │
└─────────────────┘       │ used_at         │
                          │ expired_at      │
                          │ used_order_id   │
                          └─────────────────┘
                                 │
                                 │ 1:N
                                 ▼
                    ┌─────────────────────────┐
                    │ coupon_usage_histories  │
                    ├─────────────────────────┤
                    │ id (PK)                 │
                    │ user_coupon_id (FK)     │
                    │ user_id                 │
                    │ order_id                │
                    │ order_amount            │
                    │ discount_amount         │
                    │ used_at                 │
                    └─────────────────────────┘
```

---

## 5. Flyway 마이그레이션

### 5.1 Main DB 마이그레이션 파일

```
backend/src/main/resources/db/migration/
├── V1__Create_users_table.sql
├── V2__Create_pets_table.sql
├── V3__Create_funeral_homes_table.sql
├── V4__Create_region_codes_table.sql
├── V5__Insert_region_codes.sql
├── V6__Create_user_change_logs_tables.sql
├── V7__Create_batch_logs_table.sql
├── V8__Create_statistics_table.sql
└── V9__Add_indexes.sql
```

### 5.2 Coupon DB 마이그레이션 파일

```
backend/src/main/resources/db/migration-coupon/
├── V1__Create_coupon_types_table.sql
├── V2__Create_coupon_conditions_table.sql
├── V3__Create_coupons_table.sql
├── V4__Create_user_coupons_table.sql
├── V5__Create_usage_histories_table.sql
├── V6__Insert_default_coupon_types.sql
└── V7__Add_indexes.sql
```

---

## 6. 다중 데이터소스 설정

### 6.1 application.yml

```yaml
spring:
  datasource:
    main:
      master:
        url: jdbc:postgresql://localhost:5432/petpro
        username: petpro
        password: ${MAIN_DB_PASSWORD}
        driver-class-name: org.postgresql.Driver
      slave:
        url: jdbc:postgresql://localhost:5433/petpro,jdbc:postgresql://localhost:5434/petpro
        username: petpro
        password: ${MAIN_DB_PASSWORD}
        driver-class-name: org.postgresql.Driver

    coupon:
      url: jdbc:postgresql://localhost:5435/petpro_coupon
      username: petpro
      password: ${COUPON_DB_PASSWORD}
      driver-class-name: org.postgresql.Driver

  flyway:
    main:
      locations: classpath:db/migration
    coupon:
      locations: classpath:db/migration-coupon
```

### 6.2 Java 설정

```java
@Configuration
public class MainDataSourceConfig {

    @Bean
    @Primary
    @ConfigurationProperties("spring.datasource.main.master")
    public DataSource mainMasterDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean
    @ConfigurationProperties("spring.datasource.main.slave")
    public DataSource mainSlaveDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean
    @Primary
    public DataSource mainRoutingDataSource() {
        ReadWriteRoutingDataSource routingDataSource = new ReadWriteRoutingDataSource();
        routingDataSource.setDefaultTargetDataSource(mainMasterDataSource());
        routingDataSource.setTargetDataSources(Map.of(
            "master", mainMasterDataSource(),
            "slave", mainSlaveDataSource()
        ));
        return routingDataSource;
    }
}

@Configuration
public class CouponDataSourceConfig {

    @Bean
    @ConfigurationProperties("spring.datasource.coupon")
    public DataSource couponDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean couponEntityManagerFactory() {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(couponDataSource());
        em.setPackagesToScan("com.petpro.coupon.domain");
        // ...
        return em;
    }
}
```

---

## 7. 백업 및 복구

### 7.1 백업 스크립트

```bash
#!/bin/bash
# backup.sh

DATE=$(date +%Y%m%d_%H%M%S)
BACKUP_DIR=/backup/petpro

# Main DB 백업
pg_dump -h localhost -p 5432 -U petpro -F c petpro > $BACKUP_DIR/main_$DATE.dump

# Coupon DB 백업
pg_dump -h localhost -p 5435 -U petpro -F c petpro_coupon > $BACKUP_DIR/coupon_$DATE.dump

# 7일 이상 된 백업 삭제
find $BACKUP_DIR -name "*.dump" -mtime +7 -delete
```

### 7.2 복구 스크립트

```bash
#!/bin/bash
# restore.sh

BACKUP_FILE=$1

pg_restore -h localhost -p 5432 -U petpro -d petpro -c $BACKUP_FILE
```

---

## 8. 성능 튜닝

### 8.1 PostgreSQL 설정

```
# postgresql.conf

# 메모리
shared_buffers = 4GB
effective_cache_size = 12GB
work_mem = 256MB
maintenance_work_mem = 1GB

# WAL
wal_buffers = 64MB
checkpoint_completion_target = 0.9

# 쿼리 플래너
random_page_cost = 1.1
effective_io_concurrency = 200

# 복제
max_wal_senders = 3
wal_level = replica
```

### 8.2 커넥션 풀 설정

```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      idle-timeout: 300000
      max-lifetime: 1800000
      connection-timeout: 30000
```

