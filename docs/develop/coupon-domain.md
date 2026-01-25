# Coupon Domain 영구지침

## 1. 개요

쿠폰(Coupon) 도메인은 할인 쿠폰의 생성, 발급, 사용, 만료를 관리합니다.
**별도의 데이터베이스(findplace_coupon)**를 사용하여 마이크로서비스로 분리 가능하도록 설계되었습니다.

### 1.1 핵심 특징
- **별도 DB**: `findplace_coupon` (Port: 5435)
- **EAV 패턴**: 유연한 쿠폰 조건 설정
- **자동 발급**: 이벤트 기반 쿠폰 발급
- **배치 처리**: 생일 쿠폰, 만료 쿠폰 자동 회수

### 1.2 핵심 비즈니스 규칙
- 사용자당 쿠폰 발급 횟수 제한 (연 단위)
- 쿠폰 조건은 EAV 패턴으로 유연하게 설정
- Main DB와 FK 없이 ID만 참조

---

## 2. 아키텍처

### 2.1 패키지 구조
```
domain/coupon/
├── config/
│   └── CouponDataSourceConfig.java   # 별도 데이터소스 설정
├── entity/
│   ├── CouponType.java               # 쿠폰 유형
│   ├── Coupon.java                   # 쿠폰 마스터
│   ├── CouponCondition.java          # 쿠폰 조건 (EAV)
│   ├── UserCoupon.java               # 사용자 보유 쿠폰
│   ├── CouponUsageHistory.java       # 사용 이력
│   ├── DiscountMethod.java           # enum
│   ├── IssueType.java                # enum
│   ├── AutoIssueEvent.java           # enum
│   ├── CouponStatus.java             # enum
│   └── ConditionOperator.java        # enum
├── repository/
│   ├── CouponTypeRepository.java
│   ├── CouponRepository.java
│   ├── UserCouponRepository.java
│   └── CouponUsageHistoryRepository.java
├── dto/
│   ├── CouponRequest.java
│   └── CouponResponse.java
├── service/
│   └── CouponService.java
├── controller/
│   ├── CouponController.java         # 사용자 API
│   └── AdminCouponController.java    # 관리자 API
└── event/
    └── (이벤트 리스너 - 추후 구현)
```

### 2.2 데이터베이스 구성
```
Coupon DB (findplace_coupon) - Port: 5435
├── coupon_types          쿠폰 유형 정의
├── coupons               쿠폰 마스터
├── coupon_conditions     쿠폰 조건 (EAV)
├── user_coupons          회원 보유 쿠폰
└── coupon_usage_histories 사용 이력

참조 관계 (FK 아님, ID만 참조):
- user_coupons.user_id → Main DB users.id
- user_coupons.order_id → Main DB orders.id
```

---

## 3. 설정

### 3.1 application.yml
```yaml
coupon:
  datasource:
    url: jdbc:postgresql://${COUPON_DB_HOST:localhost}:${COUPON_DB_PORT:5435}/findplace_coupon
    username: ${COUPON_DB_USERNAME:coupon}
    password: ${COUPON_DB_PASSWORD:coupon123!}
    driver-class-name: org.postgresql.Driver
    hikari:
      pool-name: coupon-pool
      maximum-pool-size: 5
```

### 3.2 환경변수
| 변수 | 설명 | 기본값 |
|------|------|--------|
| COUPON_DB_HOST | 쿠폰 DB 호스트 | localhost |
| COUPON_DB_PORT | 쿠폰 DB 포트 | 5435 |
| COUPON_DB_USERNAME | 쿠폰 DB 사용자 | coupon |
| COUPON_DB_PASSWORD | 쿠폰 DB 비밀번호 | coupon123! |

### 3.3 docker-compose.yml
```yaml
postgres-coupon:
  image: postgres:16-alpine
  container_name: findplace-postgres-coupon
  environment:
    POSTGRES_DB: findplace_coupon
    POSTGRES_USER: coupon
    POSTGRES_PASSWORD: coupon123!
  ports:
    - "5435:5432"
```

---

## 4. API 명세

### 4.1 사용자 API

| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | /api/v1/coupons/my | 내 쿠폰 목록 |
| GET | /api/v1/coupons/my/available | 사용 가능한 쿠폰 |
| POST | /api/v1/coupons/register | 쿠폰 코드 등록 |

### 4.2 관리자 API

