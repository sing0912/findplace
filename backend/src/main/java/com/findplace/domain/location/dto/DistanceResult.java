package com.findplace.domain.location.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 거리 계산 결과 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DistanceResult {

    /** 거리 (미터) */
    private long distanceMeters;

    /** 거리 텍스트 (예: "3.5km") */
    private String distanceText;

    /** 소요 시간 (초) */
    private long durationSeconds;

    /** 소요 시간 텍스트 (예: "15분") */
    private String durationText;

    public static DistanceResult ofHaversine(double distanceKm) {
        long meters = Math.round(distanceKm * 1000);
        String text = distanceKm >= 1
            ? String.format("%.1fkm", distanceKm)
            : String.format("%dm", meters);

        return DistanceResult.builder()
                .distanceMeters(meters)
                .distanceText(text)
                .build();
    }
}
