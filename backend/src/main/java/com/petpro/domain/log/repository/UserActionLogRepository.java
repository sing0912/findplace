package com.petpro.domain.log.repository;

import com.petpro.domain.log.entity.UserActionLog;
import com.petpro.domain.log.entity.UserActionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 사용자 행위 로그 Repository
 */
public interface UserActionLogRepository extends JpaRepository<UserActionLog, Long> {

    Page<UserActionLog> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end, Pageable pageable);

    Page<UserActionLog> findByUserIdAndCreatedAtBetween(
            Long userId, LocalDateTime start, LocalDateTime end, Pageable pageable);

    Page<UserActionLog> findByActionTypeAndCreatedAtBetween(
            UserActionType actionType, LocalDateTime start, LocalDateTime end, Pageable pageable);

    @Query("SELECT a.actionType, COUNT(a) FROM UserActionLog a " +
            "WHERE a.createdAt BETWEEN :start AND :end GROUP BY a.actionType")
    List<Object[]> countByActionType(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT HOUR(a.createdAt), COUNT(a) FROM UserActionLog a " +
            "WHERE a.createdAt BETWEEN :start AND :end GROUP BY HOUR(a.createdAt)")
    List<Object[]> countByHour(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT FUNCTION('DAYOFWEEK', a.createdAt), COUNT(a) FROM UserActionLog a " +
            "WHERE a.createdAt BETWEEN :start AND :end GROUP BY FUNCTION('DAYOFWEEK', a.createdAt)")
    List<Object[]> countByDayOfWeek(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT a.deviceType, COUNT(a) FROM UserActionLog a " +
            "WHERE a.createdAt BETWEEN :start AND :end GROUP BY a.deviceType")
    List<Object[]> countByDeviceType(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT COUNT(a) FROM UserActionLog a " +
            "WHERE a.actionType = :actionType AND a.createdAt BETWEEN :start AND :end")
    Long countByActionTypeAndCreatedAtBetween(
            @Param("actionType") UserActionType actionType,
            @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}
