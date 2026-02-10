/**
 * @fileoverview 펫시터 홈 데이터 훅
 */

import { useState, useCallback } from 'react';
import { PartnerHomeResponse } from '../types/home';
import { homeApi } from '../api/home';

interface UsePartnerHomeReturn {
  data: PartnerHomeResponse | null;
  loading: boolean;
  error: string | null;
  refetch: () => Promise<void>;
}

export const usePartnerHome = (): UsePartnerHomeReturn => {
  const [data, setData] = useState<PartnerHomeResponse | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const refetch = useCallback(async () => {
    setLoading(true);
    setError(null);

    try {
      const result = await homeApi.getPartnerHome();
      setData(result);
    } catch (err) {
      setError(err instanceof Error ? err.message : '데이터를 불러오는데 실패했습니다.');
    } finally {
      setLoading(false);
    }
  }, []);

  return { data, loading, error, refetch };
};
