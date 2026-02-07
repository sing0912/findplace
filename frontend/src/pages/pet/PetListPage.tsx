/**
 * @fileoverview 반려동물 목록 페이지 (MUI)
 * @see docs/develop/pet/frontend.md - U-PET-001
 */

import React from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Box,
  Container,
  Typography,
  Chip,
  Fab,
  IconButton,
  CircularProgress,
  Alert,
} from '@mui/material';
import { Add, ArrowBack } from '@mui/icons-material';
import { useMyPets, usePetMutations } from '../../hooks/usePets';
import { PetCard } from '../../components/pet';

const PetListPage: React.FC = () => {
  const navigate = useNavigate();
  const { pets, totalCount, aliveCount, deceasedCount, loading, error, refetch } = useMyPets();
  const { deletePet } = usePetMutations();

  const handleEdit = (id: number) => {
    navigate(`/mypage/pets/${id}/edit`);
  };

  const handleDelete = async (id: number) => {
    try {
      await deletePet(id);
      refetch();
    } catch {
      // 에러는 훅에서 처리
    }
  };

  if (loading) {
    return (
      <Container maxWidth="sm">
        <Box sx={{ minHeight: '60vh', display: 'flex', justifyContent: 'center', alignItems: 'center' }}>
          <CircularProgress sx={{ color: '#76BCA2' }} />
        </Box>
      </Container>
    );
  }

  return (
    <Container maxWidth="sm">
      <Box sx={{ minHeight: '100vh', py: 4 }}>
        {/* 헤더 */}
        <Box sx={{ display: 'flex', alignItems: 'center', mb: 3 }}>
          <IconButton onClick={() => navigate('/mypage')} sx={{ mr: 1 }}>
            <ArrowBack />
          </IconButton>
          <Typography
            sx={{
              fontFamily: 'Noto Sans, sans-serif',
              fontWeight: 700,
              fontSize: '16px',
              color: '#000000',
              flex: 1,
              textAlign: 'center',
              mr: 5,
            }}
          >
            내 반려동물
          </Typography>
        </Box>

        {error && (
          <Alert severity="error" sx={{ mb: 2 }} action={
            <Typography
              component="button"
              onClick={refetch}
              sx={{ background: 'none', border: 'none', color: '#76BCA2', cursor: 'pointer', fontSize: '14px' }}
            >
              다시 시도
            </Typography>
          }>
            {error}
          </Alert>
        )}

        {/* 통계 */}
        <Box sx={{ display: 'flex', gap: 1, mb: 3, px: 2 }}>
          <Chip
            label={`전체 ${totalCount}마리`}
            size="small"
            sx={{ backgroundColor: '#F5FAF8', fontSize: '12px' }}
          />
          <Chip
            label={`함께하는 ${aliveCount}마리`}
            size="small"
            sx={{ backgroundColor: '#E8F5E9', fontSize: '12px' }}
          />
          {deceasedCount > 0 && (
            <Chip
              label={`무지개다리 ${deceasedCount}마리`}
              size="small"
              sx={{ backgroundColor: '#E8E0F0', fontSize: '12px' }}
            />
          )}
        </Box>

        {/* 반려동물 목록 */}
        <Box sx={{ px: 2 }}>
          {pets.length === 0 ? (
            <Box
              sx={{
                textAlign: 'center',
                py: 8,
              }}
            >
              <Typography sx={{ fontSize: '48px', mb: 2 }}>
                {'\uD83D\uDC3E'}
              </Typography>
              <Typography
                sx={{
                  fontFamily: 'Noto Sans KR, sans-serif',
                  fontSize: '15px',
                  color: '#AEAEAE',
                  mb: 1,
                }}
              >
                등록된 반려동물이 없습니다.
              </Typography>
              <Typography
                sx={{
                  fontFamily: 'Noto Sans KR, sans-serif',
                  fontSize: '13px',
                  color: '#AEAEAE',
                  mb: 3,
                }}
              >
                새로운 반려동물을 등록해주세요!
              </Typography>
              <Fab
                variant="extended"
                onClick={() => navigate('/mypage/pets/register')}
                sx={{
                  backgroundColor: '#76BCA2',
                  color: '#FFFFFF',
                  boxShadow: 'none',
                  '&:hover': { backgroundColor: '#5FA88E' },
                }}
              >
                <Add sx={{ mr: 0.5 }} />
                반려동물 등록
              </Fab>
            </Box>
          ) : (
            <Box sx={{ display: 'flex', flexDirection: 'column', gap: 1.5 }}>
              {pets.map((pet) => (
                <PetCard
                  key={pet.id}
                  pet={pet}
                  onEdit={handleEdit}
                  onDelete={handleDelete}
                />
              ))}
            </Box>
          )}
        </Box>

        {/* 등록 FAB (목록 있을 때) */}
        {pets.length > 0 && totalCount < 10 && (
          <Fab
            onClick={() => navigate('/mypage/pets/register')}
            sx={{
              position: 'fixed',
              bottom: 80,
              right: 24,
              backgroundColor: '#76BCA2',
              color: '#FFFFFF',
              '&:hover': { backgroundColor: '#5FA88E' },
            }}
          >
            <Add />
          </Fab>
        )}
      </Box>
    </Container>
  );
};

export default PetListPage;
