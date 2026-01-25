/**
 * @fileoverview 위치 관련 타입 정의
 */

/** 좌표 */
export interface Coordinates {
  latitude: number;
  longitude: number;
}

/** 지오코딩 결과 */
export interface GeocodingResult {
  formattedAddress: string;
  latitude: number;
  longitude: number;
  placeId: string;
  components?: AddressComponents;
}

/** 주소 구성 요소 */
export interface AddressComponents {
  country?: string;
  province?: string;
  city?: string;
  district?: string;
  street?: string;
  streetNumber?: string;
  postalCode?: string;
}

/** 거리 계산 결과 */
export interface DistanceResult {
  distanceMeters: number;
  distanceText: string;
  durationSeconds?: number;
  durationText?: string;
}

/** 지도 마커 데이터 */
export interface MarkerData {
  id: string | number;
  latitude: number;
  longitude: number;
  title?: string;
  icon?: string;
  data?: any;
}

/** 마커 (지도용) */
export interface Marker {
  id: number | string;
  latitude: number;
  longitude: number;
  title?: string;
  info?: string;
  icon?: string;
}

/** 지도 옵션 */
export interface MapOptions {
  zoom?: number;
  minZoom?: number;
  maxZoom?: number;
  disableDefaultUI?: boolean;
  zoomControl?: boolean;
  mapTypeControl?: boolean;
  streetViewControl?: boolean;
  fullscreenControl?: boolean;
}

/** 장소 예측 결과 (자동완성) */
export interface PlacePrediction {
  placeId: string;
  description: string;
  mainText: string;
  secondaryText: string;
}
