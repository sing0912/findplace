/**
 * @fileoverview 브라우저 Geolocation API 커스텀 훅
 */

import { useState, useCallback } from 'react';
import { Coordinates } from '../types/location';

interface GeolocationState {
  coordinates: Coordinates | null;
  error: GeolocationPositionError | null;
  isLoading: boolean;
}

interface UseGeolocationOptions {
  enableHighAccuracy?: boolean;
  timeout?: number;
  maximumAge?: number;
}

interface UseGeolocationReturn extends GeolocationState {
  getCurrentPosition: () => Promise<Coordinates>;
  clearError: () => void;
}

const defaultOptions: UseGeolocationOptions = {
  enableHighAccuracy: true,
  timeout: 10000,
  maximumAge: 60000,
};

/**
 * 브라우저 Geolocation API를 사용한 현재 위치 조회 훅
 */
export const useGeolocation = (
  options: UseGeolocationOptions = defaultOptions
): UseGeolocationReturn => {
  const [state, setState] = useState<GeolocationState>({
    coordinates: null,
    error: null,
    isLoading: false,
  });

  const getCurrentPosition = useCallback((): Promise<Coordinates> => {
    return new Promise((resolve, reject) => {
      if (!navigator.geolocation) {
        const error = {
          code: 2,
          message: 'Geolocation is not supported by this browser.',
          PERMISSION_DENIED: 1,
          POSITION_UNAVAILABLE: 2,
          TIMEOUT: 3,
        } as GeolocationPositionError;
        setState((prev) => ({ ...prev, error, isLoading: false }));
        reject(error);
        return;
      }

      setState((prev) => ({ ...prev, isLoading: true, error: null }));

      navigator.geolocation.getCurrentPosition(
        (position) => {
          const coordinates: Coordinates = {
            latitude: position.coords.latitude,
            longitude: position.coords.longitude,
          };
          setState({
            coordinates,
            error: null,
            isLoading: false,
          });
          resolve(coordinates);
        },
        (error) => {
          setState((prev) => ({
            ...prev,
            error,
            isLoading: false,
          }));
          reject(error);
        },
        {
          enableHighAccuracy: options.enableHighAccuracy,
          timeout: options.timeout,
          maximumAge: options.maximumAge,
        }
      );
    });
  }, [options.enableHighAccuracy, options.timeout, options.maximumAge]);

  const clearError = useCallback(() => {
    setState((prev) => ({ ...prev, error: null }));
  }, []);

  return {
    ...state,
    getCurrentPosition,
    clearError,
  };
};
