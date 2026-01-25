package com.findplace.domain.coupon.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 사용자 보유 쿠폰 엔티티
 */
@Entity
@Table(name = "user_coupons", indexes = {
    @Index(name = "idx_user_coupons_user", columnList = "userId"),
    @Index(name = "idx_user_coupons_coupon", columnList = "coupon_id"),
    @Index(name = "idx_user_coupons_status", columnList = "status")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class UserCoupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 쿠폰 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coupon_id", nullable = false)
    private Coupon coupon;

    /** 사용자 ID (Main DB 참조) */
    @Column(nullable = false)
    private Long userId;

    /** 상태 */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private CouponStatus status = CouponStatus.AVAILABLE;

    /** 발급 일시 */
    @Column(nullable = false)
    private LocalDateTime issuedAt;

    /** 만료 일시 */
    @Column(nullable = false)
    private LocalDateTime expiredAt;

    /** 사용 일시 */
    private LocalDateTime usedAt;

    /** 회수 일시 */
    private LocalDateTime revokedAt;

    /** 사용한 주문 ID (Main DB 참조) */
    private Long orderId;

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

    // ========== 비즈니스 메서드 ==========

    /**
     * 사용 가능 여부 확인
     */
    public boolean isAvailable() {
        return status == CouponStatus.AVAILABLE
                && LocalDateTime.now().isBefore(expiredAt);
    }

    /**
     * 쿠폰 사용
     */
    public void use(Long orderId) {
        this.status = CouponStatus.USED;
        this.usedAt = LocalDateTime.now();
        this.orderId = orderId;
    }

    /**
     * 쿠폰 만료 처리
     */
    public void expire() {
        this.status = CouponStatus.EXPIRED;
    }

    /**
     * 쿠폰 회수
     */
    public void revoke() {
        this.status = CouponStatus.REVOKED;
        this.revokedAt = LocalDateTime.now();
    }

    /**
     * 사용 취소 (환불 시)
     */
    public void cancelUse() {
        this.status = CouponStatus.AVAILABLE;
        this.usedAt = null;
        this.orderId = null;
    }

    /**
     * 만료까지 남은 일수
     */
    public long getDaysUntilExpiry() {
        return java.time.temporal.ChronoUnit.DAYS.between(
                LocalDateTime.now(), expiredAt);
    }
}
