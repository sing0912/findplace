import React from 'react';
import { Box, Typography, Card, CardContent } from '@mui/material';
import { RevenueSummary } from '../../../types/home';

interface RevenueSummaryCardProps {
  summary: RevenueSummary | null;
}

const RevenueSummaryCard: React.FC<RevenueSummaryCardProps> = ({ summary }) => {
  const totalAmount = summary?.totalAmount ?? 0;
  const pendingAmount = summary?.pendingAmount ?? 0;
  const completedCount = summary?.completedBookingCount ?? 0;

  return (
    <Card sx={{ borderRadius: '12px', boxShadow: '0 1px 4px rgba(0,0,0,0.08)' }}>
      <CardContent sx={{ p: 2, '&:last-child': { pb: 2 } }}>
        <Typography
          sx={{
            fontFamily: 'Noto Sans KR, sans-serif',
            fontSize: '16px',
            fontWeight: 700,
            color: '#000000',
            mb: 2,
          }}
        >
          이번 달 수익
        </Typography>

        <Box sx={{ display: 'flex', flexDirection: 'column', gap: 1.5 }}>
          <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
            <Typography
              sx={{
                fontFamily: 'Noto Sans KR, sans-serif',
                fontSize: '14px',
                color: '#666666',
              }}
            >
              확정 수익
            </Typography>
            <Typography
              sx={{
                fontFamily: 'Noto Sans KR, sans-serif',
                fontSize: '16px',
                fontWeight: 700,
                color: '#000000',
              }}
            >
              {totalAmount.toLocaleString()}원
            </Typography>
          </Box>

          <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
            <Typography
              sx={{
                fontFamily: 'Noto Sans KR, sans-serif',
                fontSize: '14px',
                color: '#666666',
              }}
            >
              정산 대기
            </Typography>
            <Typography
              sx={{
                fontFamily: 'Noto Sans KR, sans-serif',
                fontSize: '14px',
                fontWeight: 500,
                color: '#666666',
              }}
            >
              {pendingAmount.toLocaleString()}원
            </Typography>
          </Box>

          <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
            <Typography
              sx={{
                fontFamily: 'Noto Sans KR, sans-serif',
                fontSize: '14px',
                color: '#666666',
              }}
            >
              완료 건수
            </Typography>
            <Typography
              sx={{
                fontFamily: 'Noto Sans KR, sans-serif',
                fontSize: '14px',
                fontWeight: 500,
                color: '#666666',
              }}
            >
              {completedCount}건
            </Typography>
          </Box>
        </Box>
      </CardContent>
    </Card>
  );
};

export default RevenueSummaryCard;
