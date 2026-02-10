import React from 'react';
import { Box, Typography, Avatar } from '@mui/material';
import FavoriteBorderIcon from '@mui/icons-material/FavoriteBorder';
import ChatBubbleOutlineIcon from '@mui/icons-material/ChatBubbleOutline';
import { useNavigate } from 'react-router-dom';
import { CommunityFeed } from '../../../types/home';

interface CommunityFeedsProps {
  feeds: CommunityFeed[];
}

const CommunityFeeds: React.FC<CommunityFeedsProps> = ({ feeds }) => {
  const navigate = useNavigate();
  const displayFeeds = feeds.slice(0, 3);

  return (
    <Box>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 1.5 }}>
        <Typography sx={{ fontSize: '16px', fontWeight: 700, color: '#000' }}>
          커뮤니티
        </Typography>
        <Typography
          onClick={() => navigate('/community')}
          sx={{ fontSize: '13px', color: '#AEAEAE', cursor: 'pointer' }}
        >
          더보기
        </Typography>
      </Box>

      {displayFeeds.length === 0 ? (
        <Box sx={{ textAlign: 'center', py: 4 }}>
          <Typography sx={{ fontSize: '14px', color: '#AEAEAE' }}>
            커뮤니티 글이 없습니다
          </Typography>
        </Box>
      ) : (
        <Box sx={{ display: 'flex', flexDirection: 'column' }}>
          {displayFeeds.map((feed) => (
            <Box
              key={feed.id}
              onClick={() => navigate(`/community/${feed.id}`)}
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
                variant="rounded"
                src={feed.thumbnailUrl || undefined}
                sx={{
                  width: 40,
                  height: 40,
                  backgroundColor: '#F5F5F5',
                  borderRadius: '8px',
                }}
              />

              <Box sx={{ flex: 1, minWidth: 0 }}>
                <Typography
                  sx={{
                    fontSize: '14px',
                    fontWeight: 500,
                    color: '#000',
                    overflow: 'hidden',
                    textOverflow: 'ellipsis',
                    whiteSpace: 'nowrap',
                    mb: 0.25,
                  }}
                >
                  {feed.title}
                </Typography>
                <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                  <Typography sx={{ fontSize: '12px', color: '#AEAEAE' }}>
                    {feed.categoryName}
                  </Typography>
                  <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.5 }}>
                    <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.25 }}>
                      <FavoriteBorderIcon sx={{ fontSize: 13, color: '#AEAEAE' }} />
                      <Typography sx={{ fontSize: '11px', color: '#AEAEAE' }}>
                        {feed.likeCount}
                      </Typography>
                    </Box>
                    <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.25 }}>
                      <ChatBubbleOutlineIcon sx={{ fontSize: 13, color: '#AEAEAE' }} />
                      <Typography sx={{ fontSize: '11px', color: '#AEAEAE' }}>
                        {feed.commentCount}
                      </Typography>
                    </Box>
                  </Box>
                </Box>
              </Box>
            </Box>
          ))}
        </Box>
      )}
    </Box>
  );
};

export default CommunityFeeds;
