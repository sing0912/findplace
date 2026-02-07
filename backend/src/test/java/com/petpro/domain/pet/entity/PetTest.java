package com.petpro.domain.pet.entity;

import com.petpro.domain.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Pet 엔티티 테스트")
class PetTest {

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .name("테스트유저")
                .build();
    }

    @Nested
    @DisplayName("나이 계산")
    class GetAge {

        @Test
        @DisplayName("생년월일이 있으면 나이를 계산한다")
        void shouldCalculateAgeWhenBirthDateExists() {
            // given
            LocalDate birthDate = LocalDate.now().minusYears(3);
            Pet pet = Pet.builder()
                    .user(testUser)
                    .name("콩이")
                    .species(Species.DOG)
                    .birthDate(birthDate)
                    .build();

            // when
            Integer age = pet.getAge();

            // then
            assertThat(age).isEqualTo(3);
        }

        @Test
        @DisplayName("생년월일이 없으면 null을 반환한다")
        void shouldReturnNullWhenNoBirthDate() {
            // given
            Pet pet = Pet.builder()
                    .user(testUser)
                    .name("콩이")
                    .species(Species.DOG)
                    .build();

            // when
            Integer age = pet.getAge();

            // then
            assertThat(age).isNull();
        }

        @Test
        @DisplayName("사망한 경우 사망일 기준으로 나이를 계산한다")
        void shouldCalculateAgeAtDeathWhenDeceased() {
            // given
            LocalDate birthDate = LocalDate.of(2020, 1, 1);
            LocalDate deceasedAt = LocalDate.of(2023, 6, 15);
            Pet pet = Pet.builder()
                    .user(testUser)
                    .name("콩이")
                    .species(Species.DOG)
                    .birthDate(birthDate)
                    .isDeceased(true)
                    .deceasedAt(deceasedAt)
                    .build();

            // when
            Integer age = pet.getAge();

            // then
            assertThat(age).isEqualTo(3);
        }
    }

    @Nested
    @DisplayName("정보 수정")
    class Update {

        @Test
        @DisplayName("반려동물 정보를 수정한다")
        void shouldUpdatePetInfo() {
            // given
            Pet pet = Pet.builder()
                    .user(testUser)
                    .name("콩이")
                    .species(Species.DOG)
                    .breed("말티즈")
                    .build();

            // when
            pet.update("보리", Species.DOG, "골든리트리버",
                    LocalDate.of(2020, 5, 10), Gender.MALE, true, "새 메모",
                    null, null, null, null);

            // then
            assertThat(pet.getName()).isEqualTo("보리");
            assertThat(pet.getBreed()).isEqualTo("골든리트리버");
            assertThat(pet.getBirthDate()).isEqualTo(LocalDate.of(2020, 5, 10));
            assertThat(pet.getGender()).isEqualTo(Gender.MALE);
            assertThat(pet.getIsNeutered()).isTrue();
            assertThat(pet.getMemo()).isEqualTo("새 메모");
        }
    }

    @Nested
    @DisplayName("사망 처리")
    class MarkAsDeceased {

        @Test
        @DisplayName("사망 처리를 한다")
        void shouldMarkAsDeceased() {
            // given
            Pet pet = Pet.builder()
                    .user(testUser)
                    .name("콩이")
                    .species(Species.DOG)
                    .build();

            LocalDate deceasedAt = LocalDate.of(2024, 1, 15);

            // when
            pet.markAsDeceased(deceasedAt);

            // then
            assertThat(pet.getIsDeceased()).isTrue();
            assertThat(pet.getDeceasedAt()).isEqualTo(deceasedAt);
        }

        @Test
        @DisplayName("사망 처리를 취소한다")
        void shouldCancelDeceased() {
            // given
            Pet pet = Pet.builder()
                    .user(testUser)
                    .name("콩이")
                    .species(Species.DOG)
                    .isDeceased(true)
                    .deceasedAt(LocalDate.of(2024, 1, 15))
                    .build();

            // when
            pet.cancelDeceased();

            // then
            assertThat(pet.getIsDeceased()).isFalse();
            assertThat(pet.getDeceasedAt()).isNull();
        }
    }

    @Nested
    @DisplayName("소프트 삭제")
    class SoftDelete {

        @Test
        @DisplayName("소프트 삭제 처리를 한다")
        void shouldSoftDelete() {
            // given
            Pet pet = Pet.builder()
                    .user(testUser)
                    .name("콩이")
                    .species(Species.DOG)
                    .build();

            // when
            pet.softDelete();

            // then
            assertThat(pet.isDeleted()).isTrue();
            assertThat(pet.getDeletedAt()).isNotNull();
        }
    }

    @Nested
    @DisplayName("소유자 확인")
    class IsOwnedBy {

        @Test
        @DisplayName("소유자가 맞으면 true를 반환한다")
        void shouldReturnTrueForOwner() {
            // given
            Pet pet = Pet.builder()
                    .user(testUser)
                    .name("콩이")
                    .species(Species.DOG)
                    .build();

            // when
            boolean result = pet.isOwnedBy(1L);

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("소유자가 아니면 false를 반환한다")
        void shouldReturnFalseForNonOwner() {
            // given
            Pet pet = Pet.builder()
                    .user(testUser)
                    .name("콩이")
                    .species(Species.DOG)
                    .build();

            // when
            boolean result = pet.isOwnedBy(999L);

            // then
            assertThat(result).isFalse();
        }
    }
}
