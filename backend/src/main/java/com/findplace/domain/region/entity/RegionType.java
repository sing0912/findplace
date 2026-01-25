package com.findplace.domain.region.entity;

/**
 * 지역 유형 열거형
 *
 * 행정구역의 계층을 구분합니다.
 */
public enum RegionType {
    /** 광역시/도 (서울특별시, 경기도 등) */
    METRO,
    /** 시/군/구 (강남구, 수원시 등) */
    CITY
}
