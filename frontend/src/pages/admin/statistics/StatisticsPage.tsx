import React from 'react';
import { Container, Box, Typography } from '@mui/material';

const StatisticsPage: React.FC = () => {
  return (
    <Container maxWidth="lg">
      <Box sx={{ py: 3 }}>
        <Typography variant="h4" gutterBottom>
          통계
        </Typography>
        <Typography color="textSecondary">
          준비 중입니다.
        </Typography>
      </Box>
    </Container>
  );
};

export default StatisticsPage;
