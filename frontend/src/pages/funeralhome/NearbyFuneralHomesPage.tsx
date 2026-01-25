/**
 * @fileoverview 근처 장례식장 페이지
 */

import React, { useState, useCallback, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import FuneralHomeCard from '../../components/funeralhome/FuneralHomeCard';
import FuneralHomeMap from '../../components/funeralhome/FuneralHomeMap';
import CurrentLocationButton from '../../components/location/CurrentLocationButton';
import { useNearbyFuneralHomes } from '../../hooks/useNearbyFuneralHomes';
import type { Coordinates } from '../../types/location';
import type { FuneralHomeListItem } from '../../types/funeralHome';

/**
 * 근처 장례식장 검색 페이지
 */
const NearbyFuneralHomesPage: React.FC = () => {
  const navigate = useNavigate();
  const { result, isLoading, error, search } = useNearbyFuneralHomes();

  const [userLocation, setUserLocation] = useState<Coordinates | null>(null);
  const [radius, setRadius] = useState(10);
  const [filters, setFilters] = useState({
    hasCrematorium: false,
    hasFuneral: false,
    hasColumbarium: false,
  });

  // 현재 위치로 검색
  const handleLocationReceived = useCallback(
    (coords: Coordinates) => {
      setUserLocation(coords);
      search({
        latitude: coords.latitude,
        longitude: coords.longitude,
        radius,
        limit: 20,
        hasCrematorium: filters.hasCrematorium || undefined,
        hasFuneral: filters.hasFuneral || undefined,
        hasColumbarium: filters.hasColumbarium || undefined,
      });
    },
    [search, radius, filters]
  );

  // 필터 또는 반경 변경 시 재검색
  useEffect(() => {
    if (userLocation) {
      search({
        latitude: userLocation.latitude,
        longitude: userLocation.longitude,
        radius,
        limit: 20,
        hasCrematorium: filters.hasCrematorium || undefined,
        hasFuneral: filters.hasFuneral || undefined,
        hasColumbarium: filters.hasColumbarium || undefined,
      });
    }
  }, [userLocation, radius, filters, search]);

  // 장례식장 상세 페이지로 이동
  const handleFuneralHomeClick = useCallback(
    (id: number) => {
      navigate(`/funeral-homes/${id}`);
    },
    [navigate]
  );

  // 지도 마커 클릭
  const handleMarkerClick = useCallback(
    (funeralHome: FuneralHomeListItem) => {
      handleFuneralHomeClick(funeralHome.id);
    },
    [handleFuneralHomeClick]
  );

  return (
    <div className="nearby-funeral-homes-page">
      <header className="nearby-funeral-homes-page__header">
        <h1>내 주변 장례식장</h1>
        <CurrentLocationButton
          onLocation={handleLocationReceived}
          label="현재 위치로 검색"
        />
      </header>

      {/* 필터 */}
      <section className="nearby-funeral-homes-page__filters">
        <div className="filter-group">
          <label htmlFor="radius">검색 반경:</label>
          <select
            id="radius"
            value={radius}
            onChange={(e) => setRadius(Number(e.target.value))}
          >
            <option value={5}>5km</option>
            <option value={10}>10km</option>
            <option value={20}>20km</option>
            <option value={50}>50km</option>
          </select>
        </div>

        <div className="filter-group">
          <label>
            <input
              type="checkbox"
              checked={filters.hasCrematorium}
              onChange={(e) =>
                setFilters((prev) => ({ ...prev, hasCrematorium: e.target.checked }))
              }
            />
            화장장
          </label>
          <label>
            <input
              type="checkbox"
              checked={filters.hasFuneral}
              onChange={(e) =>
                setFilters((prev) => ({ ...prev, hasFuneral: e.target.checked }))
              }
            />
            장례식장
          </label>
          <label>
            <input
              type="checkbox"
              checked={filters.hasColumbarium}
              onChange={(e) =>
                setFilters((prev) => ({ ...prev, hasColumbarium: e.target.checked }))
              }
            />
            납골당
          </label>
        </div>
      </section>

      {/* 로딩 상태 */}
      {isLoading && <div className="loading">검색 중...</div>}

      {/* 에러 */}
      {error && <div className="error">{error.message}</div>}

      {/* 검색 결과 */}
      {result && (
        <>
          {/* 지도 */}
          {userLocation && (
            <section className="nearby-funeral-homes-page__map">
              <FuneralHomeMap
                funeralHomes={result.content}
                userLocation={userLocation}
                showUserLocation
                onMarkerClick={handleMarkerClick}
                height="350px"
              />
            </section>
          )}

          {/* 결과 목록 */}
          <section className="nearby-funeral-homes-page__results">
            <h2>
              검색 결과 ({result.totalCount}건, 반경 {result.radius}km)
            </h2>

            {result.content.length === 0 ? (
              <p className="no-results">
                주변에 장례식장이 없습니다. 검색 반경을 넓혀보세요.
              </p>
            ) : (
              <div className="funeral-home-list">
                {result.content.map((fh) => (
                  <FuneralHomeCard
                    key={fh.id}
                    funeralHome={fh}
                    onClick={handleFuneralHomeClick}
                    showDistance
                  />
                ))}
              </div>
            )}
          </section>
        </>
      )}

      {/* 검색 전 안내 */}
      {!result && !isLoading && !error && (
        <div className="guide">
          <p>현재 위치로 검색 버튼을 눌러 주변 장례식장을 찾아보세요.</p>
        </div>
      )}
    </div>
  );
};

export default NearbyFuneralHomesPage;
