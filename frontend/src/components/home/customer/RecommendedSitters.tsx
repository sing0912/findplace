import React from 'react';
import { Box, Typography, Avatar, Card, CardContent } from '@mui/material';
import StarIcon from '@mui/icons-material/Star';
import { useNavigate } from 'react-router-dom';
import { RecommendedSitter } from '../../../types/home';

interface RecommendedSittersProps {
  sitters: RecommendedSitter[];
}

const RecommendedSitters: React.FC<RecommendedSittersProps> = ({ sitters }) => {
  const navigate = useNavigate();
  const displaySitters = sitters.slice(0, 5);

  const formatDistance = (distance: number | null): string => {
    if (distance === null) return '';
    return distance < 1 ? `${Math.round(distance * 1000)}m` : `${distance.toFixed(1)}km`;
  };

  return (
    <Box>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 1.5 }}>
        <Typography sx={{ fontSize: '16px', fontWeight: 700, color: '#000' }}>
          추천 시터
        </Typography>
        <Typography
          onClick={() => navigate('/search')}
          sx={{ fontSize: '13px', color: '#AEAEAE', cursor: 'pointer' }}
        >
          더보기
        </Typography>
      </Box>

      {displaySitters.length === 0 ? (
        <Box sx={{ textAlign: 'center', py: 4 }}>
          <Typography sx={{ fontSize: '14px', color: '#AEAEAE' }}>
            주변에 추천 시터가 없습니다
          </Typography>
        </Box>
      ) : (
        <Box
          sx={{
            display: 'flex',
            gap: 1.5,
            overflowX: 'auto',
            pb: 1,
            '&::-webkit-scrollbar': { display: 'none' },
            scrollbarWidth: 'none',
          }}
        >
          {displaySitters.map((sitter) => (
            <Card
              key={sitter.id}
              onClick={() => navigate(`/sitter/${sitter.id}`)}
              sx={{
                minWidth: 120,
                width: 120,
                height: 160,
                borderRadius: '12px',
                boxShadow: '0 1px 4px rgba(0,0,0,0.08)',
                cursor: 'pointer',
                flexShrink: 0,
              }}
            >
              <CardContent
                sx={{
                  display: 'flex',
                  flexDirection: 'column',
                  alignItems: 'center',
                  p: 1.5,
                  '&:last-child': { pb: 1.5 },
                }}
              >
                <Avatar
                  src={sitter.profileImageUrl || undefined}
                  alt={sitter.nickname}
                  sx={{ width: 60, height: 60, mb: 1, backgroundColor: '#76BCA2' }}
                >
                  {sitter.nickname.charAt(0)}
                </Avatar>
                <Typography
                  sx={{
                    fontSize: '13px',
                    fontWeight: 600,
                    color: '#000',
                    textAlign: 'center',
                    overflow: 'hidden',
                    textOverflow: 'ellipsis',
                    whiteSpace: 'nowrap',
                    width: '100%',
                  }}
                >
                  {sitter.nickname}
                </Typography>
                <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.25, mt: 0.25 }}>
                  <StarIcon sx={{ fontSize: 14, color: '#FFC107' }} />
                  <Typography sx={{ fontSize: '12px', color: '#424242' }}>
                    {sitter.averageRating.toFixed(1)}
                  </Typography>
                </Box>
                {sitter.distance !== null && (
                  <Typography sx={{ fontSize: '11px', color: '#AEAEAE', mt: 0.25 }}>
                    {formatDistance(sitter.distance)}
                  </Typography>
                )}
              </CardContent>
            </Card>
          ))}
        </Box>
      )}
    </Box>
  );
};

export default RecommendedSitters;
