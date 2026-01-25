/**
 * @fileoverview Google Map 컴포넌트
 */

import React, { useEffect, useRef, useState } from 'react';
import { useGoogleMaps } from '../../hooks/useGoogleMaps';
import { Coordinates, Marker, MapOptions } from '../../types/location';

interface GoogleMapProps {
  /** 지도 중심 좌표 */
  center: Coordinates;
  /** 확대 수준 */
  zoom?: number;
  /** 마커 목록 */
  markers?: Marker[];
  /** 마커 클릭 핸들러 */
  onMarkerClick?: (marker: Marker) => void;
  /** 지도 클릭 핸들러 */
  onMapClick?: (coords: Coordinates) => void;
  /** 지도 옵션 */
  options?: MapOptions;
  /** 커스텀 클래스 */
  className?: string;
  /** 지도 높이 */
  height?: string;
}

/**
 * Google Map 컴포넌트
 */
const GoogleMap: React.FC<GoogleMapProps> = ({
  center,
  zoom = 14,
  markers = [],
  onMarkerClick,
  onMapClick,
  options,
  className = '',
  height = '400px',
}) => {
  const mapRef = useRef<HTMLDivElement>(null);
  const { isLoaded, loadError } = useGoogleMaps();
  const [map, setMap] = useState<google.maps.Map | null>(null);
  const markersRef = useRef<google.maps.Marker[]>([]);
  const onMapClickRef = useRef(onMapClick);

  // onMapClick 핸들러를 ref로 유지
  useEffect(() => {
    onMapClickRef.current = onMapClick;
  }, [onMapClick]);

  // 지도 초기화 (isLoaded 변경 시에만 실행)
  useEffect(() => {
    if (!isLoaded || !mapRef.current) return;

    const mapInstance = new google.maps.Map(mapRef.current, {
      center: { lat: center.latitude, lng: center.longitude },
      zoom,
      ...options,
    });

    setMap(mapInstance);

    mapInstance.addListener('click', (e: google.maps.MapMouseEvent) => {
      if (e.latLng && onMapClickRef.current) {
        onMapClickRef.current({
          latitude: e.latLng.lat(),
          longitude: e.latLng.lng(),
        });
      }
    });

    return () => {
      google.maps.event.clearInstanceListeners(mapInstance);
    };
    // 지도 초기화는 isLoaded 변경 시에만 실행
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [isLoaded]);

  // 중심 좌표 업데이트
  useEffect(() => {
    if (map) {
      map.setCenter({ lat: center.latitude, lng: center.longitude });
    }
  }, [map, center.latitude, center.longitude]);

  // 마커 렌더링
  useEffect(() => {
    if (!map) return;

    // 기존 마커 제거
    markersRef.current.forEach((marker) => marker.setMap(null));
    markersRef.current = [];

    // 새 마커 생성
    markers.forEach((markerData) => {
      const marker = new google.maps.Marker({
        position: { lat: markerData.latitude, lng: markerData.longitude },
        map,
        title: markerData.title,
        icon: markerData.icon,
      });

      if (onMarkerClick) {
        marker.addListener('click', () => onMarkerClick(markerData));
      }

      markersRef.current.push(marker);
    });

    return () => {
      markersRef.current.forEach((marker) => {
        google.maps.event.clearInstanceListeners(marker);
        marker.setMap(null);
      });
    };
  }, [map, markers, onMarkerClick]);

  if (loadError) {
    return (
      <div className={`google-map google-map--error ${className}`}>
        <p>지도를 불러오는데 실패했습니다.</p>
        <p>{loadError.message}</p>
      </div>
    );
  }

  if (!isLoaded) {
    return (
      <div className={`google-map google-map--loading ${className}`} style={{ height }}>
        <p>지도를 불러오는 중...</p>
      </div>
    );
  }

  return (
    <div
      ref={mapRef}
      className={`google-map ${className}`}
      style={{ width: '100%', height }}
    />
  );
};

export default GoogleMap;
