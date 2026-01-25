# 지역 코드 도메인 설계

## 1. 개요

지역 코드(Region) 도메인은 행정안전부의 지방자치단체 코드를 관리합니다. 장례식장 데이터 필터링 및 지역별 통계에 활용됩니다.

---

## 2. 엔티티 설계

### 2.1 RegionCode 엔티티

```
┌─────────────────────────────────────────────────────────────┐
│                       RegionCode                            │
├─────────────────────────────────────────────────────────────┤
│  id                  BIGINT PK AUTO_INCREMENT               │
│  code                VARCHAR(20) NOT NULL UNIQUE            │
│  name                VARCHAR(100) NOT NULL                  │
│  type                VARCHAR(20) NOT NULL                   │
│  parentCode          VARCHAR(20)                            │
│  sortOrder           INTEGER DEFAULT 0                      │
│  isActive            BOOLEAN DEFAULT TRUE                   │
│  createdAt           TIMESTAMP NOT NULL                     │
│  updatedAt           TIMESTAMP NOT NULL                     │
└─────────────────────────────────────────────────────────────┘
```

### 2.2 RegionType 열거형

| 값 | 설명 | 예시 |
|----|------|------|
| METRO | 광역시/도 | 서울특별시, 경기도 |
| CITY | 시/군/구 | 강남구, 수원시 |

---

## 3. 지역 코드 데이터

### 3.1 광역시/도 (17개)

| 코드 | 지역명 |
|------|--------|
| 6110000 | 서울특별시 |
| 6260000 | 부산광역시 |
| 6270000 | 대구광역시 |
| 6280000 | 인천광역시 |
| 6290000 | 광주광역시 |
| 6300000 | 대전광역시 |
| 6310000 | 울산광역시 |
| 6360000 | 세종특별자치시 |
| 6410000 | 경기도 |
| 6420000 | 강원특별자치도 |
| 6430000 | 충청북도 |
| 6440000 | 충청남도 |
| 6450000 | 전북특별자치도 |
| 6460000 | 전라남도 |
| 6470000 | 경상북도 |
| 6480000 | 경상남도 |
| 6500000 | 제주특별자치도 |

### 3.2 시/군/구 (244개)

전체 261개 지역 코드 중 광역시/도 17개를 제외한 244개 시/군/구 데이터.

<details>
<summary>전체 시/군/구 코드 보기</summary>

| 코드 | 지역명 | 상위 코드 |
|------|--------|----------|
| 3000000 | 종로구 | 6110000 |
| 3010000 | 중구 | 6110000 |
| 3020000 | 용산구 | 6110000 |
| 3030000 | 성동구 | 6110000 |
| 3040000 | 광진구 | 6110000 |
| 3050000 | 동대문구 | 6110000 |
| 3060000 | 중랑구 | 6110000 |
| 3070000 | 성북구 | 6110000 |
| 3080000 | 강북구 | 6110000 |
| 3090000 | 도봉구 | 6110000 |
| 3100000 | 노원구 | 6110000 |
| 3110000 | 은평구 | 6110000 |
| 3120000 | 서대문구 | 6110000 |
| 3130000 | 마포구 | 6110000 |
| 3140000 | 양천구 | 6110000 |
| 3150000 | 강서구 | 6110000 |
| 3160000 | 구로구 | 6110000 |
| 3170000 | 금천구 | 6110000 |
| 3180000 | 영등포구 | 6110000 |
| 3190000 | 동작구 | 6110000 |
| 3200000 | 관악구 | 6110000 |
| 3210000 | 서초구 | 6110000 |
| 3220000 | 강남구 | 6110000 |
| 3230000 | 송파구 | 6110000 |
| 3240000 | 강동구 | 6110000 |
| ... | ... | ... |

</details>

---

## 4. API 설계

### 4.1 공개 API

| Method | Endpoint | 설명 | 권한 |
|--------|----------|------|------|
| GET | /regions | 전체 지역 목록 | 공개 |
| GET | /regions/metros | 광역시/도 목록 | 공개 |
| GET | /regions/{metroCode}/cities | 시/군/구 목록 | 공개 |
| GET | /regions/{code} | 지역 상세 조회 | 공개 |

### 4.2 관리자 API

| Method | Endpoint | 설명 | 권한 |
|--------|----------|------|------|
| GET | /admin/regions | 전체 지역 관리 | ADMIN |
| POST | /admin/regions | 지역 추가 | ADMIN |
| PUT | /admin/regions/{code} | 지역 수정 | ADMIN |
| PATCH | /admin/regions/{code}/status | 활성화 상태 변경 | ADMIN |
| POST | /admin/regions/bulk | 대량 등록/수정 | ADMIN |

