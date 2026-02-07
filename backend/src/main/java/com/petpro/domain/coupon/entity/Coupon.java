package com.petpro.domain.coupon.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 쿠폰 마스터 엔티티
 */
@Entity
@Table(name = "coupons", indexes = {
    @Index(name = "idx_coupons_type", columnList = "couponTypeId"),
    @Index(name = "idx_coupons_issue_type", columnList = "issueType"),
    @Index(name = "idx_coupons_active", columnList = "isActive")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Coupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ========== 기본 정보 ==========

    /** 쿠폰 코드 */
    @Column(nullable = false, unique = true, length = 50)
    private String code;

    /** 쿠폰명 */
    @Column(nullable = false, length = 200)
    private String name;

    /** 설명 */
    @Column(columnDefinition = "TEXT")
    private String description;

    /** 쿠폰 유형 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coupon_type_id")
    private CouponType couponType;

    // ========== 할인 설정 ==========

    /** 할인 방식 */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DiscountMethod discountMethod;

    /** 할인값 (금액 또는 퍼센트) */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal discountValue;

    /** 최대 할인금액 (정률 할인 시) */
    @Column(precision = 10, scale = 2)
    private BigDecimal maxDiscountAmount;

    // ========== 발급 설정 ==========

    /** 발급 유형 */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private IssueType issueType = IssueType.MANUAL;

    /** 자동 발급 이벤트 */
    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private AutoIssueEvent autoIssueEvent;

    /** 최대 발급 수량 */
    private Integer maxIssueCount;

    /** 발급된 수량 */
    @Column(nullable = false)
    @Builder.Default
    private Integer issuedCount = 0;

    /** 사용자당 최대 발급 횟수 */
    @Column(nullable = false)
    @Builder.Default
    private Integer maxPerUser = 1;

    // ========== 유효 기간 ==========

    /** 유효 시작일 */
    private LocalDate validStartDate;

    /** 유효 종료일 */
    private LocalDate validEndDate;

    /** 발급일로부터 유효일수 */
    private Integer validDays;

    // ========== 옵션 ==========

    /** 중복 사용 가능 여부 */
    @Column(nullable = false)
    @Builder.Default
    private Boolean isStackable = false;

    /** 활성화 여부 */
    @Column(nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    // ========== 조건 ==========

    @OneToMany(mappedBy = "coupon", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<CouponCondition> conditions = new ArrayList<>();

    // ========== Audit ==========

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
     * 발급 가능 여부 확인
     */
    public boolean canIssue() {
        if (!isActive) return false;
        if (maxIssueCount != null && issuedCount >= maxIssueCount) return false;
        if (validEndDate != null && LocalDate.now().isAfter(validEndDate)) return false;
        return true;
    }

    /**
     * 발급 카운트 증가
     */
    public void incrementIssuedCount() {
        this.issuedCount++;
    }

    /**
     * 조건 추가
     */
    public void addCondition(CouponCondition condition) {
        conditions.add(condition);
        condition.setCoupon(this);
    }

    /**
     * 조건 제거
     */
    public void removeCondition(CouponCondition condition) {
        conditions.remove(condition);
        condition.setCoupon(null);
    }

    /**
     * 활성화
     */
    public void activate() {
        this.isActive = true;
    }

    /**
     * 비활성화
     */
    public void deactivate() {
        this.isActive = false;
    }

    /**
     * 할인 금액 계산
     */
    public BigDecimal calculateDiscount(BigDecimal orderAmount) {
        BigDecimal discount;

        switch (discountMethod) {
            case FIXED:
                discount = discountValue;
                break;
            case PERCENT:
                discount = orderAmount.multiply(discountValue).divide(BigDecimal.valueOf(100));
                if (maxDiscountAmount != null) {
                    discount = discount.min(maxDiscountAmount);
                }
                break;
            case FREE:
                discount = orderAmount;
                break;
            default:
                discount = BigDecimal.ZERO;
        }

        return discount;
    }

    /**
     * 할인 텍스트 생성
     */
    public String getDiscountText() {
        return switch (discountMethod) {
            case FIXED -> String.format("%,d원 할인", discountValue.intValue());
            case PERCENT -> String.format("%d%% 할인", discountValue.intValue());
            case FREE -> "무료";
        };
    }
}
