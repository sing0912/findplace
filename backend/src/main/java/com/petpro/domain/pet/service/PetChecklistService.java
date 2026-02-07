package com.petpro.domain.pet.service;

import com.petpro.domain.pet.dto.PetChecklistRequest;
import com.petpro.domain.pet.dto.PetChecklistResponse;
import com.petpro.domain.pet.entity.Pet;
import com.petpro.domain.pet.entity.PetChecklist;
import com.petpro.domain.pet.repository.PetChecklistRepository;
import com.petpro.domain.pet.repository.PetRepository;
import com.petpro.global.exception.BusinessException;
import com.petpro.global.exception.EntityNotFoundException;
import com.petpro.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PetChecklistService {

    private final PetRepository petRepository;
    private final PetChecklistRepository petChecklistRepository;

    public PetChecklistResponse getChecklist(Long petId, Long userId) {
        findPetWithOwnerCheck(petId, userId);

        PetChecklist checklist = petChecklistRepository.findByPetId(petId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.CHECKLIST_NOT_FOUND));

        return PetChecklistResponse.from(checklist);
    }

    @Transactional
    public PetChecklistResponse createChecklist(Long petId, Long userId, PetChecklistRequest request) {
        Pet pet = findPetWithOwnerCheck(petId, userId);

        if (petChecklistRepository.existsByPetId(petId)) {
            throw new BusinessException(ErrorCode.CHECKLIST_ALREADY_EXISTS);
        }

        PetChecklist checklist = PetChecklist.builder()
                .pet(pet)
                .friendlyToStrangers(request.getFriendlyToStrangers())
                .friendlyToDogs(request.getFriendlyToDogs())
                .friendlyToCats(request.getFriendlyToCats())
                .activityLevel(request.getActivityLevel())
                .barkingLevel(request.getBarkingLevel())
                .separationAnxiety(request.getSeparationAnxiety())
                .houseTraining(request.getHouseTraining())
                .commandTraining(request.getCommandTraining())
                .eatingHabit(request.getEatingHabit())
                .walkPreference(request.getWalkPreference())
                .fearItems(request.getFearItems())
                .additionalNotes(request.getAdditionalNotes())
                .build();

        PetChecklist saved = petChecklistRepository.save(checklist);
        return PetChecklistResponse.from(saved);
    }

    @Transactional
    public PetChecklistResponse updateChecklist(Long petId, Long userId, PetChecklistRequest request) {
        findPetWithOwnerCheck(petId, userId);

        PetChecklist checklist = petChecklistRepository.findByPetId(petId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.CHECKLIST_NOT_FOUND));

        checklist.update(
                request.getFriendlyToStrangers(),
                request.getFriendlyToDogs(),
                request.getFriendlyToCats(),
                request.getActivityLevel(),
                request.getBarkingLevel(),
                request.getSeparationAnxiety(),
                request.getHouseTraining(),
                request.getCommandTraining(),
                request.getEatingHabit(),
                request.getWalkPreference(),
                request.getFearItems(),
                request.getAdditionalNotes()
        );

        return PetChecklistResponse.from(checklist);
    }

    private Pet findPetWithOwnerCheck(Long petId, Long userId) {
        Pet pet = petRepository.findByIdAndNotDeleted(petId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.PET_NOT_FOUND));

        if (!pet.isOwnedBy(userId)) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }

        return pet;
    }
}
