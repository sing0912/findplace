# Location Service 영구지침

## 1. 개요

위치 서비스(Location Service)는 Google Maps API를 기반으로 지오코딩, 거리 계산, 지도 표시 등 위치 관련 기능을 제공합니다.

### 1.1 주요 기능
- 주소 → 좌표 변환 (Geocoding)
- 좌표 → 주소 변환 (Reverse Geocoding)
- 두 지점 간 직선 거리 계산 (Haversine)
- 지도 표시 및 마커 관리
- 주소 자동완성 검색
- 현재 위치 조회

### 1.2 비용 최적화
- Haversine 공식으로 직선 거리 무료 계산
- 캐싱으로 API 호출 최소화
- API 키 미설정 시 목업 데이터 반환 (개발용)

---

## 2. 아키텍처

### 2.1 백엔드 패키지 구조
```
domain/location/
├── config/
│   └── GoogleMapsConfig.java     # API 설정
├── dto/
│   ├── Coordinates.java          # 좌표 DTO
│   ├── GeocodingResult.java      # 지오코딩 결과
│   └── DistanceResult.java       # 거리 계산 결과
├── service/
│   ├── GeocodingService.java     # 지오코딩 서비스
│   ├── DistanceService.java      # 거리 계산 서비스
│   └── LocationService.java      # 통합 서비스
├── util/
│   ├── HaversineCalculator.java  # Haversine 거리 계산
│   └── CoordinateValidator.java  # 좌표 유효성 검증
└── exception/
    └── GeocodingException.java   # 지오코딩 예외
```

### 2.2 프론트엔드 구조
```
frontend/src/
├── types/location.ts             # 타입 정의
├── hooks/
│   ├── useGoogleMaps.ts         # Maps API 로드
│   └── useGeolocation.ts        # 현재 위치 조회
└── components/location/
    ├── GoogleMap.tsx            # 지도 컴포넌트
    ├── AddressSearch.tsx        # 주소 검색
    └── CurrentLocationButton.tsx # 현재 위치 버튼
```

---

## 3. 설정

### 3.1 백엔드 설정 (application.yml)
```yaml
location:
  google-maps:
    api-key: ${GOOGLE_MAPS_API_KEY:}
    language: ko
    region: KR
    timeout: 5000
    max-retries: 3
```

### 3.2 프론트엔드 설정 (.env)
```
REACT_APP_GOOGLE_MAPS_API_KEY=your_api_key_here
```

### 3.3 Google Cloud Console 설정

**필요한 API:**
| API | 용도 |
|-----|------|
| Maps JavaScript API | 지도 표시 |
| Geocoding API | 주소/좌표 변환 |
| Places API | 자동완성, 장소 검색 |

**API 키 제한:**
- HTTP 리퍼러: `https://yourdomain.com/*`, `http://localhost:3000/*`
- IP 제한: 서버 IP 주소

---

## 4. API 명세

### 4.1 Coordinates DTO
```java
@Getter
@Builder
public class Coordinates {
    private double latitude;
    private double longitude;

    public static Coordinates of(double lat, double lng);
}
```

### 4.2 GeocodingResult DTO
```java
@Getter
@Builder
public class GeocodingResult {
    private String formattedAddress;
    private double latitude;
    private double longitude;
    private String placeId;
    private AddressComponents components;
}
```

### 4.3 주요 서비스 메서드

**GeocodingService:**
```java
// 주소 → 좌표 (캐싱 적용)
@Cacheable(value = "geocoding", key = "#address")
GeocodingResult geocode(String address);

// 좌표 → 주소 (캐싱 적용)
@Cacheable(value = "geocoding", key = "#latitude + ',' + #longitude")
GeocodingResult reverseGeocode(double latitude, double longitude);

// 주소로 좌표만 조회
Coordinates getCoordinates(String address);
```

**DistanceService:**
```java
// 직선 거리 (Haversine, 무료)
double calculateHaversineDistance(Coordinates from, Coordinates to);

// 거리 결과 객체 반환
DistanceResult calculateDistance(Coordinates from, Coordinates to);

// 반경 내 여부 확인
boolean isWithinRadius(Coordinates from, Coordinates to, double radiusKm);
```

