/**
 * @fileoverview OAuth 콜백 처리 페이지
 * @see docs/develop/user/frontend.md - 섹션 3.3
 */

import React, { useEffect, useState } from 'react';
import { useParams, useNavigate, useSearchParams } from 'react-router-dom';
import { Box, Container, CircularProgress, Typography } from '@mui/material';
import { useAuthStore } from '../../stores/authStore';

type OAuthProvider = 'kakao' | 'naver' | 'google';

const OAuthCallbackPage: React.FC = () => {
  const { provider } = useParams<{ provider: OAuthProvider }>();
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const [error, setError] = useState<string | null>(null);
  const { login } = useAuthStore();

  useEffect(() => {
    const code = searchParams.get('code');
    const state = searchParams.get('state');
    const errorParam = searchParams.get('error');

    if (errorParam) {
      setError('로그인이 취소되었습니다.');
      setTimeout(() => navigate('/login'), 2000);
      return;
    }

    if (!code) {
      setError('인증 코드를 받지 못했습니다.');
      setTimeout(() => navigate('/login'), 2000);
      return;
    }

    // StrictMode 이중 렌더링 방지: sessionStorage로 코드 중복 처리 차단
    const processedKey = `oauth_processed_${code}`;
    if (sessionStorage.getItem(processedKey)) {
      return;
    }
    sessionStorage.setItem(processedKey, '1');

    const handleCallback = async () => {
      try {
        const response = await fetch(`/api/v1/auth/oauth/${provider}/callback`, {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
          },
          body: JSON.stringify({ code, state }),
        });

        const response_data = await response.json();

        if (!response.ok || !response_data.success) {
          throw new Error(response_data.error?.message || '인증에 실패했습니다.');
        }

        const data = response_data.data;

        // authStore를 통해 로그인 처리 (토큰 저장 + 상태 업데이트)
        const now = new Date().toISOString();
        login(
          {
            id: data.user.id,
            email: data.user.email,
            name: data.user.nickname || data.user.email.split('@')[0],
            profileImageUrl: data.user.profileImageUrl,
            role: 'CUSTOMER',
            status: 'ACTIVE',
            createdAt: now,
            updatedAt: now,
          },
          data.accessToken,
          data.refreshToken
        );

        // OAuth 로그인은 이미 사용자가 생성되므로 바로 마이페이지로 이동
        navigate('/mypage');
      } catch (err) {
        sessionStorage.removeItem(processedKey); // 에러 시 재시도 가능
        setError(err instanceof Error ? err.message : '알 수 없는 오류가 발생했습니다.');
        setTimeout(() => navigate('/login'), 2000);
      }
    };

    handleCallback();
  }, [provider, searchParams, navigate, login]);

  return (
    <Container maxWidth="sm">
      <Box
        sx={{
          minHeight: '100vh',
          display: 'flex',
          flexDirection: 'column',
          justifyContent: 'center',
          alignItems: 'center',
        }}
      >
        {error ? (
          <>
            <Typography
              sx={{
                fontFamily: 'Noto Sans KR, sans-serif',
                fontSize: '16px',
                color: '#FF0000',
                mb: 2,
              }}
            >
              {error}
            </Typography>
            <Typography
              sx={{
                fontFamily: 'Noto Sans KR, sans-serif',
                fontSize: '14px',
                color: '#AEAEAE',
              }}
            >
              잠시 후 로그인 페이지로 이동합니다...
            </Typography>
          </>
        ) : (
          <>
            <CircularProgress
              sx={{ color: '#76BCA2', mb: 2 }}
              size={40}
            />
            <Typography
              sx={{
                fontFamily: 'Noto Sans KR, sans-serif',
                fontSize: '14px',
                color: '#404040',
              }}
            >
              로그인 처리 중...
            </Typography>
          </>
        )}
      </Box>
    </Container>
  );
};

export default OAuthCallbackPage;
