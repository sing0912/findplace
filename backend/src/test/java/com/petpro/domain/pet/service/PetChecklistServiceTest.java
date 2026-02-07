package com.petpro.domain.pet.service;

import com.petpro.domain.pet.dto.PetChecklistRequest;
import com.petpro.domain.pet.dto.PetChecklistResponse;
import com.petpro.domain.pet.entity.Gender;
import com.petpro.domain.pet.entity.Pet;
import com.petpro.domain.pet.entity.PetChecklist;
import com.petpro.domain.pet.entity.Species;
import com.petpro.domain.pet.repository.PetChecklistRepository;
import com.petpro.domain.pet.repository.PetRepository;
import com.petpro.domain.user.entity.User;
import com.petpro.domain.user.entity.UserRole;
import com.petpro.domain.user.entity.UserStatus;
import com.petpro.global.exception.BusinessException;
import com.petpro.global.exception.EntityNotFoundException;
import com.petpro.global.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("PetChecklistService 테스트")
class PetChecklistServiceTest {

    @InjectMocks
    private PetChecklistService petChecklistService;

    @Mock
    private PetRepository petRepository;

    @Mock
    private PetChecklistRepository petChecklistRepository;

    private User testUser;
    private User otherUser;
    private Pet testPet;
    private PetChecklist testChecklist;
    private PetChecklistRequest testRequest;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .password("encodedPassword")
                .name("테스트사용자")
                .role(UserRole.CUSTOMER)
                .status(UserStatus.ACTIVE)
                .build();

        otherUser = User.builder()
                .id(2L)
                .email("other@example.com")
                .password("encodedPassword")
                .name("다른사용자")
                .role(UserRole.CUSTOMER)
                .status(UserStatus.ACTIVE)
                .build();

        testPet = Pet.builder()
                .id(1L)
                .user(testUser)
                .name("콩이")
                .species(Species.DOG)
                .breed("말티즈")
                .gender(Gender.MALE)
                .isDeceased(false)
                .build();

        testChecklist = PetChecklist.builder()
                .id(1L)
                .pet(testPet)
                .friendlyToStrangers(4)
                .friendlyToDogs(3)
                .friendlyToCats(2)
                .activityLevel(5)
                .barkingLevel(3)
                .separationAnxiety(2)
                .houseTraining(4)
                .commandTraining(3)
                .eatingHabit("잘 먹음")
                .walkPreference("좋아함")
                .fearItems("천둥")
                .additionalNotes("활발한 성격")
                .build();