**LocationService (통합):**
```java
// 주소로 좌표 조회
Coordinates getCoordinates(String address);

// 좌표로 주소 조회
GeocodingResult getAddress(double latitude, double longitude);

// 두 지점 거리 (km)
double calculateDistance(Coordinates from, Coordinates to);
```

---

## 5. Haversine 공식

### 5.1 개요
두 좌표 간 직선 거리(대원 거리)를 계산하는 공식입니다. Google API 호출 없이 무료로 사용 가능합니다.

### 5.2 구현
```java
public static double calculate(double lat1, double lon1, double lat2, double lon2) {
    double dLat = Math.toRadians(lat2 - lat1);
    double dLon = Math.toRadians(lon2 - lon1);

    double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
             + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
             * Math.sin(dLon / 2) * Math.sin(dLon / 2);

    double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

    return EARTH_RADIUS_KM * c; // 6371.0 km
}
```

### 5.3 정확도
- 서울-부산: 약 325km (실제 도로 거리 ~400km)
- 단거리: 오차 1% 미만
- 장거리: 직선 거리로 실제 도로보다 짧음

---

## 6. 프론트엔드 컴포넌트

### 6.1 GoogleMap
```tsx
<GoogleMap
  center={{ latitude: 37.5666, longitude: 126.9784 }}
  zoom={14}
  markers={[
    { id: 1, latitude: 37.5666, longitude: 126.9784, title: "서울시청" }
  ]}
  onMarkerClick={(marker) => console.log(marker)}
  onMapClick={(coords) => console.log(coords)}
  height="400px"
/>
```

### 6.2 AddressSearch
```tsx
<AddressSearch
  onSelect={(result) => {
    console.log(result.formattedAddress);
    console.log(result.latitude, result.longitude);
  }}
  placeholder="주소 검색"
/>
```

### 6.3 CurrentLocationButton
```tsx
<CurrentLocationButton
  onLocation={(coords) => {
    console.log(coords.latitude, coords.longitude);
  }}
  onError={(error) => console.error(error)}
  label="현재 위치 찾기"
/>
```

### 6.4 Hooks
```tsx
// Google Maps API 로드 상태
const { isLoaded, loadError } = useGoogleMaps();

// 현재 위치 조회
const { getCurrentPosition, isLoading, error } = useGeolocation();
const position = await getCurrentPosition();
```

---

## 7. 캐싱 전략

### 7.1 백엔드 캐시
```java
@Cacheable(value = "geocoding", key = "#address")
public GeocodingResult geocode(String address) { ... }
```

### 7.2 캐시 TTL 권장값
| 캐시 | TTL | 이유 |
|------|-----|------|
| geocoding | 24시간 | 주소 변경 드묾 |
| distance | 1시간 | 교통 상황 변동 |

---

## 8. 테스트 가이드

### 8.1 단위 테스트
```java
@Test
void calculateSeoulToBusan() {
    double distance = HaversineCalculator.calculate(
        37.5666805, 126.9784147,  // 서울
        35.1795543, 129.0756416   // 부산
    );
    assertThat(distance).isBetween(320.0, 330.0);
}

@Test
void isWithinRadius_True() {
    Coordinates gangnam = Coordinates.of(37.4979, 127.0276);
    Coordinates seolleung = Coordinates.of(37.5044, 127.0490);
    boolean result = distanceService.isWithinRadius(gangnam, seolleung, 5.0);
    assertThat(result).isTrue();
}
```

### 8.2 API 키 없이 테스트
API 키가 설정되지 않은 경우 목업 데이터를 반환합니다:
- Geocoding: 서울 시청 좌표 (37.5666805, 126.9784147)
- 로그에 경고 메시지 출력

---

## 9. TypeScript 타입 설정 (프론트엔드)

### 9.1 필수 패키지 설치
Google Maps API를 TypeScript에서 사용하려면 타입 정의 패키지가 필요합니다.

```bash
npm install --save-dev @types/google.maps
```

