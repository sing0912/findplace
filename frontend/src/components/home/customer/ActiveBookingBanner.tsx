import React from 'react';
import { Box, Typography, Avatar, Button } from '@mui/material';
import { useNavigate } from 'react-router-dom';
import { ActiveBooking } from '../../../types/home';

interface ActiveBookingBannerProps {
  booking: ActiveBooking | null;
}

const ActiveBookingBanner: React.FC<ActiveBookingBannerProps> = ({ booking }) => {
  const navigate = useNavigate();

  if (!booking) return null;

  const formatTime = (dateString: string): string => {
    const date = new Date(dateString);
    return date.toLocaleTimeString('ko-KR', { hour: '2-digit', minute: '2-digit', hour12: false });
  };

  return (
    <Box
      sx={{
        background: 'linear-gradient(135deg, #E3F2FD 0%, #BBDEFB 100%)',
        borderRadius: '12px',
        p: 2,
        display: 'flex',
        alignItems: 'center',
        gap: 1.5,
      }}
    >
      <Avatar
        src={booking.partnerProfileImageUrl || undefined}
        alt={booking.partnerNickname}
        sx={{ width: 48, height: 48, backgroundColor: '#90CAF9' }}
      >
        {booking.partnerNickname.charAt(0)}
      </Avatar>

      <Box sx={{ flex: 1 }}>
        <Typography
          sx={{
            fontSize: '14px',
            fontWeight: 700,
            color: '#1565C0',
            mb: 0.25,
          }}
        >
          {booking.petNames.join(', ')} 돌봄 중
        </Typography>
        <Typography sx={{ fontSize: '13px', color: '#424242' }}>
          {booking.partnerNickname} | {booking.serviceTypeName}
        </Typography>
        <Typography sx={{ fontSize: '12px', color: '#757575' }}>
          {formatTime(booking.startDate)} ~ {formatTime(booking.endDate)}
        </Typography>
      </Box>

      <Button
        size="small"
        onClick={() => navigate(`/bookings/${booking.bookingId}`)}
        sx={{
          color: '#1565C0',
          fontWeight: 600,
          fontSize: '13px',
          whiteSpace: 'nowrap',
          minWidth: 'auto',
        }}
      >
        바로가기 &gt;
      </Button>
    </Box>
  );
};

export default ActiveBookingBanner;
