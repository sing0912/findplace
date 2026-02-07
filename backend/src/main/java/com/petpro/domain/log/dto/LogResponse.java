package com.petpro.domain.log.dto;

import com.petpro.domain.log.entity.AdminActionLog;
import com.petpro.domain.log.entity.UserActionLog;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 로그 응답 DTO
 */
public class LogResponse {

    @Getter
    @Builder
    @AllArgsConstructor
    public static class AdminActionLogResponse {
        private Long id;
        private Long adminId;
        private String actionType;
        private String targetType;
        private Long targetId;
        private String description;
        private String detailJson;
        private String ipAddress;
        private String userAgent;
        private LocalDateTime createdAt;

        public static AdminActionLogResponse from(AdminActionLog log) {
            return AdminActionLogResponse.builder()
                    .id(log.getId())
                    .adminId(log.getAdminId())
                    .actionType(log.getActionType().name())
                    .targetType(log.getTargetType().name())
                    .targetId(log.getTargetId())
                    .description(log.getDescription())
                    .detailJson(log.getDetailJson())
                    .ipAddress(log.getIpAddress())
                    .userAgent(log.getUserAgent())
                    .createdAt(log.getCreatedAt())
                    .build();
        }
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class UserActionLogResponse {
        private Long id;
        private Long userId;
        private String actionType;
        private String targetType;
        private Long targetId;
        private String description;
        private String detailJson;
        private String ipAddress;
        private String userAgent;
        private String deviceType;
        private LocalDateTime createdAt;

        public static UserActionLogResponse from(UserActionLog log) {
            return UserActionLogResponse.builder()
                    .id(log.getId())
                    .userId(log.getUserId())
                    .actionType(log.getActionType().name())
                    .targetType(log.getTargetType() != null ? log.getTargetType().name() : null)
                    .targetId(log.getTargetId())
                    .description(log.getDescription())
                    .detailJson(log.getDetailJson())
                    .ipAddress(log.getIpAddress())
                    .userAgent(log.getUserAgent())
                    .deviceType(log.getDeviceType() != null ? log.getDeviceType().name() : null)
                    .createdAt(log.getCreatedAt())
                    .build();
        }
    }
}
