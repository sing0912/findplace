/**
 * @fileoverview 문의 목록 페이지
 * @see docs/develop/user/frontend.md - 섹션 9.1
 */

import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { Box, Container, Typography, Button } from '@mui/material';
import AddIcon from '@mui/icons-material/Add';
import { InquiryCard } from '../../components/inquiry';
import { useInquiry } from '../../hooks/useInquiry';

const InquiryListPage: React.FC = () => {
  const navigate = useNavigate();
  const { inquiries, isLoading, hasMore, fetchInquiries } = useInquiry();
  const [page, setPage] = useState(0);

  useEffect(() => {
    fetchInquiries(0);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const handleLoadMore = () => {
    const nextPage = page + 1;
    setPage(nextPage);
    fetchInquiries(nextPage);
  };

  if (isLoading && inquiries.length === 0) {
    return (
      <Container maxWidth="sm">
        <Box
          sx={{
            minHeight: '100vh',
            display: 'flex',
            justifyContent: 'center',
            alignItems: 'center',
          }}
        >
          <Typography>로딩 중...</Typography>
        </Box>
      </Container>
    );
  }

  return (
    <Container maxWidth="sm">
      <Box
        sx={{
          minHeight: '100vh',
          py: 4,
        }}
      >
        {/* 헤더 */}
        <Box
          sx={{
            display: 'flex',
            justifyContent: 'space-between',
            alignItems: 'center',
            mb: 3,
            px: 2,
          }}
        >
          <Typography
            variant="h6"
            sx={{
              fontFamily: 'Noto Sans, sans-serif',
              fontWeight: 700,
              fontSize: '16px',
              color: '#000000',
            }}
          >
            문의 게시판
          </Typography>
          <Button
            startIcon={<AddIcon />}
            onClick={() => navigate('/mypage/inquiry/write')}
            sx={{
              fontFamily: 'Noto Sans KR, sans-serif',
              fontSize: '14px',
              color: '#76BCA2',
              textTransform: 'none',
            }}
          >
            문의하기
          </Button>
        </Box>

        {/* 목록 */}
        <Box sx={{ px: 2 }}>
          {inquiries.length === 0 ? (
            <Box
              sx={{
                py: 8,
                textAlign: 'center',
              }}
            >
              <Typography
                sx={{
                  fontFamily: 'Noto Sans KR, sans-serif',
                  fontSize: '14px',
                  color: '#AEAEAE',
                  mb: 2,
                }}
              >
                등록된 문의가 없습니다.
              </Typography>
              <Button
                variant="outlined"
                onClick={() => navigate('/mypage/inquiry/write')}
                sx={{
                  fontFamily: 'Noto Sans KR, sans-serif',
                  fontSize: '14px',
                  color: '#76BCA2',
                  borderColor: '#76BCA2',
                  textTransform: 'none',
                  '&:hover': {
                    borderColor: '#5FA88E',
                    backgroundColor: '#F5FAF8',
                  },
                }}
              >
                첫 번째 문의 작성하기
              </Button>
            </Box>
          ) : (
            <>
              {inquiries.map((inquiry) => (
                <InquiryCard
                  key={inquiry.id}
                  id={inquiry.id}
                  title={inquiry.title}
                  status={inquiry.status}
                  createdAt={inquiry.createdAt}
                  onClick={() => navigate(`/mypage/inquiry/${inquiry.id}`)}
                />
              ))}

              {hasMore && (
                <Box sx={{ textAlign: 'center', mt: 2 }}>
                  <Button
                    onClick={handleLoadMore}
                    sx={{
                      fontFamily: 'Noto Sans KR, sans-serif',
                      fontSize: '14px',
                      color: '#AEAEAE',
                      textTransform: 'none',
                    }}
                  >
                    더 보기
                  </Button>
                </Box>
              )}
            </>
          )}
        </Box>
      </Box>
    </Container>
  );
};

export default InquiryListPage;
