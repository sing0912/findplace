package com.petpro.domain.coupon.repository;

import com.petpro.domain.coupon.entity.AutoIssueEvent;
import com.petpro.domain.coupon.entity.Coupon;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 쿠폰 레포지토리
 */
@Repository
public interface CouponRepository extends JpaRepository<Coupon, Long> {

    Optional<Coupon> findByCode(String code);

    List<Coupon> findByIsActiveTrue();

    Page<Coupon> findByIsActiveTrue(Pageable pageable);

    /**
     * 자동 발급 이벤트로 쿠폰 조회
     */
    List<Coupon> findByAutoIssueEventAndIsActiveTrue(AutoIssueEvent event);

    /**
     * 검색 조건으로 쿠폰 조회
     */
    @Query("""
            SELECT c FROM Coupon c
            WHERE (:keyword IS NULL OR c.name LIKE %:keyword% OR c.code LIKE %:keyword%)
              AND (:isActive IS NULL OR c.isActive = :isActive)
            ORDER BY c.createdAt DESC
            """)
    Page<Coupon> findBySearchConditions(
            @Param("keyword") String keyword,
            @Param("isActive") Boolean isActive,
            Pageable pageable
    );
}
