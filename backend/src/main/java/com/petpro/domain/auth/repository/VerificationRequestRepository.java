package com.petpro.domain.auth.repository;

import com.petpro.domain.auth.entity.VerificationRequest;
import com.petpro.domain.auth.entity.VerificationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * SMS 인증 요청 Repository
 */
@Repository
public interface VerificationRequestRepository extends JpaRepository<VerificationRequest, String> {

    /**
     * 전화번호와 인증 유형으로 최근 인증 요청 조회
     */
    Optional<VerificationRequest> findTopByPhoneAndTypeOrderByCreatedAtDesc(String phone, VerificationType type);

    /**
     * 전화번호와 인증 유형으로 인증 완료된 요청 조회
     */
    Optional<VerificationRequest> findByPhoneAndTypeAndVerifiedTrue(String phone, VerificationType type);

    /**
     * 비밀번호 재설정 토큰으로 조회
     */
    Optional<VerificationRequest> findByResetTokenAndVerifiedTrue(String resetToken);

    /**
     * 만료된 인증 요청 삭제 (배치용)
     */
    void deleteByExpiresAtBefore(LocalDateTime dateTime);
}
