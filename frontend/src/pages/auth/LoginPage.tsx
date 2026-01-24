/**
 * @fileoverview 로그인 페이지 컴포넌트
 *
 * 사용자 인증을 위한 로그인 폼을 제공합니다.
 * React Hook Form과 Zod를 사용하여 폼 유효성 검사를 수행합니다.
 */

import React from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { Link as RouterLink } from 'react-router-dom';
import {
  Container,
  Box,
  Typography,
  TextField,
  Button,
  Link,
  Paper,
  CircularProgress,
} from '@mui/material';
import { useAuth } from '../../hooks/useAuth';
import { LoginRequest } from '../../types/auth';

/**
 * 로그인 폼 유효성 검사 스키마
 * Zod를 사용하여 각 필드의 유효성 규칙을 정의합니다.
 */
const loginSchema = z.object({
  /** 이메일: 유효한 이메일 형식 필요 */
  email: z.string().email('올바른 이메일 형식이 아닙니다.'),
  /** 비밀번호: 필수 입력 */
  password: z.string().min(1, '비밀번호를 입력해주세요.'),
});

/**
 * 로그인 페이지 컴포넌트
 *
 * 기존 사용자가 시스템에 로그인할 수 있는 폼을 제공합니다.
 *
 * 기능:
 * - 이메일/비밀번호 입력
 * - 실시간 유효성 검사
 * - 로그인 성공 시 홈 페이지 이동
 * - 로그인 실패 시 에러 알림 표시
 */
const LoginPage: React.FC = () => {
  const { login, isLoginLoading } = useAuth();

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<LoginRequest>({
    resolver: zodResolver(loginSchema),
  });

  /**
   * 폼 제출 핸들러
   * 로그인 API를 호출합니다.
   * @param data - 로그인 폼 데이터 (이메일, 비밀번호)
   */
  const onSubmit = (data: LoginRequest) => {
    login(data);
  };

  return (
    <Container maxWidth="sm">
      <Box
        sx={{
          minHeight: '100vh',
          display: 'flex',
          flexDirection: 'column',
          justifyContent: 'center',
        }}
      >
        <Paper elevation={3} sx={{ p: 4 }}>
          {/* 애플리케이션 타이틀 */}
          <Typography variant="h4" component="h1" align="center" gutterBottom>
            FindPlace
          </Typography>
          <Typography variant="subtitle1" align="center" color="textSecondary" sx={{ mb: 3 }}>
            반려동물 장례 토탈 플랫폼
          </Typography>

          {/* 로그인 폼 */}
          <form onSubmit={handleSubmit(onSubmit)}>
            {/* 이메일 입력 필드 */}
            <TextField
              {...register('email')}
              label="이메일"
              type="email"
              fullWidth
              margin="normal"
              error={!!errors.email}
              helperText={errors.email?.message}
              autoComplete="email"
            />

            {/* 비밀번호 입력 필드 */}
            <TextField
              {...register('password')}
              label="비밀번호"
              type="password"
              fullWidth
              margin="normal"
              error={!!errors.password}
              helperText={errors.password?.message}
              autoComplete="current-password"
            />

            {/* 로그인 버튼 */}
            <Button
              type="submit"
              variant="contained"
              fullWidth
              size="large"
              sx={{ mt: 3, mb: 2 }}
              disabled={isLoginLoading}
            >
              {isLoginLoading ? <CircularProgress size={24} /> : '로그인'}
            </Button>

            {/* 회원가입 페이지 링크 */}
            <Box sx={{ textAlign: 'center' }}>
              <Link component={RouterLink} to="/register" variant="body2">
                계정이 없으신가요? 회원가입
              </Link>
            </Box>
          </form>
        </Paper>
      </Box>
    </Container>
  );
};

export default LoginPage;
