/**
 * @fileoverview 현재 위치 버튼 컴포넌트
 */

import React, { useCallback } from 'react';
import { useGeolocation } from '../../hooks/useGeolocation';
import { Coordinates } from '../../types/location';

interface CurrentLocationButtonProps {
  /** 위치 조회 성공 핸들러 */
  onLocation: (coords: Coordinates) => void;
  /** 에러 핸들러 */
  onError?: (error: GeolocationPositionError) => void;
  /** 버튼 텍스트 */
  label?: string;
  /** 로딩 중 텍스트 */
  loadingLabel?: string;
  /** 커스텀 클래스 */
  className?: string;
  /** 비활성화 */
  disabled?: boolean;
}

/**
 * 현재 위치 조회 버튼 컴포넌트
 */
const CurrentLocationButton: React.FC<CurrentLocationButtonProps> = ({
  onLocation,
  onError,
  label = '현재 위치',
  loadingLabel = '위치 확인 중...',
  className = '',
  disabled = false,
}) => {
  const { getCurrentPosition, isLoading, error } = useGeolocation();

  const handleClick = useCallback(async () => {
    try {
      const position = await getCurrentPosition();
      onLocation(position);
    } catch (err) {
      if (onError && err instanceof GeolocationPositionError) {
        onError(err);
      }
    }
  }, [getCurrentPosition, onLocation, onError]);

  const getErrorMessage = (error: GeolocationPositionError): string => {
    switch (error.code) {
      case error.PERMISSION_DENIED:
        return '위치 접근 권한이 거부되었습니다.';
      case error.POSITION_UNAVAILABLE:
        return '위치 정보를 사용할 수 없습니다.';
      case error.TIMEOUT:
        return '위치 요청 시간이 초과되었습니다.';
      default:
        return '위치를 확인할 수 없습니다.';
    }
  };

  return (
    <div className={`current-location ${className}`}>
      <button
        type="button"
        onClick={handleClick}
        disabled={disabled || isLoading}
        className="current-location__button"
      >
        {isLoading ? loadingLabel : label}
      </button>
      {error && (
        <span className="current-location__error">
          {getErrorMessage(error)}
        </span>
      )}
    </div>
  );
};

export default CurrentLocationButton;
