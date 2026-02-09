/**
 * @fileoverview 회원가입 완료 페이지
 * @see docs/develop/user/frontend.md - 섹션 6.3
 */

import React from 'react';
import { useNavigate } from 'react-router-dom';
import { Box, Container, Typography } from '@mui/material';
import CheckCircleOutlineIcon from '@mui/icons-material/CheckCircleOutline';
import { AuthButton } from '../../components/auth';

const RegisterCompletePage: React.FC = () => {
  const navigate = useNavigate();

  const handleStart = () => {
    navigate('/');
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
        {/* 성공 아이콘 */}
        <CheckCircleOutlineIcon
          sx={{
            fontSize: 80,
            color: '#76BCA2',
            mb: 3,
          }}
        />

        {/* 축하 메시지 */}
        <Typography
          variant="h5"
          sx={{
            fontFamily: 'Noto Sans, sans-serif',
            fontWeight: 700,
            color: '#000000',
            mb: 1,
          }}
        >
          회원가입 완료
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
          펫프로 회원이 되신 것을 환영합니다.
          <br />
          이제 모든 서비스를 이용하실 수 있습니다.
        </Typography>

        {/* 시작하기 버튼 */}
        <AuthButton onClick={handleStart}>
          시작하기
        </AuthButton>
      </Box>
    </Container>
  );
};

export default RegisterCompletePage;
