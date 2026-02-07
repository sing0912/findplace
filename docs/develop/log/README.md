# 로그 도메인 영구지침

## 개요

로그 데이터를 별도 MySQL Master-Slave DB로 물리적 분리하여 서비스 DB(PostgreSQL)의 성능 영향을 최소화하고, 로그 데이터 특성(대량 쓰기, 통계 조회)에 최적화합니다.

---

## 아키텍처

### DB 구성

```
┌────────────────────────┐     ┌─────────────────────────┐
│  PostgreSQL (서비스DB)  │     │   MySQL (로그DB)         │
│  Master + Slave 1, 2   │     │   Master (CUD)           │
│  - users, orders, ...  │     │   Slave  (Read Only)     │
└────────────────────────┘     └─────────────────────────┘
```

- **MySQL Master**: 로그 쓰기 (port: 3306)
- **MySQL Slave**: 통계 조회 (port: 3307, read-only)

### 비동기 로깅 흐름

```
[비즈니스 서비스] → adminActionLogService.logAdminAction(...) // @Async
     |                        ↓
     |              logAsyncExecutor 스레드 풀에서 실행
     |                        ↓
     |              @Transactional("logTransactionManager") → MySQL Master
     |                        ↓
     |              실패 시 log.error()만 (예외 전파 안 함)
     ↓
  비즈니스 로직 계속 (차단 없음)
```

---

## 인프라 (Docker)

### MySQL Master

| 항목 | 값 |
|------|-----|
| 이미지 | mysql:8.0 |
| 포트 | ${LOG_DB_MASTER_PORT:-3306}:3306 |
| DB명 | petpro_log |
| 사용자 | ${LOG_DB_USERNAME} / ${LOG_DB_PASSWORD} |
| 특성 | binlog, GTID, ROW format, utf8mb4 |

### MySQL Slave

| 항목 | 값 |
|------|-----|
| 이미지 | mysql:8.0 |
| 포트 | ${LOG_DB_SLAVE_PORT:-3307}:3306 |
| 특성 | read-only, super-read-only, GTID 기반 복제 |

---

## Gradle 의존성

```kotlin
runtimeOnly("com.mysql:mysql-connector-j")
runtimeOnly("org.flywaydb:flyway-mysql:10.10.0")
testImplementation("org.testcontainers:mysql:1.19.7")
```

---

## Spring Boot 설정

### application.yml 추가 속성

```yaml
log:
  datasource:
    master:
      jdbc-url: jdbc:mysql://${LOG_DB_HOST:localhost}:${LOG_DB_MASTER_PORT:3306}/petpro_log
      username: ${LOG_DB_USERNAME:loguser}
      password: ${LOG_DB_PASSWORD}
      driver-class-name: com.mysql.cj.jdbc.Driver
      hikari:
        pool-name: log-master-pool
        maximum-pool-size: 10
        minimum-idle: 3
    slave:
      jdbc-url: jdbc:mysql://${LOG_DB_HOST:localhost}:${LOG_DB_SLAVE_PORT:3307}/petpro_log
      username: ${LOG_DB_USERNAME:loguser}
      password: ${LOG_DB_PASSWORD}
      driver-class-name: com.mysql.cj.jdbc.Driver
      hikari:
        pool-name: log-slave-pool
        maximum-pool-size: 10
        minimum-idle: 3
```

### Config 클래스

| 클래스 | 역할 |
|--------|------|
| `LogDataSourceConfig` | MySQL Master-Slave 라우팅 (AbstractRoutingDataSource + LazyConnectionDataSourceProxy) |
| `LogJpaConfig` | EntityManagerFactory + TransactionManager (persistenceUnit: "log", MySQLDialect) |
| `LogFlywayConfig` | MySQL 전용 Flyway (locations: db/log-migration, table: flyway_schema_history_log) |
| `LogAsyncConfig` | @EnableAsync + logAsyncExecutor (core:5, max:20, queue:500, CallerRunsPolicy) |

---

## 테이블 스키마

### admin_action_logs (운영자 행위 로그)

