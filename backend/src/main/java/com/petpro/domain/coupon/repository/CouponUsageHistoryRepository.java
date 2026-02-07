package com.petpro.domain.coupon.repository;

import com.petpro.domain.coupon.entity.CouponUsageHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 쿠폰 사용 이력 레포지토리
 */
@Repository
public interface CouponUsageHistoryRepository extends JpaRepository<CouponUsageHistory, Long> {

    List<CouponUsageHistory> findByUserId(Long userId);

    Page<CouponUsageHistory> findByUserIdOrderByUsedAtDesc(Long userId, Pageable pageable);

    Optional<CouponUsageHistory> findByOrderId(Long orderId);

    List<CouponUsageHistory> findByUserCouponId(Long userCouponId);
}
