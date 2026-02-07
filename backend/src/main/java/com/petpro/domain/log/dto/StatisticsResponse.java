package com.petpro.domain.log.dto;

import lombok.*;

import java.util.List;

/**
 * 통계 응답 DTO
 */
public class StatisticsResponse {

    @Getter
    @Builder
    @AllArgsConstructor
    public static class ActionTypeCount {
        private String actionType;
        private Long count;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class HourlyDistribution {
        private Integer hour;
        private Long count;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class DayOfWeekDistribution {
        private Integer dayOfWeek;
        private Long count;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class DeviceTypeDistribution {
        private String deviceType;
        private Long count;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class AdminActionStatistics {
        private List<ActionTypeCount> actionTypeCounts;
        private List<ActionTypeCount> adminCounts;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class UserActionStatistics {
        private List<ActionTypeCount> actionTypeCounts;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class UserBehaviorStatistics {
        private List<HourlyDistribution> hourly;
        private List<DayOfWeekDistribution> dayOfWeek;
        private List<DeviceTypeDistribution> deviceType;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class CsAnalysisStatistics {
        private Long totalInquiries;
        private Long totalOrders;
        private Double csRate;
    }
}
