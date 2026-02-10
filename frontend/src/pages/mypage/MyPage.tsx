/**
 * @fileoverview 마이페이지 메인
 * @see docs/develop/user/frontend.md - 섹션 7.1
 */

import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { Box, Container, Typography, Divider, Dialog, DialogTitle, DialogContent, DialogActions, Button, Snackbar } from '@mui/material';
import {
  Person,
  Pets,
  CreditCard,
  Notifications,
  Star,
  PersonAdd,
  HelpOutline,
  QuestionAnswer,
  ManageAccounts,
  Lock,
  Description,
  Info,
} from '@mui/icons-material';
import { ProfileCard, MenuItem } from '../../components/mypage';
import { AuthButton } from '../../components/auth';
import { useAuthStore } from '../../stores/authStore';

interface UserProfile {
  id: number;
  email: string;
  nickname: string;
  profileImageUrl: string | null;
}

const MyPage: React.FC = () => {
  const navigate = useNavigate();
  const { logout } = useAuthStore();
  const [profile, setProfile] = useState<UserProfile | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [showLogoutDialog, setShowLogoutDialog] = useState(false);
  const [showWithdrawDialog, setShowWithdrawDialog] = useState(false);
  const [showSnackbar, setShowSnackbar] = useState(false);

  const handlePlaceholder = () => {
    setShowSnackbar(true);
  };

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
    } catch (err) {
      console.error('Profile fetch error:', err);
    } finally {
      setIsLoading(false);
    }
  };

  const handleLogout = async () => {
    try {
      const token = localStorage.getItem('accessToken');
      await fetch('/api/v1/auth/logout', {
        method: 'POST',
        headers: {
          Authorization: `Bearer ${token}`,
        },
      });
    } catch (err) {
      console.error(err);
    } finally {
      logout();
      navigate('/login');
    }
  };

  const handleWithdraw = async () => {
    try {
      const token = localStorage.getItem('accessToken');
      const response = await fetch('/api/v1/users/me', {
        method: 'DELETE',
        headers: {
          'Content-Type': 'application/json',
          Authorization: `Bearer ${token}`,
        },
        body: JSON.stringify({}), // OAuth 사용자는 빈 body 가능
      });

      if (!response.ok) {
        const errorData = await response.json().catch(() => ({}));
        throw new Error(errorData.error?.message || '회원탈퇴에 실패했습니다.');
      }

      logout();
      navigate('/login');
    } catch (err) {
      console.error(err);
      alert(err instanceof Error ? err.message : '회원탈퇴에 실패했습니다. 다시 시도해주세요.');
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
        }}
      >
        {/* 헤더 */}
        <Box sx={{ mb: 3 }}>
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
            마이페이지
          </Typography>
        </Box>

        {/* 프로필 카드 */}
        <Box sx={{ px: 2, mb: 3 }}>
          {profile && (
            <ProfileCard
              profileImageUrl={profile.profileImageUrl}
              nickname={profile.nickname}
              email={profile.email}
              onEditClick={() => navigate('/mypage/edit')}
            />
          )}
        </Box>

        {/* 내 정보 */}
        <Box sx={{ px: 2 }}>
          <Typography sx={{ px: 1, pt: 2, pb: 1, fontSize: '12px', color: '#AEAEAE', fontFamily: 'Noto Sans KR, sans-serif' }}>
            내 정보
          </Typography>
          <MenuItem
            label="내 프로필"
            icon={<Person fontSize="small" />}
            onClick={() => navigate('/mypage/edit')}
          />
          <MenuItem
            label="펫 관리"
            icon={<Pets fontSize="small" />}
            onClick={() => navigate('/mypage/pets')}
          />
        </Box>

        <Divider sx={{ my: 1 }} />

        {/* 서비스 */}
        <Box sx={{ px: 2 }}>
          <Typography sx={{ px: 1, pt: 2, pb: 1, fontSize: '12px', color: '#AEAEAE', fontFamily: 'Noto Sans KR, sans-serif' }}>
            서비스
          </Typography>
          <MenuItem
            label="결제 수단 관리"
            icon={<CreditCard fontSize="small" />}
            onClick={handlePlaceholder}
          />
          <MenuItem
            label="알림 설정"
            icon={<Notifications fontSize="small" />}
            onClick={handlePlaceholder}
          />
          <MenuItem
            label="회원등급 안내"
            icon={<Star fontSize="small" />}
            onClick={handlePlaceholder}
          />
          <MenuItem
            label="친구 초대"
            icon={<PersonAdd fontSize="small" />}
            onClick={handlePlaceholder}
          />
        </Box>

        <Divider sx={{ my: 1 }} />

        {/* 고객센터 */}
        <Box sx={{ px: 2 }}>
          <Typography sx={{ px: 1, pt: 2, pb: 1, fontSize: '12px', color: '#AEAEAE', fontFamily: 'Noto Sans KR, sans-serif' }}>
            고객센터
          </Typography>
          <MenuItem
            label="FAQ"
            icon={<HelpOutline fontSize="small" />}
            onClick={handlePlaceholder}
          />
          <MenuItem
            label="1:1 문의"
            icon={<QuestionAnswer fontSize="small" />}
            onClick={() => navigate('/mypage/inquiry')}
          />
        </Box>

        <Divider sx={{ my: 1 }} />

        {/* 설정 */}
        <Box sx={{ px: 2 }}>
          <Typography sx={{ px: 1, pt: 2, pb: 1, fontSize: '12px', color: '#AEAEAE', fontFamily: 'Noto Sans KR, sans-serif' }}>
            설정
          </Typography>
          <MenuItem
            label="계정 관리"
            icon={<ManageAccounts fontSize="small" />}
            onClick={handlePlaceholder}
          />
          <MenuItem
            label="비밀번호 변경"
            icon={<Lock fontSize="small" />}
            onClick={() => navigate('/mypage/password')}
          />
          <MenuItem
            label="약관/정책"
            icon={<Description fontSize="small" />}
            onClick={() => navigate('/mypage/settings/policies')}
          />
          <MenuItem
            label="앱 정보"
            icon={<Info fontSize="small" />}
            onClick={handlePlaceholder}
          />
        </Box>

        {/* 로그아웃/회원탈퇴 */}
        <Box sx={{ px: 2, mt: 4 }}>
          <AuthButton
            variant="secondary"
            fullWidth
            onClick={() => setShowLogoutDialog(true)}
          >
            로그아웃
          </AuthButton>
          <Box sx={{ mt: 2, textAlign: 'center' }}>
            <Typography
              component="button"
              onClick={() => setShowWithdrawDialog(true)}
              sx={{
                background: 'none',
                border: 'none',
                fontFamily: 'Noto Sans KR, sans-serif',
                fontSize: '12px',
                color: '#AEAEAE',
                textDecoration: 'underline',
                cursor: 'pointer',
                '&:hover': {
                  color: '#FF0000',
                },
              }}
            >
              회원탈퇴
            </Typography>
          </Box>
        </Box>

        {/* 준비 중 Snackbar */}
        <Snackbar
          open={showSnackbar}
          autoHideDuration={2000}
          onClose={() => setShowSnackbar(false)}
          message="준비 중입니다"
          anchorOrigin={{ vertical: 'bottom', horizontal: 'center' }}
        />
      </Box>

      {/* 로그아웃 확인 다이얼로그 */}
      <Dialog
        open={showLogoutDialog}
        onClose={() => setShowLogoutDialog(false)}
      >
        <DialogTitle sx={{ fontFamily: 'Noto Sans, sans-serif' }}>
          로그아웃
        </DialogTitle>
        <DialogContent>
          <Typography sx={{ fontFamily: 'Noto Sans KR, sans-serif' }}>
            로그아웃 하시겠습니까?
          </Typography>
        </DialogContent>
        <DialogActions>
          <Button
            onClick={() => setShowLogoutDialog(false)}
            sx={{ color: '#AEAEAE' }}
          >
            취소
          </Button>
          <Button
            onClick={handleLogout}
            sx={{ color: '#76BCA2' }}
          >
            확인
          </Button>
        </DialogActions>
      </Dialog>

      {/* 회원탈퇴 확인 다이얼로그 */}
      <Dialog
        open={showWithdrawDialog}
        onClose={() => setShowWithdrawDialog(false)}
      >
        <DialogTitle sx={{ fontFamily: 'Noto Sans, sans-serif', color: '#FF0000' }}>
          회원탈퇴
        </DialogTitle>
        <DialogContent>
          <Typography sx={{ fontFamily: 'Noto Sans KR, sans-serif' }}>
            정말 탈퇴하시겠습니까?
            <br />
            탈퇴 시 모든 데이터가 삭제되며 복구할 수 없습니다.
          </Typography>
        </DialogContent>
        <DialogActions>
          <Button
            onClick={() => setShowWithdrawDialog(false)}
            sx={{ color: '#AEAEAE' }}
          >
            취소
          </Button>
          <Button
            onClick={handleWithdraw}
            sx={{ color: '#FF0000' }}
          >
            탈퇴하기
          </Button>
        </DialogActions>
      </Dialog>
    </Container>
  );
};

export default MyPage;
