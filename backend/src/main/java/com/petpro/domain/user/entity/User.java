package com.petpro.domain.user.entity;

import com.petpro.domain.auth.entity.AuthProvider;
import com.petpro.global.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 사용자 엔티티
 *
 * PetPro 플랫폼의 모든 사용자 정보를 관리하는 핵심 엔티티
 * - 반려인(CUSTOMER), 펫시터(PARTNER), 관리자(ADMIN/SUPER_ADMIN) 역할 포함
 * - Soft Delete 지원 (실제 삭제 대신 삭제 플래그 사용)
 */
@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class User extends BaseEntity {

    /** 사용자 고유 식별자 (자동 증가) */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 이메일 (로그인 ID로 사용, 중복 불가) */
    @Column(nullable = false, unique = true)
    private String email;

    /** 암호화된 비밀번호 */
    @Column(nullable = false)
    private String password;

    /** 사용자 이름 */
    @Column(nullable = false, length = 100)
    private String name;

    /** 전화번호 */
    @Column(length = 20)
    private String phone;

    /** 사용자 역할 (CUSTOMER, PARTNER, ADMIN, SUPER_ADMIN) */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private UserRole role = UserRole.CUSTOMER;

    /** 계정 상태 (ACTIVE, INACTIVE, SUSPENDED, DELETED) */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private UserStatus status = UserStatus.ACTIVE;

    /** 프로필 이미지 URL (MinIO 저장 경로) */
    @Column(name = "profile_image_url")
    private String profileImageUrl;

    /** 닉네임 */
    @Column(length = 50)
    private String nickname;

    /** 인증 제공자 (EMAIL, KAKAO, NAVER, GOOGLE) */
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    @Builder.Default
    private AuthProvider provider = AuthProvider.EMAIL;

    /** 소셜 로그인 제공자 ID */
    @Column(name = "provider_id")
    private String providerId;

    /** 이용약관 동의 여부 */
    @Column(name = "agree_terms")
    @Builder.Default
    private boolean agreeTerms = false;

    /** 개인정보처리방침 동의 여부 */
    @Column(name = "agree_privacy")
    @Builder.Default
    private boolean agreePrivacy = false;

    /** 마케팅 정보 수신 동의 여부 */
    @Column(name = "agree_marketing")
    @Builder.Default
    private boolean agreeMarketing = false;

    /** 마지막 로그인 일시 */
    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    /** 생년월일 */
    @Column(name = "birth_date")
    private java.time.LocalDate birthDate;

    /** 기본 주소 */
    @Column(length = 500)
    private String address;

    /** 상세 주소 */
    @Column(name = "address_detail", length = 200)
    private String addressDetail;

    /** 우편번호 */
    @Column(name = "zip_code", length = 10)
    private String zipCode;

    /** 위도 */
    @Column(precision = 10, scale = 7)
    private java.math.BigDecimal latitude;

    /** 경도 */
    @Column(precision = 10, scale = 7)
    private java.math.BigDecimal longitude;

    /** 현재 유효한 Refresh Token (1회용 로테이션) */
    @Column(name = "refresh_token", length = 500)
    private String refreshToken;

    /** 로그인 실패 횟수 */
    @Column(name = "login_fail_count")
    @Builder.Default
    private int loginFailCount = 0;

    /** 계정 잠금 일시 (5회 실패 시 30분 잠금) */
    @Column(name = "locked_at")
    private LocalDateTime lockedAt;

    /** 삭제 일시 (Soft Delete) */
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    /** 삭제 처리한 관리자 ID */
    @Column(name = "deleted_by")
    private Long deletedBy;

    // ========== 비즈니스 메서드 ==========

    /**
     * 프로필 정보 수정
     *
     * @param name 변경할 이름
     * @param phone 변경할 전화번호
     * @param profileImageUrl 변경할 프로필 이미지 URL
     */
    public void updateProfile(String name, String phone, String profileImageUrl) {
        this.name = name;
        this.phone = phone;
        this.profileImageUrl = profileImageUrl;
    }

    /**
     * 닉네임 수정
     *
     * @param nickname 변경할 닉네임
     */
    public void updateNickname(String nickname) {
        this.nickname = nickname;
    }

    /**
     * 프로필 이미지 수정
     *
     * @param profileImageUrl 변경할 프로필 이미지 URL
     */
    public void updateProfileImage(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    /**
     * 소셜 로그인 여부 확인
     *
     * @return 소셜 로그인 사용자이면 true
     */
    public boolean isSocialUser() {
        return this.provider != null && this.provider != AuthProvider.EMAIL;
    }

    /**
     * 비밀번호 변경
     *
     * @param encodedPassword 암호화된 새 비밀번호
     */
    public void updatePassword(String encodedPassword) {
        this.password = encodedPassword;
    }

    /**
     * 마지막 로그인 시간 갱신
     */
    public void updateLastLogin() {
        this.lastLoginAt = LocalDateTime.now();
    }

    /**
     * OAuth 정보 업데이트
     *
     * @param provider OAuth 제공자
     * @param providerId OAuth 제공자에서의 사용자 ID
     */
    public void updateOAuthInfo(AuthProvider provider, String providerId) {
        this.provider = provider;
        this.providerId = providerId;
    }

    /**
     * 사용자 역할 변경
     *
     * @param role 새로운 역할
     */
    public void changeRole(UserRole role) {
        this.role = role;
    }

    /**
     * 계정 활성화
     */
    public void activate() {
        this.status = UserStatus.ACTIVE;
    }

    /**
     * 계정 정지
     */
    public void suspend() {
        this.status = UserStatus.SUSPENDED;
    }

    /**
     * 계정 휴면 처리
     */
    public void inactivate() {
        this.status = UserStatus.INACTIVE;
    }

    /**
     * 계정 소프트 삭제 (논리적 삭제)
     *
     * @param deletedBy 삭제 처리한 관리자 ID
     */
    public void softDelete(Long deletedBy) {
        this.status = UserStatus.DELETED;
        this.deletedAt = LocalDateTime.now();
        this.deletedBy = deletedBy;
    }

    /**
     * 주소 정보 수정
     *
     * @param address 기본 주소
     * @param addressDetail 상세 주소
     * @param zipCode 우편번호
     * @param latitude 위도
     * @param longitude 경도
     */
    public void updateAddress(String address, String addressDetail, String zipCode,
                              java.math.BigDecimal latitude, java.math.BigDecimal longitude) {
        this.address = address;
        this.addressDetail = addressDetail;
        this.zipCode = zipCode;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    /**
     * 생년월일 수정
     *
     * @param birthDate 생년월일
     */
    public void updateBirthDate(java.time.LocalDate birthDate) {
        this.birthDate = birthDate;
    }

    /**
     * Refresh Token 저장 (로테이션 추적)
     *
     * @param refreshToken 새로 발급된 Refresh Token
     */
    public void updateRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    /**
     * Refresh Token 무효화
     */
    public void invalidateRefreshToken() {
        this.refreshToken = null;
    }

    /**
     * 로그인 실패 기록
     * 5회 실패 시 계정 잠금 (30분)
     */
    public void recordLoginFailure() {
        this.loginFailCount++;
        if (this.loginFailCount >= 5) {
            this.lockedAt = LocalDateTime.now();
        }
    }

    /**
     * 로그인 성공 시 실패 횟수 초기화
     */
    public void resetLoginFailure() {
        this.loginFailCount = 0;
        this.lockedAt = null;
    }

    /**
     * 계정 잠금 여부 확인
     * 잠금 후 30분이 지나면 자동 해제
     *
     * @return 잠금 상태이면 true
     */
    public boolean isLocked() {
        if (this.lockedAt == null) {
            return false;
        }
        if (this.lockedAt.plusMinutes(30).isBefore(LocalDateTime.now())) {
            // 30분 경과 → 자동 해제
            this.loginFailCount = 0;
            this.lockedAt = null;
            return false;
        }
        return true;
    }

    /**
     * 활성 계정 여부 확인
     *
     * @return 활성 상태이면 true
     */
    public boolean isActive() {
        return this.status == UserStatus.ACTIVE;
    }

    /**
     * 삭제된 계정 여부 확인
     *
     * @return 삭제되었으면 true
     */
    public boolean isDeleted() {
        return this.status == UserStatus.DELETED || this.deletedAt != null;
    }
}
