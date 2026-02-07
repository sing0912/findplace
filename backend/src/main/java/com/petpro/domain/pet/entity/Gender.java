package com.petpro.domain.pet.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 반려동물 성별 열거형
 */
@Getter
@RequiredArgsConstructor
public enum Gender {
    MALE("수컷"),
    FEMALE("암컷"),
    UNKNOWN("모름");

    private final String displayName;
}
