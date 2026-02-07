package com.petpro.domain.user.service;

import com.petpro.domain.user.dto.AddressUpdateRequest;
import com.petpro.domain.user.dto.PasswordChangeRequest;
import com.petpro.domain.user.dto.ProfileResponse;
import com.petpro.domain.user.dto.ProfileUpdateRequest;
import com.petpro.domain.user.entity.User;
import com.petpro.domain.user.entity.UserRole;
import com.petpro.domain.user.entity.UserStatus;
import com.petpro.domain.user.repository.UserRepository;
import com.petpro.global.exception.BusinessException;
import com.petpro.global.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserProfileService 테스트")
class UserProfileServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserProfileService userProfileService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .password("encodedPassword")
                .name("테스트")
                .nickname("테스트닉네임")
                .phone("01012345678")
                .role(UserRole.CUSTOMER)
                .status(UserStatus.ACTIVE)
                .build();
    }

    @Nested
    @DisplayName("프로필 조회")
    class GetProfile {

        @Test
        @DisplayName("성공: 프로필 조회")
        void getProfile_Success() {
            // given
            given(userRepository.findByIdAndDeletedAtIsNull(1L)).willReturn(Optional.of(testUser));

            // when
            ProfileResponse response = userProfileService.getProfile(1L);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getEmail()).isEqualTo("test@example.com");
            assertThat(response.getName()).isEqualTo("테스트");
        }

        @Test
        @DisplayName("실패: 사용자를 찾을 수 없음")
        void getProfile_UserNotFound() {
            // given
            given(userRepository.findByIdAndDeletedAtIsNull(999L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> userProfileService.getProfile(999L))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("프로필 수정")
    class UpdateProfile {

        @Test
        @DisplayName("성공: 프로필 수정")
        void updateProfile_Success() {
            // given
            ProfileUpdateRequest request = ProfileUpdateRequest.builder()
                    .name("수정된이름")
                    .phone("01087654321")
                    .profileImageUrl("http://example.com/profile.jpg")
                    .build();

            given(userRepository.findByIdAndDeletedAtIsNull(1L)).willReturn(Optional.of(testUser));

            // when
            ProfileResponse response = userProfileService.updateProfile(1L, request);

            // then
            assertThat(response).isNotNull();
        }

        @Test
        @DisplayName("성공: 프로필 수정 - 생년월일 포함")
        void updateProfile_WithBirthDate() {
            // given
            ProfileUpdateRequest request = ProfileUpdateRequest.builder()
                    .name("수정된이름")
                    .phone("01087654321")
                    .birthDate(LocalDate.of(1990, 1, 1))
                    .build();

            given(userRepository.findByIdAndDeletedAtIsNull(1L)).willReturn(Optional.of(testUser));

            // when
            ProfileResponse response = userProfileService.updateProfile(1L, request);

            // then
            assertThat(response).isNotNull();
        }
    }

    @Nested
    @DisplayName("비밀번호 변경")
    class ChangePassword {

        @Test
        @DisplayName("성공: 비밀번호 변경")
        void changePassword_Success() {
            // given
            PasswordChangeRequest request = PasswordChangeRequest.builder()
                    .currentPassword("currentPassword123!")
                    .newPassword("newPassword123!")
                    .newPasswordConfirm("newPassword123!")
                    .build();

            given(userRepository.findByIdAndDeletedAtIsNull(1L)).willReturn(Optional.of(testUser));
            given(passwordEncoder.matches("currentPassword123!", "encodedPassword")).willReturn(true);
            given(passwordEncoder.encode("newPassword123!")).willReturn("newEncodedPassword");

            // when
            userProfileService.changePassword(1L, request);

            // then
            verify(passwordEncoder).encode("newPassword123!");
        }

        @Test
        @DisplayName("실패: 현재 비밀번호 불일치")
        void changePassword_InvalidCurrentPassword() {
            // given
            PasswordChangeRequest request = PasswordChangeRequest.builder()
                    .currentPassword("wrongPassword")
                    .newPassword("newPassword123!")
                    .newPasswordConfirm("newPassword123!")
                    .build();

            given(userRepository.findByIdAndDeletedAtIsNull(1L)).willReturn(Optional.of(testUser));
            given(passwordEncoder.matches("wrongPassword", "encodedPassword")).willReturn(false);

            // when & then
            assertThatThrownBy(() -> userProfileService.changePassword(1L, request))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_PASSWORD);
        }

        @Test
        @DisplayName("실패: 새 비밀번호 확인 불일치")
        void changePassword_PasswordMismatch() {
            // given
            PasswordChangeRequest request = PasswordChangeRequest.builder()
                    .currentPassword("currentPassword123!")
                    .newPassword("newPassword123!")
                    .newPasswordConfirm("differentPassword123!")
                    .build();

            given(userRepository.findByIdAndDeletedAtIsNull(1L)).willReturn(Optional.of(testUser));
            given(passwordEncoder.matches("currentPassword123!", "encodedPassword")).willReturn(true);

            // when & then
            assertThatThrownBy(() -> userProfileService.changePassword(1L, request))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PASSWORD_MISMATCH);
        }
    }

    @Nested
    @DisplayName("주소 변경")
    class UpdateAddress {

        @Test
        @DisplayName("성공: 주소 변경")
        void updateAddress_Success() {
            // given
            AddressUpdateRequest request = AddressUpdateRequest.builder()
                    .address("서울시 강남구")
                    .addressDetail("123번지")
                    .zipCode("06000")
                    .latitude(new BigDecimal("37.12345"))
                    .longitude(new BigDecimal("127.12345"))
                    .build();

            given(userRepository.findByIdAndDeletedAtIsNull(1L)).willReturn(Optional.of(testUser));

            // when
            ProfileResponse response = userProfileService.updateAddress(1L, request);

            // then
            assertThat(response).isNotNull();
        }
    }

    @Nested
    @DisplayName("회원 탈퇴")
    class Withdraw {

        @Test
        @DisplayName("성공: 회원 탈퇴")
        void withdraw_Success() {
            // given
            given(userRepository.findByIdAndDeletedAtIsNull(1L)).willReturn(Optional.of(testUser));
            given(passwordEncoder.matches("password123", "encodedPassword")).willReturn(true);

            // when
            userProfileService.withdraw(1L, "password123");

            // then
            assertThat(testUser.getDeletedAt()).isNotNull();
        }

        @Test
        @DisplayName("실패: 비밀번호 불일치")
        void withdraw_InvalidPassword() {
            // given
            given(userRepository.findByIdAndDeletedAtIsNull(1L)).willReturn(Optional.of(testUser));
            given(passwordEncoder.matches("wrongPassword", "encodedPassword")).willReturn(false);

            // when & then
            assertThatThrownBy(() -> userProfileService.withdraw(1L, "wrongPassword"))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_PASSWORD);
        }
    }
}
