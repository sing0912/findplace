package com.petpro.domain.log.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 인구통계 스냅샷 엔티티
 */
@Entity
@Table(name = "user_demographics_snapshots",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "snapshot_date"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class UserDemographicsSnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "age_group", length = 10)
    private String ageGroup;

    @Column(name = "gender", length = 10)
    private String gender;

    @Column(name = "region_code", length = 20)
    private String regionCode;

    @Column(name = "snapshot_date", nullable = false)
    private LocalDate snapshotDate;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    public static UserDemographicsSnapshot create(Long userId, String ageGroup,
                                                    String gender, String regionCode,
                                                    LocalDate snapshotDate) {
        return UserDemographicsSnapshot.builder()
                .userId(userId)
                .ageGroup(ageGroup)
                .gender(gender)
                .regionCode(regionCode)
                .snapshotDate(snapshotDate)
                .createdAt(LocalDateTime.now())
                .build();
    }
}
