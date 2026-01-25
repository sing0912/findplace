/**
 * @fileoverview 장례식장 지도 컴포넌트
 */

import React, { useCallback } from 'react';
import GoogleMap from '../location/GoogleMap';
import type { FuneralHomeListItem } from '../../types/funeralHome';
import type { Marker } from '../../types/location';

interface FuneralHomeMapProps {
  /** 장례식장 목록 */
  funeralHomes: FuneralHomeListItem[];
  /** 중심 좌표 */
  center?: { latitude: number; longitude: number };
  /** 줌 레벨 */
  zoom?: number;
  /** 마커 클릭 핸들러 */
  onMarkerClick?: (funeralHome: FuneralHomeListItem) => void;
  /** 지도 클릭 핸들러 */
  onMapClick?: (coords: { latitude: number; longitude: number }) => void;
  /** 높이 */
  height?: string;
  /** 사용자 위치 표시 여부 */
  showUserLocation?: boolean;
  /** 사용자 위치 */
  userLocation?: { latitude: number; longitude: number };
}

/**
 * 장례식장 지도 컴포넌트
 */
const FuneralHomeMap: React.FC<FuneralHomeMapProps> = ({
  funeralHomes,
  center,
  zoom = 13,
  onMarkerClick,
  onMapClick,
  height = '400px',
  showUserLocation = false,
  userLocation,
}) => {
  // 장례식장 목록을 마커로 변환
  const markers: Marker[] = funeralHomes
    .filter((fh) => fh.latitude && fh.longitude)
    .map((fh) => ({
      id: fh.id,
      latitude: fh.latitude!,
      longitude: fh.longitude!,
      title: fh.name,
      info: `${fh.roadAddress}${fh.distance ? ` (${fh.distance.toFixed(1)}km)` : ''}`,
    }));

  // 사용자 위치 마커 추가
  if (showUserLocation && userLocation) {
    markers.unshift({
      id: -1, // 특별한 ID
      latitude: userLocation.latitude,
      longitude: userLocation.longitude,
      title: '내 위치',
      info: '현재 위치',
    });
  }

  const handleMarkerClick = useCallback(
    (marker: Marker) => {
      if (marker.id === -1) {
        // 사용자 위치 마커 클릭은 무시
        return;
      }
      const funeralHome = funeralHomes.find((fh) => fh.id === marker.id);
      if (funeralHome && onMarkerClick) {
        onMarkerClick(funeralHome);
      }
    },
    [funeralHomes, onMarkerClick]
  );

  // 기본 중심점 계산
  const defaultCenter = center || {
    latitude: userLocation?.latitude || 37.5665,
    longitude: userLocation?.longitude || 126.978,
  };

  return (
    <div className="funeral-home-map">
      <GoogleMap
        center={defaultCenter}
        zoom={zoom}
        markers={markers}
        onMarkerClick={handleMarkerClick}
        onMapClick={onMapClick}
        height={height}
      />
    </div>
  );
};

export default FuneralHomeMap;
