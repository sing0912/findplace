package com.petpro.domain.pet.service;

import com.petpro.domain.pet.dto.PetRequest;
import com.petpro.domain.pet.dto.PetResponse;
import com.petpro.domain.pet.dto.PetResponse.Detail;
import com.petpro.domain.pet.dto.PetResponse.ListDto;
import com.petpro.domain.pet.dto.PetResponse.Summary;
import com.petpro.domain.pet.entity.Pet;
import com.petpro.domain.pet.repository.PetChecklistRepository;
import com.petpro.domain.pet.repository.PetRepository;
import com.petpro.domain.user.entity.User;
import com.petpro.domain.user.repository.UserRepository;
import com.petpro.global.exception.BusinessException;
import com.petpro.global.exception.EntityNotFoundException;
import com.petpro.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PetService {

    private final PetRepository petRepository;
    private final PetChecklistRepository petChecklistRepository;
    private final UserRepository userRepository;
    private final PetImageService petImageService;

    private static final int MAX_PETS_PER_USER = 10;

    /**
     * 내 반려동물 목록 조회
     */
    public ListDto getMyPets(Long userId) {
        List<Pet> pets = petRepository.findAllByUserId(userId);

        List<Summary> summaries = pets.stream()
                .map(Summary::from)
                .toList();

        int aliveCount = (int) pets.stream().filter(p -> !p.getIsDeceased()).count();
        int deceasedCount = (int) pets.stream().filter(Pet::getIsDeceased).count();

        return ListDto.builder()
                .content(summaries)
                .totalCount(summaries.size())
                .aliveCount(aliveCount)
                .deceasedCount(deceasedCount)
                .build();
    }

    /**
     * 반려동물 상세 조회
     */
    public Detail getPet(Long petId, Long userId) {
        Pet pet = findPetWithOwnerCheck(petId, userId);
        return Detail.from(pet);
    }

    /**
     * 반려동물 등록
     */
    @Transactional
    public Detail createPet(Long userId, PetRequest.Create request) {
        // 등록 한도 확인
        long currentCount = petRepository.countByUserId(userId);
        if (currentCount >= MAX_PETS_PER_USER) {
            throw new BusinessException(ErrorCode.PET_LIMIT_EXCEEDED);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.USER_NOT_FOUND));

        Pet pet = Pet.builder()
                .user(user)
                .name(request.getName())
                .species(request.getSpecies())
                .breed(request.getBreed())
                .weight(request.getWeight())
                .birthDate(request.getBirthDate())
                .gender(request.getGender())
                .isNeutered(request.getIsNeutered() != null ? request.getIsNeutered() : false)
                .vaccinationStatus(request.getVaccinationStatus())
                .allergies(request.getAllergies())
                .specialNotes(request.getSpecialNotes())
                .memo(request.getMemo())
                .build();

        Pet savedPet = petRepository.save(pet);
        return Detail.from(savedPet);
    }

    /**
     * 반려동물 정보 수정
     */
    @Transactional
    public Detail updatePet(Long petId, Long userId, PetRequest.Update request) {
        Pet pet = findPetWithOwnerCheck(petId, userId);

        pet.update(
                request.getName(),
                request.getSpecies(),
                request.getBreed(),
                request.getBirthDate(),
                request.getGender(),
                request.getIsNeutered(),
                request.getMemo(),
                request.getWeight(),
                request.getVaccinationStatus(),
                request.getAllergies(),
                request.getSpecialNotes()
        );

        return Detail.from(pet);
    }

    /**
     * 프로필 이미지 업로드
     */
    @Transactional
    public Detail uploadProfileImage(Long petId, Long userId, MultipartFile file) {
        Pet pet = findPetWithOwnerCheck(petId, userId);

        // 기존 이미지 삭제
        if (pet.getProfileImageUrl() != null) {
            petImageService.deleteProfileImage(pet.getProfileImageUrl());
        }

        // 새 이미지 업로드
        String imageUrl = petImageService.uploadProfileImage(petId, file);
        pet.updateProfileImage(imageUrl);

        return Detail.from(pet);
    }

    /**
     * 반려동물 삭제
     */
    @Transactional
    public void deletePet(Long petId, Long userId) {
        Pet pet = findPetWithOwnerCheck(petId, userId);

        // 프로필 이미지 삭제
        if (pet.getProfileImageUrl() != null) {
            petImageService.deleteProfileImage(pet.getProfileImageUrl());
        }

        // 연관 체크리스트 삭제
        if (petChecklistRepository.existsByPetId(petId)) {
            petChecklistRepository.deleteByPetId(petId);
        }

        pet.softDelete();
    }

    /**
     * 사망 처리
     */
    @Transactional
    public Detail markAsDeceased(Long petId, Long userId, PetRequest.Deceased request) {
        Pet pet = findPetWithOwnerCheck(petId, userId);

        // 생년월일 이후인지 확인
        if (pet.getBirthDate() != null && request.getDeceasedAt().isBefore(pet.getBirthDate())) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }

        pet.markAsDeceased(request.getDeceasedAt());
        return Detail.from(pet);
    }

    /**
     * 관리자용: 특정 회원의 반려동물 목록 조회
     */
    public ListDto getPetsByUserId(Long userId) {
        return getMyPets(userId);
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
