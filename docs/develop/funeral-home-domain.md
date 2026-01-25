# Funeral Home Domain 영구지침

## 1. 개요

장례식장(Funeral Home) 도메인은 공공데이터포털의 동물장묘업 API를 통해 데이터를 동기화하고, 사용자에게 가까운 장례식장 정보를 제공합니다.

### 1.1 주요 기능
- 공공 API 데이터 동기화 (증분/전체)
- 근처 장례식장 검색 (Haversine 공식)
- 장례식장 목록/상세 조회
- Geocoding 연동 (좌표 자동 등록)
- 관리자 상태 관리

### 1.2 핵심 비즈니스 규칙
- 동기화는 일일 1회(02:00) 증분, 주간 1회(일요일 03:00) 전체 수행
- API 일일 호출 제한: 10,000회
- 비활성화된 장례식장은 사용자 API에서 제외
- 좌표 없는 항목은 비동기 Geocoding 처리

---

## 2. 아키텍처

### 2.1 패키지 구조
```
domain/funeralhome/
├── entity/
│   ├── FuneralHome.java          # 장례식장 엔티티
│   ├── FuneralHomeSyncLog.java   # 동기화 로그 엔티티
│   ├── SyncType.java             # 동기화 유형 enum
│   └── SyncStatus.java           # 동기화 상태 enum
├── repository/
│   ├── FuneralHomeRepository.java
│   └── FuneralHomeSyncLogRepository.java
├── dto/
│   ├── FuneralHomeRequest.java   # 요청 DTO
│   ├── FuneralHomeResponse.java  # 응답 DTO
│   └── GovApiResponse.java       # 공공 API 응답 DTO
├── service/
│   ├── FuneralHomeService.java   # 비즈니스 서비스
│   ├── FuneralHomeSyncService.java # 동기화 서비스
│   └── GovApiService.java        # 공공 API 클라이언트
├── controller/
│   ├── FuneralHomeController.java      # 사용자 API
│   └── AdminFuneralHomeController.java # 관리자 API
└── scheduler/
    └── FuneralHomeSyncScheduler.java   # 스케줄러
```

### 2.2 의존성
- `LocationService`: Geocoding을 위해 사용
- `RestTemplate`: 공공 API 호출
- `Redis Cache`: 조회 결과 캐싱

---

## 3. 설정

### 3.1 application.yml
```yaml
app:
  gov-api:
    base-url: ${GOV_API_BASE_URL:https://apis.data.go.kr/1741000/animal_cremation}
    service-key: ${GOV_API_SERVICE_KEY:}
    daily-limit: ${GOV_API_DAILY_LIMIT:10000}
```

### 3.2 환경변수
| 변수 | 설명 | 기본값 |
|------|------|--------|
| GOV_API_BASE_URL | 공공 API 기본 URL | https://apis.data.go.kr/... |
| GOV_API_SERVICE_KEY | 공공데이터포털 인증키 | (필수) |
| GOV_API_DAILY_LIMIT | 일일 호출 제한 | 10000 |

---

## 4. API 명세

> **참고**: 아래 경로는 클라이언트가 사용하는 전체 URL입니다.
> 컨트롤러에서는 `context-path: /api` 설정으로 인해 `/v1/...` 경로만 사용합니다.
> (자세한 내용은 `docs/develop/_common/api-convention.md` 참조)

### 4.1 사용자 API

| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | /api/v1/funeral-homes/nearby | 근처 장례식장 검색 |
| GET | /api/v1/funeral-homes/{id} | 장례식장 상세 조회 |
| GET | /api/v1/funeral-homes | 장례식장 목록 조회 |

### 4.2 관리자 API

| Method | Endpoint | 설명 | 권한 |
|--------|----------|------|------|
| GET | /api/v1/admin/funeral-homes | 전체 목록 조회 | ADMIN |
| PATCH | /api/v1/admin/funeral-homes/{id}/status | 상태 변경 | ADMIN |
| POST | /api/v1/admin/funeral-homes/sync/incremental | 증분 동기화 | ADMIN |
| POST | /api/v1/admin/funeral-homes/sync/full | 전체 동기화 | ADMIN |
| GET | /api/v1/admin/funeral-homes/sync/logs | 동기화 로그 조회 | ADMIN |

### 4.3 근처 장례식장 검색 요청

```
GET /api/v1/funeral-homes/nearby?latitude=37.5065&longitude=127.0536&radius=10&limit=20
```

