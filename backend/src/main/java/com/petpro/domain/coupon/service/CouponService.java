package com.petpro.domain.coupon.service;

import com.petpro.domain.coupon.dto.CouponRequest;
import com.petpro.domain.coupon.dto.CouponResponse;
import com.petpro.domain.coupon.entity.*;
import com.petpro.domain.coupon.repository.*;
import com.petpro.global.exception.BusinessException;
import com.petpro.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 쿠폰 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(transactionManager = "couponTransactionManager", readOnly = true)
public class CouponService {

    private final CouponRepository couponRepository;
    private final CouponTypeRepository couponTypeRepository;
    private final UserCouponRepository userCouponRepository;
    private final CouponUsageHistoryRepository usageHistoryRepository;

    // ========== 쿠폰 유형 조회 ==========

    public List<CouponResponse.TypeItem> getAllCouponTypes() {
        return couponTypeRepository.findByIsActiveTrue().stream()
                .map(CouponResponse.TypeItem::from)
                .collect(Collectors.toList());
    }

    // ========== 쿠폰 생성/관리 ==========

    @Transactional(transactionManager = "couponTransactionManager")
    public CouponResponse.Detail createCoupon(CouponRequest.Create request) {
        // 코드 중복 체크
        if (couponRepository.findByCode(request.getCode()).isPresent()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }

        CouponType couponType = null;
        if (request.getCouponTypeId() != null) {
            couponType = couponTypeRepository.findById(request.getCouponTypeId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.COUPON_NOT_FOUND));
        }

        Coupon coupon = Coupon.builder()
                .code(request.getCode())
                .name(request.getName())
                .description(request.getDescription())
                .couponType(couponType)
                .discountType(request.getDiscountType())
                .discountValue(request.getDiscountValue())
                .maxDiscountAmount(request.getMaxDiscountAmount())
                .issueType(request.getIssueType())
                .autoIssueEvent(request.getAutoIssueEvent())
                .maxIssueCount(request.getMaxIssueCount())
                .maxPerUser(request.getMaxPerUser())
                .validStartDate(request.getValidStartDate())
                .validEndDate(request.getValidEndDate())
                .validDays(request.getValidDays())
                .isStackable(request.getIsStackable())
                .build();

        // 조건 추가
        if (request.getConditions() != null) {
            for (CouponRequest.ConditionCreate condReq : request.getConditions()) {
                CouponCondition condition = CouponCondition.builder()
                        .conditionKey(condReq.getConditionKey())
                        .conditionOperator(condReq.getConditionOperator())
                        .conditionValue(condReq.getConditionValue())
                        .build();
                coupon.addCondition(condition);
            }
        }

        coupon = couponRepository.save(coupon);
        log.info("Coupon created: code={}, name={}", coupon.getCode(), coupon.getName());

        return CouponResponse.Detail.from(coupon);
    }

    public CouponResponse.Detail getCouponById(Long id) {
        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.COUPON_NOT_FOUND));
        return CouponResponse.Detail.from(coupon);
    }

    public Page<CouponResponse.ListItem> getCoupons(String keyword, Boolean isActive, int page, int size) {
        PageRequest pageable = PageRequest.of(page, size);
        Page<Coupon> coupons = couponRepository.findBySearchConditions(keyword, isActive, pageable);
        return coupons.map(CouponResponse.ListItem::from);
    }

    // ========== 쿠폰 발급 ==========

    @Transactional(transactionManager = "couponTransactionManager")
    public CouponResponse.MyCoupon issueCoupon(Long couponId, Long userId) {
        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COUPON_NOT_FOUND));

        if (!coupon.canIssue()) {
            throw new BusinessException(ErrorCode.COUPON_NOT_USABLE);
        }

        // 사용자별 발급 횟수 체크
        int issuedThisYear = userCouponRepository.countUserCouponIssuedThisYear(
                userId, couponId, LocalDateTime.now());
        if (issuedThisYear >= coupon.getMaxPerUser()) {
            throw new BusinessException(ErrorCode.COUPON_ALREADY_ISSUED);
        }

        // 유효기간 계산
        LocalDateTime expiredAt;
        if (coupon.getValidDays() != null) {
            expiredAt = LocalDateTime.now().plusDays(coupon.getValidDays()).withHour(23).withMinute(59).withSecond(59);
        } else if (coupon.getValidEndDate() != null) {
            expiredAt = coupon.getValidEndDate().atTime(23, 59, 59);
        } else {
            expiredAt = LocalDateTime.now().plusYears(1);
        }

        UserCoupon userCoupon = UserCoupon.builder()
                .coupon(coupon)
                .userId(userId)
                .issuedAt(LocalDateTime.now())
                .expiredAt(expiredAt)
                .build();

        userCoupon = userCouponRepository.save(userCoupon);
        coupon.incrementIssuedCount();
        couponRepository.save(coupon);

        log.info("Coupon issued: couponId={}, userId={}", couponId, userId);

        return CouponResponse.MyCoupon.from(userCoupon);
    }

    @Transactional(transactionManager = "couponTransactionManager")
    public CouponResponse.MyCoupon registerCouponCode(String code, Long userId) {
        Coupon coupon = couponRepository.findByCode(code)
                .orElseThrow(() -> new BusinessException(ErrorCode.COUPON_NOT_FOUND));

        if (coupon.getIssueType() != IssueType.CODE) {
            throw new BusinessException(ErrorCode.COUPON_NOT_USABLE);
        }

        return issueCoupon(coupon.getId(), userId);
    }

    // ========== 내 쿠폰 조회 ==========

    public List<CouponResponse.MyCoupon> getMyCoupons(Long userId) {
        List<UserCoupon> userCoupons = userCouponRepository.findByUserIdOrderByIssuedAtDesc(
                userId, PageRequest.of(0, 100)).getContent();
        return userCoupons.stream()
                .map(CouponResponse.MyCoupon::from)
                .collect(Collectors.toList());
    }

    public List<CouponResponse.MyCoupon> getMyAvailableCoupons(Long userId) {
        List<UserCoupon> userCoupons = userCouponRepository.findAvailableCoupons(
                userId, LocalDateTime.now());
        return userCoupons.stream()
                .map(CouponResponse.MyCoupon::from)
                .collect(Collectors.toList());
    }

    // ========== 배치 작업 ==========

    @Transactional(transactionManager = "couponTransactionManager")
    public int expireCoupons() {
        int count = userCouponRepository.expireOldCoupons(LocalDateTime.now());
        log.info("Expired {} coupons", count);
        return count;
    }
}
