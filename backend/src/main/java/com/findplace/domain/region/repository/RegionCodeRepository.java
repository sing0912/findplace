package com.findplace.domain.region.repository;

import com.findplace.domain.region.entity.RegionCode;
import com.findplace.domain.region.entity.RegionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RegionCodeRepository extends JpaRepository<RegionCode, Long> {

    Optional<RegionCode> findByCode(String code);

    List<RegionCode> findByTypeAndIsActiveTrueOrderBySortOrder(RegionType type);

    List<RegionCode> findByParentCodeAndIsActiveTrueOrderBySortOrder(String parentCode);

    @Query("SELECT r FROM RegionCode r WHERE r.type = :type ORDER BY r.sortOrder")
    List<RegionCode> findByType(@Param("type") RegionType type);

    @Query("SELECT r FROM RegionCode r WHERE r.parentCode = :parentCode ORDER BY r.sortOrder")
    List<RegionCode> findByParentCode(@Param("parentCode") String parentCode);

    @Query("SELECT COUNT(r) FROM RegionCode r WHERE r.parentCode = :parentCode AND r.isActive = true")
    long countCitiesByMetroCode(@Param("parentCode") String parentCode);

    boolean existsByCode(String code);

    @Query("SELECT r FROM RegionCode r WHERE r.isActive = true ORDER BY r.type, r.sortOrder")
    List<RegionCode> findAllActive();
}
