import React from 'react';
import { Box, Typography, Avatar } from '@mui/material';
import StarIcon from '@mui/icons-material/Star';
import { useNavigate } from 'react-router-dom';
import { RecentSitter } from '../../../types/home';

interface RecentSittersProps {
  sitters: RecentSitter[];
}

const RecentSitters: React.FC<RecentSittersProps> = ({ sitters }) => {
  const navigate = useNavigate();
  const displaySitters = sitters.slice(0, 3);

  if (displaySitters.length === 0) return null;

  const formatDate = (dateString: string): string => {
    const date = new Date(dateString);
    return `${date.getMonth() + 1}/${date.getDate()}`;
  };

  return (
    <Box>
      <Typography sx={{ fontSize: '16px', fontWeight: 700, color: '#000', mb: 1.5 }}>
        최근 이용 시터
      </Typography>

      <Box sx={{ display: 'flex', flexDirection: 'column' }}>
        {displaySitters.map((sitter) => (
          <Box
            key={sitter.id}
            onClick={() => navigate(`/sitter/${sitter.id}`)}
            sx={{
              display: 'flex',
              alignItems: 'center',
              gap: 1.5,
              py: 1.5,
              borderBottom: '1px solid #F5F5F5',
              cursor: 'pointer',
              '&:hover': { backgroundColor: '#FAFAFA' },
            }}
          >
            <Avatar
              src={sitter.profileImageUrl || undefined}
              alt={sitter.nickname}
              sx={{ width: 40, height: 40, backgroundColor: '#76BCA2' }}
            >
              {sitter.nickname.charAt(0)}
            </Avatar>

            <Box sx={{ flex: 1 }}>
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.5, mb: 0.25 }}>
                <Typography sx={{ fontSize: '14px', fontWeight: 600, color: '#000' }}>
                  {sitter.nickname}
                </Typography>
                <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.25 }}>
                  <StarIcon sx={{ fontSize: 14, color: '#FFC107' }} />
                  <Typography sx={{ fontSize: '12px', color: '#424242' }}>
                    {sitter.averageRating.toFixed(1)}
                  </Typography>
                </Box>
              </Box>
              <Typography sx={{ fontSize: '12px', color: '#AEAEAE' }}>
                {sitter.serviceTypeName} | {formatDate(sitter.lastBookingDate)} 이용
              </Typography>
            </Box>
          </Box>
        ))}
      </Box>
    </Box>
  );
};

export default RecentSitters;
