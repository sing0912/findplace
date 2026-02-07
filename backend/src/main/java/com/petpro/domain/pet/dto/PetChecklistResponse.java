package com.petpro.domain.pet.dto;

import com.petpro.domain.pet.entity.PetChecklist;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PetChecklistResponse {

    private Long id;
    private Long petId;
    private Integer friendlyToStrangers;
    private Integer friendlyToDogs;
    private Integer friendlyToCats;
    private Integer activityLevel;
    private Integer barkingLevel;
    private Integer separationAnxiety;
    private Integer houseTraining;
    private Integer commandTraining;
    private String eatingHabit;
    private String walkPreference;
    private String fearItems;
    private String additionalNotes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static PetChecklistResponse from(PetChecklist checklist) {
        return PetChecklistResponse.builder()
                .id(checklist.getId())
                .petId(checklist.getPet().getId())
                .friendlyToStrangers(checklist.getFriendlyToStrangers())
                .friendlyToDogs(checklist.getFriendlyToDogs())
                .friendlyToCats(checklist.getFriendlyToCats())
                .activityLevel(checklist.getActivityLevel())
                .barkingLevel(checklist.getBarkingLevel())
                .separationAnxiety(checklist.getSeparationAnxiety())
                .houseTraining(checklist.getHouseTraining())
                .commandTraining(checklist.getCommandTraining())
                .eatingHabit(checklist.getEatingHabit())
                .walkPreference(checklist.getWalkPreference())
                .fearItems(checklist.getFearItems())
                .additionalNotes(checklist.getAdditionalNotes())
                .createdAt(checklist.getCreatedAt())
                .updatedAt(checklist.getUpdatedAt())
                .build();
    }
}