### 9.2 tsconfig.json 설정
```json
{
  "compilerOptions": {
    "types": ["node", "google.maps"]
  }
}
```

### 9.3 Marker 타입 정의 (types/location.ts)
```typescript
/** 마커 (지도용) */
export interface Marker {
  id: number | string;
  latitude: number;
  longitude: number;
  title?: string;
  info?: string;
  icon?: string;
}
```

### 9.4 Google Maps 콜백 함수 타입
자동완성 및 장소 상세 조회 콜백에 명시적 타입 지정 필요:

```typescript
// getPlacePredictions 콜백
(
  predictions: google.maps.places.AutocompletePrediction[] | null,
  status: google.maps.places.PlacesServiceStatus
) => { ... }

// getDetails 콜백
(
  place: google.maps.places.PlaceResult | null,
  status: google.maps.places.PlacesServiceStatus
) => { ... }
```

---

## 10. 트러블슈팅

### 10.1 Google Maps API 에러
```
ErrorCode: GEOCODING_FAILED
원인: API 키 미설정, 할당량 초과, 네트워크 오류
해결:
1. GOOGLE_MAPS_API_KEY 환경변수 확인
2. Google Cloud Console에서 할당량 확인
3. API 활성화 상태 확인
```

### 10.2 CORS 에러 (프론트엔드)
```
해결: Google Maps JavaScript API는 클라이언트에서 직접 호출
백엔드 프록시 불필요
```

### 10.3 현재 위치 권한 거부
```javascript
error.code === error.PERMISSION_DENIED
해결: 사용자에게 위치 권한 요청 UI 표시
HTTPS 필수 (localhost 제외)
```

### 10.4 TypeScript 타입 에러
```
에러: Cannot find namespace 'google'
원인: @types/google.maps 패키지 미설치 또는 tsconfig.json 설정 누락
해결:
1. npm install --save-dev @types/google.maps
2. tsconfig.json에 "types": ["node", "google.maps"] 추가
```

```
에러: Module has no exported member 'apiClient'
원인: default export를 named import로 가져옴
해결: import apiClient from './client' (중괄호 제거)
```

---

## 11. 비용 최적화

### 11.1 무료 대안 사용
- 직선 거리: Haversine (항상 무료)
- 정확한 도로 거리가 필요한 경우에만 Google API 사용

### 11.2 캐싱 활용
- 같은 주소 반복 조회 방지
- Redis 캐시 활용

### 11.3 일일 할당량 설정
Google Cloud Console에서 API별 할당량 제한 설정 권장

---

## 12. 네비게이션 및 라우팅

### 12.1 라우트 설정 (App.tsx)
```tsx
// 보호된 라우트 내부
<Route path="nearby" element={<NearbyFuneralHomesPage />} />
```

### 12.2 사이드바 메뉴 (Sidebar.tsx)
```tsx
const menuItems: MenuItem[] = [
  { text: '홈', icon: <Home />, path: '/' },
  { text: '내 주변 장례식장', icon: <LocationOn />, path: '/nearby' },  // LocationOn 아이콘 사용
  { text: '장례업체', icon: <Business />, path: '/companies' },
  // ...
];
```

### 12.3 접근 경로
- **URL**: `/nearby`
- **메뉴 위치**: 사이드바 > "내 주변 장례식장"
- **필요 권한**: 로그인 필수 (ProtectedRoute)

---

## 13. 빌드 설정

### 13.1 Deprecation Warning 해결
react-scripts의 webpack 내부 경고를 숨기기 위해 `package.json` 스크립트 수정:

```json
{
  "scripts": {
    "start": "NODE_OPTIONS=--no-deprecation react-scripts start",
    "build": "NODE_OPTIONS=--no-deprecation react-scripts build",
    "test": "NODE_OPTIONS=--no-deprecation react-scripts test"
  }
}
```

---

## 14. 관련 도메인

- **FuneralHome**: 장례식장 위치 표시, 가까운 장례식장 검색
- **User**: 사용자 주소 좌표 저장
- **MyPage**: 내 주변 장례식장 찾기 기능
