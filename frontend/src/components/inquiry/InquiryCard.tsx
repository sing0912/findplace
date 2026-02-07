/**
 * @fileoverview 문의 카드 컴포넌트
 * @see docs/develop/user/frontend.md - 섹션 9.1
 */

import React from 'react';
import { Box, Typography, Chip } from '@mui/material';
import ChevronRightIcon from '@mui/icons-material/ChevronRight';

type InquiryStatus = 'WAITING' | 'ANSWERED';

interface InquiryCardProps {
  id: number;
  title: string;
  status: InquiryStatus;
  createdAt: string;
  onClick?: () => void;
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

const InquiryCard: React.FC<InquiryCardProps> = ({
  title,
  status,
  createdAt,
  onClick,
}) => {
  const statusInfo = statusConfig[status];

  const formatDate = (dateString: string): string => {
    const date = new Date(dateString);
    return date.toLocaleDateString('ko-KR', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
    });
  };

  return (
    <Box
      onClick={onClick}
      sx={{
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'space-between',
        py: 2,
        px: 1,
        borderBottom: '1px solid #F5F5F5',
        cursor: onClick ? 'pointer' : 'default',
        '&:hover': onClick
          ? {
              backgroundColor: '#F5FAF8',
            }
          : {},
      }}
    >
      <Box sx={{ flex: 1 }}>
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 0.5 }}>
          <Typography
            sx={{
              fontFamily: 'Noto Sans KR, sans-serif',
              fontSize: '14px',
              fontWeight: 500,
              color: '#000000',
              overflow: 'hidden',
              textOverflow: 'ellipsis',
              whiteSpace: 'nowrap',
              maxWidth: '200px',
            }}
          >
            {title}
          </Typography>
          <Chip
            label={statusInfo.label}
            size="small"
            sx={{
              height: '20px',
              fontSize: '11px',
              fontWeight: 500,
              color: statusInfo.color,
              backgroundColor: statusInfo.bgColor,
              '& .MuiChip-label': {
                px: 1,
              },
            }}
          />
        </Box>
        <Typography
          sx={{
            fontFamily: 'Noto Sans KR, sans-serif',
            fontSize: '12px',
            color: '#AEAEAE',
          }}
        >
          {formatDate(createdAt)}
        </Typography>
      </Box>
      <ChevronRightIcon sx={{ color: '#AEAEAE', width: 20, height: 20 }} />
    </Box>
  );
};

export default InquiryCard;
