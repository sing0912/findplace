/**
 * @fileoverview 회원정보 수정 페이지
 * @see docs/develop/user/frontend.md - 섹션 8.1
 */

import React, { useState, useEffect, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import { Box, Container, Typography, Avatar, IconButton, Alert, Link } from '@mui/material';
import CameraAltIcon from '@mui/icons-material/CameraAlt';
import { AuthInput, AuthButton } from '../../components/auth';
import { useAuthStore } from '../../stores/authStore';

interface UserProfile {
  id: number;
  email: string;
  nickname: string;
  phone: string;
  profileImageUrl: string | null;
}

const EditProfilePage: React.FC = () => {
  const navigate = useNavigate();
  const { logout } = useAuthStore();
  const fileInputRef = useRef<HTMLInputElement>(null);

  const [profile, setProfile] = useState<UserProfile | null>(null);
  const [nickname, setNickname] = useState('');
  const [nicknameError, setNicknameError] = useState('');
  const [nicknameValid, setNicknameValid] = useState(false);
  const [previewImage, setPreviewImage] = useState<string | null>(null);
  const [selectedFile, setSelectedFile] = useState<File | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [isSaving, setIsSaving] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  useEffect(() => {
    fetchProfile();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const fetchProfile = async () => {
    try {
      const token = localStorage.getItem('accessToken');
      if (!token) {
        navigate('/login');
        return;
      }

      const response = await fetch('/api/v1/users/me', {
        headers: {
          Authorization: `Bearer ${token}`,
        },
      });

      if (!response.ok) {
        if (response.status === 401 || response.status === 403) {
          console.error('Authentication failed, status:', response.status);
          logout();
          navigate('/login');
          return;
        }
        throw new Error('프로필 조회에 실패했습니다.');
      }

      const result = await response.json();
      const data = result.data || result;
      setProfile(data);
      setNickname(data.nickname || '');
      setPreviewImage(data.profileImageUrl);
      setNicknameValid(true);
    } catch (err) {
      console.error('Profile fetch error:', err);
      navigate('/mypage');
    } finally {
      setIsLoading(false);
    }
  };

  const validateNickname = (value: string): string => {
    if (!value) return '';
    if (value.length < 2 || value.length > 20) {
      return '닉네임은 2-20자여야 합니다.';
    }
    return '';
  };

  const checkNicknameDuplicate = async (value: string): Promise<boolean> => {
    if (value === profile?.nickname) return true;
    try {
      const response = await fetch(`/api/v1/auth/check-nickname?nickname=${encodeURIComponent(value)}`);
      const data = await response.json();
      return data.available;
    } catch {
      return false;
    }
  };

  const handleNicknameBlur = async () => {
    const validationError = validateNickname(nickname);
    if (validationError) {
      setNicknameError(validationError);
      setNicknameValid(false);
      return;
    }

    if (nickname !== profile?.nickname) {
      const available = await checkNicknameDuplicate(nickname);
      if (!available) {
        setNicknameError('이미 사용 중인 닉네임입니다.');
        setNicknameValid(false);
        return;
      }
    }

    setNicknameError('');
    setNicknameValid(true);
  };

  const handleImageClick = () => {
    fileInputRef.current?.click();
  };

  const handleImageChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;

    // 파일 크기 검사 (5MB)
    if (file.size > 5 * 1024 * 1024) {
      setError('이미지 크기는 5MB 이하여야 합니다.');
      return;
    }

    // 파일 타입 검사
    if (!file.type.startsWith('image/')) {
      setError('이미지 파일만 업로드 가능합니다.');
      return;
    }

    setSelectedFile(file);
    const reader = new FileReader();
    reader.onload = () => {
      setPreviewImage(reader.result as string);
    };
    reader.readAsDataURL(file);
    setError('');
  };

  const uploadProfileImage = async (): Promise<string | null> => {
    if (!selectedFile) return null;

    const token = localStorage.getItem('accessToken');
    const formData = new FormData();
    formData.append('file', selectedFile);

    const response = await fetch('/api/v1/users/me/profile-image', {
      method: 'POST',
      headers: {
        Authorization: `Bearer ${token}`,
      },
      body: formData,
    });

    if (!response.ok) {
      throw new Error('이미지 업로드에 실패했습니다.');
    }

    const result = await response.json();
    const data = result.data || result;
    return data.profileImageUrl;
  };

  const handleSave = async () => {
    if (!nicknameValid) return;

    setIsSaving(true);
    setError('');
    setSuccess('');

    try {
      const token = localStorage.getItem('accessToken');

      // 이미지가 선택된 경우 먼저 업로드 (서버에서 프로필 이미지 URL 자동 업데이트)
      if (selectedFile) {
        await uploadProfileImage();
      }

      // 닉네임 업데이트
      const response = await fetch('/api/v1/users/me', {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
          Authorization: `Bearer ${token}`,
        },
        body: JSON.stringify({ nickname }),
      });

      if (!response.ok) {
        throw new Error('프로필 수정에 실패했습니다.');
      }

      setSuccess('프로필이 수정되었습니다.');
      setSelectedFile(null);

      // 프로필 새로고침
      await fetchProfile();
    } catch (err) {
      setError(err instanceof Error ? err.message : '알 수 없는 오류가 발생했습니다.');
    } finally {
      setIsSaving(false);
    }
  };

  if (isLoading) {
    return (
      <Container maxWidth="sm">
        <Box
          sx={{
            minHeight: '100vh',
            display: 'flex',
            justifyContent: 'center',
            alignItems: 'center',
          }}
        >
          <Typography>로딩 중...</Typography>
        </Box>
      </Container>
    );
  }

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
            회원정보 수정
          </Typography>
        </Box>

        {/* 프로필 이미지 */}
        <Box
          sx={{
            display: 'flex',
            justifyContent: 'center',
            mb: 4,
          }}
        >
          <Box sx={{ position: 'relative' }}>
            <Avatar
              src={previewImage || undefined}
              sx={{
                width: 100,
                height: 100,
                backgroundColor: '#76BCA2',
                fontSize: '36px',
                fontWeight: 700,
              }}
            >
              {nickname?.charAt(0).toUpperCase()}
            </Avatar>
            <IconButton
              onClick={handleImageClick}
              sx={{
                position: 'absolute',
                bottom: 0,
                right: 0,
                backgroundColor: '#FFFFFF',
                border: '1px solid #AEAEAE',
                width: 32,
                height: 32,
                '&:hover': {
                  backgroundColor: '#F5F5F5',
                },
              }}
            >
              <CameraAltIcon sx={{ fontSize: 18, color: '#404040' }} />
            </IconButton>
            <input
              ref={fileInputRef}
              type="file"
              accept="image/*"
              onChange={handleImageChange}
              style={{ display: 'none' }}
            />
          </Box>
        </Box>

        {/* 폼 */}
        <Box sx={{ px: 2 }}>
          {error && (
            <Alert severity="error" sx={{ mb: 2 }}>
              {error}
            </Alert>
          )}
          {success && (
            <Alert severity="success" sx={{ mb: 2 }}>
              {success}
            </Alert>
          )}

          <AuthInput
            label="이메일"
            name="email"
            type="email"
            value={profile?.email || ''}
            disabled
          />

          <AuthInput
            label="닉네임"
            name="nickname"
            type="text"
            placeholder="2-20자 사이로 입력해주세요"
            value={nickname}
            onChange={(e) => setNickname(e.target.value)}
            onBlur={handleNicknameBlur}
            error={!!nicknameError}
            helperText={nicknameError}
            isValid={nicknameValid && nickname !== profile?.nickname}
            required
          />

          <AuthInput
            label="휴대폰 번호"
            name="phone"
            type="tel"
            value={profile?.phone?.replace(/(\d{3})(\d{4})(\d{4})/, '$1-$2-$3') || ''}
            disabled
          />

          {/* 비밀번호 변경 링크 */}
          <Box sx={{ mt: 1, mb: 3 }}>
            <Link
              component="button"
              onClick={() => navigate('/mypage/password')}
              sx={{
                fontFamily: 'Noto Sans KR, sans-serif',
                fontSize: '14px',
                color: '#76BCA2',
                textDecoration: 'underline',
                cursor: 'pointer',
              }}
            >
              비밀번호 변경
            </Link>
          </Box>
        </Box>

        {/* 저장 버튼 */}
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
            onClick={handleSave}
            disabled={!nicknameValid}
            loading={isSaving}
            fullWidth
          >
            저장
          </AuthButton>
        </Box>
      </Box>
    </Container>
  );
};

export default EditProfilePage;
