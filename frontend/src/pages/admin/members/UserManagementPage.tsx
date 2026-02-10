import React from 'react';
import { Container, Box, Typography } from '@mui/material';

const UserManagementPage: React.FC = () => {
  return (
    <Container maxWidth="lg">
      <Box sx={{ py: 3 }}>
        <Typography variant="h4" gutterBottom>
          회원 관리
        </Typography>
        <Typography color="textSecondary">
          준비 중입니다.
        </Typography>
      </Box>
    </Container>
  );
};

export default UserManagementPage;
