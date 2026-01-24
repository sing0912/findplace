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
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 사용자 서비스 클래스
 *
 * 사용자 관련 비즈니스 로직을 처리하는 서비스
 * - @Transactional: Master DB로 라우팅 (쓰기 작업)
 * - @Transactional(readOnly = true): Slave DB로 라우팅 (읽기 작업)
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * 사용자 생성 (Master DB)
     *
     * @param request 사용자 생성 요청 DTO
     * @return 생성된 사용자 정보
     * @throws BusinessException 이메일 또는 전화번호 중복 시
     */
    @Transactional
    public UserResponse.Info createUser(UserRequest.Create request) {
        // 이메일 중복 체크
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException(ErrorCode.DUPLICATE_EMAIL);
        }

        // 전화번호 중복 체크
        if (request.getPhone() != null && userRepository.existsByPhone(request.getPhone())) {
            throw new BusinessException(ErrorCode.DUPLICATE_PHONE);
        }

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .name(request.getName())
                .phone(request.getPhone())
                .role(request.getRole() != null ? request.getRole() : UserRole.USER)
                .status(UserStatus.ACTIVE)
                .build();

        User savedUser = userRepository.save(user);
        return UserResponse.Info.from(savedUser);
    }

    /**
     * 사용자 조회 (Slave DB - Round Robin)
     *
     * @param id 사용자 ID
     * @return 사용자 정보
     * @throws EntityNotFoundException 사용자가 존재하지 않을 때
     */
    @Transactional(readOnly = true)
    public UserResponse.Info getUser(Long id) {
        User user = findUserById(id);
        return UserResponse.Info.from(user);
    }

    /**
     * 이메일로 사용자 조회 (Slave DB)
     *
     * @param email 이메일
     * @return 사용자 정보
     * @throws EntityNotFoundException 사용자가 존재하지 않을 때
     */
    @Transactional(readOnly = true)
    public UserResponse.Info getUserByEmail(String email) {
        User user = userRepository.findByEmailAndDeletedAtIsNull(email)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.USER_NOT_FOUND, email));
        return UserResponse.Info.from(user);
    }

    /**
     * 사용자 목록 조회 (Slave DB)
     *
     * @param pageable 페이지네이션 정보
     * @return 사용자 목록 페이지
     */
    @Transactional(readOnly = true)
    public Page<UserResponse.Simple> getUsers(Pageable pageable) {
        return userRepository.findAllActive(pageable)
                .map(UserResponse.Simple::from);
    }

    /**
     * 상태별 사용자 목록 조회 (Slave DB)
     *
     * @param status 계정 상태
     * @param pageable 페이지네이션 정보
     * @return 사용자 목록 페이지
     */
    @Transactional(readOnly = true)
    public Page<UserResponse.Simple> getUsersByStatus(UserStatus status, Pageable pageable) {
        return userRepository.findAllByStatus(status, pageable)
                .map(UserResponse.Simple::from);
    }

    /**
     * 사용자 검색 (Slave DB)
     *
     * @param keyword 검색 키워드 (이름, 이메일)
     * @param pageable 페이지네이션 정보
     * @return 검색 결과 페이지
     */
    @Transactional(readOnly = true)
    public Page<UserResponse.Simple> searchUsers(String keyword, Pageable pageable) {
        return userRepository.searchByKeyword(keyword, pageable)
                .map(UserResponse.Simple::from);
    }

    /**
     * 사용자 정보 수정 (Master DB)
     *
     * @param id 사용자 ID
     * @param request 수정 요청 DTO
     * @return 수정된 사용자 정보
     */
    @Transactional
    public UserResponse.Info updateUser(Long id, UserRequest.Update request) {
        User user = findUserById(id);
        user.updateProfile(
                request.getName() != null ? request.getName() : user.getName(),
                request.getPhone() != null ? request.getPhone() : user.getPhone(),
                request.getProfileImageUrl() != null ? request.getProfileImageUrl() : user.getProfileImageUrl()
        );
        return UserResponse.Info.from(user);
    }

    /**
     * 비밀번호 변경 (Master DB)
     *
     * @param id 사용자 ID
     * @param request 비밀번호 변경 요청 DTO
     * @throws BusinessException 현재 비밀번호가 일치하지 않을 때
     */
    @Transactional
    public void updatePassword(Long id, UserRequest.UpdatePassword request) {
        User user = findUserById(id);

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS, "현재 비밀번호가 일치하지 않습니다.");
        }

        user.updatePassword(passwordEncoder.encode(request.getNewPassword()));
    }

    /**
     * 사용자 삭제 (Soft Delete, Master DB)
     *
     * 실제로 데이터를 삭제하지 않고 삭제 플래그만 설정
     *
     * @param id 삭제할 사용자 ID
     * @param deletedBy 삭제를 수행한 관리자 ID
     */
    @Transactional
    public void deleteUser(Long id, Long deletedBy) {
        User user = findUserById(id);
        user.softDelete(deletedBy);
    }

    /**
     * 사용자 상태 변경 (Master DB)
     *
     * @param id 사용자 ID
     * @param status 변경할 상태
     * @return 변경된 사용자 정보
     * @throws BusinessException 지원하지 않는 상태일 때
     */
    @Transactional
    public UserResponse.Info changeUserStatus(Long id, UserStatus status) {
        User user = findUserById(id);

        switch (status) {
            case ACTIVE -> user.activate();
            case SUSPENDED -> user.suspend();
            case DELETED -> user.softDelete(null);
            default -> throw new BusinessException(ErrorCode.INVALID_INPUT, "지원하지 않는 상태입니다.");
        }

        return UserResponse.Info.from(user);
    }

    /**
     * 역할 변경 (Master DB)
     *
     * @param id 사용자 ID
     * @param role 변경할 역할
     * @return 변경된 사용자 정보
     */
    @Transactional
    public UserResponse.Info changeUserRole(Long id, UserRole role) {
        User user = findUserById(id);
        user.changeRole(role);
        return UserResponse.Info.from(user);
    }

    /**
     * 마지막 로그인 시간 업데이트 (Master DB)
     *
     * 로그인 성공 시 호출되어 마지막 로그인 시간을 갱신
     *
     * @param id 사용자 ID
     */
    @Transactional
    public void updateLastLogin(Long id) {
        User user = findUserById(id);
        user.updateLastLogin();
    }

    /**
     * ID로 사용자 조회 (내부 메서드)
     *
     * @param id 사용자 ID
     * @return User 엔티티
     * @throws EntityNotFoundException 사용자가 존재하지 않을 때
     */
    private User findUserById(Long id) {
        return userRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.USER_NOT_FOUND, id));
    }
}