| 컬럼 | 타입 | 제약 | 설명 |
|------|------|------|------|
| id | BIGINT AUTO_INCREMENT | PK | |
| admin_id | BIGINT | NOT NULL | 운영자 ID |
| action_type | VARCHAR(50) | NOT NULL | 행위 유형 |
| target_type | VARCHAR(50) | NOT NULL | 대상 유형 |
| target_id | BIGINT | | 대상 ID |
| description | TEXT | | 설명 |
| detail_json | JSON | | 상세 JSON |
| ip_address | VARCHAR(45) | | IP 주소 |
| user_agent | VARCHAR(500) | | UserAgent |
| created_at | DATETIME(3) | DEFAULT CURRENT_TIMESTAMP(3) | 생성일시 |

**인덱스**: admin_id, action_type, (target_type, target_id), created_at

### user_action_logs (사용자 행위 로그)

| 컬럼 | 타입 | 제약 | 설명 |
|------|------|------|------|
| id | BIGINT AUTO_INCREMENT | PK | |
| user_id | BIGINT | NOT NULL | 사용자 ID |
| action_type | VARCHAR(50) | NOT NULL | 행위 유형 |
| target_type | VARCHAR(50) | | 대상 유형 |
| target_id | BIGINT | | 대상 ID |
| description | TEXT | | 설명 |
| detail_json | JSON | | 상세 JSON |
| ip_address | VARCHAR(45) | | IP 주소 |
| user_agent | VARCHAR(500) | | UserAgent |
| device_type | VARCHAR(20) | | 디바이스 유형 |
| created_at | DATETIME(3) | DEFAULT CURRENT_TIMESTAMP(3) | 생성일시 |

**인덱스**: user_id, action_type, (target_type, target_id), created_at, device_type, (user_id, created_at)

### user_demographics_snapshots (인구통계 스냅샷)

| 컬럼 | 타입 | 제약 | 설명 |
|------|------|------|------|
| id | BIGINT AUTO_INCREMENT | PK | |
| user_id | BIGINT | NOT NULL | 사용자 ID |
| age_group | VARCHAR(10) | | 연령대 |
| gender | VARCHAR(10) | | 성별 |
| region_code | VARCHAR(20) | | 지역코드 |
| snapshot_date | DATE | NOT NULL | 스냅샷 날짜 |
| created_at | DATETIME(3) | DEFAULT CURRENT_TIMESTAMP(3) | 생성일시 |

**UNIQUE**: (user_id, snapshot_date)

### batch_job_execution_logs (배치 실행 로그)

| 컬럼 | 타입 | 제약 | 설명 |
|------|------|------|------|
| id | BIGINT AUTO_INCREMENT | PK | |
| job_name | VARCHAR(100) | NOT NULL | 작업명 |
| job_type | VARCHAR(50) | NOT NULL | 작업유형 |
| started_at | DATETIME(3) | NOT NULL | 시작시간 |
| completed_at | DATETIME(3) | | 완료시간 |
| status | VARCHAR(20) | NOT NULL | 상태 |
| total_count | INT | DEFAULT 0 | 총 건수 |
| success_count | INT | DEFAULT 0 | 성공 건수 |
| fail_count | INT | DEFAULT 0 | 실패 건수 |
| error_message | TEXT | | 에러 메시지 |
| execution_time_ms | BIGINT | | 실행시간(ms) |
| created_at | DATETIME(3) | DEFAULT CURRENT_TIMESTAMP(3) | 생성일시 |

**인덱스**: job_name, started_at, status

### system_sync_logs (시스템 연동 로그)

| 컬럼 | 타입 | 제약 | 설명 |
|------|------|------|------|
| id | BIGINT AUTO_INCREMENT | PK | |
| sync_type | VARCHAR(50) | NOT NULL | 동기화 유형 |
| source_system | VARCHAR(50) | NOT NULL | 소스 시스템 |
| started_at | DATETIME(3) | NOT NULL | 시작시간 |
| completed_at | DATETIME(3) | | 완료시간 |
| status | VARCHAR(20) | NOT NULL | 상태 |
| total_count | INT | DEFAULT 0 | 총 건수 |
| inserted_count | INT | DEFAULT 0 | 추가 건수 |
| updated_count | INT | DEFAULT 0 | 수정 건수 |
| deleted_count | INT | DEFAULT 0 | 삭제 건수 |
| error_count | INT | DEFAULT 0 | 에러 건수 |
| error_message | TEXT | | 에러 메시지 |
| created_at | DATETIME(3) | DEFAULT CURRENT_TIMESTAMP(3) | 생성일시 |

