package com.findplace.domain.admin.service;

import com.findplace.domain.admin.dto.*;
import com.findplace.domain.admin.entity.UserRoleChangeLog;
import com.findplace.domain.admin.entity.UserStatusChangeLog;
import com.findplace.domain.admin.repository.UserRoleChangeLogRepository;
import com.findplace.domain.admin.repository.UserStatusChangeLogRepository;
import com.findplace.domain.user.entity.User;
import com.findplace.domain.user.entity.UserRole;
import com.findplace.domain.user.entity.UserStatus;
import com.findplace.domain.user.repository.UserRepository;
import com.findplace.global.exception.BusinessException;
import com.findplace.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 관리자 사용자 관리 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminUserService {

    private final UserRepository userRepository;
    private final UserStatusChangeLogRepository statusChangeLogRepository;
    private final UserRoleChangeLogRepository roleChangeLogRepository;

    /**
     * 사용자 목록 조회
     */
    public Page<AdminUserResponse> getUsers(Pageable pageable) {
        return userRepository.findAllActive(pageable)
                .map(AdminUserResponse::from);
    }

    /**
     * 키워드로 사용자 검색
     */
    public Page<AdminUserResponse> searchUsers(String keyword, Pageable pageable) {
        return userRepository.searchByKeyword(keyword, pageable)
                .map(AdminUserResponse::from);
    }

    /**
     * 상태별 사용자 조회
     */
    public Page<AdminUserResponse> getUsersByStatus(UserStatus status, Pageable pageable) {
        return userRepository.findAllByStatus(status, pageable)
                .map(AdminUserResponse::from);
    }

    /**
     * 사용자 상세 조회
     */
    public AdminUserResponse getUser(Long userId) {
        User user = findUserById(userId);
        return AdminUserResponse.from(user);
    }

    /**
     * 사용자 상태 변경
     */
    @Transactional
    public AdminUserResponse changeStatus(Long userId, StatusChangeRequest request, Long adminId) {
        User user = findUserById(userId);

        // 본인 상태 변경 불가
        if (userId.equals(adminId)) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }

        // 이전 상태 저장
        UserStatus previousStatus = user.getStatus();

        // 상태 변경
        switch (request.getStatus()) {
            case ACTIVE -> user.activate();
            case SUSPENDED -> user.suspend();
            case DELETED -> user.softDelete(adminId);
            case INACTIVE -> throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }

        // 변경 이력 저장
        UserStatusChangeLog changeLog = UserStatusChangeLog.create(
                userId, previousStatus, request.getStatus(), request.getReason(), adminId);
        statusChangeLogRepository.save(changeLog);

        log.info("사용자 상태 변경: userId={}, {} -> {}, by={}",
                userId, previousStatus, request.getStatus(), adminId);

        return AdminUserResponse.from(user);
    }

    /**
     * 사용자 역할 변경 (SUPER_ADMIN만 가능)
     */
    @Transactional
    public AdminUserResponse changeRole(Long userId, RoleChangeRequest request, Long adminId) {
        User user = findUserById(userId);

        // 본인 역할 변경 불가
        if (userId.equals(adminId)) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }

        // 이전 역할 저장
        UserRole previousRole = user.getRole();

        // 역할 변경
        user.changeRole(request.getRole());

        // 변경 이력 저장
        UserRoleChangeLog changeLog = UserRoleChangeLog.create(
                userId, previousRole, request.getRole(), request.getReason(), adminId);
        roleChangeLogRepository.save(changeLog);

        log.info("사용자 역할 변경: userId={}, {} -> {}, by={}",
                userId, previousRole, request.getRole(), adminId);

        return AdminUserResponse.from(user);
    }

    /**
     * 상태 변경 이력 조회
     */
    public List<StatusChangeLogResponse> getStatusHistory(Long userId) {
        return statusChangeLogRepository.findByUserIdOrderByChangedAtDesc(userId)
                .stream()
                .map(StatusChangeLogResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 역할 변경 이력 조회
     */
    public List<RoleChangeLogResponse> getRoleHistory(Long userId) {
        return roleChangeLogRepository.findByUserIdOrderByChangedAtDesc(userId)
                .stream()
                .map(RoleChangeLogResponse::from)
                .collect(Collectors.toList());
    }

    private User findUserById(Long userId) {
        return userRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }
}
