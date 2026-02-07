/**
 * @fileoverview 비밀번호 변경 페이지
 * @see docs/develop/user/frontend.md - 섹션 8.2
 */

import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Box, Container, Typography, Alert } from '@mui/material';
import { AuthInput, AuthButton } from '../../components/auth';
import { useAuthStore } from '../../stores/authStore';

const ChangePasswordPage: React.FC = () => {
  const navigate = useNavigate();
  const { logout } = useAuthStore();

  const [currentPassword, setCurrentPassword] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');

  const [currentPasswordError, setCurrentPasswordError] = useState('');
  const [newPasswordError, setNewPasswordError] = useState('');
  const [confirmPasswordError, setConfirmPasswordError] = useState('');

  const [isSaving, setIsSaving] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  const validateNewPassword = (value: string): string => {
    if (!value) return '';
    if (value.length < 8 || value.length > 50) {
      return '비밀번호는 8자 이상 50자 이하여야 합니다.';
    }
    const hasLetter = /[a-zA-Z]/.test(value);
    const hasDigit = /\d/.test(value);
    if (!hasLetter || !hasDigit) {
      return '비밀번호는 영문과 숫자를 포함해야 합니다.';
    }
    return '';
  };

  const handleNewPasswordBlur = () => {
    const err = validateNewPassword(newPassword);
    setNewPasswordError(err);
    if (confirmPassword && newPassword !== confirmPassword) {
      setConfirmPasswordError('비밀번호가 일치하지 않습니다.');
    } else {
      setConfirmPasswordError('');
    }
  };

  const handleConfirmPasswordBlur = () => {
    if (!confirmPassword) {
      setConfirmPasswordError('');
      return;
    }
    if (newPassword !== confirmPassword) {
      setConfirmPasswordError('비밀번호가 일치하지 않습니다.');
    } else {
      setConfirmPasswordError('');
    }
  };

  const isFormValid = (): boolean => {
    return (
      currentPassword.length > 0 &&
      newPassword.length >= 8 &&
      confirmPassword.length > 0 &&
      newPassword === confirmPassword &&
      !newPasswordError &&
      !confirmPasswordError
    );
  };

  const handleSubmit = async () => {
    if (!currentPassword) {
      setCurrentPasswordError('현재 비밀번호를 입력해주세요.');
      return;
    }

    const pwError = validateNewPassword(newPassword);
    if (pwError) {
      setNewPasswordError(pwError);
      return;
    }

    if (newPassword !== confirmPassword) {
      setConfirmPasswordError('비밀번호가 일치하지 않습니다.');
      return;
    }

    setIsSaving(true);
    setError('');
    setSuccess('');

    try {
      const token = localStorage.getItem('accessToken');
      if (!token) {
        logout();
        navigate('/login');
        return;
      }

      const response = await fetch('/api/v1/users/me/password', {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
          Authorization: `Bearer ${token}`,
        },
        body: JSON.stringify({
          currentPassword,
          newPassword,
        }),
      });

      if (!response.ok) {
        if (response.status === 401 || response.status === 403) {
          logout();
          navigate('/login');
          return;
        }

        const errorData = await response.json().catch(() => ({}));
        const errorCode = errorData.error?.code;
        const errorMessage = errorData.error?.message;

        if (errorCode === 'A009') {
          setError('소셜 로그인 사용자는 비밀번호를 변경할 수 없습니다.');
        } else if (errorCode === 'U004') {
          setCurrentPasswordError('현재 비밀번호가 일치하지 않습니다.');
        } else if (errorCode === 'U009') {
          setNewPasswordError('비밀번호는 8자 이상, 영문과 숫자를 포함해야 합니다.');
        } else {
          setError(errorMessage || '비밀번호 변경에 실패했습니다.');
        }
        return;
      }

      setSuccess('비밀번호가 변경되었습니다.');
      setTimeout(() => {
        navigate('/mypage');
      }, 1500);
    } catch (err) {
      setError('알 수 없는 오류가 발생했습니다.');
    } finally {
      setIsSaving(false);
    }
  };

  return (
    <Container maxWidth="sm">
      <Box
        sx={{
          minHeight: '100vh',
          py: 4,
          pb: 10,
        }}
      >
        {/* 헤더 */}
        <Box sx={{ mb: 4 }}>
          <Typography
            variant="h6"
            sx={{
              fontFamily: 'Noto Sans, sans-serif',
              fontWeight: 700,
              fontSize: '16px',
              color: '#000000',
              textAlign: 'center',
            }}
          >
            비밀번호 변경
          </Typography>
        </Box>

        {/* 폼 */}
        <Box sx={{ px: 2 }}>
          {error && (
            <Alert severity="error" sx={{ mb: 2 }}>
              {error}
            </Alert>
          )}
          {success && (
            <Alert severity="success" sx={{ mb: 2 }}>
              {success}
            </Alert>
          )}

          <AuthInput
            label="현재 비밀번호"
            name="currentPassword"
            type="password"
            placeholder="현재 비밀번호를 입력해주세요"
            value={currentPassword}
            onChange={(e) => {
              setCurrentPassword(e.target.value);
              setCurrentPasswordError('');
            }}
            error={!!currentPasswordError}
            helperText={currentPasswordError}
            required
          />

          <AuthInput
            label="새 비밀번호"
            name="newPassword"
            type="password"
            placeholder="8자 이상, 영문과 숫자를 포함"
            value={newPassword}
            onChange={(e) => {
              setNewPassword(e.target.value);
              setNewPasswordError('');
            }}
            onBlur={handleNewPasswordBlur}
            error={!!newPasswordError}
            helperText={newPasswordError}
            isValid={newPassword.length >= 8 && !newPasswordError && !validateNewPassword(newPassword)}
            required
          />

          <AuthInput
            label="새 비밀번호 확인"
            name="confirmPassword"
            type="password"
            placeholder="새 비밀번호를 한 번 더 입력"
            value={confirmPassword}
            onChange={(e) => {
              setConfirmPassword(e.target.value);
              setConfirmPasswordError('');
            }}
            onBlur={handleConfirmPasswordBlur}
            error={!!confirmPasswordError}
            helperText={confirmPasswordError}
            isValid={confirmPassword.length > 0 && newPassword === confirmPassword && !confirmPasswordError}
            required
          />
        </Box>

        {/* 변경 버튼 */}
        <Box
          sx={{
            position: 'fixed',
            bottom: 0,
            left: 0,
            right: 0,
            p: 2,
            backgroundColor: '#FFFFFF',
          }}
        >
          <AuthButton
            onClick={handleSubmit}
            disabled={!isFormValid()}
            loading={isSaving}
            fullWidth
          >
            변경하기
          </AuthButton>
        </Box>
      </Box>
    </Container>
  );
};

export default ChangePasswordPage;