**인덱스**: sync_type, source_system, started_at, status

---

## Enum 정의

### AdminActionType

```
USER_INFO_EDIT, USER_STATUS_CHANGE, USER_ROLE_CHANGE,
COUPON_ISSUE, COUPON_CANCEL,
ORDER_PROCESS, ORDER_CANCEL,
PRODUCT_REGISTER, PRODUCT_EDIT, PRODUCT_DELETE,
RESERVATION_PROCESS, SETTLEMENT_PROCESS,
SYSTEM_CONFIG_CHANGE
```

### UserActionType

```
LOGIN, LOGOUT, PROFILE_EDIT, PASSWORD_CHANGE,
REWARD_USE, DEPOSIT_USE, COUPON_USE,
ORDER_CREATE, ORDER_CANCEL,
RESERVATION_CREATE, RESERVATION_CANCEL,
REVIEW_WRITE, INQUIRY_CREATE
```

### TargetType

```
USER, ORDER, PRODUCT, COUPON, RESERVATION, PAYMENT, REVIEW, INQUIRY, SYSTEM
```

### DeviceType

```
MOBILE, TABLET, DESKTOP, OTHER
```

---

## Entity

### AdminActionLog

| 필드 | 타입 | 매핑 |
|------|------|------|
| id | Long | @Id @GeneratedValue |
| adminId | Long | NOT NULL |
| actionType | AdminActionType | @Enumerated(STRING) |
| targetType | TargetType | @Enumerated(STRING) |
| targetId | Long | |
| description | String | TEXT |
| detailJson | String | @Column(columnDefinition="json") |
| ipAddress | String | VARCHAR(45) |
| userAgent | String | VARCHAR(500) |
| createdAt | LocalDateTime | |

### UserActionLog

| 필드 | 타입 | 매핑 |
|------|------|------|
| id | Long | @Id @GeneratedValue |
| userId | Long | NOT NULL |
| actionType | UserActionType | @Enumerated(STRING) |
| targetType | TargetType | @Enumerated(STRING) |
| targetId | Long | |
| description | String | TEXT |
| detailJson | String | @Column(columnDefinition="json") |
| ipAddress | String | VARCHAR(45) |
| userAgent | String | VARCHAR(500) |
| deviceType | DeviceType | @Enumerated(STRING) |
| createdAt | LocalDateTime | |

### UserDemographicsSnapshot

| 필드 | 타입 | 매핑 |
|------|------|------|
| id | Long | @Id @GeneratedValue |
| userId | Long | NOT NULL |
| ageGroup | String | VARCHAR(10) |
| gender | String | VARCHAR(10) |
| regionCode | String | VARCHAR(20) |
| snapshotDate | LocalDate | NOT NULL |
| createdAt | LocalDateTime | |

### BatchJobExecutionLog

| 필드 | 타입 | 매핑 |
|------|------|------|
| id | Long | @Id @GeneratedValue |
| jobName | String | NOT NULL, VARCHAR(100) |
| jobType | String | NOT NULL, VARCHAR(50) |
| startedAt | LocalDateTime | NOT NULL |
| completedAt | LocalDateTime | |
| status | String | NOT NULL, VARCHAR(20) |
| totalCount | Integer | DEFAULT 0 |
| successCount | Integer | DEFAULT 0 |
| failCount | Integer | DEFAULT 0 |
| errorMessage | String | TEXT |
| executionTimeMs | Long | |
| createdAt | LocalDateTime | |

### SystemSyncLog

| 필드 | 타입 | 매핑 |
|------|------|------|
| id | Long | @Id @GeneratedValue |
| syncType | String | NOT NULL, VARCHAR(50) |
| sourceSystem | String | NOT NULL, VARCHAR(50) |
| startedAt | LocalDateTime | NOT NULL |
| completedAt | LocalDateTime | |
| status | String | NOT NULL, VARCHAR(20) |
| totalCount | Integer | DEFAULT 0 |
| insertedCount | Integer | DEFAULT 0 |
| updatedCount | Integer | DEFAULT 0 |
| deletedCount | Integer | DEFAULT 0 |
| errorCount | Integer | DEFAULT 0 |
| errorMessage | String | TEXT |
| createdAt | LocalDateTime | |

---