---

## 5. 요청/응답 DTO

### 5.1 광역시/도 목록 응답

```json
{
  "metros": [
    {
      "code": "6110000",
      "name": "서울특별시",
      "cityCount": 25
    },
    {
      "code": "6260000",
      "name": "부산광역시",
      "cityCount": 16
    },
    {
      "code": "6410000",
      "name": "경기도",
      "cityCount": 31
    }
  ],
  "totalCount": 17
}
```

### 5.2 시/군/구 목록 응답

```json
{
  "metroCode": "6110000",
  "metroName": "서울특별시",
  "cities": [
    {
      "code": "3000000",
      "name": "종로구"
    },
    {
      "code": "3010000",
      "name": "중구"
    },
    {
      "code": "3220000",
      "name": "강남구"
    }
  ],
  "totalCount": 25
}
```

### 5.3 계층 구조 응답

```json
{
  "regions": [
    {
      "code": "6110000",
      "name": "서울특별시",
      "type": "METRO",
      "children": [
        {
          "code": "3000000",
          "name": "종로구",
          "type": "CITY"
        },
        {
          "code": "3010000",
          "name": "중구",
          "type": "CITY"
        }
      ]
    }
  ]
}
```

---

## 6. 비즈니스 로직

### 6.1 지역 코드 매핑

```java
@Service
public class RegionService {

    public String getMetroCode(String cityCode) {
        return regionRepository.findByCode(cityCode)
            .map(RegionCode::getParentCode)
            .orElse(null);
    }

    public List<String> getAllCityCodes(String metroCode) {
        return regionRepository.findByParentCode(metroCode)
            .stream()
            .map(RegionCode::getCode)
            .collect(Collectors.toList());
    }
}
```

### 6.2 지역별 장례식장 통계

```java
public Map<String, Long> getFuneralHomeCountByMetro() {
    return funeralHomeRepository.findAll().stream()
        .collect(Collectors.groupingBy(
            fh -> regionService.getMetroCode(fh.getLocCode()),
            Collectors.counting()
        ));
}
```

---

## 7. 인덱스

```sql
CREATE UNIQUE INDEX idx_region_codes_code ON region_codes(code);
CREATE INDEX idx_region_codes_parent ON region_codes(parent_code);
CREATE INDEX idx_region_codes_type ON region_codes(type);
CREATE INDEX idx_region_codes_active ON region_codes(is_active);
```

---

## 8. 초기 데이터 로드

### 8.1 Flyway 마이그레이션

```sql
-- V1.3__Insert_region_codes.sql

INSERT INTO region_codes (code, name, type, parent_code, sort_order, is_active) VALUES
-- 광역시/도
('6110000', '서울특별시', 'METRO', NULL, 1, true),
('6260000', '부산광역시', 'METRO', NULL, 2, true),
('6270000', '대구광역시', 'METRO', NULL, 3, true),
('6280000', '인천광역시', 'METRO', NULL, 4, true),
('6290000', '광주광역시', 'METRO', NULL, 5, true),
('6300000', '대전광역시', 'METRO', NULL, 6, true),
('6310000', '울산광역시', 'METRO', NULL, 7, true),
('6360000', '세종특별자치시', 'METRO', NULL, 8, true),
('6410000', '경기도', 'METRO', NULL, 9, true),
('6420000', '강원특별자치도', 'METRO', NULL, 10, true),
('6430000', '충청북도', 'METRO', NULL, 11, true),
('6440000', '충청남도', 'METRO', NULL, 12, true),
('6450000', '전북특별자치도', 'METRO', NULL, 13, true),
('6460000', '전라남도', 'METRO', NULL, 14, true),
('6470000', '경상북도', 'METRO', NULL, 15, true),
('6480000', '경상남도', 'METRO', NULL, 16, true),
('6500000', '제주특별자치도', 'METRO', NULL, 17, true),

-- 서울특별시 구
('3000000', '종로구', 'CITY', '6110000', 1, true),
('3010000', '중구', 'CITY', '6110000', 2, true),
('3020000', '용산구', 'CITY', '6110000', 3, true),
('3030000', '성동구', 'CITY', '6110000', 4, true),
('3040000', '광진구', 'CITY', '6110000', 5, true),
('3050000', '동대문구', 'CITY', '6110000', 6, true),
('3060000', '중랑구', 'CITY', '6110000', 7, true),
('3070000', '성북구', 'CITY', '6110000', 8, true),
('3080000', '강북구', 'CITY', '6110000', 9, true),
('3090000', '도봉구', 'CITY', '6110000', 10, true),
('3100000', '노원구', 'CITY', '6110000', 11, true),
('3110000', '은평구', 'CITY', '6110000', 12, true),
('3120000', '서대문구', 'CITY', '6110000', 13, true),
('3130000', '마포구', 'CITY', '6110000', 14, true),
('3140000', '양천구', 'CITY', '6110000', 15, true),
('3150000', '강서구', 'CITY', '6110000', 16, true),
('3160000', '구로구', 'CITY', '6110000', 17, true),
('3170000', '금천구', 'CITY', '6110000', 18, true),
('3180000', '영등포구', 'CITY', '6110000', 19, true),
('3190000', '동작구', 'CITY', '6110000', 20, true),
('3200000', '관악구', 'CITY', '6110000', 21, true),
('3210000', '서초구', 'CITY', '6110000', 22, true),
('3220000', '강남구', 'CITY', '6110000', 23, true),
('3230000', '송파구', 'CITY', '6110000', 24, true),
('3240000', '강동구', 'CITY', '6110000', 25, true);

-- ... 나머지 지역 코드 (전체 261개)
```