| 파라미터 | 타입 | 필수 | 설명 |
|----------|------|------|------|
| latitude | Double | O | 위도 (33~43) |
| longitude | Double | O | 경도 (124~132) |
| radius | Integer | X | 검색 반경 km (기본: 10) |
| limit | Integer | X | 결과 수 (기본: 20) |
| hasCrematorium | Boolean | X | 화장장 필터 |
| hasFuneral | Boolean | X | 장례식장 필터 |
| hasColumbarium | Boolean | X | 납골당 필터 |

---

## 5. 데이터 모델

### 5.1 FuneralHome 엔티티
```java
@Entity
@Table(name = "funeral_homes")
public class FuneralHome extends BaseEntity {
    @Id @GeneratedValue
    private Long id;

    // 기본 정보
    private String name;
    private String roadAddress;
    private String lotAddress;
    private String phone;

    // 지역 정보
    private String locCode;
    private String locName;

    // 서비스 유형
    private Boolean hasCrematorium;
    private Boolean hasColumbarium;
    private Boolean hasFuneral;

    // 좌표
    private BigDecimal latitude;
    private BigDecimal longitude;
    private LocalDateTime geocodedAt;

    // 상태
    private Boolean isActive;
    private LocalDateTime syncedAt;
}
```

### 5.2 SyncType Enum
| 값 | 설명 | 스케줄 |
|----|------|--------|
| INCREMENTAL | 증분 동기화 | 매일 02:00 |
| FULL | 전체 동기화 | 매주 일요일 03:00 |

### 5.3 SyncStatus Enum
| 값 | 설명 |
|----|------|
| RUNNING | 실행 중 |
| COMPLETED | 완료 |
| FAILED | 실패 |
| PARTIAL | 부분 완료 |

---

## 6. Haversine 거리 계산

### 6.1 네이티브 쿼리

**주의**: `SELECT f.*` 사용 금지! 컬럼 순서가 데이터베이스에 따라 달라질 수 있어 매핑 오류 발생.

```sql
SELECT f.id, f.name, f.road_address, f.lot_address, f.phone,
       f.loc_code, f.loc_name, f.has_crematorium, f.has_columbarium,
       f.has_funeral, f.latitude, f.longitude, f.is_active,
       (6371 * acos(
           cos(radians(:lat)) * cos(radians(f.latitude))
           * cos(radians(f.longitude) - radians(:lng))
           + sin(radians(:lat)) * sin(radians(f.latitude))
       )) AS distance
FROM funeral_homes f
WHERE f.is_active = true
  AND f.latitude IS NOT NULL
  AND f.longitude IS NOT NULL
  AND (거리 계산) <= :radius
ORDER BY distance
LIMIT :limit
```

### 6.2 컬럼 인덱스 매핑

네이티브 쿼리 결과를 엔티티로 매핑할 때 사용하는 인덱스:

| Index | Column | Type |
|-------|--------|------|
| 0 | id | Long |
| 1 | name | String |
| 2 | road_address | String |
| 3 | lot_address | String |
| 4 | phone | String |
| 5 | loc_code | String |
| 6 | loc_name | String |
| 7 | has_crematorium | Boolean |
| 8 | has_columbarium | Boolean |
| 9 | has_funeral | Boolean |
| 10 | latitude | BigDecimal |
| 11 | longitude | BigDecimal |
| 12 | is_active | Boolean |
| 13 | distance | Double |

### 6.3 매핑 메서드
```java
private FuneralHome mapToFuneralHome(Object[] row) {
    return FuneralHome.builder()
            .id(((Number) row[0]).longValue())
            .name((String) row[1])
            .roadAddress((String) row[2])
            .lotAddress((String) row[3])
            .phone((String) row[4])
            .locCode((String) row[5])
            .locName((String) row[6])
            .hasCrematorium((Boolean) row[7])
            .hasColumbarium((Boolean) row[8])
            .hasFuneral((Boolean) row[9])
            .latitude(row[10] != null ? (BigDecimal) row[10] : null)
            .longitude(row[11] != null ? (BigDecimal) row[11] : null)
            .isActive((Boolean) row[12])
            .build();
}
```

### 6.4 정확도
- 서울-부산: 약 325km (실제 도로 ~400km)
- 근거리(10km 이내): 오차 1% 미만

---

## 7. 동기화 프로세스

### 7.1 증분 동기화 (매일 02:00)
1. 실행 중인 동기화 확인
2. API 전체 데이터 페이징 조회
3. 기존 데이터와 비교 (name + locCode)
4. 변경 항목 UPDATE, 신규 항목 INSERT
5. 좌표 없는 항목 비동기 Geocoding
6. 로그 저장

