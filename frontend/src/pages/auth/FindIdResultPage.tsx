/**
 * @fileoverview 아이디 찾기 - 결과
 * @see docs/develop/user/frontend.md - 섹션 4.3
 */

import React from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { Box, Container, Typography } from '@mui/material';
import CheckCircleOutlineIcon from '@mui/icons-material/CheckCircleOutline';
import ErrorOutlineIcon from '@mui/icons-material/ErrorOutline';
import { AuthButton } from '../../components/auth';

const FindIdResultPage: React.FC = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const { found, email } = location.state || {};

  if (found) {
    // 아이디 찾기 성공
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
            아이디 찾기 결과
          </Typography>

          <Typography
            sx={{
              fontFamily: 'Noto Sans KR, sans-serif',
              fontSize: '14px',
              color: '#404040',
              mb: 1,
            }}
          >
            회원님의 아이디는
          </Typography>

          <Typography
            sx={{
              fontFamily: 'Noto Sans, sans-serif',
              fontSize: '18px',
              fontWeight: 700,
              color: '#76BCA2',
              mb: 4,
            }}
          >
            {email}
          </Typography>

          <Box sx={{ display: 'flex', gap: 2 }}>
            <AuthButton
              onClick={() => navigate('/login')}
            >
              로그인
            </AuthButton>
            <AuthButton
              variant="secondary"
              onClick={() => navigate('/reset-password')}
            >
              비밀번호 찾기
            </AuthButton>
          </Box>
        </Box>
      </Container>
    );
  }

  // 아이디 찾기 실패
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
        <ErrorOutlineIcon
          sx={{
            fontSize: 80,
            color: '#AEAEAE',
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
          일치하는 계정이 없습니다
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
          입력하신 정보와 일치하는 계정을 찾을 수 없습니다.
          <br />
          정보를 확인 후 다시 시도해주세요.
        </Typography>

        <Box sx={{ display: 'flex', gap: 2 }}>
          <AuthButton
            onClick={() => navigate('/register')}
          >
            회원가입
          </AuthButton>
          <AuthButton
            variant="secondary"
            onClick={() => navigate('/find-id')}
          >
            다시 찾기
          </AuthButton>
        </Box>
      </Box>
    </Container>
  );
};

export default FindIdResultPage;
