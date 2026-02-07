package com.petpro.domain.log.dto;

import com.petpro.domain.log.entity.AdminActionType;
import com.petpro.domain.log.entity.TargetType;
import com.petpro.domain.log.entity.UserActionType;
import lombok.*;

/**
 * 로그 요청 DTO
 */
public class LogRequest {

    @Getter
    @Builder
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor
    public static class AdminActionLogRequest {
        private Long adminId;
        private AdminActionType actionType;
        private TargetType targetType;
        private Long targetId;
        private String description;
        private String detailJson;
    }

    @Getter
    @Builder
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor
    public static class UserActionLogRequest {
        private Long userId;
        private UserActionType actionType;
        private TargetType targetType;
        private Long targetId;
        private String description;
        private String detailJson;
    }
}
