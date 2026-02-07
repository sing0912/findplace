package com.petpro.domain.coupon.repository;

import com.petpro.domain.coupon.entity.CouponStatus;
import com.petpro.domain.coupon.entity.UserCoupon;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 사용자 쿠폰 레포지토리
 */
@Repository
public interface UserCouponRepository extends JpaRepository<UserCoupon, Long> {

    List<UserCoupon> findByUserId(Long userId);

    List<UserCoupon> findByUserIdAndStatus(Long userId, CouponStatus status);

    Page<UserCoupon> findByUserIdOrderByIssuedAtDesc(Long userId, Pageable pageable);

    /**
     * 사용 가능한 쿠폰 조회
     */
    @Query("""
            SELECT uc FROM UserCoupon uc
            WHERE uc.userId = :userId
              AND uc.status = 'AVAILABLE'
              AND uc.expiredAt > :now
            ORDER BY uc.expiredAt ASC
            """)
    List<UserCoupon> findAvailableCoupons(
            @Param("userId") Long userId,
            @Param("now") LocalDateTime now
    );

    /**
     * 사용자의 특정 쿠폰 발급 횟수 조회 (올해)
     */
    @Query("""
            SELECT COUNT(uc) FROM UserCoupon uc
            WHERE uc.userId = :userId
              AND uc.coupon.id = :couponId
              AND YEAR(uc.issuedAt) = YEAR(:now)
            """)
    int countUserCouponIssuedThisYear(
            @Param("userId") Long userId,
            @Param("couponId") Long couponId,
            @Param("now") LocalDateTime now
    );

    /**
     * 만료된 쿠폰 일괄 상태 변경
     */
    @Modifying
    @Query("""
            UPDATE UserCoupon uc
            SET uc.status = 'EXPIRED', uc.updatedAt = :now
            WHERE uc.status = 'AVAILABLE'
              AND uc.expiredAt < :now
            """)
    int expireOldCoupons(@Param("now") LocalDateTime now);

    /**
     * 사용자의 특정 쿠폰 보유 여부
     */
    boolean existsByUserIdAndCouponIdAndStatusIn(Long userId, Long couponId, List<CouponStatus> statuses);

    /**
     * 특정 기간 내 사용자가 쿠폰을 발급받았는지 확인
     */
    boolean existsByUserIdAndCouponIdAndIssuedAtBetween(
            Long userId, Long couponId, LocalDateTime start, LocalDateTime end);

    /**
     * 만료 대상 쿠폰 조회 (상태가 AVAILABLE이고 만료일이 지난 쿠폰)
     */
    List<UserCoupon> findByStatusAndExpiredAtBefore(CouponStatus status, LocalDateTime expiredAt);
}
