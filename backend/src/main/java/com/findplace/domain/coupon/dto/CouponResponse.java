package com.findplace.domain.coupon.dto;

import com.findplace.domain.coupon.entity.*;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 쿠폰 응답 DTO
 */
public class CouponResponse {

    /**
     * 쿠폰 유형 응답
     */
    @Getter
    @Builder
    public static class TypeItem {
        private Long id;
        private String code;
        private String name;
        private String description;

        public static TypeItem from(CouponType type) {
            return TypeItem.builder()
                    .id(type.getId())
                    .code(type.getCode())
                    .name(type.getName())
                    .description(type.getDescription())
                    .build();
        }
    }

    /**
     * 쿠폰 상세 응답
     */
    @Getter
    @Builder
    public static class Detail {
        private Long id;
        private String code;
        private String name;
        private String description;
        private TypeItem couponType;
        private DiscountMethod discountMethod;
        private BigDecimal discountValue;
        private String discountText;
        private BigDecimal maxDiscountAmount;
        private IssueType issueType;
        private AutoIssueEvent autoIssueEvent;
        private Integer maxIssueCount;
        private Integer issuedCount;
        private Integer maxPerUser;
        private Integer validDays;
        private LocalDate validStartDate;
        private LocalDate validEndDate;
        private Boolean isStackable;
        private Boolean isActive;
        private List<ConditionItem> conditions;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public static Detail from(Coupon coupon) {
            return Detail.builder()
                    .id(coupon.getId())
                    .code(coupon.getCode())
                    .name(coupon.getName())
                    .description(coupon.getDescription())
                    .couponType(coupon.getCouponType() != null
                            ? TypeItem.from(coupon.getCouponType()) : null)
                    .discountMethod(coupon.getDiscountMethod())
                    .discountValue(coupon.getDiscountValue())
                    .discountText(coupon.getDiscountText())
                    .maxDiscountAmount(coupon.getMaxDiscountAmount())
                    .issueType(coupon.getIssueType())
                    .autoIssueEvent(coupon.getAutoIssueEvent())
                    .maxIssueCount(coupon.getMaxIssueCount())
                    .issuedCount(coupon.getIssuedCount())
                    .maxPerUser(coupon.getMaxPerUser())
                    .validDays(coupon.getValidDays())
                    .validStartDate(coupon.getValidStartDate())
                    .validEndDate(coupon.getValidEndDate())
                    .isStackable(coupon.getIsStackable())
                    .isActive(coupon.getIsActive())
                    .conditions(coupon.getConditions().stream()
                            .map(ConditionItem::from)
                            .collect(Collectors.toList()))
                    .createdAt(coupon.getCreatedAt())
                    .updatedAt(coupon.getUpdatedAt())
                    .build();
        }
    }

    /**
     * 쿠폰 목록 항목 응답
     */
    @Getter
    @Builder
    public static class ListItem {
        private Long id;
        private String code;
        private String name;
        private String discountText;
        private IssueType issueType;
        private Integer issuedCount;
        private Integer maxIssueCount;
        private LocalDate validEndDate;
        private Boolean isActive;

        public static ListItem from(Coupon coupon) {
            return ListItem.builder()
                    .id(coupon.getId())
                    .code(coupon.getCode())
                    .name(coupon.getName())
                    .discountText(coupon.getDiscountText())
                    .issueType(coupon.getIssueType())
                    .issuedCount(coupon.getIssuedCount())
                    .maxIssueCount(coupon.getMaxIssueCount())
                    .validEndDate(coupon.getValidEndDate())
                    .isActive(coupon.getIsActive())
                    .build();
        }
    }

    /**
     * 조건 응답
     */
    @Getter
    @Builder
    public static class ConditionItem {
        private String key;
        private ConditionOperator operator;
        private String value;
        private String displayText;

        public static ConditionItem from(CouponCondition condition) {
            return ConditionItem.builder()
                    .key(condition.getConditionKey())
                    .operator(condition.getConditionOperator())
                    .value(condition.getConditionValue())
                    .displayText(condition.getDisplayText())
                    .build();
        }
    }

    /**
     * 내 쿠폰 응답
     */
    @Getter
    @Builder
    public static class MyCoupon {
        private Long id;
        private CouponInfo coupon;
        private CouponStatus status;
        private LocalDateTime issuedAt;
        private LocalDateTime expiredAt;
        private Long daysUntilExpiry;
        private LocalDateTime usedAt;
        private Long orderId;

        public static MyCoupon from(UserCoupon userCoupon) {
            Coupon coupon = userCoupon.getCoupon();
            return MyCoupon.builder()
                    .id(userCoupon.getId())
                    .coupon(CouponInfo.builder()
                            .id(coupon.getId())
                            .code(coupon.getCode())
                            .name(coupon.getName())
                            .discountText(coupon.getDiscountText())
                            .conditions(coupon.getConditions().stream()
                                    .map(CouponCondition::getDisplayText)
                                    .collect(Collectors.toList()))
                            .build())
                    .status(userCoupon.getStatus())
                    .issuedAt(userCoupon.getIssuedAt())
                    .expiredAt(userCoupon.getExpiredAt())
                    .daysUntilExpiry(userCoupon.getDaysUntilExpiry())
                    .usedAt(userCoupon.getUsedAt())
                    .orderId(userCoupon.getOrderId())
                    .build();
        }
    }

    @Getter
    @Builder
    public static class CouponInfo {
        private Long id;
        private String code;
        private String name;
        private String discountText;
        private List<String> conditions;
    }
}
