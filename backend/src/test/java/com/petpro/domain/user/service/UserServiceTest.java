package com.petpro.domain.user.service;

import com.petpro.domain.auth.entity.AuthProvider;
import com.petpro.domain.user.dto.UserRequest;
import com.petpro.domain.user.dto.UserResponse;
import com.petpro.domain.user.entity.User;
import com.petpro.domain.user.entity.UserRole;
import com.petpro.domain.user.entity.UserStatus;
import com.petpro.domain.user.repository.UserRepository;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService 테스트")
class UserServiceTest {

    @InjectMocks
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private User testUser;
    private UserRequest.Create createRequest;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .password("encodedPassword")
                .name("테스트사용자")
                .phone("010-1234-5678")
                .role(UserRole.CUSTOMER)
                .status(UserStatus.ACTIVE)
                .build();

        createRequest = UserRequest.Create.builder()
                .email("test@example.com")
                .password("password123")
                .name("테스트사용자")
                .phone("010-1234-5678")
                .build();
    }

    @Nested
    @DisplayName("사용자 생성")
    class CreateUser {

        @Test
        @DisplayName("성공: 새로운 사용자 생성")
        void createUser_Success() {
            // given
            given(userRepository.existsByEmail(anyString())).willReturn(false);
            given(userRepository.existsByPhone(anyString())).willReturn(false);
            given(passwordEncoder.encode(anyString())).willReturn("encodedPassword");
            given(userRepository.save(any(User.class))).willReturn(testUser);

            // when
            UserResponse.Info response = userService.createUser(createRequest);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getEmail()).isEqualTo("test@example.com");
            assertThat(response.getName()).isEqualTo("테스트사용자");
            verify(userRepository).save(any(User.class));
        }

        @Test
        @DisplayName("실패: 이메일 중복")
        void createUser_DuplicateEmail() {
            // given
            given(userRepository.existsByEmail(anyString())).willReturn(true);

            // when & then
            assertThatThrownBy(() -> userService.createUser(createRequest))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.DUPLICATE_EMAIL);
        }

        @Test
        @DisplayName("실패: 전화번호 중복")
        void createUser_DuplicatePhone() {
            // given
            given(userRepository.existsByEmail(anyString())).willReturn(false);
            given(userRepository.existsByPhone(anyString())).willReturn(true);

            // when & then
            assertThatThrownBy(() -> userService.createUser(createRequest))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.DUPLICATE_PHONE);
        }
    }

    @Nested
    @DisplayName("사용자 조회")
    class GetUser {

        @Test
        @DisplayName("성공: ID로 사용자 조회")
        void getUser_Success() {
            // given
            given(userRepository.findByIdAndDeletedAtIsNull(1L)).willReturn(Optional.of(testUser));

            // when
            UserResponse.Info response = userService.getUser(1L);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo(1L);
            assertThat(response.getEmail()).isEqualTo("test@example.com");
        }

        @Test
        @DisplayName("실패: 존재하지 않는 사용자")
        void getUser_NotFound() {
            // given
            given(userRepository.findByIdAndDeletedAtIsNull(999L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> userService.getUser(999L))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("사용자 목록 조회")
    class GetUsers {

        @Test
        @DisplayName("성공: 전체 사용자 목록 조회")
        void getUsers_Success() {
            // given
            Pageable pageable = PageRequest.of(0, 10);
            Page<User> userPage = new PageImpl<>(List.of(testUser), pageable, 1);
            given(userRepository.findAllActive(pageable)).willReturn(userPage);

            // when
            Page<UserResponse.Simple> response = userService.getUsers(pageable);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getContent()).hasSize(1);
            assertThat(response.getContent().get(0).getEmail()).isEqualTo("test@example.com");
        }

        @Test
        @DisplayName("성공: 키워드로 검색")
        void searchUsers_Success() {
            // given
            Pageable pageable = PageRequest.of(0, 10);
            Page<User> userPage = new PageImpl<>(List.of(testUser), pageable, 1);
            given(userRepository.searchByKeyword("테스트", pageable)).willReturn(userPage);

            // when
            Page<UserResponse.Simple> response = userService.searchUsers("테스트", pageable);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getContent()).hasSize(1);
        }
    }

    @Nested
    @DisplayName("사용자 수정")
    class UpdateUser {

        @Test
        @DisplayName("성공: 사용자 정보 수정")
        void updateUser_Success() {
            // given
            UserRequest.Update updateRequest = UserRequest.Update.builder()
                    .name("수정된이름")
                    .phone("010-9999-9999")
                    .build();

            given(userRepository.findByIdAndDeletedAtIsNull(1L)).willReturn(Optional.of(testUser));

            // when
            UserResponse.Info response = userService.updateUser(1L, updateRequest);

            // then
            assertThat(response).isNotNull();
            assertThat(testUser.getName()).isEqualTo("수정된이름");
            assertThat(testUser.getPhone()).isEqualTo("010-9999-9999");
        }
    }

    @Nested
    @DisplayName("비밀번호 변경")
    class UpdatePassword {

        @Test
        @DisplayName("성공: 비밀번호 변경")
        void updatePassword_Success() {
            // given
            UserRequest.UpdatePassword request = UserRequest.UpdatePassword.builder()
                    .currentPassword("password123")
                    .newPassword("newPassword456")
                    .build();

            given(userRepository.findByIdAndDeletedAtIsNull(1L)).willReturn(Optional.of(testUser));
            given(passwordEncoder.matches("password123", "encodedPassword")).willReturn(true);
            given(passwordEncoder.encode("newPassword456")).willReturn("newEncodedPassword");

            // when
            userService.updatePassword(1L, request);

            // then
            verify(passwordEncoder).encode("newPassword456");
        }

        @Test
        @DisplayName("실패: 현재 비밀번호 불일치")
        void updatePassword_WrongCurrentPassword() {
            // given
            UserRequest.UpdatePassword request = UserRequest.UpdatePassword.builder()
                    .currentPassword("wrongPassword")
                    .newPassword("newPassword456")
                    .build();

            given(userRepository.findByIdAndDeletedAtIsNull(1L)).willReturn(Optional.of(testUser));
            given(passwordEncoder.matches("wrongPassword", "encodedPassword")).willReturn(false);

            // when & then
            assertThatThrownBy(() -> userService.updatePassword(1L, request))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_CREDENTIALS);
        }
    }

    @Nested
    @DisplayName("사용자 삭제")
    class DeleteUser {

        @Test
        @DisplayName("성공: 사용자 소프트 삭제")
        void deleteUser_Success() {
            // given
            given(userRepository.findByIdAndDeletedAtIsNull(1L)).willReturn(Optional.of(testUser));

            // when
            userService.deleteUser(1L, 2L);

            // then
            assertThat(testUser.getStatus()).isEqualTo(UserStatus.DELETED);
            assertThat(testUser.getDeletedAt()).isNotNull();
            assertThat(testUser.getDeletedBy()).isEqualTo(2L);
        }
    }

    @Nested
    @DisplayName("상태 변경")
    class ChangeStatus {

        @Test
        @DisplayName("성공: 사용자 활성화")
        void activateUser_Success() {
            // given
            testUser.suspend();
            given(userRepository.findByIdAndDeletedAtIsNull(1L)).willReturn(Optional.of(testUser));

            // when
            UserResponse.Info response = userService.changeUserStatus(1L, UserStatus.ACTIVE);

            // then
            assertThat(testUser.getStatus()).isEqualTo(UserStatus.ACTIVE);
        }

        @Test
        @DisplayName("성공: 사용자 정지")
        void suspendUser_Success() {
            // given
            given(userRepository.findByIdAndDeletedAtIsNull(1L)).willReturn(Optional.of(testUser));

            // when
            UserResponse.Info response = userService.changeUserStatus(1L, UserStatus.SUSPENDED);

            // then
            assertThat(testUser.getStatus()).isEqualTo(UserStatus.SUSPENDED);
        }
    }

    // ==================== 마이페이지 API 테스트 ====================

    @Nested
    @DisplayName("닉네임 수정")
    class UpdateNickname {

        @Test
        @DisplayName("성공: 닉네임 수정")
        void updateNickname_Success() {
            // given
            UserRequest.UpdateNickname request = UserRequest.UpdateNickname.builder()
                    .nickname("새닉네임")
                    .build();

            given(userRepository.findByIdAndDeletedAtIsNull(1L)).willReturn(Optional.of(testUser));
            given(userRepository.existsByNickname("새닉네임")).willReturn(false);

            // when
            UserResponse.Info response = userService.updateNickname(1L, request);

            // then
            assertThat(testUser.getNickname()).isEqualTo("새닉네임");
        }

        @Test
        @DisplayName("실패: 닉네임 중복")
        void updateNickname_Duplicate() {
            // given
            UserRequest.UpdateNickname request = UserRequest.UpdateNickname.builder()
                    .nickname("중복닉네임")
                    .build();

            given(userRepository.findByIdAndDeletedAtIsNull(1L)).willReturn(Optional.of(testUser));
            given(userRepository.existsByNickname("중복닉네임")).willReturn(true);

            // when & then
            assertThatThrownBy(() -> userService.updateNickname(1L, request))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.DUPLICATE_NICKNAME);
        }
    }

    @Nested
    @DisplayName("프로필 이미지 수정")
    class UpdateProfileImage {

        @Test
        @DisplayName("성공: 프로필 이미지 수정")
        void updateProfileImage_Success() {
            // given
            String imageUrl = "https://storage.example.com/profiles/1/image.jpg";
            given(userRepository.findByIdAndDeletedAtIsNull(1L)).willReturn(Optional.of(testUser));

            // when
            String result = userService.updateProfileImage(1L, imageUrl);

            // then
            assertThat(result).isEqualTo(imageUrl);
            assertThat(testUser.getProfileImageUrl()).isEqualTo(imageUrl);
        }
    }

    @Nested
    @DisplayName("내 비밀번호 변경")
    class ChangeMyPassword {

        @Test
        @DisplayName("성공: 비밀번호 변경")
        void changeMyPassword_Success() {
            // given
            UserRequest.ChangeMyPassword request = UserRequest.ChangeMyPassword.builder()
                    .currentPassword("password123")
                    .newPassword("newPassword456")
                    .build();

            given(userRepository.findByIdAndDeletedAtIsNull(1L)).willReturn(Optional.of(testUser));
            given(passwordEncoder.matches("password123", "encodedPassword")).willReturn(true);
            given(passwordEncoder.encode("newPassword456")).willReturn("newEncodedPassword");

            // when
            userService.changeMyPassword(1L, request);

            // then
            verify(passwordEncoder).encode("newPassword456");
        }

        @Test
        @DisplayName("실패: 소셜 로그인 사용자")
        void changeMyPassword_SocialUser() {
            // given
            User socialUser = User.builder()
                    .id(2L)
                    .email("social@kakao.com")
                    .password("encodedPassword")
                    .name("소셜사용자")
                    .provider(AuthProvider.KAKAO)
                    .providerId("kakao_123")
                    .role(UserRole.CUSTOMER)
                    .status(UserStatus.ACTIVE)
                    .build();

            UserRequest.ChangeMyPassword request = UserRequest.ChangeMyPassword.builder()
                    .currentPassword("password123")
                    .newPassword("newPassword456")
                    .build();

            given(userRepository.findByIdAndDeletedAtIsNull(2L)).willReturn(Optional.of(socialUser));

            // when & then
            assertThatThrownBy(() -> userService.changeMyPassword(2L, request))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.SOCIAL_LOGIN_USER);
        }

        @Test
        @DisplayName("실패: 현재 비밀번호 불일치")
        void changeMyPassword_WrongCurrentPassword() {
            // given
            UserRequest.ChangeMyPassword request = UserRequest.ChangeMyPassword.builder()
                    .currentPassword("wrongPassword")
                    .newPassword("newPassword456")
                    .build();

            given(userRepository.findByIdAndDeletedAtIsNull(1L)).willReturn(Optional.of(testUser));
            given(passwordEncoder.matches("wrongPassword", "encodedPassword")).willReturn(false);

            // when & then
            assertThatThrownBy(() -> userService.changeMyPassword(1L, request))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_PASSWORD);
        }

        @Test
        @DisplayName("실패: 취약한 새 비밀번호")
        void changeMyPassword_WeakPassword() {
            // given
            UserRequest.ChangeMyPassword request = UserRequest.ChangeMyPassword.builder()
                    .currentPassword("password123")
                    .newPassword("weak")
                    .build();

            given(userRepository.findByIdAndDeletedAtIsNull(1L)).willReturn(Optional.of(testUser));
            given(passwordEncoder.matches("password123", "encodedPassword")).willReturn(true);

            // when & then
            assertThatThrownBy(() -> userService.changeMyPassword(1L, request))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.WEAK_PASSWORD);
        }
    }

    @Nested
    @DisplayName("회원 탈퇴")
    class DeleteMyAccount {

        @Test
        @DisplayName("성공: 회원 탈퇴")
        void deleteMyAccount_Success() {
            // given
            UserRequest.DeleteAccount request = UserRequest.DeleteAccount.builder()
                    .password("password123")
                    .build();

            given(userRepository.findByIdAndDeletedAtIsNull(1L)).willReturn(Optional.of(testUser));
            given(passwordEncoder.matches("password123", "encodedPassword")).willReturn(true);

            // when
            userService.deleteMyAccount(1L, request);

            // then
            assertThat(testUser.getStatus()).isEqualTo(UserStatus.DELETED);
            assertThat(testUser.getDeletedAt()).isNotNull();
        }

        @Test
        @DisplayName("실패: 비밀번호 불일치")
        void deleteMyAccount_WrongPassword() {
            // given
            UserRequest.DeleteAccount request = UserRequest.DeleteAccount.builder()
                    .password("wrongPassword")
                    .build();

            given(userRepository.findByIdAndDeletedAtIsNull(1L)).willReturn(Optional.of(testUser));
            given(passwordEncoder.matches("wrongPassword", "encodedPassword")).willReturn(false);

            // when & then
            assertThatThrownBy(() -> userService.deleteMyAccount(1L, request))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_PASSWORD);
        }

        @Test
        @DisplayName("성공: 소셜 로그인 사용자 탈퇴 (비밀번호 확인 생략)")
        void deleteMyAccount_SocialUser() {
            // given
            User socialUser = User.builder()
                    .id(2L)
                    .email("social@kakao.com")
                    .password("encodedPassword")
                    .name("소셜사용자")
                    .provider(AuthProvider.KAKAO)
                    .providerId("kakao_123")
                    .role(UserRole.CUSTOMER)
                    .status(UserStatus.ACTIVE)
                    .build();

            UserRequest.DeleteAccount request = UserRequest.DeleteAccount.builder()
                    .password("anyPassword")
                    .build();

            given(userRepository.findByIdAndDeletedAtIsNull(2L)).willReturn(Optional.of(socialUser));

            // when
            userService.deleteMyAccount(2L, request);

            // then
            assertThat(socialUser.getStatus()).isEqualTo(UserStatus.DELETED);
        }
    }

    @Nested
    @DisplayName("비밀번호 유효성 검사 - 누락된 브랜치")
    class IsValidPasswordAdditionalTests {

        @Test
        @DisplayName("실패: 숫자만 포함 (영문 없음)")
        void changeMyPassword_PasswordOnlyDigits() {
            // given - 숫자만 있는 비밀번호 (8자 이상이지만 영문 없음)
            UserRequest.ChangeMyPassword request = UserRequest.ChangeMyPassword.builder()
                    .currentPassword("password123")
                    .newPassword("12345678")
                    .build();

            given(userRepository.findByIdAndDeletedAtIsNull(1L)).willReturn(Optional.of(testUser));
            given(passwordEncoder.matches("password123", "encodedPassword")).willReturn(true);

            // when & then
            assertThatThrownBy(() -> userService.changeMyPassword(1L, request))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.WEAK_PASSWORD);
        }

        @Test
        @DisplayName("실패: 영문만 포함 (숫자 없음)")
        void changeMyPassword_PasswordOnlyLetters() {
            // given - 영문만 있는 비밀번호 (8자 이상이지만 숫자 없음)
            UserRequest.ChangeMyPassword request = UserRequest.ChangeMyPassword.builder()
                    .currentPassword("password123")
                    .newPassword("abcdefgh")
                    .build();

            given(userRepository.findByIdAndDeletedAtIsNull(1L)).willReturn(Optional.of(testUser));
            given(passwordEncoder.matches("password123", "encodedPassword")).willReturn(true);

            // when & then
            assertThatThrownBy(() -> userService.changeMyPassword(1L, request))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.WEAK_PASSWORD);
        }

        @Test
        @DisplayName("실패: null 비밀번호")
        void changeMyPassword_PasswordNull() {
            // given
            UserRequest.ChangeMyPassword request = UserRequest.ChangeMyPassword.builder()
                    .currentPassword("password123")
                    .newPassword(null)
                    .build();

            given(userRepository.findByIdAndDeletedAtIsNull(1L)).willReturn(Optional.of(testUser));
            given(passwordEncoder.matches("password123", "encodedPassword")).willReturn(true);

            // when & then
            assertThatThrownBy(() -> userService.changeMyPassword(1L, request))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.WEAK_PASSWORD);
        }
    }

    @Nested
    @DisplayName("사용자 생성 - 누락된 브랜치")
    class CreateUserAdditionalTests {

        @Test
        @DisplayName("성공: 전화번호 없이 사용자 생성")
        void createUser_WithoutPhone() {
            // given
            UserRequest.Create requestWithoutPhone = UserRequest.Create.builder()
                    .email("test2@example.com")
                    .password("password123")
                    .name("테스트사용자2")
                    .phone(null)
                    .build();

            User userWithoutPhone = User.builder()
                    .id(2L)
                    .email("test2@example.com")
                    .password("encodedPassword")
                    .name("테스트사용자2")
                    .role(UserRole.CUSTOMER)
                    .status(UserStatus.ACTIVE)
                    .build();

            given(userRepository.existsByEmail(anyString())).willReturn(false);
            given(passwordEncoder.encode(anyString())).willReturn("encodedPassword");
            given(userRepository.save(any(User.class))).willReturn(userWithoutPhone);

            // when
            UserResponse.Info response = userService.createUser(requestWithoutPhone);

            // then
            assertThat(response).isNotNull();
            // existsByPhone should NOT be called when phone is null
            verify(userRepository, never()).existsByPhone(anyString());
        }

        @Test
        @DisplayName("성공: 역할 지정 없이 사용자 생성 (기본 USER)")
        void createUser_WithoutRole() {
            // given
            UserRequest.Create requestWithoutRole = UserRequest.Create.builder()
                    .email("test3@example.com")
                    .password("password123")
                    .name("테스트사용자3")
                    .phone("010-1111-2222")
                    .role(null)
                    .build();

            User savedUser = User.builder()
                    .id(3L)
                    .email("test3@example.com")
                    .password("encodedPassword")
                    .name("테스트사용자3")
                    .role(UserRole.CUSTOMER)
                    .status(UserStatus.ACTIVE)
                    .build();

            given(userRepository.existsByEmail(anyString())).willReturn(false);
            given(userRepository.existsByPhone(anyString())).willReturn(false);
            given(passwordEncoder.encode(anyString())).willReturn("encodedPassword");
            given(userRepository.save(any(User.class))).willReturn(savedUser);

            // when
            UserResponse.Info response = userService.createUser(requestWithoutRole);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getRole()).isEqualTo(UserRole.CUSTOMER);
        }

        @Test
        @DisplayName("성공: 역할 지정하여 사용자 생성")
        void createUser_WithRole() {
            // given
            UserRequest.Create requestWithRole = UserRequest.Create.builder()
                    .email("admin@example.com")
                    .password("password123")
                    .name("관리자")
                    .phone("010-3333-4444")
                    .role(UserRole.ADMIN)
                    .build();

            User adminUser = User.builder()
                    .id(4L)
                    .email("admin@example.com")
                    .password("encodedPassword")
                    .name("관리자")
                    .role(UserRole.ADMIN)
                    .status(UserStatus.ACTIVE)
                    .build();

            given(userRepository.existsByEmail(anyString())).willReturn(false);
            given(userRepository.existsByPhone(anyString())).willReturn(false);
            given(passwordEncoder.encode(anyString())).willReturn("encodedPassword");
            given(userRepository.save(any(User.class))).willReturn(adminUser);

            // when
            UserResponse.Info response = userService.createUser(requestWithRole);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getRole()).isEqualTo(UserRole.ADMIN);
        }
    }

    @Nested
    @DisplayName("사용자 수정 - 누락된 브랜치")
    class UpdateUserAdditionalTests {

        @Test
        @DisplayName("성공: 모든 필드가 null인 경우 기존 값 유지")
        void updateUser_AllNullFieldsPreserveExisting() {
            // given
            UserRequest.Update updateRequest = UserRequest.Update.builder()
                    .name(null)
                    .phone(null)
                    .profileImageUrl(null)
                    .build();

            String originalName = testUser.getName();
            String originalPhone = testUser.getPhone();

            given(userRepository.findByIdAndDeletedAtIsNull(1L)).willReturn(Optional.of(testUser));

            // when
            UserResponse.Info response = userService.updateUser(1L, updateRequest);

            // then
            assertThat(response).isNotNull();
            assertThat(testUser.getName()).isEqualTo(originalName);
            assertThat(testUser.getPhone()).isEqualTo(originalPhone);
        }

        @Test
        @DisplayName("성공: 일부 필드만 업데이트")
        void updateUser_PartialUpdate() {
            // given
            UserRequest.Update updateRequest = UserRequest.Update.builder()
                    .name("새이름")
                    .phone(null)
                    .profileImageUrl("http://image.url")
                    .build();

            String originalPhone = testUser.getPhone();

            given(userRepository.findByIdAndDeletedAtIsNull(1L)).willReturn(Optional.of(testUser));

            // when
            UserResponse.Info response = userService.updateUser(1L, updateRequest);

            // then
            assertThat(testUser.getName()).isEqualTo("새이름");
            assertThat(testUser.getPhone()).isEqualTo(originalPhone);
            assertThat(testUser.getProfileImageUrl()).isEqualTo("http://image.url");
        }
    }

    @Nested
    @DisplayName("상태 변경 - 누락된 브랜치")
    class ChangeStatusAdditionalTests {

        @Test
        @DisplayName("실패: 지원하지 않는 상태 (INACTIVE)")
        void changeUserStatus_UnsupportedStatus() {
            // given - INACTIVE status is not supported in the switch (only ACTIVE, SUSPENDED, DELETED)
            given(userRepository.findByIdAndDeletedAtIsNull(1L)).willReturn(Optional.of(testUser));

            // when & then
            assertThatThrownBy(() -> userService.changeUserStatus(1L, UserStatus.INACTIVE))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_INPUT);
        }
    }

    @Nested
    @DisplayName("닉네임 수정 - 누락된 브랜치")
    class UpdateNicknameAdditionalTests {

        @Test
        @DisplayName("성공: 현재 닉네임과 동일한 닉네임으로 변경 시도 (중복 체크 생략)")
        void updateNickname_SameAsCurrent() {
            // given
            testUser.updateNickname("현재닉네임");
            UserRequest.UpdateNickname request = UserRequest.UpdateNickname.builder()
                    .nickname("현재닉네임")
                    .build();

            given(userRepository.findByIdAndDeletedAtIsNull(1L)).willReturn(Optional.of(testUser));
            // existsByNickname should NOT be called

            // when
            UserResponse.Info response = userService.updateNickname(1L, request);

            // then
            assertThat(response).isNotNull();
            assertThat(testUser.getNickname()).isEqualTo("현재닉네임");
            // Verify existsByNickname was never called (same nickname check)
            verify(userRepository, never()).existsByNickname(anyString());
        }
    }

    @Nested
    @DisplayName("추가 관리자 기능")
    class AdditionalAdminOperations {

        @Test
        @DisplayName("성공: 이메일로 사용자 조회")
        void getUserByEmail_Success() {
            // given
            given(userRepository.findByEmailAndDeletedAtIsNull("test@example.com")).willReturn(Optional.of(testUser));

            // when
            UserResponse.Info response = userService.getUserByEmail("test@example.com");

            // then
            assertThat(response).isNotNull();
            assertThat(response.getEmail()).isEqualTo("test@example.com");
        }

        @Test
        @DisplayName("실패: 이메일로 사용자 조회 - 사용자 없음")
        void getUserByEmail_NotFound() {
            // given
            given(userRepository.findByEmailAndDeletedAtIsNull("notfound@example.com")).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> userService.getUserByEmail("notfound@example.com"))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_NOT_FOUND);
        }

        @Test
        @DisplayName("성공: 상태별 사용자 조회")
        void getUsersByStatus_Success() {
            // given
            Page<User> userPage = new PageImpl<>(List.of(testUser));
            Pageable pageable = PageRequest.of(0, 10);
            given(userRepository.findAllByStatus(UserStatus.ACTIVE, pageable)).willReturn(userPage);

            // when
            Page<UserResponse.Simple> response = userService.getUsersByStatus(UserStatus.ACTIVE, pageable);

            // then
            assertThat(response.getContent()).hasSize(1);
        }

        @Test
        @DisplayName("성공: 사용자 역할 변경")
        void changeUserRole_Success() {
            // given
            given(userRepository.findByIdAndDeletedAtIsNull(1L)).willReturn(Optional.of(testUser));

            // when
            UserResponse.Info response = userService.changeUserRole(1L, UserRole.ADMIN);

            // then
            assertThat(testUser.getRole()).isEqualTo(UserRole.ADMIN);
            assertThat(response).isNotNull();
        }

        @Test
        @DisplayName("성공: 마지막 로그인 시간 업데이트")
        void updateLastLogin_Success() {
            // given
            given(userRepository.findByIdAndDeletedAtIsNull(1L)).willReturn(Optional.of(testUser));

            // when
            userService.updateLastLogin(1L);

            // then
            assertThat(testUser.getLastLoginAt()).isNotNull();
        }

        @Test
        @DisplayName("성공: 사용자 상태 삭제로 변경")
        void changeUserStatus_ToDeleted() {
            // given
            given(userRepository.findByIdAndDeletedAtIsNull(1L)).willReturn(Optional.of(testUser));

            // when
            UserResponse.Info response = userService.changeUserStatus(1L, UserStatus.DELETED);

            // then
            assertThat(response).isNotNull();
            assertThat(testUser.getStatus()).isEqualTo(UserStatus.DELETED);
        }
    }
}
