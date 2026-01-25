package com.findplace.domain.coupon.repository;

import com.findplace.domain.coupon.entity.CouponType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 쿠폰 유형 레포지토리
 */
@Repository
public interface CouponTypeRepository extends JpaRepository<CouponType, Long> {

    Optional<CouponType> findByCode(String code);

    List<CouponType> findByIsActiveTrue();
}