| Method | Endpoint | 설명 | 권한 |
|--------|----------|------|------|
| GET | /api/v1/admin/coupons | 쿠폰 목록 | ADMIN |
| GET | /api/v1/admin/coupons/{id} | 쿠폰 상세 | ADMIN |
| POST | /api/v1/admin/coupons | 쿠폰 생성 | ADMIN |
| POST | /api/v1/admin/coupons/{id}/issue | 특정 회원에게 발급 | ADMIN |
| GET | /api/v1/admin/coupons/types | 쿠폰 유형 목록 | ADMIN |

---

## 5. 데이터 모델

### 5.1 DiscountMethod (할인 방식)
| 값 | 설명 |
|----|------|
| FIXED | 정액 할인 (discountValue = 금액) |
| PERCENT | 정률 할인 (discountValue = 퍼센트) |
| FREE | 무료 (배송비 등) |

### 5.2 IssueType (발급 유형)
| 값 | 설명 |
|----|------|
| MANUAL | 관리자 수동 발급 |
| CODE | 쿠폰 코드 입력 |
| AUTO | 이벤트 자동 발급 |

### 5.3 AutoIssueEvent (자동 발급 이벤트)
| 값 | 설명 | 처리 방식 |
|----|------|----------|
| SIGNUP | 회원가입 | 실시간 (이벤트) |
| FIRST_ORDER | 첫 주문 | 실시간 (이벤트) |
| BIRTHDAY | 생일 | 배치잡 (매일) |
| DORMANT_RETURN | 휴면 해제 | 실시간 (이벤트) |
| REVIEW_WRITE | 리뷰 작성 | 실시간 (이벤트) |

### 5.4 CouponStatus (사용자 쿠폰 상태)
| 값 | 설명 |
|----|------|
| AVAILABLE | 사용 가능 |
| USED | 사용 완료 |
| EXPIRED | 만료됨 |
| REVOKED | 회수됨 |

### 5.5 ConditionOperator (조건 연산자)
| 값 | 설명 | 사용 예 |
|----|------|--------|
| EQ | 같음 | FIRST_ORDER EQ true |
| GTE | 이상 | MIN_ORDER_AMOUNT GTE 30000 |
| IN | 포함 | CATEGORY IN SERVICE,PRODUCT |
| BETWEEN | 범위 | TIME_RANGE BETWEEN 09:00,18:00 |

---

## 6. 비즈니스 로직

### 6.1 쿠폰 발급 프로세스
```
1. 쿠폰 활성화 상태 확인
2. 발급 수량 한도 확인 (maxIssueCount)
3. 사용자별 발급 횟수 확인 (maxPerUser, 올해 기준)
4. 유효기간 계산
   - validDays 있으면: 발급일 + validDays
   - 없으면: validEndDate
5. UserCoupon 생성 (status: AVAILABLE)
6. 발급 카운트 증가 (issuedCount++)
```

### 6.2 할인 금액 계산
```java
public BigDecimal calculateDiscount(BigDecimal orderAmount) {
    switch (discountMethod) {
        case FIXED:
            return discountValue;
        case PERCENT:
            BigDecimal discount = orderAmount.multiply(discountValue)
                                             .divide(BigDecimal.valueOf(100));
            return maxDiscountAmount != null
                ? discount.min(maxDiscountAmount) : discount;
        case FREE:
            return orderAmount;
    }
}
```

---

## 7. Flyway 마이그레이션

Coupon DB 전용 마이그레이션 (db/migration-coupon/)
| 버전 | 내용 |
|------|------|
| V1 | coupon_types + 기본 데이터 (8종) |
| V2 | coupons |
| V3 | coupon_conditions |
| V4 | user_coupons |
| V5 | coupon_usage_histories |

---

## 8. 멀티 데이터소스 설정

### 8.1 CouponDataSourceConfig
```java
@Configuration
@EnableJpaRepositories(
    basePackages = "com.findplace.domain.coupon.repository",
    entityManagerFactoryRef = "couponEntityManagerFactory",
    transactionManagerRef = "couponTransactionManager"
)
public class CouponDataSourceConfig {
    // 별도의 DataSource, EntityManagerFactory, TransactionManager

    // Hibernate 설정
    Map<String, Object> properties = new HashMap<>();
    properties.put("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
    properties.put("hibernate.hbm2ddl.auto", "update");  // 개발용: 테이블 자동 생성
    properties.put("hibernate.format_sql", true);
    properties.put("hibernate.physical_naming_strategy",
            "org.hibernate.boot.model.naming.CamelCaseToUnderscoresNamingStrategy");
}
```

