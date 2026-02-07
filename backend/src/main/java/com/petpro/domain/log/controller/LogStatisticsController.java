package com.petpro.domain.log.controller;

import com.petpro.domain.log.dto.StatisticsResponse.*;
import com.petpro.domain.log.service.LogStatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

/**
 * 로그 통계 API 컨트롤러
 *
 * 관리자 전용 통계 조회 엔드포인트
 */
@RestController
@RequestMapping("/v1/admin/statistics")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
public class LogStatisticsController {

    private final LogStatisticsService logStatisticsService;

    /**
     * 운영자 행위 통계
     */
    @GetMapping("/admin-actions")
    public ResponseEntity<AdminActionStatistics> getAdminActionStatistics(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        AdminActionStatistics statistics = logStatisticsService.getAdminActionStatistics(startDate, endDate);
        return ResponseEntity.ok(statistics);
    }

    /**
     * 사용자 행위 통계
     */
    @GetMapping("/user-actions")
    public ResponseEntity<UserActionStatistics> getUserActionStatistics(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        UserActionStatistics statistics = logStatisticsService.getUserActionStatistics(startDate, endDate);
        return ResponseEntity.ok(statistics);
    }

    /**
     * 사용자 행동 패턴 통계
     */
    @GetMapping("/user-behavior")
    public ResponseEntity<UserBehaviorStatistics> getUserBehaviorStatistics(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        UserBehaviorStatistics statistics = logStatisticsService.getUserBehaviorStatistics(startDate, endDate);
        return ResponseEntity.ok(statistics);
    }

    /**
     * CS 패턴 분석 통계
     */
    @GetMapping("/cs-analysis")
    public ResponseEntity<CsAnalysisStatistics> getCsAnalysisStatistics(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        CsAnalysisStatistics statistics = logStatisticsService.getCsAnalysisStatistics(startDate, endDate);
        return ResponseEntity.ok(statistics);
    }
}
