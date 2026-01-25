package com.findplace.domain.admin.dto;

import com.findplace.domain.admin.entity.UserRoleChangeLog;
import com.findplace.domain.user.entity.UserRole;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 역할 변경 이력 응답 DTO
 */
@Getter
@Builder
public class RoleChangeLogResponse {

    private Long id;
    private UserRole previousRole;
    private UserRole newRole;
    private String reason;
    private Long changedBy;
    private LocalDateTime changedAt;

    public static RoleChangeLogResponse from(UserRoleChangeLog log) {
        return RoleChangeLogResponse.builder()
                .id(log.getId())
                .previousRole(log.getPreviousRole())
                .newRole(log.getNewRole())
                .reason(log.getReason())
                .changedBy(log.getChangedBy())
                .changedAt(log.getChangedAt())
                .build();
    }
}
