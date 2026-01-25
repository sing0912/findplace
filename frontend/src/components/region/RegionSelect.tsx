/**
 * @fileoverview 지역 선택 컴포넌트 (Cascading 드롭다운)
 */

import React from 'react';
import { useRegionSelect } from '../../hooks/useRegions';

interface RegionSelectProps {
  /** 지역 선택 시 콜백 */
  onSelect?: (metroCode: string | null, cityCode: string | null) => void;
  /** 초기 광역시/도 코드 */
  initialMetroCode?: string | null;
  /** 초기 시/군/구 코드 */
  initialCityCode?: string | null;
  /** 시/군/구 선택 필수 여부 */
  requireCity?: boolean;
  /** 비활성화 여부 */
  disabled?: boolean;
  /** 커스텀 클래스 */
  className?: string;
}

/**
 * 지역 선택 Cascading 드롭다운 컴포넌트
 *
 * 광역시/도를 선택하면 해당 지역의 시/군/구 목록이 자동으로 로드됩니다.
 */
const RegionSelect: React.FC<RegionSelectProps> = ({
  onSelect,
  initialMetroCode = null,
  initialCityCode = null,
  requireCity = false,
  disabled = false,
  className = '',
}) => {
  const {
    metros,
    cities,
    selected,
    loading,
    error,
    selectMetro,
    selectCity,
    reset,
  } = useRegionSelect();

  // 초기값 설정
  React.useEffect(() => {
    if (initialMetroCode && metros.length > 0 && !selected.metroCode) {
      selectMetro(initialMetroCode);
    }
  }, [initialMetroCode, metros, selected.metroCode, selectMetro]);

  React.useEffect(() => {
    if (initialCityCode && cities.length > 0 && !selected.cityCode) {
      selectCity(initialCityCode);
    }
  }, [initialCityCode, cities, selected.cityCode, selectCity]);

  // 선택 변경 시 콜백 호출
  React.useEffect(() => {
    if (onSelect) {
      onSelect(selected.metroCode, selected.cityCode);
    }
  }, [selected.metroCode, selected.cityCode, onSelect]);

  const handleMetroChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
    const value = e.target.value || null;
    selectMetro(value);
  };

  const handleCityChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
    const value = e.target.value || null;
    selectCity(value);
  };

  const handleReset = () => {
    reset();
  };

  if (error) {
    return (
      <div className={`region-select region-select--error ${className}`}>
        <p className="region-select__error-message">{error}</p>
        <button onClick={reset} className="region-select__retry-btn">
          다시 시도
        </button>
      </div>
    );
  }

  return (
    <div className={`region-select ${className}`}>
      <div className="region-select__row">
        {/* 광역시/도 선택 */}
        <div className="region-select__field">
          <label htmlFor="metro-select" className="region-select__label">
            광역시/도
          </label>
          <select
            id="metro-select"
            value={selected.metroCode || ''}
            onChange={handleMetroChange}
            disabled={disabled || loading}
            className="region-select__dropdown"
          >
            <option value="">전체</option>
            {metros.map((metro) => (
              <option key={metro.code} value={metro.code}>
                {metro.name} ({metro.cityCount})
              </option>
            ))}
          </select>
        </div>

        {/* 시/군/구 선택 */}
        <div className="region-select__field">
          <label htmlFor="city-select" className="region-select__label">
            시/군/구
          </label>
          <select
            id="city-select"
            value={selected.cityCode || ''}
            onChange={handleCityChange}
            disabled={disabled || loading || !selected.metroCode}
            className="region-select__dropdown"
          >
            <option value="">{requireCity ? '선택하세요' : '전체'}</option>
            {cities.map((city) => (
              <option key={city.code} value={city.code}>
                {city.name}
              </option>
            ))}
          </select>
        </div>

        {/* 초기화 버튼 */}
        <button
          type="button"
          onClick={handleReset}
          disabled={disabled || (!selected.metroCode && !selected.cityCode)}
          className="region-select__reset-btn"
        >
          초기화
        </button>
      </div>

      {/* 선택된 지역 표시 */}
      {(selected.metroName || selected.cityName) && (
        <div className="region-select__selected">
          <span className="region-select__selected-label">선택된 지역:</span>
          <span className="region-select__selected-value">
            {selected.metroName}
            {selected.cityName && ` > ${selected.cityName}`}
          </span>
        </div>
      )}

      {loading && <div className="region-select__loading">로딩 중...</div>}
    </div>
  );
};

export default RegionSelect;