### 7.2 전체 동기화 (매주 일요일 03:00)
1. 증분 동기화와 동일
2. 동기화되지 않은 데이터 비활성화
3. 전체 좌표 재검증

### 7.3 API 호출 제한
- 일일 10,000회 제한
- 자정 카운트 리셋
- 제한 도달 시 동기화 중단

---

## 8. 캐싱 전략

### 8.1 캐시 키
```java
@Cacheable(value = "funeralHomes",
    key = "'nearby:' + #lat + ':' + #lng + ':' + #radius")
@Cacheable(value = "funeralHomes", key = "'detail:' + #id")
```

### 8.2 캐시 TTL
| 캐시 | TTL | 이유 |
|------|-----|------|
| 근처 장례식장 | 1시간 | 데이터 변경 빈도 낮음 |
| 장례식장 상세 | 30분 | 단건 조회 빈번 |

### 8.3 캐시 무효화
- 동기화 완료 시 전체 캐시 삭제
- 상태 변경 시 전체 캐시 삭제

---

## 9. 프론트엔드

### 9.1 컴포넌트
```
frontend/src/
├── types/funeralHome.ts
├── api/funeralHome.ts
├── hooks/useNearbyFuneralHomes.ts
├── components/funeralhome/
│   ├── FuneralHomeCard.tsx
│   └── FuneralHomeMap.tsx
└── pages/funeralhome/
    └── NearbyFuneralHomesPage.tsx
```

### 9.2 주요 훅
```typescript
const { result, isLoading, error, search } = useNearbyFuneralHomes();

// 검색 실행
search({
  latitude: 37.5065,
  longitude: 127.0536,
  radius: 10,
  hasCrematorium: true,
});
```

---

## 10. 테스트 가이드

### 10.1 엔티티 테스트
```java
@Test
void fromApiResponse_Success() {
    FuneralHome home = FuneralHome.fromApiResponse(
        "펫메모리얼", "도로명주소", "지번주소",
        "02-1234-5678", "6110000", "서울특별시",
        true, false, true
    );

    assertThat(home.getName()).isEqualTo("펫메모리얼");
    assertThat(home.getHasCrematorium()).isTrue();
}
```

### 10.2 API 키 없이 테스트
- GOV_API_SERVICE_KEY 미설정 시 빈 응답 반환
- 로그에 경고 메시지 출력

---

## 11. 트러블슈팅

### 11.1 공공 API 에러
```
ErrorCode: EXTERNAL_API_ERROR
원인: API 키 미설정, 할당량 초과, 네트워크 오류
해결:
1. GOV_API_SERVICE_KEY 환경변수 확인
2. 공공데이터포털에서 할당량 확인
3. 네트워크 연결 상태 확인
```

### 11.2 동기화 실패
```
원인: API 호출 제한, 네트워크 오류
해결:
1. 동기화 로그 확인 (/admin/sync/logs)
2. API 호출 잔여 횟수 확인
3. 수동 동기화 재실행
```

### 11.3 Geocoding 실패율 높음
```
원인: Google Maps API 키 미설정, 할당량 초과
해결:
1. GOOGLE_MAPS_API_KEY 환경변수 확인
2. Google Cloud Console에서 할당량 확인
3. 비동기 Geocoding 재실행
```

### 11.4 근처 장례식장 검색 500 에러
```
에러: Internal Server Error (500) at /api/v1/funeral-homes/nearby
원인: 네이티브 쿼리 결과 매핑 시 컬럼 인덱스 불일치
      - SELECT f.* 사용 시 데이터베이스 컬럼 순서에 의존
      - Hibernate 스키마 업데이트로 컬럼이 추가되면 인덱스 변경됨

해결:
1. 네이티브 쿼리에서 명시적 컬럼 선택 사용 (SELECT f.id, f.name, ...)
2. mapToFuneralHome 메서드의 인덱스가 쿼리 SELECT 순서와 일치하는지 확인
3. 섹션 6.2의 컬럼 인덱스 매핑 테이블 참조
```

### 11.5 BaseEntity 감사 컬럼 누락
```
에러: Schema-validation: missing column [created_by] in table [funeral_homes]
원인: V6 마이그레이션에 created_by, updated_by 컬럼 누락

해결:
1. V10__add_audit_columns.sql 마이그레이션 적용
2. 또는 hibernate.hbm2ddl.auto=update로 자동 추가 (개발 환경)
```

---

## 12. 관련 도메인

- **Location**: Geocoding 서비스 사용
- **MyPage**: 내 주변 장례식장 찾기 기능
- **Region**: 지역별 장례식장 필터링
