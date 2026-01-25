# 장례식장 도메인 설계

## 1. 개요

장례식장(FuneralHome) 도메인은 공공데이터포털의 동물장묘업 API를 통해 데이터를 수집하고, 사용자에게 가까운 장례식장 정보를 제공합니다.

---

## 2. 외부 API 연동

### 2.1 공공데이터포털 API

| 항목 | 값 |
|------|-----|
| API명 | 행정안전부_동물장묘업 |
| Endpoint | `https://apis.data.go.kr/1741000/animal_cremation` |
| 인증방식 | ServiceKey (Query Parameter) |
| 일일 호출 한도 | 10,000회 |
| 데이터 형식 | JSON |

### 2.2 API 요청 파라미터

| 파라미터 | 타입 | 필수 | 설명 |
|----------|------|------|------|
| serviceKey | String | O | 인증키 |
| pageNo | Integer | X | 페이지 번호 (기본: 1) |
| numOfRows | Integer | X | 페이지당 행 수 (기본: 10, 최대: 100) |
| type | String | X | 응답 형식 (json/xml) |
| locCode | String | X | 지역 코드 |

### 2.3 API 응답 구조

```json
{
  "response": {
    "header": {
      "resultCode": "00",
      "resultMsg": "NORMAL_CODE"
    },
    "body": {
      "items": {
        "item": [
          {
            "locCode": "6110000",
            "locName": "서울특별시",
            "crematorium": "Y",
            "columbarium": "N",
            "funeral": "Y",
            "nm": "반려동물장례식장 하늘나라",
            "roadAddr": "서울특별시 강남구 테헤란로 123",
            "lotAddr": "서울특별시 강남구 역삼동 123-45",
            "telno": "02-1234-5678"
          }
        ]
      },
      "numOfRows": 10,
      "pageNo": 1,
      "totalCount": 523
    }
  }
}
```

---

## 3. 엔티티 설계

### 3.1 FuneralHome 엔티티

```
┌─────────────────────────────────────────────────────────────┐
│                       FuneralHome                           │
├─────────────────────────────────────────────────────────────┤
│  id                  BIGINT PK AUTO_INCREMENT               │
│                                                              │
│  [기본 정보]                                                  │
│  name                VARCHAR(200) NOT NULL                  │
│  roadAddress         VARCHAR(500)                           │
│  lotAddress          VARCHAR(500)                           │
│  phone               VARCHAR(50)                            │
│                                                              │
│  [지역 정보]                                                  │
│  locCode             VARCHAR(20)                            │
│  locName             VARCHAR(100)                           │
│                                                              │
│  [서비스 유형]                                                │
│  hasCrematorium      BOOLEAN DEFAULT FALSE                  │
│  hasColumbarium      BOOLEAN DEFAULT FALSE                  │
│  hasFuneral          BOOLEAN DEFAULT FALSE                  │
│                                                              │
│  [좌표 (Geocoding)]                                          │
│  latitude            DECIMAL(10,7)                          │
│  longitude           DECIMAL(10,7)                          │
│  geocodedAt          TIMESTAMP                              │
│                                                              │
│  [상태]                                                      │
│  isActive            BOOLEAN DEFAULT TRUE                   │
│  verifiedAt          TIMESTAMP                              │
│                                                              │
│  [Audit]                                                     │
│  createdAt           TIMESTAMP NOT NULL                     │
│  updatedAt           TIMESTAMP NOT NULL                     │
│  syncedAt            TIMESTAMP                              │
└─────────────────────────────────────────────────────────────┘
```

### 3.2 FuneralHomeSyncLog 엔티티

```
┌─────────────────────────────────────────────────────────────┐
│                   FuneralHomeSyncLog                        │
├─────────────────────────────────────────────────────────────┤
│  id                  BIGINT PK AUTO_INCREMENT               │
│  syncType            VARCHAR(20) NOT NULL                   │
│  startedAt           TIMESTAMP NOT NULL                     │
│  completedAt         TIMESTAMP                              │
│  status              VARCHAR(20) NOT NULL                   │
│  totalCount          INTEGER                                │
│  insertedCount       INTEGER                                │
│  updatedCount        INTEGER                                │
│  deletedCount        INTEGER                                │
│  errorCount          INTEGER                                │
│  errorMessage        TEXT                                   │
└─────────────────────────────────────────────────────────────┘
```