### 8.2 대량 등록 API

```java
@PostMapping("/admin/regions/bulk")
public ResponseEntity<BulkResult> bulkUpsert(@RequestBody List<RegionCodeDto> regions) {
    int inserted = 0;
    int updated = 0;

    for (RegionCodeDto dto : regions) {
        Optional<RegionCode> existing = regionRepository.findByCode(dto.getCode());
        if (existing.isPresent()) {
            existing.get().setName(dto.getName());
            regionRepository.save(existing.get());
            updated++;
        } else {
            RegionCode newRegion = new RegionCode();
            newRegion.setCode(dto.getCode());
            newRegion.setName(dto.getName());
            newRegion.setType(dto.getType());
            newRegion.setParentCode(dto.getParentCode());
            regionRepository.save(newRegion);
            inserted++;
        }
    }

    return ResponseEntity.ok(new BulkResult(inserted, updated));
}
```

---

## 9. 캐싱 전략

### 9.1 Redis 캐시

```java
@Cacheable(value = "regions", key = "'metros'")
public List<RegionCodeDto> getMetros() {
    return regionRepository.findByType(RegionType.METRO);
}

@Cacheable(value = "regions", key = "'cities:' + #metroCode")
public List<RegionCodeDto> getCities(String metroCode) {
    return regionRepository.findByParentCode(metroCode);
}

@CacheEvict(value = "regions", allEntries = true)
public void evictCache() {
    // 지역 코드 변경 시 캐시 삭제
}
```

### 9.2 캐시 TTL

| 캐시 | TTL | 이유 |
|------|-----|------|
| 광역시/도 목록 | 24시간 | 변경 거의 없음 |
| 시/군/구 목록 | 24시간 | 변경 거의 없음 |
| 계층 구조 | 24시간 | 변경 거의 없음 |

---

## 10. 프론트엔드 UI

### 10.1 지역 선택 드롭다운

```
┌─────────────────────────────────────────┐
│  지역 선택                               │
├─────────────────────────────────────────┤
│                                         │
│  광역시/도    [서울특별시        ▼]      │
│                                         │
│  시/군/구    [강남구            ▼]      │
│                                         │
│              [검색]  [초기화]           │
│                                         │
└─────────────────────────────────────────┘
```

### 10.2 지역 필터 칩

```
┌─────────────────────────────────────────────────────────────┐
│  [서울 ×] [경기 ×] [부산 ×]                    + 지역 추가   │
└─────────────────────────────────────────────────────────────┘
```

---

## 11. 관리자 지역 관리 화면

```
┌─────────────────────────────────────────────────────────────┐
│  지역 코드 관리                              [+ 추가] [업로드] │
├─────────────────────────────────────────────────────────────┤
│  검색: [               ]  유형: [전체 ▼]  상태: [전체 ▼]    │
├─────────────────────────────────────────────────────────────┤
│  코드      │ 지역명        │ 유형    │ 상위지역    │ 상태   │
│───────────────────────────────────────────────────────────── │
│  6110000  │ 서울특별시    │ METRO  │ -          │ 활성   │
│  3000000  │ 종로구        │ CITY   │ 서울특별시  │ 활성   │
│  3010000  │ 중구          │ CITY   │ 서울특별시  │ 활성   │
│  3020000  │ 용산구        │ CITY   │ 서울특별시  │ 활성   │
│───────────────────────────────────────────────────────────── │
│                    < 1 2 3 4 5 ... 27 >                      │
└─────────────────────────────────────────────────────────────┘
```

---

## 12. 연관 관계

```
RegionCode (1) ─────────── (N) RegionCode  : 광역시/도 - 시/군/구 계층
RegionCode (1) ─────────── (N) FuneralHome : 지역별 장례식장
```

