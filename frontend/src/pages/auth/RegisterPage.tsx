/**
 * @fileoverview 회원가입 페이지 컴포넌트
 *
 * 새 사용자 계정을 생성하기 위한 회원가입 폼을 제공합니다.
 * React Hook Form과 Zod를 사용하여 폼 유효성 검사를 수행합니다.
 */

import React, { useState } from 'react';
import { useForm, Controller } from 'react-hook-form';
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
  FormControlLabel,
  Checkbox,
  FormHelperText,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  IconButton,
} from '@mui/material';
import CloseIcon from '@mui/icons-material/Close';
import { useAuth } from '../../hooks/useAuth';
import PrivacyPolicyContent from '../../components/legal/PrivacyPolicyContent';

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
  /** 개인정보처리방침 동의: 필수 */
  agreePrivacy: z.boolean().refine((val) => val === true, {
    message: '개인정보처리방침에 동의해주세요.',
  }),
  /** 이용약관 동의: 필수 */
  agreeTerms: z.boolean().refine((val) => val === true, {
    message: '이용약관에 동의해주세요.',
  }),
  /** 마케팅 정보 수신 동의: 선택 */
  agreeMarketing: z.boolean().optional(),
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
 * - 개인정보처리방침 및 이용약관 동의
 * - 회원가입 성공 시 자동 로그인 및 홈 페이지 이동
 */