### 3.3 SyncType 열거형

| 값 | 설명 | 실행 주기 |
|----|------|----------|
| INCREMENTAL | 증분 동기화 | 매일 02:00 |
| FULL | 전체 동기화 | 매주 일요일 03:00 |

### 3.4 SyncStatus 열거형

| 값 | 설명 |
|----|------|
| RUNNING | 실행 중 |
| COMPLETED | 완료 |
| FAILED | 실패 |
| PARTIAL | 부분 완료 |

---

## 4. API 설계

### 4.1 사용자 API

| Method | Endpoint | 설명 | 권한 |
|--------|----------|------|------|
| GET | /funeral-homes | 장례식장 목록 조회 | 공개 |
| GET | /funeral-homes/{id} | 장례식장 상세 조회 | 공개 |
| GET | /funeral-homes/nearby | 근처 장례식장 검색 | 공개 |

### 4.2 관리자 API

| Method | Endpoint | 설명 | 권한 |
|--------|----------|------|------|
| GET | /admin/funeral-homes | 전체 목록 조회 | ADMIN |
| POST | /admin/funeral-homes/sync | 수동 동기화 실행 | ADMIN |
| GET | /admin/funeral-homes/sync/logs | 동기화 로그 조회 | ADMIN |
| PATCH | /admin/funeral-homes/{id}/status | 활성화 상태 변경 | ADMIN |

---

## 5. 요청/응답 DTO

### 5.1 근처 장례식장 검색 요청

```
GET /funeral-homes/nearby?latitude=37.5065&longitude=127.0536&radius=10&limit=20
```

| 파라미터 | 타입 | 필수 | 설명 |
|----------|------|------|------|
| latitude | Double | O | 사용자 위도 |
| longitude | Double | O | 사용자 경도 |
| radius | Integer | X | 검색 반경 (km, 기본: 10) |
| limit | Integer | X | 결과 수 (기본: 20) |
| hasCrematorium | Boolean | X | 화장장 필터 |
| hasFuneral | Boolean | X | 장례식장 필터 |

### 5.2 장례식장 목록 응답

```json
{
  "content": [
    {
      "id": 1,
      "name": "반려동물장례식장 하늘나라",
      "roadAddress": "서울특별시 강남구 테헤란로 123",
      "phone": "02-1234-5678",
      "locName": "서울특별시",
      "hasCrematorium": true,
      "hasColumbarium": false,
      "hasFuneral": true,
      "latitude": 37.5065,
      "longitude": 127.0536,
      "distance": 2.3
    }
  ],
  "totalCount": 15,
  "radius": 10
}
```

### 5.3 장례식장 상세 응답

```json
{
  "id": 1,
  "name": "반려동물장례식장 하늘나라",
  "roadAddress": "서울특별시 강남구 테헤란로 123",
  "lotAddress": "서울특별시 강남구 역삼동 123-45",
  "phone": "02-1234-5678",
  "locCode": "6110000",
  "locName": "서울특별시",
  "services": {
    "hasCrematorium": true,
    "hasColumbarium": false,
    "hasFuneral": true
  },
  "location": {
    "latitude": 37.5065,
    "longitude": 127.0536
  },
  "isActive": true,
  "syncedAt": "2025-01-25T02:00:00",
  "createdAt": "2025-01-20T10:00:00"
}
```

---

## 6. 비즈니스 로직

### 6.1 근처 장례식장 검색 (Haversine)

```java
public List<FuneralHomeDto> findNearby(double lat, double lng, int radiusKm) {
    // Haversine 공식을 이용한 거리 계산
    String sql = """
        SELECT *,
            (6371 * acos(
                cos(radians(:lat)) * cos(radians(latitude))
                * cos(radians(longitude) - radians(:lng))
                + sin(radians(:lat)) * sin(radians(latitude))
            )) AS distance
        FROM funeral_homes
        WHERE is_active = true
          AND latitude IS NOT NULL
          AND longitude IS NOT NULL
        HAVING distance <= :radius
        ORDER BY distance
        LIMIT :limit
        """;
    return jdbcTemplate.query(sql, params, rowMapper);
}
```

