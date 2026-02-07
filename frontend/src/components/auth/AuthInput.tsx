/**
 * @fileoverview 인증용 입력 컴포넌트
 * @see docs/develop/user/frontend.md - 디자인 시스템 2.3
 */

import React, { forwardRef, useState } from 'react';
import { TextField, InputAdornment, IconButton, Box, Typography } from '@mui/material';
import CheckCircleIcon from '@mui/icons-material/CheckCircle';
import VisibilityIcon from '@mui/icons-material/Visibility';
import VisibilityOffIcon from '@mui/icons-material/VisibilityOff';

interface AuthInputProps {
  label: string;
  name: string;
  type?: 'text' | 'email' | 'password' | 'tel';
  placeholder?: string;
  value?: string;
  onChange?: (e: React.ChangeEvent<HTMLInputElement>) => void;
  onBlur?: (e: React.FocusEvent<HTMLInputElement>) => void;
  error?: boolean;
  helperText?: string;
  required?: boolean;
  isValid?: boolean;
  disabled?: boolean;
  autoComplete?: string;
}

const AuthInput = forwardRef<HTMLInputElement, AuthInputProps>(
  (
    {
      label,
      name,
      type = 'text',
      placeholder,
      value,
      onChange,
      onBlur,
      error = false,
      helperText,
      required = false,
      isValid = false,
      disabled = false,
      autoComplete,
    },
    ref
  ) => {
    const [showPassword, setShowPassword] = useState(false);
    const isPasswordType = type === 'password';
    const inputType = isPasswordType && showPassword ? 'text' : type;

    return (
      <Box sx={{ mb: 2, width: '100%' }}>
        <Typography
          component="label"
          htmlFor={name}
          sx={{
            display: 'block',
            mb: 0.5,
            fontFamily: 'Noto Sans, sans-serif',
            fontSize: '14px',
            fontWeight: 400,
            color: '#000000',
          }}
        >
          {label} {required && <span style={{ color: '#000000' }}>*</span>}
        </Typography>

        <TextField
          inputRef={ref}
          id={name}
          name={name}
          type={inputType}
          placeholder={placeholder}
          value={value}
          onChange={onChange}
          onBlur={onBlur}
          error={error}
          helperText={helperText}
          disabled={disabled}
          autoComplete={autoComplete}
          fullWidth
          InputProps={{
            endAdornment: (
              <InputAdornment position="end">
                {isPasswordType && (
                  <IconButton
                    onClick={() => setShowPassword(!showPassword)}
                    edge="end"
                    sx={{ p: 0.5, mr: isValid ? 0.5 : 0 }}
                    tabIndex={-1}
                  >
                    {showPassword ? (
                      <VisibilityOffIcon sx={{ width: 24, height: 24, color: '#AEAEAE' }} />
                    ) : (
                      <VisibilityIcon sx={{ width: 24, height: 24, color: '#AEAEAE' }} />
                    )}
                  </IconButton>
                )}
                {isValid && (
                  <CheckCircleIcon sx={{ width: 24, height: 24, color: '#76BCA2' }} />
                )}
              </InputAdornment>
            ),
          }}
          sx={{
            '& .MuiOutlinedInput-root': {
              height: '50px',
              backgroundColor: disabled ? '#F5F5F5' : '#FFFFFF',
              borderRadius: '5px',
              fontFamily: 'Noto Sans KR, sans-serif',
              fontSize: '14px',
              '& fieldset': {
                borderColor: error ? '#FF0000' : '#AEAEAE',
                borderWidth: '1px',
              },
              '&:hover fieldset': {
                borderColor: error ? '#FF0000' : '#76BCA2',
              },
              '&.Mui-focused fieldset': {
                borderColor: error ? '#FF0000' : '#76BCA2',
                borderWidth: '1px',
              },
              '&.Mui-disabled fieldset': {
                borderColor: '#AEAEAE',
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
              marginTop: '4px',
              fontFamily: 'Noto Sans KR, sans-serif',
              fontSize: '12px',
              color: error ? '#FF0000' : '#404040',
            },
          }}
        />
      </Box>
    );
  }
);

AuthInput.displayName = 'AuthInput';

export default AuthInput;
