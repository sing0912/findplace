# Region (지역) 도메인 영구지침

## 1. 개요

지역 코드(Region) 도메인은 행정안전부의 지방자치단체 코드를 관리합니다. 장례식장 데이터 필터링 및 지역별 통계에 활용됩니다.

### 1.1 도메인 범위
- 광역시/도 (17개): 서울특별시, 부산광역시 등
- 시/군/구 (244개): 강남구, 수원시 등
- 총 261개 지역 코드 관리

### 1.2 주요 기능
- 지역 코드 계층적 조회 (METRO → CITY)
- 장례식장, 사용자 주소 등과 연동
- Redis 캐싱으로 조회 성능 최적화

---

## 2. 아키텍처

### 2.1 패키지 구조
```
domain/region/
├── entity/
│   ├── RegionCode.java      # 지역 코드 엔티티
│   └── RegionType.java      # 지역 유형 enum (METRO, CITY)
├── repository/
│   └── RegionCodeRepository.java
├── service/
│   └── RegionCodeService.java
├── controller/
│   └── RegionController.java
└── dto/
    └── RegionResponse.java  # 응답 DTO (내부 클래스)
```

### 2.2 클래스 다이어그램
```
┌─────────────────┐      ┌──────────────────────┐
│  RegionType     │      │     RegionCode       │
├─────────────────┤      ├──────────────────────┤
│ METRO           │◄─────│ id: Long             │
│ CITY            │      │ code: String         │
└─────────────────┘      │ name: String         │
                         │ type: RegionType     │
                         │ parentCode: String   │
                         │ sortOrder: Integer   │
                         │ isActive: Boolean    │
                         └──────────────────────┘
                                   ▲
                                   │ self-reference
                                   │ (parentCode)
                                   └─────────────────
```

---

## 3. API 명세

### 3.1 공개 API

| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | `/regions` | 전체 활성 지역 목록 |
| GET | `/regions/metros` | 광역시/도 목록 (cityCount 포함) |
| GET | `/regions/{metroCode}/cities` | 해당 광역시/도의 시/군/구 목록 |
| GET | `/regions/{code}` | 지역 코드로 상세 조회 |
| GET | `/regions/hierarchy` | 전체 계층 구조 조회 |

### 3.2 응답 형식

#### 광역시/도 목록 (`/regions/metros`)
```json
{
  "success": true,
  "data": {
    "metros": [
      { "code": "6110000", "name": "서울특별시", "cityCount": 25 }
    ],
    "totalCount": 17
  }
}
```

#### 시/군/구 목록 (`/regions/{metroCode}/cities`)
```json
{
  "success": true,
  "data": {
    "metroCode": "6110000",
    "metroName": "서울특별시",
    "cities": [
      { "code": "3000000", "name": "종로구" }
    ],
    "totalCount": 25
  }
}
```

---

## 4. 데이터 모델

### 4.1 region_codes 테이블
```sql
CREATE TABLE region_codes (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(20) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    type VARCHAR(20) NOT NULL,        -- 'METRO' | 'CITY'
    parent_code VARCHAR(20),          -- METRO인 경우 NULL
    sort_order INTEGER DEFAULT 0,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);
```

### 4.2 인덱스
```sql
CREATE UNIQUE INDEX idx_region_codes_code ON region_codes(code);
CREATE INDEX idx_region_codes_parent ON region_codes(parent_code);
CREATE INDEX idx_region_codes_type ON region_codes(type);
CREATE INDEX idx_region_codes_active ON region_codes(is_active);
```

### 4.3 지역 코드 체계
- 광역시/도: 7자리 (예: 6110000 - 서울특별시)
- 시/군/구: 7자리 (예: 3000000 - 종로구)
- 시/군/구의 parent_code는 소속 광역시/도의 code

---

## 5. 비즈니스 규칙

### 5.1 핵심 규칙
1. 지역 코드는 고유해야 함 (unique constraint)
2. 시/군/구는 반드시 유효한 광역시/도를 parent로 가져야 함
3. is_active가 false인 지역은 일반 조회에서 제외
4. 정렬은 항상 sort_order 기준

### 5.2 데이터 무결성
- 광역시/도 삭제 시 하위 시/군/구도 비활성화 처리 필요
- 지역 코드 변경은 관리자 권한으로만 가능

---

## 6. 설정

### 6.1 캐싱 설정 (application.yml)
```yaml
spring:
  cache:
    type: redis
    redis:
      time-to-live: 86400000  # 24시간 (ms)
```

### 6.2 캐시 키
| 캐시 키 | TTL | 설명 |
|---------|-----|------|
| `regions::metros` | 24h | 광역시/도 목록 |
| `regions::cities:{metroCode}` | 24h | 시/군/구 목록 |
| `regions::hierarchy` | 24h | 계층 구조 |

---

## 7. 프론트엔드

### 7.1 파일 구조
```
frontend/src/
├── types/region.ts           # 타입 정의
├── api/region.ts             # API 서비스
├── hooks/useRegions.ts       # 커스텀 훅
└── components/region/
    └── RegionSelect.tsx      # Cascading 드롭다운
```

### 7.2 주요 훅
```typescript
// 광역시/도 목록
const { metros, loading, error } = useMetros();

// 시/군/구 목록 (metroCode 변경 시 자동 fetch)
const { cities, metroName } = useCities(metroCode);

// 통합 선택 훅 (RegionSelect 컴포넌트용)
const { metros, cities, selected, selectMetro, selectCity, reset } = useRegionSelect();
```

### 7.3 컴포넌트 사용
```tsx
<RegionSelect
  onSelect={(metroCode, cityCode) => console.log(metroCode, cityCode)}
  initialMetroCode="6110000"
  requireCity={false}
/>
```

---

## 8. 테스트 가이드

### 8.1 백엔드 테스트
```java
@Test
void getMetros_ShouldReturn17Metros() {
    MetroListDto result = regionCodeService.getMetros();
    assertThat(result.getTotalCount()).isEqualTo(17);
}

@Test
void getCities_Seoul_ShouldReturn25Districts() {
    CityListDto result = regionCodeService.getCities("6110000");
    assertThat(result.getTotalCount()).isEqualTo(25);
    assertThat(result.getMetroName()).isEqualTo("서울특별시");
}
```

### 8.2 API 테스트
```bash
# 광역시/도 목록
curl http://localhost:8080/api/regions/metros

# 서울시 구 목록
curl http://localhost:8080/api/regions/6110000/cities

# 계층 구조
curl http://localhost:8080/api/regions/hierarchy
```

---

## 9. 트러블슈팅

### 9.1 캐시 무효화
지역 코드 데이터 변경 시 캐시 수동 삭제 필요:
```java
regionCodeService.evictCache();
```

### 9.2 초기 데이터 누락
V4 마이그레이션이 실행되었는지 확인:
```sql
SELECT COUNT(*) FROM region_codes;  -- 261이어야 함
```

### 9.3 성능 이슈
- 계층 구조 조회가 느린 경우 캐시 확인
- N+1 문제 방지를 위해 cityCount는 별도 count 쿼리 사용

---

## 10. 관련 도메인

- **FuneralHome**: 장례식장 위치 필터링에 지역 코드 사용
- **User**: 사용자 주소 정보와 연동 가능
- **Statistics**: 지역별 통계 집계에 활용
