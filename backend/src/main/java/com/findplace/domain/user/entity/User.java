package com.findplace.domain.user.entity;

import com.findplace.global.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 사용자 엔티티
 *
 * 시스템의 모든 사용자 정보를 관리하는 핵심 엔티티
 * - 일반 사용자, 업체 관리자, 공급사 관리자, 시스템 관리자 등 모든 역할 포함
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

    /** 사용자 역할 (USER, COMPANY_ADMIN, SUPPLIER_ADMIN, ADMIN, SUPER_ADMIN) */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private UserRole role = UserRole.USER;

    /** 계정 상태 (ACTIVE, INACTIVE, SUSPENDED, DELETED) */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private UserStatus status = UserStatus.ACTIVE;

    /** 프로필 이미지 URL (MinIO 저장 경로) */
    @Column(name = "profile_image_url")
    private String profileImageUrl;

    /** 마지막 로그인 일시 */
    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

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
