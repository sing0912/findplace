/**
 * @fileoverview 회원가입 페이지 컴포넌트
 *
 * 새 사용자 계정을 생성하기 위한 회원가입 폼을 제공합니다.
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

/**
 * 회원가입 폼 유효성 검사 스키마
 * Zod를 사용하여 각 필드의 유효성 규칙을 정의합니다.
 */
const registerSchema = z.object({
  /** 이메일: 유효한 이메일 형식 필요 */
  email: z.string().email('올바른 이메일 형식이 아닙니다.'),
  /** 비밀번호: 최소 8자 이상 */
  password: z.string().min(8, '비밀번호는 8자 이상이어야 합니다.'),
  /** 비밀번호 확인: password와 일치해야 함 */
  confirmPassword: z.string(),
  /** 이름: 필수 입력 */
  name: z.string().min(1, '이름을 입력해주세요.'),
  /** 전화번호: 선택 입력 */
  phone: z.string().optional(),
}).refine((data) => data.password === data.confirmPassword, {
  message: '비밀번호가 일치하지 않습니다.',
  path: ['confirmPassword'],
});

/**
 * 회원가입 폼 데이터 타입
 * 스키마에서 자동으로 추론됩니다.
 */
type RegisterFormData = z.infer<typeof registerSchema>;

/**
 * 회원가입 페이지 컴포넌트
 *
 * 새 사용자가 계정을 생성할 수 있는 폼을 제공합니다.
 *
 * 기능:
 * - 이메일, 이름, 전화번호, 비밀번호 입력
 * - 실시간 유효성 검사
 * - 비밀번호 확인 일치 검사
 * - 회원가입 성공 시 자동 로그인 및 홈 페이지 이동
 */
const RegisterPage: React.FC = () => {
  const { register: registerUser, isRegisterLoading } = useAuth();

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<RegisterFormData>({
    resolver: zodResolver(registerSchema),
  });

  /**
   * 폼 제출 핸들러
   * 비밀번호 확인 필드를 제외하고 회원가입 API를 호출합니다.
   * @param data - 폼 데이터
   */
  const onSubmit = (data: RegisterFormData) => {
    const { confirmPassword, ...registerData } = data;
    registerUser(registerData);
  };

  return (
    <Container maxWidth="sm">
      <Box
        sx={{
          minHeight: '100vh',
          display: 'flex',
          flexDirection: 'column',
          justifyContent: 'center',
          py: 4,
        }}
      >
        <Paper elevation={3} sx={{ p: 4 }}>
          {/* 페이지 제목 */}
          <Typography variant="h4" component="h1" align="center" gutterBottom>
            회원가입
          </Typography>
          <Typography variant="subtitle1" align="center" color="textSecondary" sx={{ mb: 3 }}>
            FindPlace에 오신 것을 환영합니다
          </Typography>

          {/* 회원가입 폼 */}
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

            {/* 이름 입력 필드 */}
            <TextField
              {...register('name')}
              label="이름"
              fullWidth
              margin="normal"
              error={!!errors.name}
              helperText={errors.name?.message}
              autoComplete="name"
            />

            {/* 전화번호 입력 필드 (선택) */}
            <TextField
              {...register('phone')}
              label="전화번호 (선택)"
              fullWidth
              margin="normal"
              error={!!errors.phone}
              helperText={errors.phone?.message}
              autoComplete="tel"
              placeholder="010-0000-0000"
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
              autoComplete="new-password"
            />

            {/* 비밀번호 확인 입력 필드 */}
            <TextField
              {...register('confirmPassword')}
              label="비밀번호 확인"
              type="password"
              fullWidth
              margin="normal"
              error={!!errors.confirmPassword}
              helperText={errors.confirmPassword?.message}
              autoComplete="new-password"
            />

            {/* 회원가입 버튼 */}
            <Button
              type="submit"
              variant="contained"
              fullWidth
              size="large"
              sx={{ mt: 3, mb: 2 }}
              disabled={isRegisterLoading}
            >
              {isRegisterLoading ? <CircularProgress size={24} /> : '회원가입'}
            </Button>

            {/* 로그인 페이지 링크 */}
            <Box sx={{ textAlign: 'center' }}>
              <Link component={RouterLink} to="/login" variant="body2">
                이미 계정이 있으신가요? 로그인
              </Link>
            </Box>
          </form>
        </Paper>
      </Box>
    </Container>
  );
};

export default RegisterPage;
