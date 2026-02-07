/**
 * @fileoverview 인증용 버튼 컴포넌트
 * @see docs/develop/user/frontend.md - 디자인 시스템 2.3
 */

import React from 'react';
import { Button, CircularProgress } from '@mui/material';

interface AuthButtonProps {
  children: React.ReactNode;
  onClick?: () => void;
  type?: 'button' | 'submit';
  variant?: 'primary' | 'secondary' | 'text';
  fullWidth?: boolean;
  disabled?: boolean;
  loading?: boolean;
}

const AuthButton: React.FC<AuthButtonProps> = ({
  children,
  onClick,
  type = 'button',
  variant = 'primary',
  fullWidth = false,
  disabled = false,
  loading = false,
}) => {
  const getStyles = () => {
    const baseStyles = {
      height: '45px',
      borderRadius: '10px',
      fontFamily: 'Noto Sans, sans-serif',
      fontSize: '14px',
      fontWeight: 400,
      textTransform: 'none' as const,
      boxShadow: 'none',
    };

    switch (variant) {
      case 'primary':
        return {
          ...baseStyles,
          backgroundColor: '#76BCA2',
          color: '#FFFFFF',
          '&:hover': {
            backgroundColor: '#5FA88E',
            boxShadow: 'none',
          },
          '&:disabled': {
            backgroundColor: '#AEAEAE',
            color: '#FFFFFF',
          },
        };
      case 'secondary':
        return {
          ...baseStyles,
          backgroundColor: '#FFFFFF',
          color: '#76BCA2',
          border: '1px solid #76BCA2',
          '&:hover': {
            backgroundColor: '#F5FAF8',
            boxShadow: 'none',
          },
          '&:disabled': {
            backgroundColor: '#FFFFFF',
            color: '#AEAEAE',
            borderColor: '#AEAEAE',
          },
        };
      case 'text':
        return {
          ...baseStyles,
          backgroundColor: 'transparent',
          color: '#404040',
          '&:hover': {
            backgroundColor: '#F5F5F5',
            boxShadow: 'none',
          },
          '&:disabled': {
            backgroundColor: 'transparent',
            color: '#AEAEAE',
          },
        };
      default:
        return baseStyles;
    }
  };

  return (
    <Button
      type={type}
      onClick={onClick}
      disabled={disabled || loading}
      fullWidth={fullWidth}
      sx={{
        width: fullWidth ? '100%' : '230px',
        minWidth: fullWidth ? undefined : '230px',
        ...getStyles(),
      }}
    >
      {loading ? <CircularProgress size={20} color="inherit" /> : children}
    </Button>
  );
};

export default AuthButton;
