/**
 * @fileoverview 아이디 찾기 - SMS 인증
 * @see docs/develop/user/frontend.md - 섹션 4.2
 */

import React, { useState, useEffect } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { Box, Container, Typography, Alert, Link } from '@mui/material';
import { AuthInput, AuthButton } from '../../components/auth';

const TIMER_SECONDS = 180; // 3분

const FindIdVerifyPage: React.FC = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const { verificationId, name, phone } = location.state || {};

  const [code, setCode] = useState('');
  const [timeLeft, setTimeLeft] = useState(TIMER_SECONDS);
  const [isLoading, setIsLoading] = useState(false);
  const [isResending, setIsResending] = useState(false);
  const [error, setError] = useState('');
  const [currentVerificationId, setCurrentVerificationId] = useState(verificationId);

  useEffect(() => {
    if (!verificationId) {
      navigate('/find-id');
      return;
    }

    const timer = setInterval(() => {
      setTimeLeft((prev) => {
        if (prev <= 1) {
          clearInterval(timer);
          return 0;
        }
        return prev - 1;
      });
    }, 1000);

    return () => clearInterval(timer);
  }, [verificationId, navigate, currentVerificationId]);

  const formatTime = (seconds: number): string => {
    const mins = Math.floor(seconds / 60);
    const secs = seconds % 60;
    return `${mins}:${secs.toString().padStart(2, '0')}`;
  };

  const handleResend = async () => {
    setIsResending(true);
    setError('');

    try {
      const response = await fetch('/api/v1/auth/find-id/resend', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          name,
          phone,
        }),
      });

      if (!response.ok) {
        throw new Error('인증번호 재전송에 실패했습니다.');
      }

      const data = await response.json();
      setCurrentVerificationId(data.verificationId);
      setTimeLeft(TIMER_SECONDS);
      setCode('');
    } catch (err) {
      setError(err instanceof Error ? err.message : '알 수 없는 오류가 발생했습니다.');
    } finally {
      setIsResending(false);
    }
  };

  const handleSubmit = async () => {
    if (code.length !== 6) return;

    setIsLoading(true);
    setError('');

    try {
      const response = await fetch('/api/v1/auth/find-id/verify', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          verificationId: currentVerificationId,
          code,
        }),
      });

      const data = await response.json();

      if (!response.ok) {
        throw new Error(data.message || '인증에 실패했습니다.');
      }

      navigate('/find-id/result', {
        state: {
          found: true,
          email: data.email,
        },
      });
    } catch (err) {
      if (err instanceof Error && err.message.includes('일치하는 계정')) {
        navigate('/find-id/result', {
          state: {
            found: false,
          },
        });
      } else {
        setError(err instanceof Error ? err.message : '알 수 없는 오류가 발생했습니다.');
      }
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
            아이디 찾기
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
            {phone?.replace(/(\d{3})(\d{4})(\d{4})/, '$1-$2-$3')}로 발송된 인증번호를 입력해주세요.
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
            label="인증번호"
            name="code"
            type="text"
            placeholder="6자리 숫자 입력"
            value={code}
            onChange={(e) => setCode(e.target.value.replace(/[^\d]/g, '').slice(0, 6))}
            required
            autoComplete="one-time-code"
          />

          {/* 타이머 및 재전송 */}
          <Box
            sx={{
              display: 'flex',
              justifyContent: 'space-between',
              alignItems: 'center',
              mt: 1,
            }}
          >
            <Typography
              sx={{
                fontFamily: 'Noto Sans KR, sans-serif',
                fontSize: '14px',
                color: timeLeft > 0 ? '#76BCA2' : '#FF0000',
              }}
            >
              {timeLeft > 0 ? formatTime(timeLeft) : '시간 만료'}
            </Typography>
            <Link
              component="button"
              onClick={handleResend}
              disabled={isResending}
              sx={{
                fontFamily: 'Noto Sans KR, sans-serif',
                fontSize: '14px',
                color: '#AEAEAE',
                textDecoration: 'underline',
                cursor: isResending ? 'not-allowed' : 'pointer',
                '&:hover': {
                  color: '#76BCA2',
                },
              }}
            >
              {isResending ? '전송 중...' : '재전송'}
            </Link>
          </Box>
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
            disabled={code.length !== 6 || timeLeft === 0}
            loading={isLoading}
            fullWidth
          >
            확인
          </AuthButton>
        </Box>
      </Box>
    </Container>
  );
};

export default FindIdVerifyPage;
