package com.petpro.domain.log.service;

import com.petpro.domain.log.dto.StatisticsResponse.*;
import com.petpro.domain.log.entity.UserActionType;
import com.petpro.domain.log.repository.AdminActionLogRepository;
import com.petpro.domain.log.repository.UserActionLogRepository;
import com.petpro.domain.log.repository.UserDemographicsSnapshotRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 로그 통계 서비스
 *
 * MySQL Slave에서 통계 데이터를 조회합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(value = "logTransactionManager", readOnly = true)
public class LogStatisticsService {

    private final AdminActionLogRepository adminActionLogRepository;
    private final UserActionLogRepository userActionLogRepository;
    private final UserDemographicsSnapshotRepository demographicsSnapshotRepository;

    /**
     * 운영자 행위 통계
     */
    public AdminActionStatistics getAdminActionStatistics(LocalDateTime start, LocalDateTime end) {
        List<ActionTypeCount> actionTypeCounts = adminActionLogRepository.countByActionType(start, end)
                .stream()
                .map(row -> ActionTypeCount.builder()
                        .actionType(row[0].toString())
                        .count((Long) row[1])
                        .build())
                .collect(Collectors.toList());

        List<ActionTypeCount> adminCounts = adminActionLogRepository.countByAdminId(start, end)
                .stream()
                .map(row -> ActionTypeCount.builder()
                        .actionType(row[0].toString())
                        .count((Long) row[1])
                        .build())
                .collect(Collectors.toList());

        return AdminActionStatistics.builder()
                .actionTypeCounts(actionTypeCounts)
                .adminCounts(adminCounts)
                .build();
    }

    /**
     * 사용자 행위 통계
     */
    public UserActionStatistics getUserActionStatistics(LocalDateTime start, LocalDateTime end) {
        List<ActionTypeCount> actionTypeCounts = userActionLogRepository.countByActionType(start, end)
                .stream()
                .map(row -> ActionTypeCount.builder()
                        .actionType(row[0].toString())
                        .count((Long) row[1])
                        .build())
                .collect(Collectors.toList());

        return UserActionStatistics.builder()
                .actionTypeCounts(actionTypeCounts)
                .build();
    }

    /**
     * 사용자 행동 패턴 통계 (시간대, 요일, 디바이스)
     */
    public UserBehaviorStatistics getUserBehaviorStatistics(LocalDateTime start, LocalDateTime end) {
        List<HourlyDistribution> hourly = userActionLogRepository.countByHour(start, end)
                .stream()
                .map(row -> HourlyDistribution.builder()
                        .hour(((Number) row[0]).intValue())
                        .count((Long) row[1])
                        .build())
                .collect(Collectors.toList());

        List<DayOfWeekDistribution> dayOfWeek = userActionLogRepository.countByDayOfWeek(start, end)
                .stream()
                .map(row -> DayOfWeekDistribution.builder()
                        .dayOfWeek(((Number) row[0]).intValue())
                        .count((Long) row[1])
                        .build())
                .collect(Collectors.toList());

        List<DeviceTypeDistribution> deviceType = userActionLogRepository.countByDeviceType(start, end)
                .stream()
                .map(row -> DeviceTypeDistribution.builder()
                        .deviceType(row[0] != null ? row[0].toString() : "UNKNOWN")
                        .count((Long) row[1])
                        .build())
                .collect(Collectors.toList());

        return UserBehaviorStatistics.builder()
                .hourly(hourly)
                .dayOfWeek(dayOfWeek)
                .deviceType(deviceType)
                .build();
    }

    /**
     * CS 패턴 분석 (문의 발생률)
     */
    public CsAnalysisStatistics getCsAnalysisStatistics(LocalDateTime start, LocalDateTime end) {
        Long totalInquiries = userActionLogRepository.countByActionTypeAndCreatedAtBetween(
                UserActionType.INQUIRY_CREATE, start, end);
        Long totalOrders = userActionLogRepository.countByActionTypeAndCreatedAtBetween(
                UserActionType.ORDER_CREATE, start, end);

        double csRate = totalOrders > 0
                ? (double) totalInquiries / totalOrders * 100
                : 0.0;

        return CsAnalysisStatistics.builder()
                .totalInquiries(totalInquiries)
                .totalOrders(totalOrders)
                .csRate(Math.round(csRate * 100.0) / 100.0)
                .build();
    }
}
