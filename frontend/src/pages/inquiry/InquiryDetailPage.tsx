/**
 * @fileoverview 문의 상세 페이지
 * @see docs/develop/user/frontend.md - 섹션 9.3
 */

import React, { useState, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { Box, Container, Typography, Chip, Divider, Dialog, DialogTitle, DialogContent, DialogActions, Button, Alert } from '@mui/material';
import { InquiryForm } from '../../components/inquiry';
import { AuthButton } from '../../components/auth';

type InquiryStatus = 'WAITING' | 'ANSWERED';

interface InquiryAnswer {
  content: string;
  createdAt: string;
}

interface InquiryDetail {
  id: number;
  title: string;
  content: string;
  status: InquiryStatus;
  createdAt: string;
  answer: InquiryAnswer | null;
}

const statusConfig: Record<InquiryStatus, { label: string; color: string; bgColor: string }> = {
  WAITING: {
    label: '답변대기',
    color: '#FF9800',
    bgColor: '#FFF3E0',
  },
  ANSWERED: {
    label: '답변완료',
    color: '#76BCA2',
    bgColor: '#F5FAF8',
  },
};

const InquiryDetailPage: React.FC = () => {
  const navigate = useNavigate();
  const { id } = useParams<{ id: string }>();

  const [inquiry, setInquiry] = useState<InquiryDetail | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [isEditing, setIsEditing] = useState(false);
  const [editTitle, setEditTitle] = useState('');
  const [editContent, setEditContent] = useState('');
  const [isSaving, setIsSaving] = useState(false);
  const [showDeleteDialog, setShowDeleteDialog] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    fetchInquiry();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [id]);

  const fetchInquiry = async () => {
    try {
      const token = localStorage.getItem('accessToken');
      if (!token) {
        navigate('/login');
        return;
      }

      const response = await fetch(`/api/v1/inquiries/${id}`, {
        headers: {
          Authorization: `Bearer ${token}`,
        },
      });

      if (!response.ok) {
        if (response.status === 404) {
          navigate('/mypage/inquiry');
          return;
        }
        throw new Error('문의를 불러오는데 실패했습니다.');
      }

      const result = await response.json();
      const data: InquiryDetail = result.data || result;
      setInquiry(data);
      setEditTitle(data.title);
      setEditContent(data.content);
    } catch (err) {
      console.error(err);
      navigate('/mypage/inquiry');
    } finally {
      setIsLoading(false);
    }
  };

  const handleEdit = async () => {
    if (!editTitle.trim() || !editContent.trim()) return;

    setIsSaving(true);
    setError('');

    try {
      const token = localStorage.getItem('accessToken');
      const response = await fetch(`/api/v1/inquiries/${id}`, {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
          Authorization: `Bearer ${token}`,
        },
        body: JSON.stringify({
          title: editTitle.trim(),
          content: editContent.trim(),
        }),
      });

      if (!response.ok) {
        const errorData = await response.json();
        throw new Error(errorData.message || '수정에 실패했습니다.');
      }

      await fetchInquiry();
      setIsEditing(false);
    } catch (err) {
      setError(err instanceof Error ? err.message : '알 수 없는 오류가 발생했습니다.');
    } finally {
      setIsSaving(false);
    }
  };

  const handleDelete = async () => {
    try {
      const token = localStorage.getItem('accessToken');
      const response = await fetch(`/api/v1/inquiries/${id}`, {
        method: 'DELETE',
        headers: {
          Authorization: `Bearer ${token}`,
        },
      });

      if (!response.ok) {
        const errorData = await response.json();
        throw new Error(errorData.message || '삭제에 실패했습니다.');
      }

      navigate('/mypage/inquiry');
    } catch (err) {
      setError(err instanceof Error ? err.message : '알 수 없는 오류가 발생했습니다.');
      setShowDeleteDialog(false);
    }
  };

  const formatDate = (dateString: string): string => {
    const date = new Date(dateString);
    return date.toLocaleDateString('ko-KR', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit',
    });
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

  if (!inquiry) return null;

  const statusInfo = statusConfig[inquiry.status] || statusConfig.WAITING;
  const canEdit = inquiry.status === 'WAITING';

  return (
    <Container maxWidth="sm">
      <Box
        sx={{
          minHeight: '100vh',
          py: 4,
          pb: 10,
        }}
      >
        {/* 헤더 */}
        <Box sx={{ mb: 3 }}>
          <Typography
            variant="h6"
            sx={{
              fontFamily: 'Noto Sans, sans-serif',
              fontWeight: 700,
              fontSize: '16px',
              color: '#000000',
              textAlign: 'center',
            }}
          >
            문의 상세
          </Typography>
        </Box>

        <Box sx={{ px: 2 }}>
          {error && (
            <Alert severity="error" sx={{ mb: 2 }}>
              {error}
            </Alert>
          )}

          {isEditing ? (
            // 수정 모드
            <>
              <InquiryForm
                title={editTitle}
                content={editContent}
                onTitleChange={setEditTitle}
                onContentChange={setEditContent}
              />
              <Box sx={{ display: 'flex', gap: 2, mt: 3 }}>
                <AuthButton
                  variant="secondary"
                  onClick={() => {
                    setIsEditing(false);
                    setEditTitle(inquiry.title);
                    setEditContent(inquiry.content);
                  }}
                  fullWidth
                >
                  취소
                </AuthButton>
                <AuthButton
                  onClick={handleEdit}
                  loading={isSaving}
                  disabled={!editTitle.trim() || !editContent.trim()}
                  fullWidth
                >
                  저장
                </AuthButton>
              </Box>
            </>
          ) : (
            // 조회 모드
            <>
              {/* 문의 정보 */}
              <Box sx={{ mb: 3 }}>
                <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 1 }}>
                  <Chip
                    label={statusInfo.label}
                    size="small"
                    sx={{
                      height: '24px',
                      fontSize: '12px',
                      fontWeight: 500,
                      color: statusInfo.color,
                      backgroundColor: statusInfo.bgColor,
                    }}
                  />
                  <Typography
                    sx={{
                      fontFamily: 'Noto Sans KR, sans-serif',
                      fontSize: '12px',
                      color: '#AEAEAE',
                    }}
                  >
                    {formatDate(inquiry.createdAt)}
                  </Typography>
                </Box>
                <Typography
                  sx={{
                    fontFamily: 'Noto Sans, sans-serif',
                    fontSize: '16px',
                    fontWeight: 700,
                    color: '#000000',
                    mb: 2,
                  }}
                >
                  {inquiry.title}
                </Typography>
                <Typography
                  sx={{
                    fontFamily: 'Noto Sans KR, sans-serif',
                    fontSize: '14px',
                    color: '#404040',
                    whiteSpace: 'pre-wrap',
                  }}
                >
                  {inquiry.content}
                </Typography>
              </Box>

              {/* 답변 */}
              {inquiry.answer && (
                <>
                  <Divider sx={{ my: 3 }} />
                  <Box
                    sx={{
                      backgroundColor: '#F5FAF8',
                      borderRadius: '8px',
                      p: 2,
                    }}
                  >
                    <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 1 }}>
                      <Typography
                        sx={{
                          fontFamily: 'Noto Sans, sans-serif',
                          fontSize: '14px',
                          fontWeight: 700,
                          color: '#76BCA2',
                        }}
                      >
                        답변
                      </Typography>
                      <Typography
                        sx={{
                          fontFamily: 'Noto Sans KR, sans-serif',
                          fontSize: '12px',
                          color: '#AEAEAE',
                        }}
                      >
                        {formatDate(inquiry.answer.createdAt)}
                      </Typography>
                    </Box>
                    <Typography
                      sx={{
                        fontFamily: 'Noto Sans KR, sans-serif',
                        fontSize: '14px',
                        color: '#404040',
                        whiteSpace: 'pre-wrap',
                      }}
                    >
                      {inquiry.answer.content}
                    </Typography>
                  </Box>
                </>
              )}

              {/* 수정/삭제 버튼 (답변 전만) */}
              {canEdit && (
                <Box sx={{ display: 'flex', gap: 2, mt: 4 }}>
                  <AuthButton
                    variant="secondary"
                    onClick={() => setShowDeleteDialog(true)}
                    fullWidth
                  >
                    삭제
                  </AuthButton>
                  <AuthButton
                    onClick={() => setIsEditing(true)}
                    fullWidth
                  >
                    수정
                  </AuthButton>
                </Box>
              )}
            </>
          )}
        </Box>
      </Box>

      {/* 삭제 확인 다이얼로그 */}
      <Dialog
        open={showDeleteDialog}
        onClose={() => setShowDeleteDialog(false)}
      >
        <DialogTitle sx={{ fontFamily: 'Noto Sans, sans-serif' }}>
          문의 삭제
        </DialogTitle>
        <DialogContent>
          <Typography sx={{ fontFamily: 'Noto Sans KR, sans-serif' }}>
            이 문의를 삭제하시겠습니까?
          </Typography>
        </DialogContent>
        <DialogActions>
          <Button
            onClick={() => setShowDeleteDialog(false)}
            sx={{ color: '#AEAEAE' }}
          >
            취소
          </Button>
          <Button
            onClick={handleDelete}
            sx={{ color: '#FF0000' }}
          >
            삭제
          </Button>
        </DialogActions>
      </Dialog>
    </Container>
  );
};

export default InquiryDetailPage;
