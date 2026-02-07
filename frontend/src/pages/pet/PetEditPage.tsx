/**
 * @fileoverview 반려동물 수정 페이지
 * @see docs/develop/pet/frontend.md - U-PET-003
 */

import React from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import {
  Box,
  Container,
  Typography,
  IconButton,
  Alert,
  CircularProgress,
  Button,
} from '@mui/material';
import { ArrowBack, Checklist } from '@mui/icons-material';
import { PetForm } from '../../components/pet';
import { usePet, usePetMutations } from '../../hooks/usePets';
import { CreatePetRequest, UpdatePetRequest } from '../../types/pet';

const PetEditPage: React.FC = () => {
  const navigate = useNavigate();
  const { id } = useParams<{ id: string }>();
  const petId = id ? Number(id) : null;
  const { pet, loading: petLoading, error: petError } = usePet(petId);
  const { updatePet, uploadImage, loading, error } = usePetMutations();

  const handleSubmit = async (data: CreatePetRequest | UpdatePetRequest) => {
    if (petId) {
      await updatePet(petId, data as UpdatePetRequest);
    }
  };

  const handleImageUpload = async (file: File) => {
    if (petId) {
      await uploadImage(petId, file);
    }
  };

  if (petLoading) {
    return (
      <Container maxWidth="sm">
        <Box sx={{ minHeight: '60vh', display: 'flex', justifyContent: 'center', alignItems: 'center' }}>
          <CircularProgress sx={{ color: '#76BCA2' }} />
        </Box>
      </Container>
    );
  }

  if (petError) {
    return (
      <Container maxWidth="sm">
        <Box sx={{ py: 4 }}>
          <Alert severity="error">{petError}</Alert>
        </Box>
      </Container>
    );
  }

  return (
    <Container maxWidth="sm">
      <Box sx={{ minHeight: '100vh', py: 4 }}>
        {/* 헤더 */}
        <Box sx={{ display: 'flex', alignItems: 'center', mb: 3 }}>
          <IconButton onClick={() => navigate('/mypage/pets')} sx={{ mr: 1 }}>
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
            반려동물 수정
          </Typography>
        </Box>

        {error && (
          <Alert severity="error" sx={{ mb: 2 }}>
            {error}
          </Alert>
        )}

        <Box sx={{ px: 2 }}>
          <PetForm
            pet={pet}
            onSubmit={handleSubmit}
            onCancel={() => navigate('/mypage/pets')}
            onImageUpload={handleImageUpload}
            onSuccess={() => navigate('/mypage/pets')}
            loading={loading}
          />

          {/* 성향 체크리스트 링크 */}
          {petId && (
            <Box sx={{ mt: 3, textAlign: 'center' }}>
              <Button
                startIcon={<Checklist />}
                onClick={() => navigate(`/mypage/pets/${petId}/checklist`)}
                sx={{
                  color: '#76BCA2',
                  fontFamily: 'Noto Sans KR, sans-serif',
                  fontSize: '14px',
                  textTransform: 'none',
                  '&:hover': {
                    backgroundColor: '#F5FAF8',
                  },
                }}
              >
                성향 체크리스트 작성
              </Button>
            </Box>
          )}
        </Box>
      </Box>
    </Container>
  );
};

export default PetEditPage;
