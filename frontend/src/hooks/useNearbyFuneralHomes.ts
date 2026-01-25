/**
 * @fileoverview 근처 장례식장 검색 훅
 */

import { useState, useCallback } from 'react';
import { searchNearbyFuneralHomes } from '../api/funeralHome';
import type {
  NearbyFuneralHomesResult,
  NearbySearchRequest,
} from '../types/funeralHome';

interface UseNearbyFuneralHomesReturn {
  /** 검색 결과 */
  result: NearbyFuneralHomesResult | null;
  /** 로딩 상태 */
  isLoading: boolean;
  /** 에러 */
  error: Error | null;
  /** 검색 실행 */
  search: (request: NearbySearchRequest) => Promise<void>;
  /** 결과 초기화 */
  reset: () => void;
}

/**
 * 근처 장례식장 검색 커스텀 훅
 */
export const useNearbyFuneralHomes = (): UseNearbyFuneralHomesReturn => {
  const [result, setResult] = useState<NearbyFuneralHomesResult | null>(null);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<Error | null>(null);

  const search = useCallback(async (request: NearbySearchRequest) => {
    setIsLoading(true);
    setError(null);

    try {
      const data = await searchNearbyFuneralHomes(request);
      setResult(data);
    } catch (err) {
      setError(err instanceof Error ? err : new Error('검색 중 오류가 발생했습니다'));
      setResult(null);
    } finally {
      setIsLoading(false);
    }
  }, []);

  const reset = useCallback(() => {
    setResult(null);
    setError(null);
    setIsLoading(false);
  }, []);

  return {
    result,
    isLoading,
    error,
    search,
    reset,
  };
};

export default useNearbyFuneralHomes;
