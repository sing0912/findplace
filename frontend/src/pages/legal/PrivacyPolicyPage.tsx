/**
 * @fileoverview 개인정보처리방침 페이지
 */

import React from 'react';
import { Container, Box } from '@mui/material';
import PrivacyPolicyContent from '../../components/legal/PrivacyPolicyContent';

/**
 * 개인정보처리방침 페이지 컴포넌트
 */
const PrivacyPolicyPage: React.FC = () => {
  return (
    <Container maxWidth="md">
      <Box sx={{ py: 4 }}>
        <PrivacyPolicyContent />
      </Box>
    </Container>
  );
};

export default PrivacyPolicyPage;
