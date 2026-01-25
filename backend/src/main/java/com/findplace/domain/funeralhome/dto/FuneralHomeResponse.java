package com.findplace.domain.funeralhome.dto;

import com.findplace.domain.funeralhome.entity.FuneralHome;
import com.findplace.domain.funeralhome.entity.FuneralHomeSyncLog;
import com.findplace.domain.funeralhome.entity.SyncStatus;
import com.findplace.domain.funeralhome.entity.SyncType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 장례식장 응답 DTO
 */
public class FuneralHomeResponse {

    /**
     * 장례식장 목록 항목 (거리 포함)
     */
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ListItem {
        private Long id;
        private String name;
        private String roadAddress;
        private String phone;
        private String locName;
        private Boolean hasCrematorium;
        private Boolean hasColumbarium;
        private Boolean hasFuneral;
        private BigDecimal latitude;
        private BigDecimal longitude;
        private Double distance; // km

        public static ListItem from(FuneralHome home, Double distance) {
            return ListItem.builder()
                    .id(home.getId())
                    .name(home.getName())
                    .roadAddress(home.getRoadAddress())
                    .phone(home.getPhone())
                    .locName(home.getLocName())
                    .hasCrematorium(home.getHasCrematorium())
                    .hasColumbarium(home.getHasColumbarium())
                    .hasFuneral(home.getHasFuneral())
                    .latitude(home.getLatitude())
                    .longitude(home.getLongitude())
                    .distance(distance)
                    .build();
        }

        public static ListItem from(FuneralHome home) {
            return from(home, null);
        }
    }

    /**
     * 근처 장례식장 검색 결과
     */
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NearbyResult {
        private List<ListItem> content;
        private Integer totalCount;
        private Integer radius;
    }

    /**
     * 장례식장 상세 정보
     */
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Detail {
        private Long id;
        private String name;
        private String roadAddress;
        private String lotAddress;
        private String phone;
        private String locCode;
        private String locName;
        private Services services;
        private Location location;
        private Boolean isActive;
        private LocalDateTime syncedAt;
        private LocalDateTime createdAt;

        public static Detail from(FuneralHome home) {
            return Detail.builder()
                    .id(home.getId())
                    .name(home.getName())
                    .roadAddress(home.getRoadAddress())
                    .lotAddress(home.getLotAddress())
                    .phone(home.getPhone())
                    .locCode(home.getLocCode())
                    .locName(home.getLocName())
                    .services(Services.builder()
                            .hasCrematorium(home.getHasCrematorium())
                            .hasColumbarium(home.getHasColumbarium())
                            .hasFuneral(home.getHasFuneral())
                            .build())
                    .location(Location.builder()
                            .latitude(home.getLatitude())
                            .longitude(home.getLongitude())
                            .build())
                    .isActive(home.getIsActive())
                    .syncedAt(home.getSyncedAt())
                    .createdAt(home.getCreatedAt())
                    .build();
        }
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Services {
        private Boolean hasCrematorium;
        private Boolean hasColumbarium;
        private Boolean hasFuneral;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Location {
        private BigDecimal latitude;
        private BigDecimal longitude;
    }

    /**
     * 동기화 로그 응답
     */
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SyncLogItem {
        private Long id;
        private SyncType syncType;
        private LocalDateTime startedAt;
        private LocalDateTime completedAt;
        private SyncStatus status;
        private Integer totalCount;
        private Integer insertedCount;
        private Integer updatedCount;
        private Integer deletedCount;
        private Integer errorCount;
        private String errorMessage;

        public static SyncLogItem from(FuneralHomeSyncLog log) {
            return SyncLogItem.builder()
                    .id(log.getId())
                    .syncType(log.getSyncType())
                    .startedAt(log.getStartedAt())
                    .completedAt(log.getCompletedAt())
                    .status(log.getStatus())
                    .totalCount(log.getTotalCount())
                    .insertedCount(log.getInsertedCount())
                    .updatedCount(log.getUpdatedCount())
                    .deletedCount(log.getDeletedCount())
                    .errorCount(log.getErrorCount())
                    .errorMessage(log.getErrorMessage())
                    .build();
        }
    }

    /**
     * 동기화 실행 결과
     */
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SyncResult {
        private Long logId;
        private SyncType syncType;
        private SyncStatus status;
        private Integer totalCount;
        private Integer insertedCount;
        private Integer updatedCount;
        private Integer deletedCount;
        private String message;
    }
}
