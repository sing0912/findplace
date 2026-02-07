/**
 * @fileoverview 반려동물 등록 페이지
 * @see docs/develop/pet/frontend.md - U-PET-002
 */

import React, { useEffect, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import { Box, Container, Typography, IconButton, Alert } from '@mui/material';
import { ArrowBack } from '@mui/icons-material';
import { PetForm } from '../../components/pet';
import { useMyPets, usePetMutations } from '../../hooks/usePets';
import { CreatePetRequest, UpdatePetRequest } from '../../types/pet';

const PetRegisterPage: React.FC = () => {
  const navigate = useNavigate();
  const { totalCount, loading: listLoading } = useMyPets();
  const { createPet, uploadImage, loading, error } = usePetMutations();
  const createdPetIdRef = useRef<number | null>(null);

  useEffect(() => {
    if (!listLoading && totalCount >= 10) {
      alert('반려동물은 최대 10마리까지 등록 가능합니다.');
      navigate('/mypage/pets');
    }
  }, [listLoading, totalCount, navigate]);

  const handleSubmit = async (data: CreatePetRequest | UpdatePetRequest) => {
    const pet = await createPet(data as CreatePetRequest);
    createdPetIdRef.current = pet.id;
  };

  const handleImageUpload = async (file: File) => {
    if (createdPetIdRef.current) {
      await uploadImage(createdPetIdRef.current, file);
    }
  };

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
            반려동물 등록
          </Typography>
        </Box>

        {error && (
          <Alert severity="error" sx={{ mb: 2 }}>
            {error}
          </Alert>
        )}

        <Box sx={{ px: 2 }}>
          <PetForm
            onSubmit={handleSubmit}
            onCancel={() => navigate('/mypage/pets')}
            onImageUpload={handleImageUpload}
            onSuccess={() => navigate('/mypage/pets')}
            loading={loading}
          />
        </Box>
      </Box>
    </Container>
  );
};

export default PetRegisterPage;
