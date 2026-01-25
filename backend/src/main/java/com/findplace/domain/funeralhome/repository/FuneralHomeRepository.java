package com.findplace.domain.funeralhome.repository;

import com.findplace.domain.funeralhome.entity.FuneralHome;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 장례식장 레포지토리
 */
@Repository
public interface FuneralHomeRepository extends JpaRepository<FuneralHome, Long> {

    /**
     * 이름과 지역 코드로 장례식장 조회 (중복 체크용)
     */
    Optional<FuneralHome> findByNameAndLocCode(String name, String locCode);

    /**
     * 이름으로 장례식장 목록 조회
     */
    List<FuneralHome> findByNameContaining(String name);

    /**
     * 활성화된 장례식장만 조회
     */
    List<FuneralHome> findByIsActiveTrue();

    /**
     * 지역 코드로 장례식장 조회
     */
    List<FuneralHome> findByLocCodeAndIsActiveTrue(String locCode);

    /**
     * 좌표가 없는 장례식장 조회 (Geocoding 필요)
     */
    List<FuneralHome> findByLatitudeIsNullAndIsActiveTrue();

    /**
     * 특정 시간 이후 동기화된 장례식장 ID 목록 조회
     */
    @Query("SELECT f.id FROM FuneralHome f WHERE f.syncedAt > :since")
    List<Long> findIdsSyncedAfter(@Param("since") LocalDateTime since);

    /**
     * Haversine 공식을 이용한 근처 장례식장 검색
     * PostgreSQL 네이티브 쿼리 사용
     *
     * 반환 컬럼 순서 (0-based index):
     * 0: id, 1: name, 2: road_address, 3: lot_address, 4: phone,
     * 5: loc_code, 6: loc_name, 7: has_crematorium, 8: has_columbarium,
     * 9: has_funeral, 10: latitude, 11: longitude, 12: is_active, 13: distance
     */
    @Query(value = """
            SELECT f.id, f.name, f.road_address, f.lot_address, f.phone,
                   f.loc_code, f.loc_name, f.has_crematorium, f.has_columbarium,
                   f.has_funeral, f.latitude, f.longitude, f.is_active,
                   (6371 * acos(
                       cos(radians(:lat)) * cos(radians(f.latitude))
                       * cos(radians(f.longitude) - radians(:lng))
                       + sin(radians(:lat)) * sin(radians(f.latitude))
                   )) AS distance
            FROM funeral_homes f
            WHERE f.is_active = true
              AND f.latitude IS NOT NULL
              AND f.longitude IS NOT NULL
              AND (6371 * acos(
                       cos(radians(:lat)) * cos(radians(f.latitude))
                       * cos(radians(f.longitude) - radians(:lng))
                       + sin(radians(:lat)) * sin(radians(f.latitude))
                   )) <= :radius
              AND (:hasCrematorium IS NULL OR f.has_crematorium = :hasCrematorium)
              AND (:hasFuneral IS NULL OR f.has_funeral = :hasFuneral)
              AND (:hasColumbarium IS NULL OR f.has_columbarium = :hasColumbarium)
            ORDER BY distance
            LIMIT :limitCount
            """, nativeQuery = true)
    List<Object[]> findNearbyWithDistance(
            @Param("lat") double latitude,
            @Param("lng") double longitude,
            @Param("radius") int radiusKm,
            @Param("limitCount") int limit,
            @Param("hasCrematorium") Boolean hasCrematorium,
            @Param("hasFuneral") Boolean hasFuneral,
            @Param("hasColumbarium") Boolean hasColumbarium
    );

    /**
     * 검색 조건으로 장례식장 페이징 조회
     */
    @Query("""
            SELECT f FROM FuneralHome f
            WHERE (:keyword IS NULL OR f.name LIKE %:keyword% OR f.roadAddress LIKE %:keyword%)
              AND (:locCode IS NULL OR f.locCode = :locCode)
              AND (:hasCrematorium IS NULL OR f.hasCrematorium = :hasCrematorium)
              AND (:hasFuneral IS NULL OR f.hasFuneral = :hasFuneral)
              AND (:hasColumbarium IS NULL OR f.hasColumbarium = :hasColumbarium)
              AND (:isActive IS NULL OR f.isActive = :isActive)
            ORDER BY f.name
            """)
    Page<FuneralHome> findBySearchConditions(
            @Param("keyword") String keyword,
            @Param("locCode") String locCode,
            @Param("hasCrematorium") Boolean hasCrematorium,
            @Param("hasFuneral") Boolean hasFuneral,
            @Param("hasColumbarium") Boolean hasColumbarium,
            @Param("isActive") Boolean isActive,
            Pageable pageable
    );

    /**
     * 동기화 시간 기준 오래된 장례식장 비활성화
     */
    @Modifying
    @Query("UPDATE FuneralHome f SET f.isActive = false WHERE f.syncedAt < :before AND f.isActive = true")
    int deactivateOldEntries(@Param("before") LocalDateTime before);

    /**
     * 통계: 총 장례식장 수
     */
    long countByIsActiveTrue();

    /**
     * 통계: 좌표가 등록된 장례식장 수
     */
    @Query("SELECT COUNT(f) FROM FuneralHome f WHERE f.isActive = true AND f.latitude IS NOT NULL")
    long countWithCoordinates();
}