## Repository

### AdminActionLogRepository

```java
Page<AdminActionLog> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end, Pageable pageable);
Page<AdminActionLog> findByAdminIdAndCreatedAtBetween(Long adminId, LocalDateTime start, LocalDateTime end, Pageable pageable);
Page<AdminActionLog> findByActionTypeAndCreatedAtBetween(AdminActionType actionType, LocalDateTime start, LocalDateTime end, Pageable pageable);

// 통계
@Query("SELECT a.actionType, COUNT(a) FROM AdminActionLog a WHERE a.createdAt BETWEEN :start AND :end GROUP BY a.actionType")
List<Object[]> countByActionType(LocalDateTime start, LocalDateTime end);

@Query("SELECT a.adminId, COUNT(a) FROM AdminActionLog a WHERE a.createdAt BETWEEN :start AND :end GROUP BY a.adminId ORDER BY COUNT(a) DESC")
List<Object[]> countByAdminId(LocalDateTime start, LocalDateTime end);
```

### UserActionLogRepository

```java
Page<UserActionLog> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end, Pageable pageable);
Page<UserActionLog> findByUserIdAndCreatedAtBetween(Long userId, LocalDateTime start, LocalDateTime end, Pageable pageable);
Page<UserActionLog> findByActionTypeAndCreatedAtBetween(UserActionType actionType, LocalDateTime start, LocalDateTime end, Pageable pageable);

// 통계
@Query("SELECT a.actionType, COUNT(a) FROM UserActionLog a WHERE a.createdAt BETWEEN :start AND :end GROUP BY a.actionType")
List<Object[]> countByActionType(LocalDateTime start, LocalDateTime end);

@Query("SELECT HOUR(a.createdAt), COUNT(a) FROM UserActionLog a WHERE a.createdAt BETWEEN :start AND :end GROUP BY HOUR(a.createdAt)")
List<Object[]> countByHour(LocalDateTime start, LocalDateTime end);

@Query("SELECT DAYOFWEEK(a.createdAt), COUNT(a) FROM UserActionLog a WHERE a.createdAt BETWEEN :start AND :end GROUP BY DAYOFWEEK(a.createdAt)")
List<Object[]> countByDayOfWeek(LocalDateTime start, LocalDateTime end);

@Query("SELECT a.deviceType, COUNT(a) FROM UserActionLog a WHERE a.createdAt BETWEEN :start AND :end GROUP BY a.deviceType")
List<Object[]> countByDeviceType(LocalDateTime start, LocalDateTime end);
```

### UserDemographicsSnapshotRepository

```java
List<UserDemographicsSnapshot> findBySnapshotDate(LocalDate date);

@Query("SELECT d.ageGroup, COUNT(d) FROM UserDemographicsSnapshot d WHERE d.snapshotDate = :date GROUP BY d.ageGroup")
List<Object[]> countByAgeGroup(LocalDate date);

@Query("SELECT d.gender, COUNT(d) FROM UserDemographicsSnapshot d WHERE d.snapshotDate = :date GROUP BY d.gender")
List<Object[]> countByGender(LocalDate date);

@Query("SELECT d.regionCode, COUNT(d) FROM UserDemographicsSnapshot d WHERE d.snapshotDate = :date GROUP BY d.regionCode")
List<Object[]> countByRegion(LocalDate date);
```

---

## DTO

### LogRequest

- `AdminActionLogRequest`: adminId, actionType, targetType, targetId, description, detailJson
- `UserActionLogRequest`: userId, actionType, targetType, targetId, description, detailJson

### LogResponse

- `AdminActionLogResponse`: id, adminId, actionType, targetType, targetId, description, detailJson, ipAddress, userAgent, createdAt
- `UserActionLogResponse`: id, userId, actionType, targetType, targetId, description, detailJson, ipAddress, userAgent, deviceType, createdAt

### StatisticsResponse

- `ActionTypeCount`: actionType (String), count (Long)
- `HourlyDistribution`: hour (Integer), count (Long)
- `DayOfWeekDistribution`: dayOfWeek (Integer), count (Long)
- `DeviceTypeDistribution`: deviceType (String), count (Long)
- `AdminActionStatistics`: List<ActionTypeCount> actionTypeCounts, List<ActionTypeCount> adminCounts
- `UserActionStatistics`: List<ActionTypeCount> actionTypeCounts
- `UserBehaviorStatistics`: List<HourlyDistribution> hourly, List<DayOfWeekDistribution> dayOfWeek, List<DeviceTypeDistribution> deviceType
- `CsAnalysisStatistics`: totalInquiries (Long), totalOrders (Long), csRate (Double)

