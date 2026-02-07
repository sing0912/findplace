package com.petpro.domain.coupon.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 쿠폰 유형 엔티티
 */
@Entity
@Table(name = "coupon_types")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class CouponType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 유형 코드 */
    @Column(nullable = false, unique = true, length = 50)
    private String code;

    /** 유형명 */
    @Column(nullable = false, length = 100)
    private String name;

    /** 설명 */
    @Column(columnDefinition = "TEXT")
    private String description;

    /** 활성화 여부 */
    @Column(nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
