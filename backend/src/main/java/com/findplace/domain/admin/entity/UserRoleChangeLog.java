package com.findplace.domain.admin.entity;

import com.findplace.domain.user.entity.UserRole;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 사용자 역할 변경 이력 엔티티
 */
@Entity
@Table(name = "user_role_change_logs")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class UserRoleChangeLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "previous_role", nullable = false, length = 20)
    private UserRole previousRole;

    @Enumerated(EnumType.STRING)
    @Column(name = "new_role", nullable = false, length = 20)
    private UserRole newRole;

    @Column(name = "reason", columnDefinition = "TEXT")
    private String reason;

    @Column(name = "changed_by")
    private Long changedBy;

    @Column(name = "changed_at", nullable = false)
    private LocalDateTime changedAt;

    public static UserRoleChangeLog create(Long userId, UserRole previousRole,
                                            UserRole newRole, String reason, Long changedBy) {
        return UserRoleChangeLog.builder()
                .userId(userId)
                .previousRole(previousRole)
                .newRole(newRole)
                .reason(reason)
                .changedBy(changedBy)
                .changedAt(LocalDateTime.now())
                .build();
    }
}
