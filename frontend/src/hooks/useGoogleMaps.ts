/**
 * @fileoverview Google Maps JavaScript API 로드 훅
 */

import { useState, useEffect } from 'react';

const GOOGLE_MAPS_SCRIPT_ID = 'google-maps-script';

interface UseGoogleMapsReturn {
  isLoaded: boolean;
  loadError: Error | null;
}

/**
 * Google Maps JavaScript API 로드 상태 관리 훅
 */
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
      const handleLoad = () => setIsLoaded(true);
      existingScript.addEventListener('load', handleLoad);
      return () => existingScript.removeEventListener('load', handleLoad);
    }

    // API 키 확인
    const key = apiKey || process.env.REACT_APP_GOOGLE_MAPS_API_KEY;
    if (!key) {
      setLoadError(new Error('Google Maps API key is not configured'));
      return;
    }

    // 새로 스크립트 추가
    const script = document.createElement('script');
    script.id = GOOGLE_MAPS_SCRIPT_ID;
    script.src = `https://maps.googleapis.com/maps/api/js?key=${key}&libraries=places&language=ko`;
    script.async = true;
    script.defer = true;

    script.onload = () => setIsLoaded(true);
    script.onerror = () => setLoadError(new Error('Google Maps 로드 실패'));

    document.head.appendChild(script);

    return () => {
      // 클린업 시 스크립트 제거하지 않음 (다른 컴포넌트에서 사용 가능)
    };
  }, [apiKey]);

  return { isLoaded, loadError };
};

// 타입 선언 (window.google)
// @types/google.maps에서 제공되는 타입 사용
declare global {
  interface Window {
    google?: typeof google;
  }
}
