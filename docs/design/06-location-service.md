# 위치 서비스 패키지 설계

## 1. 개요

위치 서비스(Location Service)는 Google Maps API를 기반으로 한 독립적인 패키지입니다. 다른 프로젝트에서도 재사용할 수 있도록 완전히 독립된 구조로 설계되었습니다.

---

## 2. 패키지 구조

### 2.1 전체 구조

```
packages/
├── location-service/              # 백엔드 Java 패키지
│   ├── src/main/java/
│   │   └── com/findplace/location/
│   │       ├── config/
│   │       ├── service/
│   │       ├── dto/
│   │       └── util/
│   ├── build.gradle
│   └── README.md
│
└── location-ui/                   # 프론트엔드 NPM 패키지
    ├── src/
    │   ├── components/
    │   ├── hooks/
    │   ├── services/
    │   └── types/
    ├── package.json
    └── README.md
```

### 2.2 백엔드 패키지 구조

```
com.findplace.location/
├── config/
│   ├── GoogleMapsConfig.java
│   └── LocationServiceAutoConfiguration.java
├── service/
│   ├── GeocodingService.java
│   ├── DistanceService.java
│   ├── PlaceSearchService.java
│   └── LocationService.java
├── dto/
│   ├── Coordinates.java
│   ├── GeocodingResult.java
│   ├── DistanceResult.java
│   ├── PlaceResult.java
│   └── AddressComponents.java
├── util/
│   ├── HaversineCalculator.java
│   └── CoordinateValidator.java
└── exception/
    ├── GeocodingException.java
    └── LocationServiceException.java
```

### 2.3 프론트엔드 패키지 구조

```
@findplace/location-ui/
├── src/
│   ├── components/
│   │   ├── GoogleMap/
│   │   │   ├── GoogleMap.tsx
│   │   │   ├── MapMarker.tsx
│   │   │   ├── MapInfoWindow.tsx
│   │   │   └── index.ts
│   │   ├── AddressSearch/
│   │   │   ├── AddressSearch.tsx
│   │   │   ├── AddressSuggestions.tsx
│   │   │   └── index.ts
│   │   ├── NearbyPlaces/
│   │   │   ├── NearbyPlaces.tsx
│   │   │   ├── PlaceCard.tsx
│   │   │   └── index.ts
│   │   └── CurrentLocation/
│   │       ├── CurrentLocationButton.tsx
│   │       └── index.ts
│   ├── hooks/
│   │   ├── useGoogleMaps.ts
│   │   ├── useGeolocation.ts
│   │   ├── useGeocode.ts
│   │   └── useDistance.ts
│   ├── services/
│   │   ├── geocodingService.ts
│   │   ├── distanceService.ts
│   │   └── placesService.ts
│   ├── types/
│   │   ├── coordinates.ts
│   │   ├── place.ts
│   │   └── mapOptions.ts
│   ├── utils/
│   │   ├── haversine.ts
│   │   └── formatters.ts
│   └── index.ts
├── package.json
├── tsconfig.json
└── README.md
```

---

## 3. 백엔드 설계

### 3.1 설정 클래스

```java
@Configuration
@ConfigurationProperties(prefix = "location.google-maps")
public class GoogleMapsConfig {
    private String apiKey;
    private String language = "ko";
    private String region = "KR";
    private int timeout = 5000;
    private int maxRetries = 3;

    // Getters, Setters
}
```

### 3.2 Geocoding 서비스

```java
@Service
public class GeocodingService {

    private final GeoApiContext context;

    public GeocodingService(GoogleMapsConfig config) {
        this.context = new GeoApiContext.Builder()
            .apiKey(config.getApiKey())
            .connectTimeout(config.getTimeout(), TimeUnit.MILLISECONDS)
            .build();
    }

    /**
     * 주소를 좌표로 변환 (Geocoding)
     */
    public GeocodingResult geocode(String address) {
        try {
            GeocodingApiRequest request = GeocodingApi.geocode(context, address)
                .language("ko")
                .region("KR");

            com.google.maps.model.GeocodingResult[] results = request.await();

            if (results.length > 0) {
                return mapToResult(results[0]);
            }
            return null;
        } catch (Exception e) {
            throw new GeocodingException("Geocoding failed: " + address, e);
        }
    }

    /**
     * 좌표를 주소로 변환 (Reverse Geocoding)
     */
    public GeocodingResult reverseGeocode(double latitude, double longitude) {
        try {
            LatLng latLng = new LatLng(latitude, longitude);
            GeocodingApiRequest request = GeocodingApi.reverseGeocode(context, latLng)
                .language("ko");

            com.google.maps.model.GeocodingResult[] results = request.await();

            if (results.length > 0) {
                return mapToResult(results[0]);
            }
            return null;
        } catch (Exception e) {
            throw new GeocodingException("Reverse geocoding failed", e);
        }
    }

    private GeocodingResult mapToResult(com.google.maps.model.GeocodingResult result) {
        return GeocodingResult.builder()
            .formattedAddress(result.formattedAddress)
            .latitude(result.geometry.location.lat)
            .longitude(result.geometry.location.lng)
            .placeId(result.placeId)
            .components(parseComponents(result.addressComponents))
            .build();
    }
}
```

