package com.findplace.domain.pet.dto;

import com.findplace.domain.pet.entity.Gender;
import com.findplace.domain.pet.entity.Species;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

public class PetRequest {

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Create {
        @NotBlank(message = "반려동물 이름은 필수입니다.")
        @Size(min = 1, max = 100, message = "이름은 1-100자 사이여야 합니다.")
        private String name;

        @NotNull(message = "종류는 필수입니다.")
        private Species species;

        @Size(max = 100, message = "품종은 최대 100자까지 입력 가능합니다.")
        private String breed;

        @PastOrPresent(message = "생년월일은 과거 또는 오늘이어야 합니다.")
        private LocalDate birthDate;

        private Gender gender;

        private Boolean isNeutered;

        @Size(max = 1000, message = "메모는 최대 1000자까지 입력 가능합니다.")
        private String memo;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Update {
        @NotBlank(message = "반려동물 이름은 필수입니다.")
        @Size(min = 1, max = 100, message = "이름은 1-100자 사이여야 합니다.")
        private String name;

        @NotNull(message = "종류는 필수입니다.")
        private Species species;

        @Size(max = 100, message = "품종은 최대 100자까지 입력 가능합니다.")
        private String breed;

        @PastOrPresent(message = "생년월일은 과거 또는 오늘이어야 합니다.")
        private LocalDate birthDate;

        private Gender gender;

        private Boolean isNeutered;

        @Size(max = 1000, message = "메모는 최대 1000자까지 입력 가능합니다.")
        private String memo;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Deceased {
        @NotNull(message = "사망일은 필수입니다.")
        @PastOrPresent(message = "사망일은 과거 또는 오늘이어야 합니다.")
        private LocalDate deceasedAt;
    }
}
