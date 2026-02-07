package com.petpro.domain.pet.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class PetChecklistRequest {

    @NotNull(message = "낯선 사람에 대한 친화도는 필수입니다.")
    @Min(value = 1, message = "1~5 사이 값이어야 합니다.")
    @Max(value = 5, message = "1~5 사이 값이어야 합니다.")
    private Integer friendlyToStrangers;

    @NotNull(message = "다른 강아지에 대한 친화도는 필수입니다.")
    @Min(value = 1, message = "1~5 사이 값이어야 합니다.")
    @Max(value = 5, message = "1~5 사이 값이어야 합니다.")
    private Integer friendlyToDogs;

    @NotNull(message = "고양이에 대한 친화도는 필수입니다.")
    @Min(value = 1, message = "1~5 사이 값이어야 합니다.")
    @Max(value = 5, message = "1~5 사이 값이어야 합니다.")
    private Integer friendlyToCats;

    @NotNull(message = "활동량은 필수입니다.")
    @Min(value = 1, message = "1~5 사이 값이어야 합니다.")
    @Max(value = 5, message = "1~5 사이 값이어야 합니다.")
    private Integer activityLevel;

    @NotNull(message = "짖음 정도는 필수입니다.")
    @Min(value = 1, message = "1~5 사이 값이어야 합니다.")
    @Max(value = 5, message = "1~5 사이 값이어야 합니다.")
    private Integer barkingLevel;

    @NotNull(message = "분리불안 정도는 필수입니다.")
    @Min(value = 1, message = "1~5 사이 값이어야 합니다.")
    @Max(value = 5, message = "1~5 사이 값이어야 합니다.")
    private Integer separationAnxiety;

    @NotNull(message = "배변 훈련 정도는 필수입니다.")
    @Min(value = 1, message = "1~5 사이 값이어야 합니다.")
    @Max(value = 5, message = "1~5 사이 값이어야 합니다.")
    private Integer houseTraining;

    @NotNull(message = "명령어 훈련 정도는 필수입니다.")
    @Min(value = 1, message = "1~5 사이 값이어야 합니다.")
    @Max(value = 5, message = "1~5 사이 값이어야 합니다.")
    private Integer commandTraining;

    @Size(max = 50, message = "식사 습관은 최대 50자까지 입력 가능합니다.")
    private String eatingHabit;

    @Size(max = 50, message = "산책 선호도는 최대 50자까지 입력 가능합니다.")
    private String walkPreference;

    @Size(max = 500, message = "무서워하는 것은 최대 500자까지 입력 가능합니다.")
    private String fearItems;

    @Size(max = 1000, message = "추가 성향 메모는 최대 1000자까지 입력 가능합니다.")
    private String additionalNotes;
}
