/**
 * @fileoverview 로그인 시작 페이지 (소셜 로그인)
 * @see docs/develop/user/frontend.md - 섹션 3.1
 */

import React from 'react';
import { useNavigate } from 'react-router-dom';
import { Box, Container, Typography, Link } from '@mui/material';
import { SocialLoginButton } from '../../components/auth';

const LoginStartPage: React.FC = () => {
  const navigate = useNavigate();

  const handleSocialLogin = (provider: 'kakao' | 'naver' | 'google') => {
    const clientIds: Record<string, string | undefined> = {
      kakao: process.env.REACT_APP_KAKAO_CLIENT_ID,
      naver: process.env.REACT_APP_NAVER_CLIENT_ID,
      google: process.env.REACT_APP_GOOGLE_CLIENT_ID,
    };

    const redirectUri = `${window.location.origin}/oauth/${provider}/callback`;
    const clientId = clientIds[provider];

    if (!clientId) {
      console.error(`${provider} client ID is not configured`);
      return;
    }

    let authUrl = '';

    switch (provider) {
      case 'kakao':
        authUrl = `https://kauth.kakao.com/oauth/authorize?client_id=${clientId}&redirect_uri=${encodeURIComponent(redirectUri)}&response_type=code`;
        break;
      case 'naver':
        const naverState = Math.random().toString(36).substring(7);
        authUrl = `https://nid.naver.com/oauth2.0/authorize?client_id=${clientId}&redirect_uri=${encodeURIComponent(redirectUri)}&response_type=code&state=${naverState}`;
        break;
      case 'google':
        authUrl = `https://accounts.google.com/o/oauth2/v2/auth?client_id=${clientId}&redirect_uri=${encodeURIComponent(redirectUri)}&response_type=code&scope=email%20profile`;
        break;
    }

    window.location.href = authUrl;
  };

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
        {/* 로고 및 슬로건 */}
        <Box sx={{ textAlign: 'center', mb: 6 }}>
          <Typography
            variant="h4"
            sx={{
              fontFamily: 'Noto Sans, sans-serif',
              fontWeight: 700,
              color: '#76BCA2',
              mb: 1,
            }}
          >
            펫프
          </Typography>
          <Typography
            sx={{
              fontFamily: 'Noto Sans KR, sans-serif',
              fontSize: '14px',
              color: '#404040',
            }}
          >
            소중한 반려동물과의 마지막 순간을 함께합니다
          </Typography>
        </Box>

        {/* 소셜 로그인 버튼 */}
        <Box sx={{ width: '100%', maxWidth: '320px' }}>
          <Box sx={{ mb: 2 }}>
            <SocialLoginButton
              provider="kakao"
              onClick={() => handleSocialLogin('kakao')}
            />
          </Box>
          <Box sx={{ mb: 2 }}>
            <SocialLoginButton
              provider="naver"
              onClick={() => handleSocialLogin('naver')}
            />
          </Box>
          <Box sx={{ mb: 4 }}>
            <SocialLoginButton
              provider="google"
              onClick={() => handleSocialLogin('google')}
            />
          </Box>

          {/* 이메일 회원가입 링크 */}
          <Box sx={{ textAlign: 'center', mb: 3 }}>
            <Link
              component="button"
              onClick={() => navigate('/register')}
              sx={{
                fontFamily: 'Noto Sans KR, sans-serif',
                fontSize: '14px',
                color: '#404040',
                textDecoration: 'underline',
                cursor: 'pointer',
                '&:hover': {
                  color: '#76BCA2',
                },
              }}
            >
              이메일로 회원가입
            </Link>
          </Box>

          {/* 아이디/비밀번호 찾기 링크 */}
          <Box
            sx={{
              display: 'flex',
              justifyContent: 'center',
              gap: 2,
            }}
          >
            <Link
              component="button"
              onClick={() => navigate('/find-id')}
              sx={{
                fontFamily: 'Noto Sans KR, sans-serif',
                fontSize: '12px',
                color: '#AEAEAE',
                textDecoration: 'none',
                cursor: 'pointer',
                '&:hover': {
                  textDecoration: 'underline',
                  color: '#76BCA2',
                },
              }}
            >
              아이디 찾기
            </Link>
            <Typography
              sx={{
                fontSize: '12px',
                color: '#AEAEAE',
              }}
            >
              |
            </Typography>
            <Link
              component="button"
              onClick={() => navigate('/reset-password')}
              sx={{
                fontFamily: 'Noto Sans KR, sans-serif',
                fontSize: '12px',
                color: '#AEAEAE',
                textDecoration: 'none',
                cursor: 'pointer',
                '&:hover': {
                  textDecoration: 'underline',
                  color: '#76BCA2',
                },
              }}
            >
              비밀번호 찾기
            </Link>
          </Box>
        </Box>
      </Box>
    </Container>
  );
};

export default LoginStartPage;
