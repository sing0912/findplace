/**
 * @fileoverview 약관/정책 목록 페이지
 * @see docs/develop/policy/frontend.md - 섹션 4
 */

import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Box, Container, Typography, Snackbar } from '@mui/material';
import {
  ArrowBackIosNew,
  Gavel,
  Security,
  LocationOn,
  Campaign,
} from '@mui/icons-material';
import { MenuItem } from '../../components/mypage';

const PolicyListPage: React.FC = () => {
  const navigate = useNavigate();
  const [showSnackbar, setShowSnackbar] = useState(false);

  const handlePlaceholder = () => {
    setShowSnackbar(true);
  };

  return (
    <Container maxWidth="sm">
      <Box sx={{ minHeight: '100vh', py: 4 }}>
        {/* 헤더 */}
        <Box
          sx={{
            mb: 3,
            display: 'flex',
            alignItems: 'center',
            position: 'relative',
          }}
        >
          <Box
            onClick={() => navigate('/mypage')}
            sx={{
              position: 'absolute',
              left: 0,
              cursor: 'pointer',
              display: 'flex',
              alignItems: 'center',
            }}
          >
            <ArrowBackIosNew sx={{ fontSize: 20, color: '#404040' }} />
          </Box>
          <Typography
            variant="h6"
            sx={{
              fontFamily: 'Noto Sans, sans-serif',
              fontWeight: 700,
              fontSize: '16px',
              color: '#000000',
              textAlign: 'center',
              width: '100%',
            }}
          >
            약관/정책
          </Typography>
        </Box>

        {/* 약관 목록 */}
        <Box sx={{ px: 2 }}>
          <MenuItem
            label="이용약관"
            icon={<Gavel fontSize="small" />}
            onClick={() => window.open('/terms', '_blank')}
          />
          <MenuItem
            label="개인정보처리방침"
            icon={<Security fontSize="small" />}
            onClick={() => window.open('/privacy', '_blank')}
          />
          <MenuItem
            label="위치기반서비스 이용약관"
            icon={<LocationOn fontSize="small" />}
            onClick={handlePlaceholder}
          />
          <MenuItem
            label="마케팅 수신 동의"
            icon={<Campaign fontSize="small" />}
            onClick={handlePlaceholder}
          />
        </Box>

        {/* 준비 중 Snackbar */}
        <Snackbar
          open={showSnackbar}
          autoHideDuration={2000}
          onClose={() => setShowSnackbar(false)}
          message="준비 중입니다"
          anchorOrigin={{ vertical: 'bottom', horizontal: 'center' }}
        />
      </Box>
    </Container>
  );
};

export default PolicyListPage;
