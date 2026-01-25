package com.findplace.domain.pet.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 반려동물 종류 열거형
 */
@Getter
@RequiredArgsConstructor
public enum Species {
    DOG("강아지"),
    CAT("고양이"),
    BIRD("새"),
    HAMSTER("햄스터"),
    RABBIT("토끼"),
    FISH("물고기"),
    REPTILE("파충류"),
    ETC("기타");

    private final String displayName;
}
