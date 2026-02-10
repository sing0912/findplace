import React from 'react';
import { Box, Typography, Card, CardContent, Button, Divider } from '@mui/material';
import { useNavigate } from 'react-router-dom';
import { NoticeItem } from '../../../types/home';

interface NoticeListProps {
  notices: NoticeItem[];
}

const formatDate = (dateString: string): string => {
  const date = new Date(dateString);
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  return `${month}.${day}`;
};

const NoticeList: React.FC<NoticeListProps> = ({ notices }) => {
  const navigate = useNavigate();

  const displayNotices = notices.slice(0, 3);

  return (
    <Card sx={{ borderRadius: '12px', boxShadow: '0 1px 4px rgba(0,0,0,0.08)' }}>
      <CardContent sx={{ p: 2, '&:last-child': { pb: 2 } }}>
        <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
          <Typography
            sx={{
              fontFamily: 'Noto Sans KR, sans-serif',
              fontSize: '16px',
              fontWeight: 700,
              color: '#000000',
            }}
          >
            공지사항
          </Typography>
          <Button
            onClick={() => navigate('/notices')}
            sx={{
              fontFamily: 'Noto Sans KR, sans-serif',
              fontSize: '13px',
              color: '#AEAEAE',
              minWidth: 'auto',
              p: 0,
              '&:hover': { backgroundColor: 'transparent', color: '#666666' },
            }}
          >
            더보기
          </Button>
        </Box>

        {displayNotices.length === 0 ? (
          <Typography
            sx={{
              fontFamily: 'Noto Sans KR, sans-serif',
              fontSize: '14px',
              color: '#AEAEAE',
              textAlign: 'center',
              py: 2,
            }}
          >
            등록된 공지사항이 없습니다
          </Typography>
        ) : (
          displayNotices.map((notice, index) => (
            <React.Fragment key={notice.id}>
              {index > 0 && <Divider sx={{ my: 1 }} />}
              <Box
                onClick={() => navigate(`/notices/${notice.id}`)}
                sx={{
                  display: 'flex',
                  justifyContent: 'space-between',
                  alignItems: 'center',
                  py: 1,
                  cursor: 'pointer',
                  '&:hover': { backgroundColor: '#F5FAF8', borderRadius: '4px' },
                }}
              >
                <Typography
                  sx={{
                    fontFamily: 'Noto Sans KR, sans-serif',
                    fontSize: '14px',
                    fontWeight: 500,
                    color: '#333333',
                    overflow: 'hidden',
                    textOverflow: 'ellipsis',
                    whiteSpace: 'nowrap',
                    flex: 1,
                    mr: 2,
                  }}
                >
                  {notice.title}
                </Typography>
                <Typography
                  sx={{
                    fontFamily: 'Noto Sans KR, sans-serif',
                    fontSize: '12px',
                    color: '#AEAEAE',
                    flexShrink: 0,
                  }}
                >
                  {formatDate(notice.createdAt)}
                </Typography>
              </Box>
            </React.Fragment>
          ))
        )}
      </CardContent>
    </Card>
  );
};

export default NoticeList;
