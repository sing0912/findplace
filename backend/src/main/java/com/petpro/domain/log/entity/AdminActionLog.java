package com.petpro.domain.log.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 운영자 행위 로그 엔티티
 */
@Entity
@Table(name = "admin_action_logs")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class AdminActionLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "admin_id", nullable = false)
    private Long adminId;

    @Enumerated(EnumType.STRING)
    @Column(name = "action_type", nullable = false, columnDefinition = "VARCHAR(50)")
    private AdminActionType actionType;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", nullable = false, columnDefinition = "VARCHAR(50)")
    private TargetType targetType;

    @Column(name = "target_id")
    private Long targetId;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "detail_json", columnDefinition = "json")
    private String detailJson;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent", length = 500)
    private String userAgent;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    public static AdminActionLog create(Long adminId, AdminActionType actionType,
                                         TargetType targetType, Long targetId,
                                         String description, String detailJson,
                                         String ipAddress, String userAgent) {
        return AdminActionLog.builder()
                .adminId(adminId)
                .actionType(actionType)
                .targetType(targetType)
                .targetId(targetId)
                .description(description)
                .detailJson(detailJson)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .createdAt(LocalDateTime.now())
                .build();
    }
}