### 3.3 거리 계산 서비스

```java
@Service
public class DistanceService {

    private final GeoApiContext context;

    /**
     * 두 지점 간 거리 계산 (Haversine - 빠름, 무료)
     */
    public double calculateHaversineDistance(Coordinates from, Coordinates to) {
        return HaversineCalculator.calculate(
            from.getLatitude(), from.getLongitude(),
            to.getLatitude(), to.getLongitude()
        );
    }

    /**
     * 두 지점 간 실제 도로 거리 및 시간 (Google Distance Matrix API)
     */
    public DistanceResult calculateDrivingDistance(Coordinates from, Coordinates to) {
        try {
            DistanceMatrix matrix = DistanceMatrixApi.newRequest(context)
                .origins(new LatLng(from.getLatitude(), from.getLongitude()))
                .destinations(new LatLng(to.getLatitude(), to.getLongitude()))
                .mode(TravelMode.DRIVING)
                .language("ko")
                .await();

            DistanceMatrixRow row = matrix.rows[0];
            DistanceMatrixElement element = row.elements[0];

            return DistanceResult.builder()
                .distanceMeters(element.distance.inMeters)
                .distanceText(element.distance.humanReadable)
                .durationSeconds(element.duration.inSeconds)
                .durationText(element.duration.humanReadable)
                .build();
        } catch (Exception e) {
            throw new LocationServiceException("Distance calculation failed", e);
        }
    }

    /**
     * 여러 목적지까지의 거리 일괄 계산
     */
    public List<DistanceResult> calculateDistances(Coordinates from, List<Coordinates> destinations) {
        // Google API 호출 최소화를 위해 배치 처리
        // 최대 25개씩 분할하여 호출
    }
}
```

### 3.4 Haversine 계산 유틸리티

```java
public class HaversineCalculator {

    private static final double EARTH_RADIUS_KM = 6371.0;

    /**
     * 두 좌표 간 직선 거리 계산 (km)
     */
    public static double calculate(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                 + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                 * Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS_KM * c;
    }
}
```

### 3.5 장소 검색 서비스

```java
@Service
public class PlaceSearchService {

    private final GeoApiContext context;

    /**
     * 근처 장소 검색
     */
    public List<PlaceResult> searchNearby(Coordinates center, int radiusMeters, String type) {
        try {
            PlacesSearchResponse response = PlacesApi.nearbySearchQuery(context,
                    new LatLng(center.getLatitude(), center.getLongitude()))
                .radius(radiusMeters)
                .type(PlaceType.valueOf(type.toUpperCase()))
                .language("ko")
                .await();

            return Arrays.stream(response.results)
                .map(this::mapToResult)
                .collect(Collectors.toList());
        } catch (Exception e) {
            throw new LocationServiceException("Place search failed", e);
        }
    }

    /**
     * 장소 자동완성
     */
    public List<PlaceResult> autocomplete(String input) {
        try {
            AutocompletePrediction[] predictions = PlacesApi.placeAutocomplete(context, input, null)
                .language("ko")
                .components(ComponentFilter.country("KR"))
                .await();

            return Arrays.stream(predictions)
                .map(this::mapPredictionToResult)
                .collect(Collectors.toList());
        } catch (Exception e) {
            throw new LocationServiceException("Autocomplete failed", e);
        }
    }
}
```

### 3.6 통합 위치 서비스

```java
@Service
public class LocationService {

    private final GeocodingService geocodingService;
    private final DistanceService distanceService;
    private final PlaceSearchService placeSearchService;

    /**
     * 주소로 좌표 조회
     */
    public Coordinates getCoordinates(String address) {
        GeocodingResult result = geocodingService.geocode(address);
        return new Coordinates(result.getLatitude(), result.getLongitude());
    }

    /**
     * 근처 장소 검색 (거리순 정렬)
     */
    public List<PlaceWithDistance> findNearbyPlaces(Coordinates center, int radiusKm, String type) {
        List<PlaceResult> places = placeSearchService.searchNearby(center, radiusKm * 1000, type);

        return places.stream()
            .map(place -> {
                double distance = distanceService.calculateHaversineDistance(
                    center,
                    new Coordinates(place.getLatitude(), place.getLongitude())
                );
                return new PlaceWithDistance(place, distance);
            })
            .sorted(Comparator.comparing(PlaceWithDistance::getDistance))
            .collect(Collectors.toList());
    }
}
```