### 6.2 API 호출 로직

```java
@Service
public class GovApiService {

    private final String BASE_URL = "https://apis.data.go.kr/1741000/animal_cremation";
    private final String SERVICE_KEY;

    @RateLimiter(name = "govApi", fallbackMethod = "fallback")
    public GovApiResponse fetchFuneralHomes(int pageNo, int numOfRows) {
        String url = UriComponentsBuilder.fromHttpUrl(BASE_URL)
            .queryParam("serviceKey", SERVICE_KEY)
            .queryParam("pageNo", pageNo)
            .queryParam("numOfRows", numOfRows)
            .queryParam("type", "json")
            .build()
            .toUriString();

        return restTemplate.getForObject(url, GovApiResponse.class);
    }
}
```

### 6.3 동기화 프로세스

```
[증분 동기화 - 매일 02:00]
1. 마지막 동기화 시간 조회
2. API 호출 (전체 데이터)
3. 기존 데이터와 비교
4. 변경된 항목만 UPDATE
5. 새 항목 INSERT
6. 삭제된 항목 isActive = false
7. 좌표 없는 항목 Geocoding
8. 동기화 로그 저장

[전체 동기화 - 매주 일요일 03:00]
1. 모든 API 데이터 수집
2. 임시 테이블에 저장
3. 기존 테이블과 MERGE
4. 전체 좌표 재검증
5. 동기화 로그 저장
```

### 6.4 Geocoding 처리

```java
@Async
public void geocodeFuneralHomes(List<FuneralHome> homes) {
    for (FuneralHome home : homes) {
        if (home.getLatitude() == null) {
            try {
                GeocodingResult result = locationService.geocode(home.getRoadAddress());
                if (result != null) {
                    home.setLatitude(result.getLatitude());
                    home.setLongitude(result.getLongitude());
                    home.setGeocodedAt(LocalDateTime.now());
                }
                // Rate limiting: Google API 초당 50회 제한
                Thread.sleep(50);
            } catch (Exception e) {
                log.warn("Geocoding failed for: {}", home.getName(), e);
            }
        }
    }
    funeralHomeRepository.saveAll(homes);
}
```

---

## 7. 인덱스

```sql
-- 기본 조회용
CREATE INDEX idx_funeral_homes_loc_code ON funeral_homes(loc_code);
CREATE INDEX idx_funeral_homes_is_active ON funeral_homes(is_active);
CREATE INDEX idx_funeral_homes_name ON funeral_homes(name);

-- 위치 기반 검색용
CREATE INDEX idx_funeral_homes_location ON funeral_homes(latitude, longitude);

-- 서비스 유형 필터용
CREATE INDEX idx_funeral_homes_services ON funeral_homes(has_crematorium, has_funeral, has_columbarium);

-- 동기화 로그용
CREATE INDEX idx_sync_logs_sync_type ON funeral_home_sync_logs(sync_type);
CREATE INDEX idx_sync_logs_started_at ON funeral_home_sync_logs(started_at DESC);
```

---

## 8. 배치 스케줄러

### 8.1 스케줄 설정

```java
@Configuration
@EnableScheduling
public class FuneralHomeSyncScheduler {

    @Scheduled(cron = "0 0 2 * * *")  // 매일 02:00
    public void incrementalSync() {
        syncService.runIncrementalSync();
    }

    @Scheduled(cron = "0 0 3 * * SUN")  // 매주 일요일 03:00
    public void fullSync() {
        syncService.runFullSync();
    }
}
```

### 8.2 API 호출 제한 관리

```java
@Component
public class ApiRateLimiter {

    private final AtomicInteger dailyCallCount = new AtomicInteger(0);
    private static final int DAILY_LIMIT = 10000;

    @Scheduled(cron = "0 0 0 * * *")  // 매일 자정 리셋
    public void resetDailyCount() {
        dailyCallCount.set(0);
    }

    public boolean canCall() {
        return dailyCallCount.get() < DAILY_LIMIT;
    }

    public void incrementCount() {
        dailyCallCount.incrementAndGet();
    }
}
```

