package com.petpro.domain.coupon.dto;

import com.petpro.domain.coupon.entity.AutoIssueEvent;
import com.petpro.domain.coupon.entity.ConditionOperator;
import com.petpro.domain.coupon.entity.DiscountMethod;
import com.petpro.domain.coupon.entity.IssueType;
import jakarta.validation.constraints.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 쿠폰 요청 DTO
 */
public class CouponRequest {

    /**
     * 쿠폰 생성 요청
     */
    @Getter
    @Builder
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor
    public static class Create {
        @NotBlank(message = "쿠폰 코드는 필수입니다")
        @Size(max = 50, message = "쿠폰 코드는 50자 이하여야 합니다")
        private String code;

        @NotBlank(message = "쿠폰명은 필수입니다")
        @Size(max = 200, message = "쿠폰명은 200자 이하여야 합니다")
        private String name;

        private String description;

        private Long couponTypeId;

        @NotNull(message = "할인 방식은 필수입니다")
        private DiscountMethod discountMethod;

        @NotNull(message = "할인 값은 필수입니다")
        @DecimalMin(value = "0", inclusive = false, message = "할인 값은 0보다 커야 합니다")
        private BigDecimal discountValue;

        private BigDecimal maxDiscountAmount;

        @Builder.Default
        private IssueType issueType = IssueType.MANUAL;

        private AutoIssueEvent autoIssueEvent;

        @Min(value = 1, message = "최대 발급 수량은 1 이상이어야 합니다")
        private Integer maxIssueCount;

        @Min(value = 1, message = "사용자당 최대 발급 횟수는 1 이상이어야 합니다")
        @Builder.Default
        private Integer maxPerUser = 1;

        private LocalDate validStartDate;
        private LocalDate validEndDate;

        @Min(value = 1, message = "유효일수는 1일 이상이어야 합니다")
        private Integer validDays;

        @Builder.Default
        private Boolean isStackable = false;

        private List<ConditionCreate> conditions;
    }

    /**
     * 쿠폰 조건 생성 요청
     */
    @Getter
    @Builder
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor
    public static class ConditionCreate {
        @NotBlank(message = "조건 키는 필수입니다")
        private String conditionKey;

        @NotNull(message = "조건 연산자는 필수입니다")
        private ConditionOperator conditionOperator;

        @NotBlank(message = "조건 값은 필수입니다")
        private String conditionValue;
    }

    /**
     * 쿠폰 코드 등록 요청
     */
    @Getter
    @Builder
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor
    public static class RegisterCode {
        @NotBlank(message = "쿠폰 코드는 필수입니다")
        private String code;
    }

    /**
     * 쿠폰 발급 요청 (관리자)
     */
    @Getter
    @Builder
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor
    public static class Issue {
        @NotNull(message = "사용자 ID는 필수입니다")
        private Long userId;
    }

    /**
     * 일괄 발급 요청 (관리자)
     */
    @Getter
    @Builder
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor
    public static class BulkIssue {
        @NotEmpty(message = "사용자 ID 목록은 필수입니다")
        private List<Long> userIds;
    }
}
