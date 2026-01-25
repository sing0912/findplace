package com.findplace.domain.funeralhome.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;

/**
 * 장례식장 요청 DTO
 */
public class FuneralHomeRequest {

    /**
     * 근처 장례식장 검색 요청
     */
    @Getter
    @Builder
    public static class NearbySearch {
        @NotNull(message = "위도는 필수입니다")
        @Min(value = 33, message = "위도는 33 이상이어야 합니다")
        @Max(value = 43, message = "위도는 43 이하여야 합니다")
        private Double latitude;

        @NotNull(message = "경도는 필수입니다")
        @Min(value = 124, message = "경도는 124 이상이어야 합니다")
        @Max(value = 132, message = "경도는 132 이하여야 합니다")
        private Double longitude;

        @Min(value = 1, message = "검색 반경은 1km 이상이어야 합니다")
        @Max(value = 100, message = "검색 반경은 100km 이하여야 합니다")
        @Builder.Default
        private Integer radius = 10;

        @Min(value = 1, message = "결과 수는 1 이상이어야 합니다")
        @Max(value = 100, message = "결과 수는 100 이하여야 합니다")
        @Builder.Default
        private Integer limit = 20;

        private Boolean hasCrematorium;
        private Boolean hasFuneral;
        private Boolean hasColumbarium;
    }

    /**
     * 장례식장 목록 검색 요청
     */
    @Getter
    @Builder
    public static class ListSearch {
        private String keyword;
        private String locCode;
        private Boolean hasCrematorium;
        private Boolean hasFuneral;
        private Boolean hasColumbarium;
        private Boolean isActive;

        @Builder.Default
        private Integer page = 0;

        @Builder.Default
        private Integer size = 20;
    }

    /**
     * 상태 변경 요청 (관리자)
     */
    @Getter
    @Builder
    public static class StatusUpdate {
        @NotNull(message = "활성화 여부는 필수입니다")
        private Boolean isActive;
    }
}
