package com.findplace.domain.admin.dto;

import com.findplace.domain.admin.entity.UserStatusChangeLog;
import com.findplace.domain.user.entity.UserStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 상태 변경 이력 응답 DTO
 */
@Getter
@Builder
public class StatusChangeLogResponse {

    private Long id;
    private UserStatus previousStatus;
    private UserStatus newStatus;
    private String reason;
    private Long changedBy;
    private LocalDateTime changedAt;

    public static StatusChangeLogResponse from(UserStatusChangeLog log) {
        return StatusChangeLogResponse.builder()
                .id(log.getId())
                .previousStatus(log.getPreviousStatus())
                .newStatus(log.getNewStatus())
                .reason(log.getReason())
                .changedBy(log.getChangedBy())
                .changedAt(log.getChangedAt())
                .build();
    }
}
