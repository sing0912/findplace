package com.findplace.domain.user.service;

import com.findplace.domain.user.dto.UserRequest;
import com.findplace.domain.user.dto.UserResponse;
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
                .role(UserRole.USER)
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
}
