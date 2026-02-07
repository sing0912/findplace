/**
 * @fileoverview 비밀번호 재설정 - 정보 입력
 * @see docs/develop/user/frontend.md - 섹션 5.1
 */

import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Box, Container, Typography, Alert } from '@mui/material';
import { AuthInput, AuthButton } from '../../components/auth';

const ResetPasswordPage: React.FC = () => {
  const navigate = useNavigate();
  const [email, setEmail] = useState('');
  const [phone, setPhone] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState('');

  const formatPhoneNumber = (value: string): string => {
    const numbers = value.replace(/[^\d]/g, '');
    if (numbers.length <= 3) return numbers;
    if (numbers.length <= 7) return `${numbers.slice(0, 3)}-${numbers.slice(3)}`;
    return `${numbers.slice(0, 3)}-${numbers.slice(3, 7)}-${numbers.slice(7, 11)}`;
  };

  const handlePhoneChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const formatted = formatPhoneNumber(e.target.value);
    setPhone(formatted);
  };

  const validateEmail = (email: string): boolean => {
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return emailRegex.test(email);
  };

  const isFormValid = validateEmail(email) && phone.replace(/-/g, '').length === 11;

  const handleSubmit = async () => {
    if (!isFormValid) return;

    setIsLoading(true);
    setError('');

    try {
      const response = await fetch('/api/v1/auth/reset-password/request', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          email,
          phone: phone.replace(/-/g, ''),
        }),
      });

      if (!response.ok) {
        const errorData = await response.json();
        throw new Error(errorData.message || '인증번호 발송에 실패했습니다.');
      }

      const data = await response.json();

      navigate('/reset-password/verify', {
        state: {
          verificationId: data.verificationId,
          email,
          phone: phone.replace(/-/g, ''),
        },
      });
    } catch (err) {
      setError(err instanceof Error ? err.message : '알 수 없는 오류가 발생했습니다.');
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
            비밀번호 재설정
          </Typography>
        </Box>

        {/* 안내 문구 */}
        <Box sx={{ px: 2, mb: 3 }}>
          <Typography
            sx={{
              fontFamily: 'Noto Sans KR, sans-serif',
              fontSize: '14px',
              color: '#404040',
            }}
          >
            회원가입 시 등록한 이메일과 휴대폰 번호를 입력해주세요.
          </Typography>
        </Box>

        {/* 폼 */}
        <Box sx={{ px: 2 }}>
          {error && (
            <Alert severity="error" sx={{ mb: 2 }}>
              {error}
            </Alert>
          )}

          <AuthInput
            label="이메일"
            name="email"
            type="email"
            placeholder="example@email.com"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            required
            autoComplete="email"
          />

          <AuthInput
            label="휴대폰 번호"
            name="phone"
            type="tel"
            placeholder="010-0000-0000"
            value={phone}
            onChange={handlePhoneChange}
            required
            autoComplete="tel"
          />
        </Box>

        {/* 확인 버튼 */}
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
            인증번호 받기
          </AuthButton>
        </Box>
      </Box>
    </Container>
  );
};

export default ResetPasswordPage;
