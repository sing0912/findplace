package com.findplace.domain.region.entity;

import com.findplace.global.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

/**
 * 지역 코드 엔티티
 *
 * 행정안전부의 지방자치단체 코드를 관리합니다.
 * 장례식장 데이터 필터링 및 지역별 통계에 활용됩니다.
 */
@Entity
@Table(name = "region_codes", indexes = {
    @Index(name = "idx_region_codes_parent", columnList = "parent_code"),
    @Index(name = "idx_region_codes_type", columnList = "type"),
    @Index(name = "idx_region_codes_active", columnList = "is_active")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class RegionCode extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 행정구역 코드 (7자리) */
    @Column(nullable = false, unique = true, length = 20)
    private String code;

    /** 지역명 */
    @Column(nullable = false, length = 100)
    private String name;

    /** 지역 유형 (METRO: 광역시/도, CITY: 시/군/구) */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RegionType type;

    /** 상위 지역 코드 (광역시/도의 경우 null) */
    @Column(name = "parent_code", length = 20)
    private String parentCode;

    /** 정렬 순서 */
    @Column(name = "sort_order")
    @Builder.Default
    private Integer sortOrder = 0;

    /** 활성화 상태 */
    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    // ========== 비즈니스 메서드 ==========

    /**
     * 지역 정보 수정
     */
    public void update(String name, Integer sortOrder) {
        this.name = name;
        this.sortOrder = sortOrder;
    }

    /**
     * 활성화 상태 변경
     */
    public void changeActiveStatus(boolean isActive) {
        this.isActive = isActive;
    }

    /**
     * 광역시/도 여부 확인
     */
    public boolean isMetro() {
        return this.type == RegionType.METRO;
    }

    /**
     * 시/군/구 여부 확인
     */
    public boolean isCity() {
        return this.type == RegionType.CITY;
    }
}
