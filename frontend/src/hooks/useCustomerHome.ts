/**
 * @fileoverview 반려인 홈 데이터 훅
 */

import { useState, useCallback } from 'react';
import { CustomerHomeResponse } from '../types/home';
import { homeApi } from '../api/home';

interface UseCustomerHomeReturn {
  data: CustomerHomeResponse | null;
  loading: boolean;
  error: string | null;
  refetch: () => Promise<void>;
}

export const useCustomerHome = (
  latitude?: number,
  longitude?: number,
): UseCustomerHomeReturn => {
  const [data, setData] = useState<CustomerHomeResponse | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const refetch = useCallback(async () => {
    setLoading(true);
    setError(null);

    try {
      const params = latitude && longitude ? { latitude, longitude } : undefined;
      const result = await homeApi.getCustomerHome(params);
      setData(result);
    } catch (err) {
      setError(err instanceof Error ? err.message : '데이터를 불러오는데 실패했습니다.');
    } finally {
      setLoading(false);
    }
  }, [latitude, longitude]);

  return { data, loading, error, refetch };
};