---

## 4. 프론트엔드 설계

### 4.1 Google Map 컴포넌트

```typescript
// components/GoogleMap/GoogleMap.tsx
import { useEffect, useRef, useState } from 'react';
import { useGoogleMaps } from '../../hooks/useGoogleMaps';
import { MapOptions, Coordinates } from '../../types';

interface GoogleMapProps {
  center: Coordinates;
  zoom?: number;
  markers?: MarkerData[];
  onMarkerClick?: (marker: MarkerData) => void;
  onMapClick?: (coords: Coordinates) => void;
  options?: MapOptions;
  className?: string;
}

export const GoogleMap: React.FC<GoogleMapProps> = ({
  center,
  zoom = 14,
  markers = [],
  onMarkerClick,
  onMapClick,
  options,
  className
}) => {
  const mapRef = useRef<HTMLDivElement>(null);
  const { isLoaded, loadError } = useGoogleMaps();
  const [map, setMap] = useState<google.maps.Map | null>(null);

  useEffect(() => {
    if (!isLoaded || !mapRef.current) return;

    const mapInstance = new google.maps.Map(mapRef.current, {
      center: { lat: center.latitude, lng: center.longitude },
      zoom,
      ...options
    });

    setMap(mapInstance);

    if (onMapClick) {
      mapInstance.addListener('click', (e: google.maps.MapMouseEvent) => {
        if (e.latLng) {
          onMapClick({
            latitude: e.latLng.lat(),
            longitude: e.latLng.lng()
          });
        }
      });
    }

    return () => {
      google.maps.event.clearInstanceListeners(mapInstance);
    };
  }, [isLoaded, center, zoom]);

  // 마커 렌더링
  useEffect(() => {
    if (!map) return;

    const googleMarkers = markers.map(marker => {
      const gMarker = new google.maps.Marker({
        position: { lat: marker.latitude, lng: marker.longitude },
        map,
        title: marker.title,
        icon: marker.icon
      });

      if (onMarkerClick) {
        gMarker.addListener('click', () => onMarkerClick(marker));
      }

      return gMarker;
    });

    return () => {
      googleMarkers.forEach(m => m.setMap(null));
    };
  }, [map, markers]);

  if (loadError) {
    return <div>지도를 불러오는데 실패했습니다.</div>;
  }

  if (!isLoaded) {
    return <div>지도를 불러오는 중...</div>;
  }

  return <div ref={mapRef} className={className} style={{ width: '100%', height: '400px' }} />;
};
```

### 4.2 주소 검색 컴포넌트

```typescript
// components/AddressSearch/AddressSearch.tsx
import { useState, useCallback } from 'react';
import { useDebounce } from '../../hooks/useDebounce';
import { placesService } from '../../services/placesService';
import { AddressSuggestions } from './AddressSuggestions';

interface AddressSearchProps {
  onSelect: (result: GeocodingResult) => void;
  placeholder?: string;
  className?: string;
}

export const AddressSearch: React.FC<AddressSearchProps> = ({
  onSelect,
  placeholder = '주소를 입력하세요',
  className
}) => {
  const [query, setQuery] = useState('');
  const [suggestions, setSuggestions] = useState<PlacePrediction[]>([]);
  const [isLoading, setIsLoading] = useState(false);
  const debouncedQuery = useDebounce(query, 300);

  useEffect(() => {
    if (debouncedQuery.length < 2) {
      setSuggestions([]);
      return;
    }

    const fetchSuggestions = async () => {
      setIsLoading(true);
      try {
        const results = await placesService.autocomplete(debouncedQuery);
        setSuggestions(results);
      } catch (error) {
        console.error('Autocomplete failed:', error);
      } finally {
        setIsLoading(false);
      }
    };

    fetchSuggestions();
  }, [debouncedQuery]);

  const handleSelect = useCallback(async (prediction: PlacePrediction) => {
    const details = await placesService.getPlaceDetails(prediction.placeId);
    onSelect({
      formattedAddress: details.formattedAddress,
      latitude: details.latitude,
      longitude: details.longitude,
      placeId: prediction.placeId
    });
    setQuery(details.formattedAddress);
    setSuggestions([]);
  }, [onSelect]);

  return (
    <div className={className}>
      <input
        type="text"
        value={query}
        onChange={(e) => setQuery(e.target.value)}
        placeholder={placeholder}
      />
      {isLoading && <span>검색 중...</span>}
      {suggestions.length > 0 && (
        <AddressSuggestions
          suggestions={suggestions}
          onSelect={handleSelect}
        />
      )}
    </div>
  );
};
```

