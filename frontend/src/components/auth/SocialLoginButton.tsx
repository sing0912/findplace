/**
 * @fileoverview 소셜 로그인 버튼 컴포넌트
 * @see docs/develop/user/frontend.md - 디자인 시스템 2.3
 */

import React from 'react';
import { Button, Box } from '@mui/material';

type SocialProvider = 'kakao' | 'naver' | 'google';

interface SocialLoginButtonProps {
  provider: SocialProvider;
  onClick?: () => void;
  disabled?: boolean;
}

interface ProviderConfig {
  label: string;
  backgroundColor: string;
  color: string;
  hoverColor: string;
  border?: string;
  icon: React.ReactNode;
}

const providerConfig: Record<SocialProvider, ProviderConfig> = {
  kakao: {
    label: '카카오로 시작하기',
    backgroundColor: '#FEE500',
    color: '#000000',
    hoverColor: '#E6CF00',
    icon: (
      <svg width="20" height="20" viewBox="0 0 20 20" fill="none">
        <path
          fillRule="evenodd"
          clipRule="evenodd"
          d="M10 2C5.02944 2 1 5.28 1 9.32C1 11.98 2.8 14.32 5.52 15.6L4.52 18.86C4.44 19.12 4.74 19.32 4.96 19.16L8.88 16.52C9.24 16.56 9.62 16.58 10 16.58C14.9706 16.58 19 13.3 19 9.26C19 5.28 14.9706 2 10 2Z"
          fill="#000000"
        />
      </svg>
    ),
  },
  naver: {
    label: '네이버로 시작하기',
    backgroundColor: '#03C75A',
    color: '#FFFFFF',
    hoverColor: '#02B350',
    icon: (
      <svg width="20" height="20" viewBox="0 0 20 20" fill="none">
        <path
          d="M13.5 10.5L6.5 2H2V18H6.5V9.5L13.5 18H18V2H13.5V10.5Z"
          fill="#FFFFFF"
        />
      </svg>
    ),
  },
  google: {
    label: 'Google로 시작하기',
    backgroundColor: '#FFFFFF',
    color: '#000000',
    hoverColor: '#F5F5F5',
    border: '1px solid #DADCE0',
    icon: (
      <svg width="20" height="20" viewBox="0 0 20 20" fill="none">
        <path
          d="M19.6 10.2273C19.6 9.51818 19.5364 8.83636 19.4182 8.18182H10V12.05H15.3818C15.15 13.3 14.4455 14.3591 13.3864 15.0682V17.5773H16.6182C18.5091 15.8364 19.6 13.2727 19.6 10.2273Z"
          fill="#4285F4"
        />
        <path
          d="M10 20C12.7 20 14.9636 19.1045 16.6182 17.5773L13.3864 15.0682C12.4909 15.6682 11.3455 16.0227 10 16.0227C7.39545 16.0227 5.19091 14.2636 4.40455 11.9H1.07727V14.4909C2.72273 17.7591 6.09091 20 10 20Z"
          fill="#34A853"
        />
        <path
          d="M4.40455 11.9C4.20455 11.3 4.09091 10.6636 4.09091 10C4.09091 9.33636 4.20455 8.7 4.40455 8.1V5.50909H1.07727C0.390909 6.85909 0 8.38636 0 10C0 11.6136 0.390909 13.1409 1.07727 14.4909L4.40455 11.9Z"
          fill="#FBBC05"
        />
        <path
          d="M10 3.97727C11.4682 3.97727 12.7864 4.48182 13.8227 5.47273L16.6909 2.60455C14.9591 0.990909 12.6955 0 10 0C6.09091 0 2.72273 2.24091 1.07727 5.50909L4.40455 8.1C5.19091 5.73636 7.39545 3.97727 10 3.97727Z"
          fill="#EA4335"
        />
      </svg>
    ),
  },
};

const SocialLoginButton: React.FC<SocialLoginButtonProps> = ({
  provider,
  onClick,
  disabled = false,
}) => {
  const config = providerConfig[provider];

  return (
    <Button
      onClick={onClick}
      disabled={disabled}
      fullWidth
      sx={{
        height: '50px',
        backgroundColor: config.backgroundColor,
        color: config.color,
        borderRadius: '8px',
        fontFamily: 'Noto Sans, sans-serif',
        fontSize: '14px',
        fontWeight: 400,
        textTransform: 'none',
        boxShadow: 'none',
        border: config.border || 'none',
        '&:hover': {
          backgroundColor: config.hoverColor,
          boxShadow: 'none',
        },
        '&:disabled': {
          backgroundColor: '#F5F5F5',
          color: '#AEAEAE',
        },
      }}
    >
      <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
        {config.icon}
        <span>{config.label}</span>
      </Box>
    </Button>
  );
};

export default SocialLoginButton;
