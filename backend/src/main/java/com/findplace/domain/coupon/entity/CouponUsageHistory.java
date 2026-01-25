package com.findplace.domain.coupon.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 쿠폰 사용 이력 엔티티
 */
@Entity
@Table(name = "coupon_usage_histories")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class CouponUsageHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 사용자 쿠폰 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_coupon_id", nullable = false)
    private UserCoupon userCoupon;

    /** 사용자 ID (Main DB 참조) */
    @Column(nullable = false)
    private Long userId;

    /** 주문 ID (Main DB 참조) */
    @Column(nullable = false)
    private Long orderId;

    /** 할인 금액 */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal discountAmount;

    /** 사용 일시 */
    @Column(nullable = false)
    private LocalDateTime usedAt;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
