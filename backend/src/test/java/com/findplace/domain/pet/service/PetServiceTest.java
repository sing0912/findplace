package com.findplace.domain.pet.service;

import com.findplace.domain.pet.dto.PetRequest;
import com.findplace.domain.pet.dto.PetResponse;
import com.findplace.domain.pet.entity.Gender;
import com.findplace.domain.pet.entity.Pet;
import com.findplace.domain.pet.entity.Species;
import com.findplace.domain.pet.repository.PetRepository;
import com.findplace.domain.user.entity.User;
import com.findplace.domain.user.entity.UserRole;
import com.findplace.domain.user.entity.UserStatus;
import com.findplace.domain.user.repository.UserRepository;
import com.findplace.global.exception.BusinessException;
import com.findplace.global.exception.EntityNotFoundException;
import com.findplace.global.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("PetService 테스트")
class PetServiceTest {

    @InjectMocks
    private PetService petService;

    @Mock
    private PetRepository petRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PetImageService petImageService;

    private User testUser;
    private Pet testPet;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .password("encodedPassword")
                .name("테스트사용자")
                .role(UserRole.USER)
                .status(UserStatus.ACTIVE)
                .build();

        testPet = Pet.builder()
                .id(1L)
                .user(testUser)
                .name("콩이")
                .species(Species.DOG)
                .breed("말티즈")
                .birthDate(LocalDate.of(2020, 3, 15))
                .gender(Gender.MALE)
                .isNeutered(true)
                .memo("활발하고 사람을 좋아함")
                .isDeceased(false)
                .build();
    }

    @Nested
    @DisplayName("반려동물 목록 조회")
    class GetMyPets {

        @Test
        @DisplayName("성공: 내 반려동물 목록 조회")
        void shouldReturnMyPetsList() {
            // given
            Pet alivePet = Pet.builder()
                    .id(1L)
                    .user(testUser)
                    .name("콩이")
                    .species(Species.DOG)
                    .isDeceased(false)
                    .build();

            Pet deceasedPet = Pet.builder()
                    .id(2L)
                    .user(testUser)
                    .name("보리")
                    .species(Species.DOG)
                    .isDeceased(true)
                    .deceasedAt(LocalDate.of(2023, 5, 20))
                    .build();

            given(petRepository.findAllByUserId(1L)).willReturn(List.of(alivePet, deceasedPet));

            // when
            PetResponse.ListDto result = petService.getMyPets(1L);

            // then
            assertThat(result.getTotalCount()).isEqualTo(2);
            assertThat(result.getAliveCount()).isEqualTo(1);
            assertThat(result.getDeceasedCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("성공: 반려동물이 없는 경우 빈 목록 반환")
        void shouldReturnEmptyListWhenNoPets() {
            // given
            given(petRepository.findAllByUserId(1L)).willReturn(List.of());

            // when
            PetResponse.ListDto result = petService.getMyPets(1L);

            // then
            assertThat(result.getTotalCount()).isEqualTo(0);
            assertThat(result.getContent()).isEmpty();
        }
    }

    @Nested
    @DisplayName("반려동물 상세 조회")
    class GetPet {

        @Test
        @DisplayName("성공: 반려동물 상세 조회")
        void shouldReturnPetDetail() {
            // given
            given(petRepository.findByIdAndNotDeleted(1L)).willReturn(Optional.of(testPet));

            // when
            PetResponse.Detail result = petService.getPet(1L, 1L);

            // then
            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getName()).isEqualTo("콩이");
            assertThat(result.getSpeciesName()).isEqualTo("강아지");
        }

        @Test
        @DisplayName("실패: 존재하지 않는 반려동물")
        void shouldThrowExceptionWhenPetNotFound() {
            // given
            given(petRepository.findByIdAndNotDeleted(999L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> petService.getPet(999L, 1L))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PET_NOT_FOUND);
        }

        @Test
        @DisplayName("실패: 다른 사용자의 반려동물 조회 시도")
        void shouldThrowExceptionWhenAccessDenied() {
            // given
            given(petRepository.findByIdAndNotDeleted(1L)).willReturn(Optional.of(testPet));

            // when & then
            assertThatThrownBy(() -> petService.getPet(1L, 999L))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ACCESS_DENIED);
        }
    }

    @Nested
    @DisplayName("반려동물 등록")
    class CreatePet {

        @Test
        @DisplayName("성공: 반려동물 등록")
        void shouldCreatePet() {
            // given
            PetRequest.Create request = PetRequest.Create.builder()
                    .name("콩이")
                    .species(Species.DOG)
                    .breed("말티즈")
                    .birthDate(LocalDate.of(2020, 3, 15))
                    .gender(Gender.MALE)
                    .isNeutered(true)
                    .memo("활발하고 사람을 좋아함")
                    .build();

            given(petRepository.countByUserId(1L)).willReturn(0L);
            given(userRepository.findById(1L)).willReturn(Optional.of(testUser));
            given(petRepository.save(any(Pet.class))).willReturn(testPet);

            // when
            PetResponse.Detail result = petService.createPet(1L, request);

            // then
            assertThat(result.getName()).isEqualTo("콩이");
            verify(petRepository).save(any(Pet.class));
        }

        @Test
        @DisplayName("실패: 반려동물 등록 한도 초과")
        void shouldThrowExceptionWhenLimitExceeded() {
            // given
            PetRequest.Create request = PetRequest.Create.builder()
                    .name("콩이")
                    .species(Species.DOG)
                    .build();

            given(petRepository.countByUserId(1L)).willReturn(10L);

            // when & then
            assertThatThrownBy(() -> petService.createPet(1L, request))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PET_LIMIT_EXCEEDED);
        }
    }

    @Nested
    @DisplayName("반려동물 정보 수정")
    class UpdatePet {

        @Test
        @DisplayName("성공: 반려동물 정보 수정")
        void shouldUpdatePet() {
            // given
            PetRequest.Update request = PetRequest.Update.builder()
                    .name("보리")
                    .species(Species.DOG)
                    .breed("골든리트리버")
                    .birthDate(LocalDate.of(2021, 5, 10))
                    .gender(Gender.FEMALE)
                    .isNeutered(false)
                    .memo("수정된 메모")
                    .build();

            given(petRepository.findByIdAndNotDeleted(1L)).willReturn(Optional.of(testPet));

            // when
            PetResponse.Detail result = petService.updatePet(1L, 1L, request);

            // then
            assertThat(testPet.getName()).isEqualTo("보리");
            assertThat(testPet.getBreed()).isEqualTo("골든리트리버");
        }
    }

    @Nested
    @DisplayName("반려동물 삭제")
    class DeletePet {

        @Test
        @DisplayName("성공: 반려동물 소프트 삭제")
        void shouldSoftDeletePet() {
            // given
            given(petRepository.findByIdAndNotDeleted(1L)).willReturn(Optional.of(testPet));

            // when
            petService.deletePet(1L, 1L);

            // then
            assertThat(testPet.isDeleted()).isTrue();
        }
    }

    @Nested
    @DisplayName("사망 처리")
    class MarkAsDeceased {

        @Test
        @DisplayName("성공: 사망 처리")
        void shouldMarkAsDeceased() {
            // given
            PetRequest.Deceased request = PetRequest.Deceased.builder()
                    .deceasedAt(LocalDate.of(2024, 1, 15))
                    .build();

            given(petRepository.findByIdAndNotDeleted(1L)).willReturn(Optional.of(testPet));

            // when
            PetResponse.Detail result = petService.markAsDeceased(1L, 1L, request);

            // then
            assertThat(testPet.getIsDeceased()).isTrue();
            assertThat(testPet.getDeceasedAt()).isEqualTo(LocalDate.of(2024, 1, 15));
        }

        @Test
        @DisplayName("실패: 사망일이 생년월일보다 이전인 경우")
        void shouldThrowExceptionWhenDeceasedBeforeBirth() {
            // given
            PetRequest.Deceased request = PetRequest.Deceased.builder()
                    .deceasedAt(LocalDate.of(2019, 1, 1))
                    .build();

            given(petRepository.findByIdAndNotDeleted(1L)).willReturn(Optional.of(testPet));

            // when & then
            assertThatThrownBy(() -> petService.markAsDeceased(1L, 1L, request))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_INPUT);
        }
    }
}
