/**
 * @fileoverview 회원가입 1단계 - 약관 동의
 * @see docs/develop/user/frontend.md - 섹션 6.1
 */

import React, { useState, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { Box, Container, Typography, Divider } from '@mui/material';
import { AuthButton, AgreementCheckbox } from '../../components/auth';

interface AgreementState {
  all: boolean;
  terms: boolean;
  privacy: boolean;
  marketing: boolean;
}

const RegisterStep1Page: React.FC = () => {
  const navigate = useNavigate();
  const [agreements, setAgreements] = useState<AgreementState>({
    all: false,
    terms: false,
    privacy: false,
    marketing: false,
  });

  const handleAllChange = useCallback((checked: boolean) => {
    setAgreements({
      all: checked,
      terms: checked,
      privacy: checked,
      marketing: checked,
    });
  }, []);

  const handleSingleChange = useCallback((key: keyof Omit<AgreementState, 'all'>, checked: boolean) => {
    setAgreements((prev) => {
      const updated = { ...prev, [key]: checked };
      updated.all = updated.terms && updated.privacy && updated.marketing;
      return updated;
    });
  }, []);

  const isNextEnabled = agreements.terms && agreements.privacy;

  const handleNext = () => {
    navigate('/register/info', {
      state: {
        agreeTerms: agreements.terms,
        agreePrivacy: agreements.privacy,
        agreeMarketing: agreements.marketing,
      },
    });
  };

  const handleShowTerms = () => {
    window.open('/terms', '_blank');
  };

  const handleShowPrivacy = () => {
    window.open('/privacy', '_blank');
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
            회원가입
          </Typography>
        </Box>

        {/* 약관 동의 영역 */}
        <Box sx={{ px: 2 }}>
          <Typography
            sx={{
              fontFamily: 'Noto Sans KR, sans-serif',
              fontSize: '14px',
              fontWeight: 500,
              color: '#000000',
              mb: 2,
            }}
          >
            약관에 동의해 주세요
          </Typography>

          {/* 전체 동의 */}
          <Box
            sx={{
              backgroundColor: '#F5FAF8',
              borderRadius: '8px',
              p: 2,
              mb: 2,
            }}
          >
            <AgreementCheckbox
              id="agree-all"
              label="전체 동의"
              checked={agreements.all}
              onChange={handleAllChange}
            />
          </Box>

          <Divider sx={{ my: 2 }} />

          {/* 개별 약관 */}
          <Box sx={{ px: 1 }}>
            <AgreementCheckbox
              id="agree-terms"
              label="이용약관 동의"
              required
              checked={agreements.terms}
              onChange={(checked) => handleSingleChange('terms', checked)}
              showDetail
              onDetailClick={handleShowTerms}
            />
            <AgreementCheckbox
              id="agree-privacy"
              label="개인정보처리방침 동의"
              required
              checked={agreements.privacy}
              onChange={(checked) => handleSingleChange('privacy', checked)}
              showDetail
              onDetailClick={handleShowPrivacy}
            />
            <AgreementCheckbox
              id="agree-marketing"
              label="마케팅 정보 수신 동의"
              checked={agreements.marketing}
              onChange={(checked) => handleSingleChange('marketing', checked)}
            />
          </Box>
        </Box>

        {/* 다음 버튼 */}
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
            onClick={handleNext}
            disabled={!isNextEnabled}
            fullWidth
          >
            다음
          </AuthButton>
        </Box>
      </Box>
    </Container>
  );
};

export default RegisterStep1Page;
