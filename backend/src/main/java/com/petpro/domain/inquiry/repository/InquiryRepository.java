package com.petpro.domain.inquiry.repository;

import com.petpro.domain.inquiry.entity.Inquiry;
import com.petpro.domain.inquiry.entity.InquiryStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 문의 Repository
 */
@Repository
public interface InquiryRepository extends JpaRepository<Inquiry, Long> {

    /**
     * 사용자의 문의 목록 조회 (최신순)
     */
    Page<Inquiry> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    /**
     * 사용자의 문의 상세 조회
     */
    @Query("SELECT i FROM Inquiry i LEFT JOIN FETCH i.answer WHERE i.id = :id")
    Optional<Inquiry> findByIdWithAnswer(@Param("id") Long id);

    /**
     * 상태별 문의 목록 조회 (관리자용)
     */
    Page<Inquiry> findByStatusOrderByCreatedAtDesc(InquiryStatus status, Pageable pageable);

    /**
     * 전체 문의 목록 조회 (관리자용, 최신순)
     */
    Page<Inquiry> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
