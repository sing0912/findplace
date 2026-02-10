/**
 * @fileoverview 반려동물 성향 체크리스트 페이지
 * @see docs/develop/pet/frontend.md - U-PET-004
 */

import React from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import {
  Box,
  Container,
  Typography,
  IconButton,
  CircularProgress,
} from '@mui/material';
import { ArrowBack } from '@mui/icons-material';
import { PetChecklistForm } from '../../components/pet';
import { usePetChecklist, usePetChecklistMutations } from '../../hooks/usePetChecklist';
import { CreatePetChecklistRequest, UpdatePetChecklistRequest } from '../../types/petChecklist';

const PetChecklistPage: React.FC = () => {
  const navigate = useNavigate();
  const { id } = useParams<{ id: string }>();
  const petId = id ? Number(id) : null;

  const { checklist, loading: checklistLoading, notFound, refetch } = usePetChecklist(petId);
  const {
    saveChecklist,
    loading: mutationLoading,
    error: mutationError,
  } = usePetChecklistMutations();

  const isEditMode = !!checklist && !notFound;

  const handleSubmit = async (
    data: CreatePetChecklistRequest | UpdatePetChecklistRequest
  ) => {
    if (!petId) return;

    try {
      await saveChecklist(petId, data as CreatePetChecklistRequest, isEditMode);
      navigate(`/mypage/pets/${petId}/edit`);
    } catch {
      // 에러는 훅에서 처리
    }
  };

  if (checklistLoading) {
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
          <IconButton onClick={() => navigate(`/mypage/pets/${petId}/edit`)} sx={{ mr: 1 }}>
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
            성향 체크리스트
          </Typography>
        </Box>

        <Box sx={{ px: 2 }}>
          <Typography
            sx={{
              fontFamily: 'Noto Sans KR, sans-serif',
              fontSize: '13px',
              color: '#AEAEAE',
              mb: 3,
            }}
          >
            반려동물의 성향을 기록하면 시터에게 자동으로 전달됩니다.
          </Typography>

          <PetChecklistForm
            checklist={checklist}
            onSubmit={handleSubmit}
            loading={mutationLoading}
            error={mutationError}
          />
        </Box>
      </Box>
    </Container>
  );
};

export default PetChecklistPage;
