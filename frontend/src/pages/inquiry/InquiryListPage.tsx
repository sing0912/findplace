/**
 * @fileoverview 문의 목록 페이지
 * @see docs/develop/user/frontend.md - 섹션 9.1
 */

import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { Box, Container, Typography, Button } from '@mui/material';
import AddIcon from '@mui/icons-material/Add';
import { InquiryCard } from '../../components/inquiry';

type InquiryStatus = 'WAITING' | 'ANSWERED';

interface Inquiry {
  id: number;
  title: string;
  status: InquiryStatus;
  createdAt: string;
}

interface InquiryListResponse {
  content: Inquiry[];
  totalElements: number;
  totalPages: number;
  number: number;
}

const InquiryListPage: React.FC = () => {
  const navigate = useNavigate();
  const [inquiries, setInquiries] = useState<Inquiry[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [page, setPage] = useState(0);
  const [hasMore, setHasMore] = useState(true);

  useEffect(() => {
    fetchInquiries();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const fetchInquiries = async (pageNum = 0) => {
    try {
      const token = localStorage.getItem('accessToken');
      if (!token) {
        navigate('/login');
        return;
      }

      const response = await fetch(`/api/v1/inquiries?page=${pageNum}&size=10`, {
        headers: {
          Authorization: `Bearer ${token}`,
        },
      });

      if (!response.ok) {
        if (response.status === 401) {
          navigate('/login');
          return;
        }
        throw new Error('문의 목록을 불러오는데 실패했습니다.');
      }

      const result = await response.json();
      const data: InquiryListResponse = result.data || result;

      if (pageNum === 0) {
        setInquiries(data.content ?? []);
      } else {
        setInquiries((prev) => [...prev, ...(data.content ?? [])]);
      }

      setPage(data.number ?? 0);
      setHasMore((data.number ?? 0) < (data.totalPages ?? 1) - 1);
    } catch (err) {
      console.error(err);
    } finally {
      setIsLoading(false);
    }
  };

  const handleLoadMore = () => {
    fetchInquiries(page + 1);
  };

  if (isLoading) {
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
