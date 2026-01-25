package com.findplace.domain.region.dto;

import com.findplace.domain.region.entity.RegionCode;
import com.findplace.domain.region.entity.RegionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

public class RegionResponse {

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RegionDto {
        private Long id;
        private String code;
        private String name;
        private RegionType type;
        private String parentCode;
        private Integer sortOrder;
        private Boolean isActive;

        public static RegionDto from(RegionCode region) {
            return RegionDto.builder()
                    .id(region.getId())
                    .code(region.getCode())
                    .name(region.getName())
                    .type(region.getType())
                    .parentCode(region.getParentCode())
                    .sortOrder(region.getSortOrder())
                    .isActive(region.getIsActive())
                    .build();
        }
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MetroDto {
        private String code;
        private String name;
        private long cityCount;

        public static MetroDto of(RegionCode metro, long cityCount) {
            return MetroDto.builder()
                    .code(metro.getCode())
                    .name(metro.getName())
                    .cityCount(cityCount)
                    .build();
        }
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MetroListDto {
        private List<MetroDto> metros;
        private int totalCount;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CityDto {
        private String code;
        private String name;

        public static CityDto from(RegionCode city) {
            return CityDto.builder()
                    .code(city.getCode())
                    .name(city.getName())
                    .build();
        }
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CityListDto {
        private String metroCode;
        private String metroName;
        private List<CityDto> cities;
        private int totalCount;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HierarchyDto {
        private String code;
        private String name;
        private RegionType type;
        private List<HierarchyDto> children;

        public static HierarchyDto of(RegionCode metro, List<RegionCode> cities) {
            List<HierarchyDto> childDtos = cities.stream()
                    .map(city -> HierarchyDto.builder()
                            .code(city.getCode())
                            .name(city.getName())
                            .type(city.getType())
                            .children(List.of())
                            .build())
                    .toList();

            return HierarchyDto.builder()
                    .code(metro.getCode())
                    .name(metro.getName())
                    .type(metro.getType())
                    .children(childDtos)
                    .build();
        }
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HierarchyListDto {
        private List<HierarchyDto> regions;
    }
}