const RegisterPage: React.FC = () => {
  const { register: registerUser, isRegisterLoading } = useAuth();
  const [privacyDialogOpen, setPrivacyDialogOpen] = useState(false);
  const [termsDialogOpen, setTermsDialogOpen] = useState(false);

  const {
    register,
    handleSubmit,
    control,
    formState: { errors },
    setValue,
    watch,
  } = useForm<RegisterFormData>({
    resolver: zodResolver(registerSchema),
    defaultValues: {
      agreePrivacy: false,
      agreeTerms: false,
      agreeMarketing: false,
    },
  });

  const agreePrivacy = watch('agreePrivacy');
  const agreeTerms = watch('agreeTerms');

  /**
   * 전체 동의 핸들러
   */
  const handleAgreeAll = (checked: boolean) => {
    setValue('agreePrivacy', checked);
    setValue('agreeTerms', checked);
    setValue('agreeMarketing', checked);
  };

  /**
   * 폼 제출 핸들러
   * 비밀번호 확인 필드와 동의 필드를 제외하고 회원가입 API를 호출합니다.
   * @param data - 폼 데이터
   */
  const onSubmit = (data: RegisterFormData) => {
    const { confirmPassword, agreePrivacy, agreeTerms, agreeMarketing, ...registerData } = data;
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

            {/* 약관 동의 섹션 */}
            <Box sx={{ mt: 3, p: 2, bgcolor: 'grey.50', borderRadius: 1 }}>
              <Typography variant="subtitle2" gutterBottom>
                약관 동의
              </Typography>

              {/* 전체 동의 */}
              <FormControlLabel
                control={
                  <Checkbox
                    checked={agreePrivacy && agreeTerms}
                    onChange={(e) => handleAgreeAll(e.target.checked)}
                  />
                }
                label={
                  <Typography variant="body2" fontWeight="bold">
                    전체 동의
                  </Typography>
                }
              />

              <Box sx={{ ml: 3 }}>
                {/* 개인정보처리방침 동의 */}
                <Box sx={{ display: 'flex', alignItems: 'center' }}>
                  <Controller
                    name="agreePrivacy"
                    control={control}
                    render={({ field }) => (
                      <FormControlLabel
                        control={
                          <Checkbox
                            {...field}
                            checked={field.value}
                            size="small"
                          />
                        }
                        label={
                          <Typography variant="body2">
                            [필수] 개인정보처리방침에 동의합니다.
                          </Typography>
                        }
                      />
                    )}
                  />
                  <Link
                    component="button"
                    type="button"
                    variant="body2"
                    onClick={() => setPrivacyDialogOpen(true)}
                    sx={{ ml: 1 }}
                  >
                    보기
                  </Link>
                </Box>
                {errors.agreePrivacy && (
                  <FormHelperText error sx={{ ml: 4 }}>
                    {errors.agreePrivacy.message}
                  </FormHelperText>
                )}

                {/* 이용약관 동의 */}
                <Box sx={{ display: 'flex', alignItems: 'center' }}>
                  <Controller
                    name="agreeTerms"
                    control={control}
                    render={({ field }) => (
                      <FormControlLabel
                        control={
                          <Checkbox
                            {...field}
                            checked={field.value}
                            size="small"
                          />
                        }
                        label={
                          <Typography variant="body2">
                            [필수] 이용약관에 동의합니다.
                          </Typography>
                        }
                      />
                    )}
                  />
                  <Link
                    component="button"
                    type="button"
                    variant="body2"
                    onClick={() => setTermsDialogOpen(true)}
                    sx={{ ml: 1 }}
                  >
                    보기
                  </Link>
                </Box>
                {errors.agreeTerms && (
                  <FormHelperText error sx={{ ml: 4 }}>
                    {errors.agreeTerms.message}
                  </FormHelperText>
                )}

                {/* 마케팅 정보 수신 동의 (선택) */}
                <Controller
                  name="agreeMarketing"
                  control={control}
                  render={({ field }) => (
                    <FormControlLabel
                      control={
                        <Checkbox
                          {...field}
                          checked={field.value || false}
                          size="small"
                        />
                      }
                      label={
                        <Typography variant="body2">
                          [선택] 마케팅 정보 수신에 동의합니다.
                        </Typography>
                      }
                    />
                  )}
                />
              </Box>
            </Box>

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

      {/* 개인정보처리방침 다이얼로그 */}
      <Dialog
        open={privacyDialogOpen}
        onClose={() => setPrivacyDialogOpen(false)}
        maxWidth="md"
        fullWidth
        scroll="paper"
      >
        <DialogTitle sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
          개인정보처리방침
          <IconButton onClick={() => setPrivacyDialogOpen(false)} size="small">
            <CloseIcon />
          </IconButton>
        </DialogTitle>
        <DialogContent dividers>
          <PrivacyPolicyContent showContainer={false} />
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setPrivacyDialogOpen(false)}>닫기</Button>
          <Button
            variant="contained"
            onClick={() => {
              setValue('agreePrivacy', true);
              setPrivacyDialogOpen(false);
            }}
          >
            동의
          </Button>
        </DialogActions>
      </Dialog>

      {/* 이용약관 다이얼로그 */}
      <Dialog
        open={termsDialogOpen}
        onClose={() => setTermsDialogOpen(false)}
        maxWidth="md"
        fullWidth
        scroll="paper"
      >
        <DialogTitle sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
          이용약관
          <IconButton onClick={() => setTermsDialogOpen(false)} size="small">
            <CloseIcon />
          </IconButton>
        </DialogTitle>
        <DialogContent dividers>
          <Typography variant="h6" gutterBottom>
            FindPlace 서비스 이용약관
          </Typography>
          <Typography variant="body2" paragraph>
            제1조 (목적)
          </Typography>
          <Typography variant="body2" paragraph>
            이 약관은 베뉴네트웍스(이하 "회사")가 제공하는 파인드플레이스(FINDPLACE) 서비스(이하 "서비스")의
            이용조건 및 절차, 회사와 회원 간의 권리, 의무 및 책임사항 등을 규정함을 목적으로 합니다.
          </Typography>
          <Typography variant="body2" paragraph>
            제2조 (정의)
          </Typography>
          <Typography variant="body2" paragraph>
            ① "서비스"란 회사가 제공하는 반려동물 장례 정보 및 관련 서비스를 말합니다.
          </Typography>
          <Typography variant="body2" paragraph>
            ② "회원"이란 이 약관에 동의하고 회사와 서비스 이용계약을 체결한 자를 말합니다.
          </Typography>
          <Typography variant="body2" paragraph>
            제3조 (약관의 효력 및 변경)
          </Typography>
          <Typography variant="body2" paragraph>
            ① 이 약관은 서비스 화면에 게시하거나 기타의 방법으로 회원에게 공지함으로써 효력이 발생합니다.
          </Typography>
          <Typography variant="body2" paragraph>
            ② 회사는 필요한 경우 관련 법령을 위배하지 않는 범위에서 이 약관을 변경할 수 있습니다.
          </Typography>
          <Typography variant="body2" paragraph>
            제4조 (회원가입)
          </Typography>
          <Typography variant="body2" paragraph>
            ① 회원이 되고자 하는 자는 회사가 정한 가입 양식에 따라 회원정보를 기입한 후 이 약관에 동의한다는
            의사표시를 함으로써 회원가입을 신청합니다.
          </Typography>
          <Typography variant="body2" paragraph>
            ② 회사는 제1항과 같이 회원으로 가입할 것을 신청한 자가 다음 각 호에 해당하지 않는 한 회원으로
            등록합니다.
          </Typography>
          <Typography variant="body2" paragraph>
            제5조 (서비스의 제공)
          </Typography>
          <Typography variant="body2" paragraph>
            회사는 회원에게 다음과 같은 서비스를 제공합니다.
          </Typography>
          <Typography variant="body2" component="div">
            <ul>
              <li>반려동물 장례 정보 제공</li>
              <li>장례식장 검색 및 예약 서비스</li>
              <li>반려동물 추모 서비스</li>
              <li>기타 회사가 정하는 서비스</li>
            </ul>
          </Typography>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setTermsDialogOpen(false)}>닫기</Button>
          <Button
            variant="contained"
            onClick={() => {
              setValue('agreeTerms', true);
              setTermsDialogOpen(false);
            }}
          >
            동의
          </Button>
        </DialogActions>
      </Dialog>
    </Container>
  );
};

export default RegisterPage;