---

## 9. 에러 처리

### 9.1 API 에러 코드

| 코드 | 설명 | 대응 |
|------|------|------|
| 00 | 정상 | - |
| 01 | 어플리케이션 에러 | 재시도 |
| 02 | DB 에러 | 알림 발송 |
| 10 | 잘못된 요청 | 파라미터 확인 |
| 20 | 서비스 접근 거부 | API 키 확인 |
| 22 | 호출 한도 초과 | 다음 날까지 대기 |
| 30 | 등록되지 않은 키 | API 키 재발급 |
| 31 | 키 사용 기간 만료 | API 키 갱신 |

### 9.2 Retry 설정

```java
@Retryable(
    value = {ApiException.class},
    maxAttempts = 3,
    backoff = @Backoff(delay = 1000, multiplier = 2)
)
public GovApiResponse callApi(int pageNo) {
    // API 호출
}

@Recover
public GovApiResponse recover(ApiException e, int pageNo) {
    log.error("API call failed after retries: page={}", pageNo, e);
    return null;
}
```

---

## 10. 프론트엔드 UI

### 10.1 장례식장 검색 결과

```
┌─────────────────────────────────────────────────────────────┐
│  내 위치 기준 가까운 장례식장                              🔄  │
├─────────────────────────────────────────────────────────────┤
│                                                              │
│  ┌─────────────────────────────────────────────────────┐   │
│  │ 🏠 반려동물장례식장 하늘나라              2.3km 📍    │   │
│  │ 서울특별시 강남구 테헤란로 123                       │   │
│  │ 📞 02-1234-5678                                      │   │
│  │ [화장] [장례]                                        │   │
│  └─────────────────────────────────────────────────────┘   │
│                                                              │
│  ┌─────────────────────────────────────────────────────┐   │
│  │ 🏠 펫메모리얼파크                        4.1km 📍    │   │
│  │ 서울특별시 서초구 서초대로 456                       │   │
│  │ 📞 02-9876-5432                                      │   │
│  │ [화장] [납골] [장례]                                 │   │
│  └─────────────────────────────────────────────────────┘   │
│                                                              │
└─────────────────────────────────────────────────────────────┘
```

### 10.2 지도 표시

```
┌─────────────────────────────────────────────────────────────┐
│                                                              │
│         📍 장례식장A        🚩 내 위치                       │
│                                                              │
│                     📍 장례식장B                             │
│                                                              │
│    📍 장례식장C                                              │
│                           📍 장례식장D                       │
│                                                              │
│  ─────────────────────────────────────────────────────────  │
│  [10km ▼]  [화장장 ☑] [장례식장 ☑] [납골당 ☐]              │
└─────────────────────────────────────────────────────────────┘
```

---

## 11. 캐싱 전략

### 11.1 Redis 캐시

```java
@Cacheable(value = "funeralHomes", key = "'nearby:' + #lat + ':' + #lng + ':' + #radius")
public List<FuneralHomeDto> findNearby(double lat, double lng, int radius) {
    // DB 조회
}

@CacheEvict(value = "funeralHomes", allEntries = true)
public void evictCache() {
    // 동기화 후 캐시 삭제
}
```

### 11.2 캐시 TTL

| 캐시 | TTL | 이유 |
|------|-----|------|
| 근처 장례식장 | 1시간 | 데이터 변경 빈도 낮음 |
| 장례식장 상세 | 30분 | 단건 조회 자주 발생 |
| 전체 목록 | 24시간 | 일 1회 동기화 |

---

## 12. 모니터링

### 12.1 알림 조건

| 조건 | 알림 방법 |
|------|----------|
| 동기화 실패 | Slack/Email |
| API 호출 한도 90% 도달 | Slack |
| Geocoding 실패율 10% 초과 | Email |

### 12.2 대시보드 지표

- 총 장례식장 수
- 활성/비활성 장례식장 수
- 좌표 등록률
- 일별 API 호출 수
- 동기화 성공률

