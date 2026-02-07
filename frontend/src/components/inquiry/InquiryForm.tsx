/**
 * @fileoverview 문의 작성 폼 컴포넌트
 * @see docs/develop/user/frontend.md - 섹션 9.2
 */

import React from 'react';
import { Box, TextField, Typography } from '@mui/material';

interface InquiryFormProps {
  title: string;
  content: string;
  onTitleChange: (value: string) => void;
  onContentChange: (value: string) => void;
  titleError?: string;
  contentError?: string;
  disabled?: boolean;
}

const InquiryForm: React.FC<InquiryFormProps> = ({
  title,
  content,
  onTitleChange,
  onContentChange,
  titleError,
  contentError,
  disabled = false,
}) => {
  return (
    <Box>
      {/* 제목 */}
      <Box sx={{ mb: 2 }}>
        <Typography
          component="label"
          htmlFor="inquiry-title"
          sx={{
            display: 'block',
            mb: 0.5,
            fontFamily: 'Noto Sans, sans-serif',
            fontSize: '14px',
            fontWeight: 400,
            color: '#000000',
          }}
        >
          제목 <span style={{ color: '#000000' }}>*</span>
        </Typography>
        <TextField
          id="inquiry-title"
          value={title}
          onChange={(e) => onTitleChange(e.target.value)}
          placeholder="제목을 입력해주세요"
          error={!!titleError}
          helperText={titleError}
          disabled={disabled}
          fullWidth
          sx={{
            '& .MuiOutlinedInput-root': {
              height: '50px',
              backgroundColor: disabled ? '#F5F5F5' : '#FFFFFF',
              borderRadius: '5px',
              fontFamily: 'Noto Sans KR, sans-serif',
              fontSize: '14px',
              '& fieldset': {
                borderColor: titleError ? '#FF0000' : '#AEAEAE',
              },
              '&:hover fieldset': {
                borderColor: titleError ? '#FF0000' : '#76BCA2',
              },
              '&.Mui-focused fieldset': {
                borderColor: titleError ? '#FF0000' : '#76BCA2',
                borderWidth: '1px',
              },
            },
            '& .MuiOutlinedInput-input': {
              padding: '0 10px',
              color: '#404040',
              '&::placeholder': {
                color: '#AEAEAE',
                opacity: 1,
              },
            },
            '& .MuiFormHelperText-root': {
              marginLeft: 0,
              color: '#FF0000',
            },
          }}
        />
      </Box>

      {/* 내용 */}
      <Box>
        <Typography
          component="label"
          htmlFor="inquiry-content"
          sx={{
            display: 'block',
            mb: 0.5,
            fontFamily: 'Noto Sans, sans-serif',
            fontSize: '14px',
            fontWeight: 400,
            color: '#000000',
          }}
        >
          내용 <span style={{ color: '#000000' }}>*</span>
        </Typography>
        <TextField
          id="inquiry-content"
          value={content}
          onChange={(e) => onContentChange(e.target.value)}
          placeholder="문의 내용을 입력해주세요"
          error={!!contentError}
          helperText={contentError}
          disabled={disabled}
          fullWidth
          multiline
          rows={8}
          sx={{
            '& .MuiOutlinedInput-root': {
              backgroundColor: disabled ? '#F5F5F5' : '#FFFFFF',
              borderRadius: '5px',
              fontFamily: 'Noto Sans KR, sans-serif',
              fontSize: '14px',
              '& fieldset': {
                borderColor: contentError ? '#FF0000' : '#AEAEAE',
              },
              '&:hover fieldset': {
                borderColor: contentError ? '#FF0000' : '#76BCA2',
              },
              '&.Mui-focused fieldset': {
                borderColor: contentError ? '#FF0000' : '#76BCA2',
                borderWidth: '1px',
              },
            },
            '& .MuiOutlinedInput-input': {
              color: '#404040',
              '&::placeholder': {
                color: '#AEAEAE',
                opacity: 1,
              },
            },
            '& .MuiFormHelperText-root': {
              marginLeft: 0,
              color: '#FF0000',
            },
          }}
        />
      </Box>
    </Box>
  );
};

export default InquiryForm;
