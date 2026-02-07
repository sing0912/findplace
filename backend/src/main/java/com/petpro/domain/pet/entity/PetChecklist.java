package com.petpro.domain.pet.entity;

import com.petpro.global.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "pet_checklists")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class PetChecklist extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pet_id", nullable = false, unique = true)
    private Pet pet;

    @Column(name = "friendly_to_strangers", nullable = false)
    private Integer friendlyToStrangers;

    @Column(name = "friendly_to_dogs", nullable = false)
    private Integer friendlyToDogs;

    @Column(name = "friendly_to_cats", nullable = false)
    private Integer friendlyToCats;

    @Column(name = "activity_level", nullable = false)
    private Integer activityLevel;

    @Column(name = "barking_level", nullable = false)
    private Integer barkingLevel;

    @Column(name = "separation_anxiety", nullable = false)
    private Integer separationAnxiety;

    @Column(name = "house_training", nullable = false)
    private Integer houseTraining;

    @Column(name = "command_training", nullable = false)
    private Integer commandTraining;

    @Column(name = "eating_habit", length = 50)
    private String eatingHabit;

    @Column(name = "walk_preference", length = 50)
    private String walkPreference;

    @Column(name = "fear_items", length = 500)
    private String fearItems;

    @Column(name = "additional_notes", columnDefinition = "TEXT")
    private String additionalNotes;

    public void update(Integer friendlyToStrangers, Integer friendlyToDogs, Integer friendlyToCats,
                       Integer activityLevel, Integer barkingLevel, Integer separationAnxiety,
                       Integer houseTraining, Integer commandTraining,
                       String eatingHabit, String walkPreference,
                       String fearItems, String additionalNotes) {
        this.friendlyToStrangers = friendlyToStrangers;
        this.friendlyToDogs = friendlyToDogs;
        this.friendlyToCats = friendlyToCats;
        this.activityLevel = activityLevel;
        this.barkingLevel = barkingLevel;
        this.separationAnxiety = separationAnxiety;
        this.houseTraining = houseTraining;
        this.commandTraining = commandTraining;
        this.eatingHabit = eatingHabit;
        this.walkPreference = walkPreference;
        this.fearItems = fearItems;
        this.additionalNotes = additionalNotes;
    }
}
