package com.petpro.domain.pet.dto;

import com.petpro.domain.pet.entity.Gender;
import com.petpro.domain.pet.entity.Pet;
import com.petpro.domain.pet.entity.Species;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class PetResponse {

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Detail {
        private Long id;
        private Long userId;
        private String name;
        private Species species;
        private String speciesName;
        private String breed;
        private BigDecimal weight;
        private LocalDate birthDate;
        private Integer age;
        private Gender gender;
        private String genderName;
        private Boolean isNeutered;
        private String vaccinationStatus;
        private String allergies;
        private String specialNotes;
        private String profileImageUrl;
        private String memo;
        private Boolean isDeceased;
        private LocalDate deceasedAt;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public static Detail from(Pet pet) {
            return Detail.builder()
                    .id(pet.getId())
                    .userId(pet.getUser().getId())
                    .name(pet.getName())
                    .species(pet.getSpecies())
                    .speciesName(pet.getSpecies().getDisplayName())
                    .breed(pet.getBreed())
                    .weight(pet.getWeight())
                    .birthDate(pet.getBirthDate())
                    .age(pet.getAge())
                    .gender(pet.getGender())
                    .genderName(pet.getGender() != null ? pet.getGender().getDisplayName() : null)
                    .isNeutered(pet.getIsNeutered())
                    .vaccinationStatus(pet.getVaccinationStatus())
                    .allergies(pet.getAllergies())
                    .specialNotes(pet.getSpecialNotes())
                    .profileImageUrl(pet.getProfileImageUrl())
                    .memo(pet.getMemo())
                    .isDeceased(pet.getIsDeceased())
                    .deceasedAt(pet.getDeceasedAt())
                    .createdAt(pet.getCreatedAt())
                    .updatedAt(pet.getUpdatedAt())
                    .build();
        }
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Summary {
        private Long id;
        private String name;
        private Species species;
        private String speciesName;
        private String breed;
        private BigDecimal weight;
        private Integer age;
        private Gender gender;
        private Boolean isDeceased;
        private LocalDate deceasedAt;
        private String profileImageUrl;

        public static Summary from(Pet pet) {
            return Summary.builder()
                    .id(pet.getId())
                    .name(pet.getName())
                    .species(pet.getSpecies())
                    .speciesName(pet.getSpecies().getDisplayName())
                    .breed(pet.getBreed())
                    .weight(pet.getWeight())
                    .age(pet.getAge())
                    .gender(pet.getGender())
                    .isDeceased(pet.getIsDeceased())
                    .deceasedAt(pet.getDeceasedAt())
                    .profileImageUrl(pet.getProfileImageUrl())
                    .build();
        }
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ListDto {
        private List<Summary> content;
        private int totalCount;
        private int aliveCount;
        private int deceasedCount;
    }
}
