/**
 * @fileoverview 약관 동의 체크박스 컴포넌트
 * @see docs/develop/user/frontend.md - 섹션 6.1
 */

import React from 'react';
import { Box, Checkbox, Typography, Link } from '@mui/material';

interface AgreementCheckboxProps {
  id: string;
  label: string;
  required?: boolean;
  checked: boolean;
  onChange: (checked: boolean) => void;
  showDetail?: boolean;
  onDetailClick?: () => void;
}

const AgreementCheckbox: React.FC<AgreementCheckboxProps> = ({
  id,
  label,
  required = false,
  checked,
  onChange,
  showDetail = false,
  onDetailClick,
}) => {
  return (
    <Box
      sx={{
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'space-between',
        py: 1,
      }}
    >
      <Box sx={{ display: 'flex', alignItems: 'center' }}>
        <Checkbox
          id={id}
          checked={checked}
          onChange={(e) => onChange(e.target.checked)}
          sx={{
            padding: 0,
            marginRight: 1,
            color: '#AEAEAE',
            '&.Mui-checked': {
              color: '#76BCA2',
            },
          }}
        />
        <Typography
          component="label"
          htmlFor={id}
          sx={{
            fontFamily: 'Noto Sans KR, sans-serif',
            fontSize: '14px',
            fontWeight: 400,
            color: '#404040',
            cursor: 'pointer',
            userSelect: 'none',
          }}
        >
          {required && (
            <Typography
              component="span"
              sx={{
                color: '#76BCA2',
                fontWeight: 500,
                marginRight: 0.5,
              }}
            >
              [필수]
            </Typography>
          )}
          {!required && label.includes('선택') === false && (
            <Typography
              component="span"
              sx={{
                color: '#AEAEAE',
                fontWeight: 500,
                marginRight: 0.5,
              }}
            >
              [선택]
            </Typography>
          )}
          {label}
        </Typography>
      </Box>
      {showDetail && onDetailClick && (
        <Link
          component="button"
          type="button"
          onClick={onDetailClick}
          sx={{
            fontFamily: 'Noto Sans KR, sans-serif',
            fontSize: '12px',
            color: '#AEAEAE',
            textDecoration: 'underline',
            cursor: 'pointer',
            '&:hover': {
              color: '#76BCA2',
            },
          }}
        >
          보기
        </Link>
      )}
    </Box>
  );
};

export default AgreementCheckbox;
