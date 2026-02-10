import React from 'react';
import { Box, Typography, Button } from '@mui/material';
import { useNavigate } from 'react-router-dom';

interface NewRequestBadgeProps {
  count: number;
}

const NewRequestBadge: React.FC<NewRequestBadgeProps> = ({ count }) => {
  const navigate = useNavigate();

  const hasRequests = count > 0;

  return (
    <Box
      sx={{
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'space-between',
        p: 2,
        borderRadius: '12px',
        backgroundColor: hasRequests ? '#FFF3E0' : '#F5F5F5',
      }}
    >
      <Typography
        sx={{
          fontFamily: 'Noto Sans KR, sans-serif',
          fontSize: '15px',
          fontWeight: 600,
          color: hasRequests ? '#E65100' : '#AEAEAE',
        }}
      >
        {hasRequests ? `새 예약 요청 ${count}건` : '새 예약 요청이 없습니다'}
      </Typography>

      {hasRequests && (
        <Button
          onClick={() => navigate('/partner/bookings?tab=requests')}
          sx={{
            fontFamily: 'Noto Sans KR, sans-serif',
            fontSize: '13px',
            fontWeight: 600,
            color: '#E65100',
            minWidth: 'auto',
            p: 0,
            '&:hover': { backgroundColor: 'transparent' },
          }}
        >
          확인&gt;
        </Button>
      )}
    </Box>
  );
};

export default NewRequestBadge;
