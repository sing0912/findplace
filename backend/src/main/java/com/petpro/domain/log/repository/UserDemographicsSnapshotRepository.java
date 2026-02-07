package com.petpro.domain.log.repository;

import com.petpro.domain.log.entity.UserDemographicsSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

/**
 * 인구통계 스냅샷 Repository
 */
public interface UserDemographicsSnapshotRepository extends JpaRepository<UserDemographicsSnapshot, Long> {

    List<UserDemographicsSnapshot> findBySnapshotDate(LocalDate date);

    @Query("SELECT d.ageGroup, COUNT(d) FROM UserDemographicsSnapshot d " +
            "WHERE d.snapshotDate = :date GROUP BY d.ageGroup")
    List<Object[]> countByAgeGroup(@Param("date") LocalDate date);

    @Query("SELECT d.gender, COUNT(d) FROM UserDemographicsSnapshot d " +
            "WHERE d.snapshotDate = :date GROUP BY d.gender")
    List<Object[]> countByGender(@Param("date") LocalDate date);

    @Query("SELECT d.regionCode, COUNT(d) FROM UserDemographicsSnapshot d " +
            "WHERE d.snapshotDate = :date GROUP BY d.regionCode")
    List<Object[]> countByRegion(@Param("date") LocalDate date);
}
