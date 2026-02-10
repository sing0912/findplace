package com.petpro.domain.auth.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * SMS 인증 요청 엔티티
 *
 * 아이디 찾기, 비밀번호 재설정 시 SMS 인증 요청을 저장
 * - 인증번호는 3분 후 만료
 * - 인증 완료 시 verified = true
 */
@Entity
@Table(name = "verification_requests")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class VerificationRequest {

    @Id
    @Column(length = 36)
    private String id;

    /** 인증 유형 */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private VerificationType type;

    /** 인증 요청 전화번호 */
    @Column(nullable = false, length = 20)
    private String phone;

    /** 인증번호 (6자리) */
    @Column(nullable = false, length = 10)
    private String code;

    /** 사용자 ID (찾은 사용자) */
    @Column(name = "user_id")
    private Long userId;

    /** 이름 (아이디 찾기용) */
    @Column(length = 100)
    private String name;

    /** 이메일 (비밀번호 재설정용) */
    @Column(length = 255)
    private String email;

    /** 만료 시간 */
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    /** 인증 완료 여부 */
    @Column(nullable = false)
    @Builder.Default
    private boolean verified = false;

    /** 비밀번호 재설정 토큰 (인증 완료 시 발급) */
    @Column(name = "reset_token", length = 36)
    private String resetToken;

    /** 인증 시도 횟수 (최대 5회) */
    @Column(name = "attempt_count", nullable = false)
    @Builder.Default
    private int attemptCount = 0;

    /** 최대 인증 시도 횟수 */
    private static final transient int MAX_ATTEMPTS = 5;

    /** 생성 시간 */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (this.id == null) {
            this.id = UUID.randomUUID().toString();
        }
        this.createdAt = LocalDateTime.now();
        if (this.expiresAt == null) {
            this.expiresAt = this.createdAt.plusMinutes(3);
        }
    }

    /**
     * 인증 완료 처리
     */
    public void verify() {
        this.verified = true;
    }

    /**
     * 인증번호가 만료되었는지 확인
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(this.expiresAt);
    }

    /**
     * 인증번호가 일치하는지 확인 (시도 횟수 증가)
     */
    public boolean matchCode(String inputCode) {
        this.attemptCount++;
        return this.code.equals(inputCode);
    }

    /**
     * 최대 시도 횟수를 초과했는지 확인
     */
    public boolean isMaxAttemptsExceeded() {
        return this.attemptCount >= MAX_ATTEMPTS;
    }

    /**
     * 인증번호 재발송 (새 코드, 새 만료시간)
     */
    public void resend(String newCode) {
        this.code = newCode;
        this.expiresAt = LocalDateTime.now().plusMinutes(3);
        this.verified = false;
    }

    /**
     * 비밀번호 재설정 토큰 발급
     */
    public void issueResetToken() {
        this.resetToken = UUID.randomUUID().toString();
    }
}