### 8.2 JpaConfig 주의사항
쿠폰 패키지는 별도 DataSource를 사용하므로 메인 JpaConfig에서 **명시적으로 제외**해야 합니다.

```java
@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
    basePackages = {
        "com.findplace.domain.user.repository",
        "com.findplace.domain.admin.repository",
        "com.findplace.domain.funeralhome.repository",
        "com.findplace.domain.region.repository",
        "com.findplace.domain.pet.repository",
        "com.findplace.domain.batch.repository"
    },
    entityManagerFactoryRef = "entityManagerFactory",
    transactionManagerRef = "transactionManager"
    // coupon 패키지는 CouponDataSourceConfig에서 별도로 관리
)
public class JpaConfig {
    @Bean
    @Primary
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(
            EntityManagerFactoryBuilder builder, DataSource dataSource) {
        Map<String, Object> properties = new HashMap<>();
        properties.put("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
        properties.put("hibernate.hbm2ddl.auto", "update");  // 개발용
        properties.put("hibernate.format_sql", true);
        properties.put("hibernate.physical_naming_strategy",
                "org.hibernate.boot.model.naming.CamelCaseToUnderscoresNamingStrategy");

        return builder.dataSource(dataSource)
                .packages(/* 엔티티 패키지 목록 */)
                .persistenceUnit("main")
                .properties(properties)
                .build();
    }

    @Bean
    @Primary
    public PlatformTransactionManager transactionManager(
            @Qualifier("entityManagerFactory") EntityManagerFactory emf) {
        return new JpaTransactionManager(emf);
    }
}
```

**주의**: `com.findplace.domain` 전체를 스캔하면 coupon 리포지토리가 중복 등록되어 `BeanDefinitionOverrideException` 발생

### 8.3 트랜잭션 관리
```java
@Service
@Transactional(transactionManager = "couponTransactionManager")
public class CouponService { ... }
```

---

## 9. 테스트 가이드

### 9.1 할인 계산 테스트
```java
@Test
void calculateDiscount_Fixed() {
    Coupon coupon = Coupon.builder()
        .discountMethod(DiscountMethod.FIXED)
        .discountValue(BigDecimal.valueOf(5000))
        .build();

    BigDecimal discount = coupon.calculateDiscount(BigDecimal.valueOf(30000));

    assertThat(discount).isEqualByComparingTo(BigDecimal.valueOf(5000));
}

@Test
void calculateDiscount_Percent_WithMax() {
    Coupon coupon = Coupon.builder()
        .discountMethod(DiscountMethod.PERCENT)
        .discountValue(BigDecimal.valueOf(20))
        .maxDiscountAmount(BigDecimal.valueOf(10000))
        .build();

    BigDecimal discount = coupon.calculateDiscount(BigDecimal.valueOf(100000));

    assertThat(discount).isEqualByComparingTo(BigDecimal.valueOf(10000));
}
```

---

## 10. 트러블슈팅

### 10.1 별도 DB 연결 오류
```
원인: 쿠폰 DB 미실행, 환경변수 오류
해결:
1. docker-compose에서 postgres-coupon 실행 확인
2. COUPON_DB_* 환경변수 확인
3. 포트 5435 사용 가능 여부 확인
```

### 10.2 트랜잭션 관리 주의
```
주의: 쿠폰 관련 작업은 반드시 couponTransactionManager 사용
잘못된 예: @Transactional (기본 트랜잭션 매니저 사용)
올바른 예: @Transactional(transactionManager = "couponTransactionManager")
```

---

## 11. 마이크로서비스 분리 시 고려사항

### 현재 구조 (모놀리식)
- 백엔드 내 `coupon` 패키지로 분리
- 별도 DB 사용
- Main DB와 ID 참조만 (FK 없음)

### 분리 시 변경사항
- REST API 또는 gRPC로 통신
- 이벤트 발행은 Kafka/RabbitMQ로 변경
- 분산 트랜잭션 처리 (Saga 패턴)

---

## 12. 관련 도메인

- **User**: 쿠폰 발급 대상 (user_id 참조)
- **Order**: 쿠폰 사용 (order_id 참조)
- **MyPage**: 쿠폰함 UI
- **Batch**: 생일 쿠폰 발급, 만료 처리