        testRequest = PetChecklistRequest.builder()
                .friendlyToStrangers(4)
                .friendlyToDogs(3)
                .friendlyToCats(2)
                .activityLevel(5)
                .barkingLevel(3)
                .separationAnxiety(2)
                .houseTraining(4)
                .commandTraining(3)
                .eatingHabit("잘 먹음")
                .walkPreference("좋아함")
                .fearItems("천둥")
                .additionalNotes("활발한 성격")
                .build();
    }

    @Nested
    @DisplayName("체크리스트 조회")
    class GetChecklist {

        @Test
        @DisplayName("성공: 체크리스트 조회")
        void shouldReturnChecklist() {
            // given
            given(petRepository.findByIdAndNotDeleted(1L)).willReturn(Optional.of(testPet));
            given(petChecklistRepository.findByPetId(1L)).willReturn(Optional.of(testChecklist));

            // when
            PetChecklistResponse result = petChecklistService.getChecklist(1L, 1L);

            // then
            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getPetId()).isEqualTo(1L);
            assertThat(result.getFriendlyToStrangers()).isEqualTo(4);
            assertThat(result.getActivityLevel()).isEqualTo(5);
            assertThat(result.getEatingHabit()).isEqualTo("잘 먹음");
        }

        @Test
        @DisplayName("실패: 존재하지 않는 반려동물")
        void shouldThrowWhenPetNotFound() {
            // given
            given(petRepository.findByIdAndNotDeleted(999L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> petChecklistService.getChecklist(999L, 1L))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PET_NOT_FOUND);
        }

        @Test
        @DisplayName("실패: 타인 소유 반려동물")
        void shouldThrowWhenAccessDenied() {
            // given
            given(petRepository.findByIdAndNotDeleted(1L)).willReturn(Optional.of(testPet));

            // when & then
            assertThatThrownBy(() -> petChecklistService.getChecklist(1L, 2L))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ACCESS_DENIED);
        }

        @Test
        @DisplayName("실패: 체크리스트 없음")
        void shouldThrowWhenChecklistNotFound() {
            // given
            given(petRepository.findByIdAndNotDeleted(1L)).willReturn(Optional.of(testPet));
            given(petChecklistRepository.findByPetId(1L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> petChecklistService.getChecklist(1L, 1L))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.CHECKLIST_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("체크리스트 생성")
    class CreateChecklist {

        @Test
        @DisplayName("성공: 체크리스트 생성")
        void shouldCreateChecklist() {
            // given
            given(petRepository.findByIdAndNotDeleted(1L)).willReturn(Optional.of(testPet));
            given(petChecklistRepository.existsByPetId(1L)).willReturn(false);
            given(petChecklistRepository.save(any(PetChecklist.class))).willReturn(testChecklist);

            // when
            PetChecklistResponse result = petChecklistService.createChecklist(1L, 1L, testRequest);

            // then
            assertThat(result.getFriendlyToStrangers()).isEqualTo(4);
            assertThat(result.getActivityLevel()).isEqualTo(5);
            assertThat(result.getEatingHabit()).isEqualTo("잘 먹음");
            verify(petChecklistRepository).save(any(PetChecklist.class));
        }

        @Test
        @DisplayName("실패: 존재하지 않는 반려동물")
        void shouldThrowWhenPetNotFound() {
            // given
            given(petRepository.findByIdAndNotDeleted(999L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> petChecklistService.createChecklist(999L, 1L, testRequest))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PET_NOT_FOUND);
        }

        @Test
        @DisplayName("실패: 타인 소유 반려동물")
        void shouldThrowWhenAccessDenied() {
            // given
            given(petRepository.findByIdAndNotDeleted(1L)).willReturn(Optional.of(testPet));

            // when & then
            assertThatThrownBy(() -> petChecklistService.createChecklist(1L, 2L, testRequest))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ACCESS_DENIED);
        }

        @Test
        @DisplayName("실패: 이미 체크리스트 존재")
        void shouldThrowWhenChecklistAlreadyExists() {
            // given
            given(petRepository.findByIdAndNotDeleted(1L)).willReturn(Optional.of(testPet));
            given(petChecklistRepository.existsByPetId(1L)).willReturn(true);

            // when & then
            assertThatThrownBy(() -> petChecklistService.createChecklist(1L, 1L, testRequest))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.CHECKLIST_ALREADY_EXISTS);
        }
    }

    @Nested
    @DisplayName("체크리스트 수정")
    class UpdateChecklist {

        @Test
        @DisplayName("성공: 체크리스트 수정")
        void shouldUpdateChecklist() {
            // given
            PetChecklistRequest updateRequest = PetChecklistRequest.builder()
                    .friendlyToStrangers(5)
                    .friendlyToDogs(4)
                    .friendlyToCats(3)
                    .activityLevel(4)
                    .barkingLevel(2)
                    .separationAnxiety(1)
                    .houseTraining(5)
                    .commandTraining(4)
                    .eatingHabit("편식")
                    .walkPreference("보통")
                    .fearItems("진공청소기")
                    .additionalNotes("수정된 메모")
                    .build();

            given(petRepository.findByIdAndNotDeleted(1L)).willReturn(Optional.of(testPet));
            given(petChecklistRepository.findByPetId(1L)).willReturn(Optional.of(testChecklist));

            // when
            PetChecklistResponse result = petChecklistService.updateChecklist(1L, 1L, updateRequest);

            // then
            assertThat(testChecklist.getFriendlyToStrangers()).isEqualTo(5);
            assertThat(testChecklist.getActivityLevel()).isEqualTo(4);
            assertThat(testChecklist.getEatingHabit()).isEqualTo("편식");
            assertThat(testChecklist.getFearItems()).isEqualTo("진공청소기");
        }

        @Test
        @DisplayName("실패: 존재하지 않는 반려동물")
        void shouldThrowWhenPetNotFound() {
            // given
            given(petRepository.findByIdAndNotDeleted(999L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> petChecklistService.updateChecklist(999L, 1L, testRequest))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PET_NOT_FOUND);
        }

        @Test
        @DisplayName("실패: 타인 소유 반려동물")
        void shouldThrowWhenAccessDenied() {
            // given
            given(petRepository.findByIdAndNotDeleted(1L)).willReturn(Optional.of(testPet));

            // when & then
            assertThatThrownBy(() -> petChecklistService.updateChecklist(1L, 2L, testRequest))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ACCESS_DENIED);
        }

        @Test
        @DisplayName("실패: 체크리스트 없음")
        void shouldThrowWhenChecklistNotFound() {
            // given
            given(petRepository.findByIdAndNotDeleted(1L)).willReturn(Optional.of(testPet));
            given(petChecklistRepository.findByPetId(1L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> petChecklistService.updateChecklist(1L, 1L, testRequest))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.CHECKLIST_NOT_FOUND);
        }
    }
}