---

## Service

### AdminActionLogService

- `@Async("logAsyncExecutor")` + `@Transactional("logTransactionManager")`
- `logAdminAction(AdminActionLogRequest request, HttpServletRequest httpRequest)` — 비동기 로그 저장
- 실패 시 `log.error()` (예외 전파 안 함)

### UserActionLogService

- `@Async("logAsyncExecutor")` + `@Transactional("logTransactionManager")`
- `logUserAction(UserActionLogRequest request, HttpServletRequest httpRequest)` — 비동기 로그 저장
- 실패 시 `log.error()` (예외 전파 안 함)

### LogStatisticsService

- `@Transactional(value = "logTransactionManager", readOnly = true)` → Slave 조회
- `getAdminActionStatistics(LocalDateTime start, LocalDateTime end)` → AdminActionStatistics
- `getUserActionStatistics(LocalDateTime start, LocalDateTime end)` → UserActionStatistics
- `getUserBehaviorStatistics(LocalDateTime start, LocalDateTime end)` → UserBehaviorStatistics
- `getCsAnalysisStatistics(LocalDateTime start, LocalDateTime end)` → CsAnalysisStatistics

### DemographicsSnapshotService

- `@Scheduled(cron = "0 0 2 * * *")` — 매일 02시 실행
- 메인DB 사용자 데이터 → 로그DB 스냅샷 저장

---

## API 엔드포인트

| Method | URL | 설명 | 권한 |
|--------|-----|------|------|
| GET | /v1/admin/statistics/admin-actions | 운영자 행위 통계 | ADMIN |
| GET | /v1/admin/statistics/user-actions | 사용자 행위 통계 | ADMIN |
| GET | /v1/admin/statistics/user-behavior | 사용자 행동 패턴 | ADMIN |
| GET | /v1/admin/statistics/cs-analysis | CS 패턴 분석 | ADMIN |
| POST | /v1/admin/log-migration/execute | 데이터 이관 실행 | ADMIN |

### 공통 파라미터

- `startDate` (LocalDateTime, 필수): 시작일시
- `endDate` (LocalDateTime, 필수): 종료일시
- `actionType` (String, 선택): 행위 유형 필터
- `adminId` (Long, 선택): 관리자 ID 필터

### 응답 형식

```json
{
  "success": true,
  "data": { ... },
  "message": null
}
```

---

## 에러 코드

| 코드 | HTTP | 설명 |
|------|------|------|
| L001 | 500 | 로그 저장 실패 |
| L002 | 500 | 로그 통계 조회 실패 |

---

## Utility

### RequestContextUtil

- `getClientIp(HttpServletRequest)` — X-Forwarded-For 또는 remoteAddr
- `getUserAgent(HttpServletRequest)` — User-Agent 헤더
- `detectDeviceType(String userAgent)` — Mobile/Tablet/Desktop/Other 판별

---

## 데이터 이관 매핑

| PostgreSQL (기존) | MySQL (신규) | 매핑 |
|-------------------|-------------|------|
| user_status_change_logs | admin_action_logs | actionType=USER_STATUS_CHANGE |
| user_role_change_logs | admin_action_logs | actionType=USER_ROLE_CHANGE |
| batch_job_logs | batch_job_execution_logs | 필드 1:1 매핑 |
| funeral_home_sync_logs | system_sync_logs | syncType→sync_type, sourceSystem=FUNERAL_HOME_API |

---

## 기존 코드 수정

### AdminUserService

- `changeStatus()`: 기존 PostgreSQL 로그 저장 유지 + `AdminActionLogService.logAdminAction()` 비동기 호출 추가
- `changeRole()`: 기존 PostgreSQL 로그 저장 유지 + `AdminActionLogService.logAdminAction()` 비동기 호출 추가

### ErrorCode

- `LOG_WRITE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "L001", "로그 저장에 실패했습니다.")`
- `LOG_STATISTICS_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "L002", "로그 통계 조회에 실패했습니다.")`
