/**
 * @fileoverview 지역 관련 커스텀 훅
 */

import { useState, useEffect, useCallback } from 'react';
import { getMetros, getCities, getRegionHierarchy } from '../api/region';
import { Metro, City, RegionHierarchy } from '../types/region';

interface UseMetrosResult {
  metros: Metro[];
  loading: boolean;
  error: string | null;
  refetch: () => Promise<void>;
}

/**
 * 광역시/도 목록 조회 훅
 */
export const useMetros = (): UseMetrosResult => {
  const [metros, setMetros] = useState<Metro[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const fetchMetros = useCallback(async () => {
    try {
      setLoading(true);
      setError(null);
      const response = await getMetros();
      setMetros(response.metros);
    } catch (err) {
      setError('광역시/도 목록을 불러오는데 실패했습니다.');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchMetros();
  }, [fetchMetros]);

  return { metros, loading, error, refetch: fetchMetros };
};

interface UseCitiesResult {
  cities: City[];
  metroName: string;
  loading: boolean;
  error: string | null;
  refetch: () => Promise<void>;
}

/**
 * 시/군/구 목록 조회 훅
 */
export const useCities = (metroCode: string | null): UseCitiesResult => {
  const [cities, setCities] = useState<City[]>([]);
  const [metroName, setMetroName] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const fetchCities = useCallback(async () => {
    if (!metroCode) {
      setCities([]);
      setMetroName('');
      return;
    }

    try {
      setLoading(true);
      setError(null);
      const response = await getCities(metroCode);
      setCities(response.cities);
      setMetroName(response.metroName);
    } catch (err) {
      setError('시/군/구 목록을 불러오는데 실패했습니다.');
    } finally {
      setLoading(false);
    }
  }, [metroCode]);

  useEffect(() => {
    fetchCities();
  }, [fetchCities]);

  return { cities, metroName, loading, error, refetch: fetchCities };
};

interface UseRegionHierarchyResult {
  regions: RegionHierarchy[];
  loading: boolean;
  error: string | null;
  refetch: () => Promise<void>;
}

/**
 * 전체 지역 계층 구조 조회 훅
 */
export const useRegionHierarchy = (): UseRegionHierarchyResult => {
  const [regions, setRegions] = useState<RegionHierarchy[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const fetchHierarchy = useCallback(async () => {
    try {
      setLoading(true);
      setError(null);
      const response = await getRegionHierarchy();
      setRegions(response.regions);
    } catch (err) {
      setError('지역 계층 구조를 불러오는데 실패했습니다.');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchHierarchy();
  }, [fetchHierarchy]);

  return { regions, loading, error, refetch: fetchHierarchy };
};

interface SelectedRegion {
  metroCode: string | null;
  cityCode: string | null;
  metroName: string;
  cityName: string;
}

interface UseRegionSelectResult {
  metros: Metro[];
  cities: City[];
  selected: SelectedRegion;
  loading: boolean;
  error: string | null;
  selectMetro: (code: string | null) => void;
  selectCity: (code: string | null) => void;
  reset: () => void;
}

/**
 * 지역 선택 통합 훅 (Cascading 드롭다운용)
 */
export const useRegionSelect = (): UseRegionSelectResult => {
  const [selected, setSelected] = useState<SelectedRegion>({
    metroCode: null,
    cityCode: null,
    metroName: '',
    cityName: '',
  });

  const { metros, loading: metrosLoading, error: metrosError } = useMetros();
  const { cities, metroName, loading: citiesLoading, error: citiesError } = useCities(
    selected.metroCode
  );

  const selectMetro = useCallback(
    (code: string | null) => {
      const metro = metros.find((m) => m.code === code);
      setSelected({
        metroCode: code,
        cityCode: null,
        metroName: metro?.name || '',
        cityName: '',
      });
    },
    [metros]
  );

  const selectCity = useCallback(
    (code: string | null) => {
      const city = cities.find((c) => c.code === code);
      setSelected((prev) => ({
        ...prev,
        cityCode: code,
        cityName: city?.name || '',
      }));
    },
    [cities]
  );

  const reset = useCallback(() => {
    setSelected({
      metroCode: null,
      cityCode: null,
      metroName: '',
      cityName: '',
    });
  }, []);

  return {
    metros,
    cities,
    selected,
    loading: metrosLoading || citiesLoading,
    error: metrosError || citiesError,
    selectMetro,
    selectCity,
    reset,
  };
};
