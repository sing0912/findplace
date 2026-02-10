/**
 * @fileoverview 회원가입 2단계 - 정보 입력
 * @see docs/develop/user/frontend.md - 섹션 6.2
 */

import React, { useState } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { Box, Container, Typography, Alert } from '@mui/material';
import { AuthInput, AuthButton } from '../../components/auth';
import { authApi } from '../../api/auth';

interface FormData {
  email: string;
  password: string;
  passwordConfirm: string;
  name: string;
  nickname: string;
  phone: string;
}

interface FormErrors {
  email: string;
  password: string;
  passwordConfirm: string;
  name: string;
  nickname: string;
  phone: string;
}

interface FormValid {
  email: boolean;
  password: boolean;
  passwordConfirm: boolean;
  name: boolean;
  nickname: boolean;
  phone: boolean;
}

const RegisterStep2Page: React.FC = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const agreeTerms = location.state?.agreeTerms ?? false;
  const agreePrivacy = location.state?.agreePrivacy ?? false;
  const agreeMarketing = location.state?.agreeMarketing ?? false;

  const [formData, setFormData] = useState<FormData>({
    email: '',
    password: '',
    passwordConfirm: '',
    name: '',
    nickname: '',
    phone: '',
  });

  const [errors, setErrors] = useState<FormErrors>({
    email: '',
    password: '',
    passwordConfirm: '',
    name: '',
    nickname: '',
    phone: '',
  });

  const [valid, setValid] = useState<FormValid>({
    email: false,
    password: false,
    passwordConfirm: false,
    name: false,
    nickname: false,
    phone: false,
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

  // 이름 유효성 검사
  const validateName = (name: string): string => {
    if (!name) return '';
    if (name.length < 2 || name.length > 20) {
      return '이름은 2-20자여야 합니다.';
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

  // 전화번호 유효성 검사
  const validatePhone = (phone: string): string => {
    if (!phone) return '';
    const phoneNumber = phone.replace(/-/g, '');
    if (phoneNumber.length !== 11) {
      return '올바른 휴대폰 번호를 입력해주세요.';
    }
    return '';
  };

  // 전화번호 포맷
  const formatPhoneNumber = (value: string): string => {
    const numbers = value.replace(/[^\d]/g, '');
    if (numbers.length <= 3) return numbers;
    if (numbers.length <= 7) return `${numbers.slice(0, 3)}-${numbers.slice(3)}`;
    return `${numbers.slice(0, 3)}-${numbers.slice(3, 7)}-${numbers.slice(7, 11)}`;
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
    let value = e.target.value;
    if (field === 'phone') {
      value = formatPhoneNumber(value);
    }
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
      case 'name':
        error = validateName(formData.name);
        isValid = !error && !!formData.name;
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
      case 'phone':
        error = validatePhone(formData.phone);
        isValid = !error && !!formData.phone;
        break;
    }

    setErrors((prev) => ({ ...prev, [field]: error }));
    setValid((prev) => ({ ...prev, [field]: isValid }));
  };

  const isFormValid = valid.email && valid.password && valid.passwordConfirm && valid.name && valid.nickname && valid.phone;

  const handleSubmit = async () => {
    if (!isFormValid) return;

    setIsLoading(true);
    setServerError('');

    try {
      await authApi.register({
        email: formData.email,
        password: formData.password,
        name: formData.name,
        nickname: formData.nickname,
        phone: formData.phone.replace(/-/g, ''),
        agreeTerms,
        agreePrivacy,
        agreeMarketing,
      });

      navigate('/register/complete');
    } catch (err) {
      setServerError(err instanceof Error ? err.message : '회원가입에 실패했습니다.');
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
            label="이름"
            name="name"
            type="text"
            placeholder="이름을 입력해주세요"
            value={formData.name}
            onChange={handleChange('name')}
            onBlur={handleBlur('name')}
            error={!!errors.name}
            helperText={errors.name}
            isValid={valid.name}
            required
            autoComplete="name"
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

          <AuthInput
            label="휴대폰 번호"
            name="phone"
            type="tel"
            placeholder="010-0000-0000"
            value={formData.phone}
            onChange={handleChange('phone')}
            onBlur={handleBlur('phone')}
            error={!!errors.phone}
            helperText={errors.phone}
            isValid={valid.phone}
            required
            autoComplete="tel"
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