### 4.3 현재 위치 버튼 컴포넌트

```typescript
// components/CurrentLocation/CurrentLocationButton.tsx
import { useGeolocation } from '../../hooks/useGeolocation';

interface CurrentLocationButtonProps {
  onLocation: (coords: Coordinates) => void;
  className?: string;
}

export const CurrentLocationButton: React.FC<CurrentLocationButtonProps> = ({
  onLocation,
  className
}) => {
  const { getCurrentPosition, isLoading, error } = useGeolocation();

  const handleClick = async () => {
    try {
      const position = await getCurrentPosition();
      onLocation({
        latitude: position.coords.latitude,
        longitude: position.coords.longitude
      });
    } catch (err) {
      console.error('Failed to get location:', err);
    }
  };

  return (
    <button
      onClick={handleClick}
      disabled={isLoading}
      className={className}
    >
      {isLoading ? '위치 확인 중...' : '현재 위치'}
    </button>
  );
};
```

### 4.4 Hooks

```typescript
// hooks/useGoogleMaps.ts
import { useState, useEffect } from 'react';

const GOOGLE_MAPS_SCRIPT_ID = 'google-maps-script';

interface UseGoogleMapsReturn {
  isLoaded: boolean;
  loadError: Error | null;
}

export const useGoogleMaps = (apiKey?: string): UseGoogleMapsReturn => {
  const [isLoaded, setIsLoaded] = useState(false);
  const [loadError, setLoadError] = useState<Error | null>(null);

  useEffect(() => {
    // 이미 로드된 경우
    if (window.google?.maps) {
      setIsLoaded(true);
      return;
    }

    // 이미 스크립트가 추가된 경우
    const existingScript = document.getElementById(GOOGLE_MAPS_SCRIPT_ID);
    if (existingScript) {
      existingScript.addEventListener('load', () => setIsLoaded(true));
      return;
    }

    // 새로 스크립트 추가
    const key = apiKey || process.env.REACT_APP_GOOGLE_MAPS_API_KEY;
    const script = document.createElement('script');
    script.id = GOOGLE_MAPS_SCRIPT_ID;
    script.src = `https://maps.googleapis.com/maps/api/js?key=${key}&libraries=places&language=ko`;
    script.async = true;
    script.defer = true;

    script.onload = () => setIsLoaded(true);
    script.onerror = () => setLoadError(new Error('Google Maps 로드 실패'));

    document.head.appendChild(script);
  }, [apiKey]);

  return { isLoaded, loadError };
};
```

```typescript
// hooks/useGeolocation.ts
import { useState, useCallback } from 'react';

interface UseGeolocationReturn {
  getCurrentPosition: () => Promise<GeolocationPosition>;
  isLoading: boolean;
  error: GeolocationPositionError | null;
}

export const useGeolocation = (): UseGeolocationReturn => {
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<GeolocationPositionError | null>(null);

  const getCurrentPosition = useCallback((): Promise<GeolocationPosition> => {
    return new Promise((resolve, reject) => {
      if (!navigator.geolocation) {
        reject(new Error('Geolocation is not supported'));
        return;
      }

      setIsLoading(true);
      setError(null);

      navigator.geolocation.getCurrentPosition(
        (position) => {
          setIsLoading(false);
          resolve(position);
        },
        (err) => {
          setIsLoading(false);
          setError(err);
          reject(err);
        },
        {
          enableHighAccuracy: true,
          timeout: 10000,
          maximumAge: 60000
        }
      );
    });
  }, []);

  return { getCurrentPosition, isLoading, error };
};
```

---

## 5. API 설계

### 5.1 Location API Endpoints

| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | /api/location/geocode | 주소 → 좌표 변환 |
| GET | /api/location/reverse-geocode | 좌표 → 주소 변환 |
| GET | /api/location/distance | 두 지점 간 거리 계산 |
| GET | /api/location/places/nearby | 근처 장소 검색 |
| GET | /api/location/places/autocomplete | 장소 자동완성 |

### 5.2 요청/응답 예시

**Geocoding**
```
GET /api/location/geocode?address=서울특별시 강남구 테헤란로 123

