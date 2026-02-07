/**
 * @fileoverview 이용약관 페이지
 * @see docs/develop/policy/frontend.md
 */

import React from 'react';
import { Container, Box } from '@mui/material';
import TermsOfUseContent from '../../components/legal/TermsOfUseContent';

/**
 * 이용약관 페이지 컴포넌트
 */
const TermsOfUsePage: React.FC = () => {
  return (
    <Container maxWidth="md">
      <Box sx={{ py: 4 }}>
        <TermsOfUseContent />
      </Box>
    </Container>
  );
};

export default TermsOfUsePage;
