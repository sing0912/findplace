package com.petpro.domain.inquiry.repository;

import com.petpro.domain.inquiry.entity.InquiryAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 문의 답변 Repository
 */
@Repository
public interface InquiryAnswerRepository extends JpaRepository<InquiryAnswer, Long> {

    /**
     * 문의 ID로 답변 조회
     */
    Optional<InquiryAnswer> findByInquiryId(Long inquiryId);
}
