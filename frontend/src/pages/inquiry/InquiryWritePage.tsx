/**
 * @fileoverview 문의 작성 페이지
 * @see docs/develop/user/frontend.md - 섹션 9.2
 */

import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Box, Container, Typography, Alert } from '@mui/material';
import { InquiryForm } from '../../components/inquiry';
import { AuthButton } from '../../components/auth';
import { inquiryApi } from '../../api/inquiry';

const InquiryWritePage: React.FC = () => {
  const navigate = useNavigate();
  const [title, setTitle] = useState('');
  const [content, setContent] = useState('');
  const [titleError, setTitleError] = useState('');
  const [contentError, setContentError] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState('');

  const validateForm = (): boolean => {
    let isValid = true;

    if (!title.trim()) {
      setTitleError('제목을 입력해주세요.');
      isValid = false;
    } else {
      setTitleError('');
    }

    if (!content.trim()) {
      setContentError('내용을 입력해주세요.');
      isValid = false;
    } else {
      setContentError('');
    }

    return isValid;
  };

  const handleSubmit = async () => {
    if (!validateForm()) return;

    setIsLoading(true);
    setError('');

    try {
      const data = await inquiryApi.create({
        title: title.trim(),
        content: content.trim(),
      });

      navigate(`/mypage/inquiry/${data.id}`);
    } catch (err) {
      setError(err instanceof Error ? err.message : '알 수 없는 오류가 발생했습니다.');
    } finally {
      setIsLoading(false);
    }
  };

  const isFormValid = title.trim().length > 0 && content.trim().length > 0;

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
        <Box sx={{ mb: 4 }}>
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
            문의하기
          </Typography>
        </Box>

        {/* 폼 */}
        <Box sx={{ px: 2 }}>
          {error && (
            <Alert severity="error" sx={{ mb: 2 }}>
              {error}
            </Alert>
          )}

          <InquiryForm
            title={title}
            content={content}
            onTitleChange={setTitle}
            onContentChange={setContent}
            titleError={titleError}
            contentError={contentError}
          />
        </Box>

        {/* 등록 버튼 */}
        <Box
          sx={{
            position: 'fixed',
            bottom: 0,
            left: 0,
            right: 0,
            p: 2,
            backgroundColor: '#FFFFFF',
          }}
        >
          <AuthButton
            onClick={handleSubmit}
            disabled={!isFormValid}
            loading={isLoading}
            fullWidth
          >
            등록
          </AuthButton>
        </Box>
      </Box>
    </Container>
  );
};

export default InquiryWritePage;
