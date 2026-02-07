/**
 * @fileoverview 관리자 로그인 페이지
 *
 * 관리자 전용 로그인 화면입니다.
 * 이메일/비밀번호 로그인만 지원합니다 (소셜 로그인 없음).
 */

import React, { useState } from 'react';
import {
  Box,
  TextField,
  Button,
  Typography,
  Paper,
  CircularProgress,
} from '@mui/material';
import { useAdminAuth } from '../../hooks/useAdminAuth';

const AdminLoginPage: React.FC = () => {
  const { login, isLoginLoading } = useAdminAuth();
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    login({ email, password });
  };

  return (
    <Box
      sx={{
        display: 'flex',
        justifyContent: 'center',
        alignItems: 'center',
        minHeight: '100vh',
        backgroundColor: '#f5f5f5',
      }}
    >
      <Paper
        elevation={3}
        sx={{
          p: 4,
          width: '100%',
          maxWidth: 400,
        }}
      >
        <Typography variant="h5" align="center" gutterBottom sx={{ fontWeight: 'bold', mb: 3 }}>
          PetPro 관리자
        </Typography>

        <form onSubmit={handleSubmit}>
          <TextField
            fullWidth
            label="이메일"
            type="email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            margin="normal"
            required
            autoFocus
          />
          <TextField
            fullWidth
            label="비밀번호"
            type="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            margin="normal"
            required
          />
          <Button
            type="submit"
            fullWidth
            variant="contained"
            disabled={isLoginLoading}
            sx={{ mt: 3, mb: 2, height: 48 }}
          >
            {isLoginLoading ? <CircularProgress size={24} /> : '로그인'}
          </Button>
        </form>
      </Paper>
    </Box>
  );
};

export default AdminLoginPage;
