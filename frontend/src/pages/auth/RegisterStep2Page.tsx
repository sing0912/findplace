/**
 * @fileoverview 회원가입 2단계 - 정보 입력
 * @see docs/develop/user/frontend.md - 섹션 6.2
 */

import React, { useState } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { Box, Container, Typography, Alert } from '@mui/material';
import { AuthInput, AuthButton } from '../../components/auth';

interface FormData {
  email: string;
  password: string;
  passwordConfirm: string;
  nickname: string;
}

interface FormErrors {
  email: string;
  password: string;
  passwordConfirm: string;
  nickname: string;
}

interface FormValid {
  email: boolean;
  password: boolean;
  passwordConfirm: boolean;
  nickname: boolean;
}

const RegisterStep2Page: React.FC = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const marketingAgreed = location.state?.marketingAgreed ?? false;

  const [formData, setFormData] = useState<FormData>({
    email: '',
    password: '',
    passwordConfirm: '',
    nickname: '',
  });

  const [errors, setErrors] = useState<FormErrors>({
    email: '',
    password: '',
    passwordConfirm: '',
    nickname: '',
  });

  const [valid, setValid] = useState<FormValid>({
    email: false,
    password: false,
    passwordConfirm: false,
    nickname: false,
  });

  const [isLoading, setIsLoading] = useState(false);
  const [serverError, setServerError] = useState('');

  // 이메일 유효성 검사
  const validateEmail = (email: string): string => {
    if (!email) return '';
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailRegex.test(email)) {
      return '올바른 이메일 형식이 아닙니다.';
    }
    return '';
  };

  // 비밀번호 유효성 검사
  const validatePassword = (password: string): string => {
    if (!password) return '';
    const passwordRegex = /^(?=.*[A-Za-z])(?=.*\d)[A-Za-z\d@$!%*#?&]{8,}$/;
    if (!passwordRegex.test(password)) {
      return '비밀번호는 8자 이상, 영문과 숫자를 포함해야 합니다.';
    }
    return '';
  };

  // 비밀번호 확인 검사
  const validatePasswordConfirm = (password: string, confirm: string): string => {
    if (!confirm) return '';
    if (password !== confirm) {
      return '비밀번호가 일치하지 않습니다.';
    }
    return '';
  };

  // 닉네임 유효성 검사
  const validateNickname = (nickname: string): string => {
    if (!nickname) return '';
    if (nickname.length < 2 || nickname.length > 20) {
      return '닉네임은 2-20자여야 합니다.';
    }
    return '';
  };

  // 이메일 중복 확인
  const checkEmailDuplicate = async (email: string): Promise<boolean> => {
    try {
      const response = await fetch(`/api/v1/auth/check-email?email=${encodeURIComponent(email)}`);
      const data = await response.json();
      return data.available;
    } catch {
      return false;
    }
  };

  // 닉네임 중복 확인
  const checkNicknameDuplicate = async (nickname: string): Promise<boolean> => {
    try {
      const response = await fetch(`/api/v1/auth/check-nickname?nickname=${encodeURIComponent(nickname)}`);
      const data = await response.json();
      return data.available;
    } catch {
      return false;
    }
  };

  const handleChange = (field: keyof FormData) => (e: React.ChangeEvent<HTMLInputElement>) => {
    const value = e.target.value;
    setFormData((prev) => ({ ...prev, [field]: value }));
    setServerError('');
  };

  const handleBlur = (field: keyof FormData) => async () => {
    let error = '';
    let isValid = false;

    switch (field) {
      case 'email':
        error = validateEmail(formData.email);
        if (!error && formData.email) {
          const available = await checkEmailDuplicate(formData.email);
          if (!available) {
            error = '이미 사용 중인 이메일입니다.';
          } else {
            isValid = true;
          }
        }
        break;
      case 'password':
        error = validatePassword(formData.password);
        isValid = !error && !!formData.password;
        // 비밀번호 확인도 다시 검사
        if (formData.passwordConfirm) {
          const confirmError = validatePasswordConfirm(formData.password, formData.passwordConfirm);
          setErrors((prev) => ({ ...prev, passwordConfirm: confirmError }));
          setValid((prev) => ({ ...prev, passwordConfirm: !confirmError && !!formData.passwordConfirm }));
        }
        break;
      case 'passwordConfirm':
        error = validatePasswordConfirm(formData.password, formData.passwordConfirm);
        isValid = !error && !!formData.passwordConfirm;
        break;
      case 'nickname':
        error = validateNickname(formData.nickname);
        if (!error && formData.nickname) {
          const available = await checkNicknameDuplicate(formData.nickname);
          if (!available) {
            error = '이미 사용 중인 닉네임입니다.';
          } else {
            isValid = true;
          }
        }
        break;
    }

    setErrors((prev) => ({ ...prev, [field]: error }));
    setValid((prev) => ({ ...prev, [field]: isValid }));
  };

  const isFormValid = valid.email && valid.password && valid.passwordConfirm && valid.nickname;

  const handleSubmit = async () => {
    if (!isFormValid) return;

    setIsLoading(true);
    setServerError('');

    try {
      const response = await fetch('/api/v1/auth/register', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          email: formData.email,
          password: formData.password,
          nickname: formData.nickname,
          marketingAgreed,
        }),
      });

      if (!response.ok) {
        const errorData = await response.json();
        throw new Error(errorData.message || '회원가입에 실패했습니다.');
      }

      navigate('/register/complete');
    } catch (err) {
      setServerError(err instanceof Error ? err.message : '알 수 없는 오류가 발생했습니다.');
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <Container maxWidth="sm">
      <Box
        sx={{
          minHeight: '100vh',
          py: 4,
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
            회원가입
          </Typography>
        </Box>

        {/* 폼 */}
        <Box sx={{ px: 2 }}>
          {serverError && (
            <Alert severity="error" sx={{ mb: 2 }}>
              {serverError}
            </Alert>
          )}

          <AuthInput
            label="이메일"
            name="email"
            type="email"
            placeholder="example@email.com"
            value={formData.email}
            onChange={handleChange('email')}
            onBlur={handleBlur('email')}
            error={!!errors.email}
            helperText={errors.email}
            isValid={valid.email}
            required
            autoComplete="email"
          />

          <AuthInput
            label="비밀번호"
            name="password"
            type="password"
            placeholder="8자 이상, 영문과 숫자 포함"
            value={formData.password}
            onChange={handleChange('password')}
            onBlur={handleBlur('password')}
            error={!!errors.password}
            helperText={errors.password}
            isValid={valid.password}
            required
            autoComplete="new-password"
          />

          <AuthInput
            label="비밀번호 확인"
            name="passwordConfirm"
            type="password"
            placeholder="비밀번호를 다시 입력해주세요"
            value={formData.passwordConfirm}
            onChange={handleChange('passwordConfirm')}
            onBlur={handleBlur('passwordConfirm')}
            error={!!errors.passwordConfirm}
            helperText={errors.passwordConfirm}
            isValid={valid.passwordConfirm}
            required
            autoComplete="new-password"
          />

          <AuthInput
            label="닉네임"
            name="nickname"
            type="text"
            placeholder="2-20자 사이로 입력해주세요"
            value={formData.nickname}
            onChange={handleChange('nickname')}
            onBlur={handleBlur('nickname')}
            error={!!errors.nickname}
            helperText={errors.nickname}
            isValid={valid.nickname}
            required
            autoComplete="nickname"
          />
        </Box>

        {/* 가입 버튼 */}
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
            disabled={!isFormValid}
            loading={isLoading}
            fullWidth
          >
            가입하기
          </AuthButton>
        </Box>
      </Box>
    </Container>
  );
};

export default RegisterStep2Page;
