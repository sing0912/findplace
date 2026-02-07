/**
 * @fileoverview 마이페이지 메뉴 아이템 컴포넌트
 * @see docs/develop/user/frontend.md - 섹션 7.1
 */

import React from 'react';
import { Box, Typography } from '@mui/material';
import ChevronRightIcon from '@mui/icons-material/ChevronRight';

interface MenuItemProps {
  label: string;
  onClick?: () => void;
  showArrow?: boolean;
  color?: string;
  icon?: React.ReactNode;
  disabled?: boolean;
}

const MenuItem: React.FC<MenuItemProps> = ({
  label,
  onClick,
  showArrow = true,
  color = '#404040',
  icon,
  disabled = false,
}) => {
  return (
    <Box
      onClick={disabled ? undefined : onClick}
      sx={{
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'space-between',
        py: 2,
        px: 1,
        cursor: disabled ? 'default' : onClick ? 'pointer' : 'default',
        opacity: disabled ? 0.5 : 1,
        borderBottom: '1px solid #F5F5F5',
        '&:hover': !disabled && onClick
          ? {
              backgroundColor: '#F5FAF8',
            }
          : {},
        '&:last-child': {
          borderBottom: 'none',
        },
      }}
    >
      <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5 }}>
        {icon && (
          <Box sx={{ display: 'flex', color: disabled ? '#AEAEAE' : '#76BCA2', fontSize: 20 }}>
            {icon}
          </Box>
        )}
        <Typography
          sx={{
            fontFamily: 'Noto Sans KR, sans-serif',
            fontSize: '14px',
            fontWeight: 400,
            color: disabled ? '#AEAEAE' : color,
          }}
        >
          {label}
        </Typography>
      </Box>
      {showArrow && (
        <ChevronRightIcon
          sx={{
            color: '#AEAEAE',
            width: 20,
            height: 20,
          }}
        />
      )}
    </Box>
  );
};

export default MenuItem;
