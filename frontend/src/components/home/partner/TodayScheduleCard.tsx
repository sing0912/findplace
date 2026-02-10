import React from 'react';
import { Box, Typography, Card, CardContent, Chip, Divider } from '@mui/material';
import { useNavigate } from 'react-router-dom';
import { TodaySchedule } from '../../../types/home';

interface TodayScheduleCardProps {
  schedule: TodaySchedule | null;
}

const statusConfig: Record<string, { label: string; color: string; bgColor: string }> = {
  CONFIRMED: {
    label: '확정',
    color: '#1976D2',
    bgColor: '#E3F2FD',
  },
  IN_PROGRESS: {
    label: '진행중',
    color: '#2E7D32',
    bgColor: '#E8F5E9',
  },
};

const formatTime = (dateString: string): string => {
  const date = new Date(dateString);
  return date.toLocaleTimeString('ko-KR', { hour: '2-digit', minute: '2-digit', hour12: false });
};

const TodayScheduleCard: React.FC<TodayScheduleCardProps> = ({ schedule }) => {
  const navigate = useNavigate();

  const bookings = schedule?.bookings ?? [];
  const totalCount = schedule?.totalCount ?? 0;

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
          오늘의 일정 ({totalCount}건)
        </Typography>

        {bookings.length === 0 ? (
          <Typography
            sx={{
              fontFamily: 'Noto Sans KR, sans-serif',
              fontSize: '14px',
              color: '#AEAEAE',
              textAlign: 'center',
              py: 3,
            }}
          >
            오늘 예정된 일정이 없습니다
          </Typography>
        ) : (
          bookings.map((booking, index) => {
            const statusInfo = statusConfig[booking.status];

            return (
              <React.Fragment key={booking.bookingId}>
                {index > 0 && <Divider sx={{ my: 1.5 }} />}
                <Box
                  onClick={() => navigate(`/partner/bookings/${booking.bookingId}`)}
                  sx={{
                    cursor: 'pointer',
                    '&:hover': { backgroundColor: '#F5FAF8', borderRadius: '8px' },
                    p: 1,
                  }}
                >
                  <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 0.5 }}>
                    <Typography
                      sx={{
                        fontFamily: 'Noto Sans KR, sans-serif',
                        fontSize: '14px',
                        fontWeight: 600,
                        color: '#333333',
                      }}
                    >
                      {formatTime(booking.startDate)}~{formatTime(booking.endDate)}{'  '}{booking.serviceTypeName}
                    </Typography>
                    {statusInfo && (
                      <Chip
                        label={statusInfo.label}
                        size="small"
                        sx={{
                          height: '22px',
                          fontSize: '11px',
                          fontWeight: 500,
                          color: statusInfo.color,
                          backgroundColor: statusInfo.bgColor,
                          '& .MuiChip-label': { px: 1 },
                        }}
                      />
                    )}
                  </Box>
                  <Typography
                    sx={{
                      fontFamily: 'Noto Sans KR, sans-serif',
                      fontSize: '13px',
                      color: '#666666',
                    }}
                  >
                    {booking.customerName} | {booking.petNames.join(', ')}
                  </Typography>
                </Box>
              </React.Fragment>
            );
          })
        )}
      </CardContent>
    </Card>
  );
};

export default TodayScheduleCard;
