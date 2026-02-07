package com.petpro.domain.funeralhome.entity;

import com.petpro.global.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 장례식장 엔티티
 *
 * 공공데이터포털 동물장묘업 API로부터 동기화된 장례식장 정보
 */
@Entity
@Table(name = "funeral_homes", indexes = {
    @Index(name = "idx_funeral_homes_loc_code", columnList = "locCode"),
    @Index(name = "idx_funeral_homes_is_active", columnList = "isActive"),
    @Index(name = "idx_funeral_homes_name", columnList = "name"),
    @Index(name = "idx_funeral_homes_location", columnList = "latitude, longitude"),
    @Index(name = "idx_funeral_homes_services", columnList = "hasCrematorium, hasFuneral, hasColumbarium")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class FuneralHome extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ========== 기본 정보 ==========

    /** 장례식장 이름 */
    @Column(nullable = false, length = 200)
    private String name;

    /** 도로명 주소 */
    @Column(length = 500)
    private String roadAddress;

    /** 지번 주소 */
    @Column(length = 500)
    private String lotAddress;

    /** 전화번호 */
    @Column(length = 50)
    private String phone;

    // ========== 지역 정보 ==========

    /** 지역 코드 (공공API) */
    @Column(length = 20)
    private String locCode;

    /** 지역명 */
    @Column(length = 100)
    private String locName;

    // ========== 서비스 유형 ==========

    /** 화장장 보유 여부 */
    @Column(nullable = false)
    @Builder.Default
    private Boolean hasCrematorium = false;

    /** 납골당 보유 여부 */
    @Column(nullable = false)
    @Builder.Default
    private Boolean hasColumbarium = false;

    /** 장례식장 보유 여부 */
    @Column(nullable = false)
    @Builder.Default
    private Boolean hasFuneral = false;

    // ========== 좌표 (Geocoding) ==========

    /** 위도 */
    @Column(precision = 10, scale = 7)
    private BigDecimal latitude;

    /** 경도 */
    @Column(precision = 10, scale = 7)
    private BigDecimal longitude;

    /** Geocoding 완료 시간 */
    private LocalDateTime geocodedAt;

    // ========== 상태 ==========

    /** 활성화 여부 */
    @Column(nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    /** 검증 완료 시간 */
    private LocalDateTime verifiedAt;

    /** 마지막 동기화 시간 */
    private LocalDateTime syncedAt;

    // ========== 비즈니스 메서드 ==========

    /**
     * API 응답으로부터 엔티티 생성
     */
    public static FuneralHome fromApiResponse(String name, String roadAddr, String lotAddr,
                                               String phone, String locCode, String locName,
                                               boolean crematorium, boolean columbarium, boolean funeral) {
        return FuneralHome.builder()
                .name(name)
                .roadAddress(roadAddr)
                .lotAddress(lotAddr)
                .phone(phone)
                .locCode(locCode)
                .locName(locName)
                .hasCrematorium(crematorium)
                .hasColumbarium(columbarium)
                .hasFuneral(funeral)
                .isActive(true)
                .syncedAt(LocalDateTime.now())
                .build();
    }

    /**
     * API 응답으로 정보 업데이트
     */
    public void updateFromApi(String roadAddr, String lotAddr, String phone,
                              boolean crematorium, boolean columbarium, boolean funeral) {
        this.roadAddress = roadAddr;
        this.lotAddress = lotAddr;
        this.phone = phone;
        this.hasCrematorium = crematorium;
        this.hasColumbarium = columbarium;
        this.hasFuneral = funeral;
        this.syncedAt = LocalDateTime.now();
    }

    /**
     * 좌표 설정 (Geocoding 결과)
     */
    public void setCoordinates(BigDecimal latitude, BigDecimal longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.geocodedAt = LocalDateTime.now();
    }

    /**
     * 활성화
     */
    public void activate() {
        this.isActive = true;
    }

    /**
     * 비활성화
     */
    public void deactivate() {
        this.isActive = false;
    }

    /**
     * 좌표 보유 여부 확인
     */
    public boolean hasCoordinates() {
        return this.latitude != null && this.longitude != null;
    }
}
