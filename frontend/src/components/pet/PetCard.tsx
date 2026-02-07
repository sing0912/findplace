/**
 * @fileoverview 반려동물 카드 컴포넌트 (MUI)
 * @see docs/develop/pet/frontend.md
 */

import React, { useState } from 'react';
import {
  Box,
  Card,
  CardContent,
  Avatar,
  Typography,
  IconButton,
  Chip,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Button,
} from '@mui/material';
import { Edit, Delete } from '@mui/icons-material';
import { PetSummary, GENDER_NAMES } from '../../types/pet';

interface PetCardProps {
  pet: PetSummary;
  onEdit?: (id: number) => void;
  onDelete?: (id: number) => void;
}

const SPECIES_ICONS: Record<string, string> = {
  DOG: '\uD83D\uDC15',
  CAT: '\uD83D\uDC08',
  BIRD: '\uD83D\uDC26',
  HAMSTER: '\uD83D\uDC39',
  RABBIT: '\uD83D\uDC30',
  FISH: '\uD83D\uDC1F',
  REPTILE: '\uD83E\uDD8E',
  ETC: '\uD83D\uDC3E',
};

const PetCard: React.FC<PetCardProps> = ({ pet, onEdit, onDelete }) => {
  const [showDeleteDialog, setShowDeleteDialog] = useState(false);
  const speciesIcon = SPECIES_ICONS[pet.species] || '\uD83D\uDC3E';

  const handleDelete = () => {
    setShowDeleteDialog(false);
    onDelete?.(pet.id);
  };

  return (
    <>
      <Card
        sx={{
          backgroundColor: '#F5FAF8',
          borderRadius: '12px',
          boxShadow: 'none',
          border: '1px solid #E8F0EC',
          opacity: pet.isDeceased ? 0.7 : 1,
        }}
      >
        <CardContent sx={{ display: 'flex', alignItems: 'center', p: 2, '&:last-child': { pb: 2 } }}>
          {/* 프로필 이미지 */}
          <Avatar
            src={pet.profileImageUrl || undefined}
            alt={pet.name}
            sx={{
              width: 56,
              height: 56,
              backgroundColor: '#76BCA2',
              fontSize: '28px',
            }}
          >
            {speciesIcon}
          </Avatar>

          {/* 정보 */}
          <Box sx={{ flex: 1, ml: 2 }}>
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 0.5 }}>
              <Typography
                sx={{
                  fontFamily: 'Noto Sans, sans-serif',
                  fontSize: '16px',
                  fontWeight: 700,
                  color: '#000000',
                }}
              >
                {pet.name}
              </Typography>
              {pet.isDeceased && (
                <Chip
                  label="\uD83C\uDF08 무지개다리"
                  size="small"
                  sx={{
                    backgroundColor: '#E8E0F0',
                    fontSize: '11px',
                    height: '22px',
                  }}
                />
              )}
            </Box>

            <Typography
              sx={{
                fontFamily: 'Noto Sans KR, sans-serif',
                fontSize: '13px',
                color: '#404040',
              }}
            >
              {pet.speciesName}
              {pet.breed && ` \u00B7 ${pet.breed}`}
              {pet.gender && ` \u00B7 ${GENDER_NAMES[pet.gender]}`}
            </Typography>

            {pet.age !== null && (
              <Typography
                sx={{
                  fontFamily: 'Noto Sans KR, sans-serif',
                  fontSize: '12px',
                  color: '#AEAEAE',
                  mt: 0.25,
                }}
              >
                {pet.age}살
              </Typography>
            )}
          </Box>

          {/* 액션 버튼 */}
          {!pet.isDeceased && (
            <Box sx={{ display: 'flex', gap: 0.5 }}>
              <IconButton
                size="small"
                onClick={() => onEdit?.(pet.id)}
                sx={{ color: '#AEAEAE', '&:hover': { color: '#76BCA2' } }}
              >
                <Edit fontSize="small" />
              </IconButton>
              <IconButton
                size="small"
                onClick={() => setShowDeleteDialog(true)}
                sx={{ color: '#AEAEAE', '&:hover': { color: '#FF0000' } }}
              >
                <Delete fontSize="small" />
              </IconButton>
            </Box>
          )}
        </CardContent>
      </Card>

      {/* 삭제 확인 다이얼로그 */}
      <Dialog open={showDeleteDialog} onClose={() => setShowDeleteDialog(false)}>
        <DialogTitle sx={{ fontFamily: 'Noto Sans, sans-serif' }}>
          반려동물 삭제
        </DialogTitle>
        <DialogContent>
          <Typography sx={{ fontFamily: 'Noto Sans KR, sans-serif' }}>
            {pet.name}을(를) 삭제하시겠습니까?
          </Typography>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setShowDeleteDialog(false)} sx={{ color: '#AEAEAE' }}>
            취소
          </Button>
          <Button onClick={handleDelete} sx={{ color: '#FF0000' }}>
            삭제
          </Button>
        </DialogActions>
      </Dialog>
    </>
  );
};

export default PetCard;
