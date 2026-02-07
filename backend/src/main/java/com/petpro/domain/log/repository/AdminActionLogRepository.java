package com.petpro.domain.log.repository;

import com.petpro.domain.log.entity.AdminActionLog;
import com.petpro.domain.log.entity.AdminActionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 운영자 행위 로그 Repository
 */
public interface AdminActionLogRepository extends JpaRepository<AdminActionLog, Long> {

    Page<AdminActionLog> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end, Pageable pageable);

    Page<AdminActionLog> findByAdminIdAndCreatedAtBetween(
            Long adminId, LocalDateTime start, LocalDateTime end, Pageable pageable);

    Page<AdminActionLog> findByActionTypeAndCreatedAtBetween(
            AdminActionType actionType, LocalDateTime start, LocalDateTime end, Pageable pageable);

    @Query("SELECT a.actionType, COUNT(a) FROM AdminActionLog a " +
            "WHERE a.createdAt BETWEEN :start AND :end GROUP BY a.actionType")
    List<Object[]> countByActionType(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT a.adminId, COUNT(a) FROM AdminActionLog a " +
            "WHERE a.createdAt BETWEEN :start AND :end GROUP BY a.adminId ORDER BY COUNT(a) DESC")
    List<Object[]> countByAdminId(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}
