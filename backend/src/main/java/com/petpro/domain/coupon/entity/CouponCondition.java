package com.petpro.domain.coupon.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 쿠폰 조건 엔티티 (EAV 패턴)
 */
@Entity
@Table(name = "coupon_conditions", indexes = {
    @Index(name = "idx_coupon_conditions_coupon", columnList = "coupon_id")
})
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class CouponCondition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 쿠폰 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coupon_id", nullable = false)
    private Coupon coupon;

    /** 조건 키 */
    @Column(nullable = false, length = 50)
    private String conditionKey;

    /** 조건 연산자 */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ConditionOperator conditionOperator;

    /** 조건 값 */
    @Column(nullable = false, length = 500)
    private String conditionValue;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    /**
     * 조건 표시 텍스트 생성
     */
    public String getDisplayText() {
        return switch (conditionKey) {
            case "MIN_ORDER_AMOUNT" -> String.format("%,d원 이상 구매 시", Integer.parseInt(conditionValue));
            case "MIN_QUANTITY" -> String.format("%s개 이상 구매 시", conditionValue);
            case "CATEGORY" -> String.format("%s 카테고리에만 적용", conditionValue);
            case "FIRST_ORDER" -> "첫 주문에만 적용";
            case "USER_ROLE" -> String.format("%s 등급에만 적용", conditionValue);
            default -> String.format("%s %s %s", conditionKey, conditionOperator, conditionValue);
        };
    }
}
