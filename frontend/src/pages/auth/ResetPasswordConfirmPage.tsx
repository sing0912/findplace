/**
 * @fileoverview 비밀번호 재설정 - 새 비밀번호 입력
 * @see docs/develop/user/frontend.md - 섹션 5.3
 */

import React, { useState, useEffect } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { Box, Container, Typography, Alert } from '@mui/material';
import CheckCircleOutlineIcon from '@mui/icons-material/CheckCircleOutline';
import { AuthInput, AuthButton } from '../../components/auth';

const ResetPasswordConfirmPage: React.FC = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const { token } = location.state || {};

  const [password, setPassword] = useState('');
  const [passwordConfirm, setPasswordConfirm] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState('');
  const [isComplete, setIsComplete] = useState(false);

  const [passwordError, setPasswordError] = useState('');
  const [confirmError, setConfirmError] = useState('');
  const [passwordValid, setPasswordValid] = useState(false);
  const [confirmValid, setConfirmValid] = useState(false);

  useEffect(() => {
    if (!token) {
      navigate('/reset-password');
    }
  }, [token, navigate]);

  const validatePassword = (value: string): string => {
    if (!value) return '';
    const passwordRegex = /^(?=.*[A-Za-z])(?=.*\d)[A-Za-z\d@$!%*#?&]{8,}$/;
    if (!passwordRegex.test(value)) {
      return '비밀번호는 8자 이상, 영문과 숫자를 포함해야 합니다.';
    }
    return '';
  };

  const handlePasswordBlur = () => {
    const error = validatePassword(password);
    setPasswordError(error);
    setPasswordValid(!error && !!password);

    // 비밀번호 확인도 다시 검사
    if (passwordConfirm) {
      const confirmErr = password !== passwordConfirm ? '비밀번호가 일치하지 않습니다.' : '';
      setConfirmError(confirmErr);
      setConfirmValid(!confirmErr && !!passwordConfirm);
    }
  };

  const handleConfirmBlur = () => {
    if (!passwordConfirm) {
      setConfirmError('');
      setConfirmValid(false);
      return;
    }
    const error = password !== passwordConfirm ? '비밀번호가 일치하지 않습니다.' : '';
    setConfirmError(error);
    setConfirmValid(!error);
  };

  const isFormValid = passwordValid && confirmValid;

  const handleSubmit = async () => {
    if (!isFormValid) return;

    setIsLoading(true);
    setError('');

    try {
      const response = await fetch('/api/v1/auth/reset-password/confirm', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          token,
          newPassword: password,
        }),
      });

      if (!response.ok) {
        const errorData = await response.json();
        throw new Error(errorData.message || '비밀번호 변경에 실패했습니다.');
      }

      setIsComplete(true);
    } catch (err) {
      setError(err instanceof Error ? err.message : '알 수 없는 오류가 발생했습니다.');
    } finally {
      setIsLoading(false);
    }
  };

  if (isComplete) {
    return (
      <Container maxWidth="sm">
        <Box
          sx={{
            minHeight: '100vh',
            display: 'flex',
            flexDirection: 'column',
            justifyContent: 'center',
            alignItems: 'center',
            py: 4,
          }}
        >
          <CheckCircleOutlineIcon
            sx={{
              fontSize: 80,
              color: '#76BCA2',
              mb: 3,
            }}
          />

          <Typography
            variant="h6"
            sx={{
              fontFamily: 'Noto Sans, sans-serif',
              fontWeight: 700,
              fontSize: '16px',
              color: '#000000',
              mb: 2,
            }}
          >
            비밀번호 변경 완료
          </Typography>

          <Typography
            sx={{
              fontFamily: 'Noto Sans KR, sans-serif',
              fontSize: '14px',
              color: '#404040',
              textAlign: 'center',
              mb: 4,
            }}
          >
            비밀번호가 성공적으로 변경되었습니다.
            <br />
            새 비밀번호로 로그인해주세요.
          </Typography>

          <AuthButton onClick={() => navigate('/login')}>
            로그인
          </AuthButton>
        </Box>
      </Container>
    );
  }

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
            새로운 비밀번호를 입력해주세요.
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
            label="새 비밀번호"
            name="password"
            type="password"
            placeholder="8자 이상, 영문과 숫자 포함"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            onBlur={handlePasswordBlur}
            error={!!passwordError}
            helperText={passwordError}
            isValid={passwordValid}
            required
            autoComplete="new-password"
          />

          <AuthInput
            label="비밀번호 확인"
            name="passwordConfirm"
            type="password"
            placeholder="비밀번호를 다시 입력해주세요"
            value={passwordConfirm}
            onChange={(e) => setPasswordConfirm(e.target.value)}
            onBlur={handleConfirmBlur}
            error={!!confirmError}
            helperText={confirmError}
            isValid={confirmValid}
            required
            autoComplete="new-password"
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
            disabled={!isFormValid}
            loading={isLoading}
            fullWidth
          >
            비밀번호 변경
          </AuthButton>
        </Box>
      </Box>
    </Container>
  );
};

export default ResetPasswordConfirmPage;
