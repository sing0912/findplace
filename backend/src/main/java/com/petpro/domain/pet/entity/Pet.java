package com.petpro.domain.pet.entity;

import com.petpro.domain.user.entity.User;
import com.petpro.global.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;

/**
 * 반려동물 엔티티
 *
 * 회원이 등록한 반려동물 정보를 관리합니다.
 * 여러 마리 등록이 가능하며, 사망한 반려동물은 추모관과 연동됩니다.
 */
@Entity
@Table(name = "pets", indexes = {
    @Index(name = "idx_pets_user_id", columnList = "user_id"),
    @Index(name = "idx_pets_species", columnList = "species"),
    @Index(name = "idx_pets_is_deceased", columnList = "is_deceased")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Pet extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 소유자 (회원) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** 반려동물 이름 */
    @Column(nullable = false, length = 100)
    private String name;

    /** 종류 */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Species species;

    /** 품종 */
    @Column(length = 100)
    private String breed;

    /** 몸무게 (kg) */
    @Column(precision = 5, scale = 2)
    private BigDecimal weight;

    /** 생년월일 */
    @Column(name = "birth_date")
    private LocalDate birthDate;

    /** 성별 */
    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private Gender gender;

    /** 중성화 여부 */
    @Column(name = "is_neutered")
    @Builder.Default
    private Boolean isNeutered = false;

    /** 예방접종 상태 */
    @Column(name = "vaccination_status", length = 200)
    private String vaccinationStatus;

    /** 알레르기 정보 */
    @Column(length = 500)
    private String allergies;

    /** 특이사항 */
    @Column(name = "special_notes", columnDefinition = "TEXT")
    private String specialNotes;

    /** 프로필 이미지 URL */
    @Column(name = "profile_image_url", length = 500)
    private String profileImageUrl;

    /** 메모 */
    @Column(columnDefinition = "TEXT")
    private String memo;

    /** 사망 여부 */
    @Column(name = "is_deceased")
    @Builder.Default
    private Boolean isDeceased = false;

    /** 사망일 */
    @Column(name = "deceased_at")
    private LocalDate deceasedAt;

    /** 삭제일 (Soft Delete) */
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    // ========== 비즈니스 메서드 ==========

    /**
     * 나이 계산 (년 단위)
     */
    public Integer getAge() {
        if (birthDate == null) {
            return null;
        }
        LocalDate endDate = isDeceased && deceasedAt != null ? deceasedAt : LocalDate.now();
        return Period.between(birthDate, endDate).getYears();
    }

    /**
     * 반려동물 정보 수정
     */
    public void update(String name, Species species, String breed, LocalDate birthDate,
                       Gender gender, Boolean isNeutered, String memo,
                       BigDecimal weight, String vaccinationStatus,
                       String allergies, String specialNotes) {
        this.name = name;
        this.species = species;
        this.breed = breed;
        this.birthDate = birthDate;
        this.gender = gender;
        this.isNeutered = isNeutered;
        this.memo = memo;
        this.weight = weight;
        this.vaccinationStatus = vaccinationStatus;
        this.allergies = allergies;
        this.specialNotes = specialNotes;
    }

    /**
     * 프로필 이미지 URL 변경
     */
    public void updateProfileImage(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    /**
     * 사망 처리
     */
    public void markAsDeceased(LocalDate deceasedAt) {
        this.isDeceased = true;
        this.deceasedAt = deceasedAt;
    }

    /**
     * 사망 취소 (잘못 처리한 경우)
     */
    public void cancelDeceased() {
        this.isDeceased = false;
        this.deceasedAt = null;
    }

    /**
     * 소프트 삭제
     */
    public void softDelete() {
        this.deletedAt = LocalDateTime.now();
    }

    /**
     * 삭제 여부 확인
     */
    public boolean isDeleted() {
        return this.deletedAt != null;
    }

    /**
     * 소유자 확인
     */
    public boolean isOwnedBy(Long userId) {
        return this.user != null && this.user.getId().equals(userId);
    }
}