Response:
{
  "formattedAddress": "서울특별시 강남구 테헤란로 123",
  "latitude": 37.5065,
  "longitude": 127.0536,
  "placeId": "ChIJ...",
  "components": {
    "country": "대한민국",
    "province": "서울특별시",
    "city": "강남구",
    "street": "테헤란로",
    "streetNumber": "123"
  }
}
```

**거리 계산**
```
GET /api/location/distance?fromLat=37.5065&fromLng=127.0536&toLat=37.4979&toLng=127.0276

Response:
{
  "straightDistance": {
    "kilometers": 3.2,
    "text": "3.2km"
  },
  "drivingDistance": {
    "meters": 4500,
    "text": "4.5km",
    "durationSeconds": 900,
    "durationText": "15분"
  }
}
```

---

## 6. 설정 및 사용법

### 6.1 백엔드 설정

```yaml
# application.yml
location:
  google-maps:
    api-key: ${GOOGLE_MAPS_API_KEY}
    language: ko
    region: KR
    timeout: 5000
    max-retries: 3
```

### 6.2 백엔드 의존성 추가

```gradle
// build.gradle
dependencies {
    implementation 'com.google.maps:google-maps-services:2.2.0'
}
```

### 6.3 프론트엔드 설치

```bash
npm install @findplace/location-ui
```

### 6.4 프론트엔드 사용 예시

```typescript
import {
  GoogleMap,
  AddressSearch,
  CurrentLocationButton,
  useGeocode
} from '@findplace/location-ui';

function MyComponent() {
  const [center, setCenter] = useState({ latitude: 37.5065, longitude: 127.0536 });
  const [markers, setMarkers] = useState([]);

  return (
    <div>
      <AddressSearch
        onSelect={(result) => {
          setCenter({ latitude: result.latitude, longitude: result.longitude });
        }}
      />

      <CurrentLocationButton
        onLocation={setCenter}
      />

      <GoogleMap
        center={center}
        markers={markers}
        onMarkerClick={(marker) => console.log(marker)}
      />
    </div>
  );
}
```

---

## 7. Google Maps API 설정

### 7.1 필요한 API

| API | 용도 | 과금 |
|-----|------|------|
| Maps JavaScript API | 지도 표시 | $7/1000 로드 |
| Geocoding API | 주소 ↔ 좌표 변환 | $5/1000 요청 |
| Places API | 장소 검색, 자동완성 | $17-32/1000 요청 |
| Distance Matrix API | 거리/시간 계산 | $5-10/1000 요소 |

### 7.2 API 키 제한 설정

```
HTTP 리퍼러 제한 (프론트엔드):
- https://findplace.com/*
- http://localhost:3000/*

IP 제한 (백엔드):
- 서버 IP 주소
```

### 7.3 일일 할당량 설정

| API | 권장 일일 한도 |
|-----|---------------|
| Geocoding | 10,000 요청 |
| Places | 5,000 요청 |
| Distance Matrix | 5,000 요소 |

---

## 8. 비용 최적화

### 8.1 Haversine 우선 사용

```java
// 대략적인 거리만 필요한 경우 Haversine (무료)
double approxDistance = distanceService.calculateHaversineDistance(from, to);

// 정확한 도로 거리 필요 시만 API 호출 (유료)
if (needExactDistance) {
    DistanceResult exact = distanceService.calculateDrivingDistance(from, to);
}
```

### 8.2 캐싱 전략

```java
@Cacheable(value = "geocoding", key = "#address")
public GeocodingResult geocode(String address) {
    // API 호출
}

@Cacheable(value = "distance", key = "#from.toString() + '-' + #to.toString()")
public DistanceResult calculateDrivingDistance(Coordinates from, Coordinates to) {
    // API 호출
}
```

### 8.3 배치 처리

```java
// 여러 목적지를 한 번의 API 호출로 처리
public List<DistanceResult> batchCalculate(Coordinates from, List<Coordinates> destinations) {
    // Distance Matrix API는 최대 25개 목적지 지원
}
```

---

## 9. 독립 패키지 배포

### 9.1 백엔드 (Maven Central)

```gradle
// build.gradle
plugins {
    id 'maven-publish'
}

publishing {
    publications {
        mavenJava(MavenPublication) {
            groupId = 'com.findplace'
            artifactId = 'location-service'
            version = '1.0.0'
            from components.java
        }
    }
}
```

### 9.2 프론트엔드 (NPM)

```json
// package.json
{
  "name": "@findplace/location-ui",
  "version": "1.0.0",
  "main": "dist/index.js",
  "types": "dist/index.d.ts",
  "peerDependencies": {
    "react": ">=18.0.0",
    "react-dom": ">=18.0.0"
  }
}
```

