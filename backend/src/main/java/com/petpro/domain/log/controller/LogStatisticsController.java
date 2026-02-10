package com.petpro.domain.log.controller;

import com.petpro.domain.log.dto.StatisticsResponse.*;
import com.petpro.domain.log.service.LogStatisticsService;
import com.petpro.global.common.response.ApiResponse;
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

    @GetMapping("/admin-actions")
    public ResponseEntity<ApiResponse<AdminActionStatistics>> getAdminActionStatistics(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        AdminActionStatistics statistics = logStatisticsService.getAdminActionStatistics(startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(statistics));
    }

    @GetMapping("/user-actions")
    public ResponseEntity<ApiResponse<UserActionStatistics>> getUserActionStatistics(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        UserActionStatistics statistics = logStatisticsService.getUserActionStatistics(startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(statistics));
    }

    @GetMapping("/user-behavior")
    public ResponseEntity<ApiResponse<UserBehaviorStatistics>> getUserBehaviorStatistics(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        UserBehaviorStatistics statistics = logStatisticsService.getUserBehaviorStatistics(startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(statistics));
    }

    @GetMapping("/cs-analysis")
    public ResponseEntity<ApiResponse<CsAnalysisStatistics>> getCsAnalysisStatistics(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        CsAnalysisStatistics statistics = logStatisticsService.getCsAnalysisStatistics(startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(statistics));
    }
}
