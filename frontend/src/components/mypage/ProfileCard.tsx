/**
 * @fileoverview 프로필 카드 컴포넌트
 * @see docs/develop/user/frontend.md - 섹션 7.1
 */

import React from 'react';
import { Box, Avatar, Typography, IconButton } from '@mui/material';
import EditIcon from '@mui/icons-material/Edit';

interface ProfileCardProps {
  profileImageUrl?: string | null;
  nickname: string;
  email: string;
  onEditClick?: () => void;
}

const ProfileCard: React.FC<ProfileCardProps> = ({
  profileImageUrl,
  nickname,
  email,
  onEditClick,
}) => {
  return (
    <Box
      sx={{
        display: 'flex',
        alignItems: 'center',
        p: 2,
        backgroundColor: '#F5FAF8',
        borderRadius: '12px',
      }}
    >
      {/* 프로필 이미지 */}
      <Avatar
        src={profileImageUrl || undefined}
        alt={nickname}
        sx={{
          width: 64,
          height: 64,
          backgroundColor: '#76BCA2',
          fontSize: '24px',
          fontWeight: 700,
        }}
      >
        {nickname?.charAt(0).toUpperCase()}
      </Avatar>

      {/* 사용자 정보 */}
      <Box sx={{ flex: 1, ml: 2 }}>
        <Typography
          sx={{
            fontFamily: 'Noto Sans, sans-serif',
            fontSize: '16px',
            fontWeight: 700,
            color: '#000000',
            mb: 0.5,
          }}
        >
          {nickname}
        </Typography>
        <Typography
          sx={{
            fontFamily: 'Noto Sans KR, sans-serif',
            fontSize: '14px',
            color: '#AEAEAE',
          }}
        >
          {email}
        </Typography>
      </Box>

      {/* 수정 버튼 */}
      {onEditClick && (
        <IconButton
          onClick={onEditClick}
          sx={{
            color: '#AEAEAE',
            '&:hover': {
              color: '#76BCA2',
            },
          }}
        >
          <EditIcon />
        </IconButton>
      )}
    </Box>
  );
};

export default ProfileCard;
