package com.findplace.domain.user.service;

import com.findplace.domain.user.dto.*;
import com.findplace.domain.user.entity.User;
import com.findplace.domain.user.repository.UserRepository;
import com.findplace.global.exception.BusinessException;
import com.findplace.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 사용자 프로필 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserProfileService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * 내 프로필 조회
     */
    public ProfileResponse getProfile(Long userId) {
        User user = findUserById(userId);
        return ProfileResponse.from(user);
    }

    /**
     * 프로필 수정
     */
    @Transactional
    public ProfileResponse updateProfile(Long userId, ProfileUpdateRequest request) {
        User user = findUserById(userId);

        user.updateProfile(request.getName(), request.getPhone(), request.getProfileImageUrl());

        if (request.getBirthDate() != null) {
            user.updateBirthDate(request.getBirthDate());
        }

        log.info("프로필 수정: userId={}", userId);
        return ProfileResponse.from(user);
    }

    /**
     * 비밀번호 변경
     */
    @Transactional
    public void changePassword(Long userId, PasswordChangeRequest request) {
        User user = findUserById(userId);

        // 현재 비밀번호 확인
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new BusinessException(ErrorCode.INVALID_PASSWORD);
        }

        // 새 비밀번호 확인
        if (!request.getNewPassword().equals(request.getNewPasswordConfirm())) {
            throw new BusinessException(ErrorCode.PASSWORD_MISMATCH);
        }

        // 비밀번호 변경
        user.updatePassword(passwordEncoder.encode(request.getNewPassword()));
        log.info("비밀번호 변경: userId={}", userId);
    }

    /**
     * 주소 변경
     */
    @Transactional
    public ProfileResponse updateAddress(Long userId, AddressUpdateRequest request) {
        User user = findUserById(userId);

        user.updateAddress(
                request.getAddress(),
                request.getAddressDetail(),
                request.getZipCode(),
                request.getLatitude(),
                request.getLongitude()
        );

        log.info("주소 변경: userId={}", userId);
        return ProfileResponse.from(user);
    }

    /**
     * 회원 탈퇴
     */
    @Transactional
    public void withdraw(Long userId, String password) {
        User user = findUserById(userId);

        // 비밀번호 확인
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BusinessException(ErrorCode.INVALID_PASSWORD);
        }

        // Soft Delete
        user.softDelete(userId);
        log.info("회원 탈퇴: userId={}", userId);
    }

    private User findUserById(Long userId) {
        return userRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }
}
