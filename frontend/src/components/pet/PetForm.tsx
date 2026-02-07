/**
 * @fileoverview 반려동물 등록/수정 폼 컴포넌트 (MUI)
 * @see docs/develop/pet/frontend.md
 */

import React, { useState, useEffect, useRef } from 'react';
import {
  Box,
  TextField,
  Select,
  MenuItem,
  FormControl,
  InputLabel,
  RadioGroup,
  FormControlLabel,
  Radio,
  Checkbox,
  Avatar,
  IconButton,
  Typography,
  Alert,
} from '@mui/material';
import { PhotoCamera } from '@mui/icons-material';
import {
  Species,
  Gender,
  SPECIES_NAMES,
  GENDER_NAMES,
  CreatePetRequest,
  UpdatePetRequest,
  Pet,
} from '../../types/pet';
import { AuthButton } from '../auth';

interface PetFormProps {
  pet?: Pet | null;
  onSubmit: (data: CreatePetRequest | UpdatePetRequest) => Promise<void>;
  onCancel: () => void;
  onImageUpload?: (file: File) => Promise<void>;
  onSuccess?: () => void;
  loading?: boolean;
}

const SPECIES_OPTIONS: Species[] = ['DOG', 'CAT', 'BIRD', 'HAMSTER', 'RABBIT', 'FISH', 'REPTILE', 'ETC'];
const GENDER_OPTIONS: Gender[] = ['MALE', 'FEMALE', 'UNKNOWN'];

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

const PetForm: React.FC<PetFormProps> = ({
  pet,
  onSubmit,
  onCancel,
  onImageUpload,
  onSuccess,
  loading = false,
}) => {
  const fileInputRef = useRef<HTMLInputElement>(null);
  const [formData, setFormData] = useState<CreatePetRequest>({
    name: '',
    species: 'DOG',
    breed: '',
    birthDate: '',
    gender: undefined,
    isNeutered: false,
    memo: '',
  });

  const [imageFile, setImageFile] = useState<File | null>(null);
  const [imagePreview, setImagePreview] = useState<string | null>(null);
  const [errors, setErrors] = useState<Record<string, string>>({});

  useEffect(() => {
    if (pet) {
      setFormData({
        name: pet.name,
        species: pet.species,
        breed: pet.breed || '',
        birthDate: pet.birthDate || '',
        gender: pet.gender || undefined,
        isNeutered: pet.isNeutered,
        memo: pet.memo || '',
      });
      if (pet.profileImageUrl) {
        setImagePreview(pet.profileImageUrl);
      }
    }
  }, [pet]);

  const handleChange = (field: string, value: any) => {
    setFormData((prev) => ({ ...prev, [field]: value }));
    if (errors[field]) {
      setErrors((prev) => ({ ...prev, [field]: '' }));
    }
  };

  const handleImageChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;

    const validTypes = ['image/jpeg', 'image/png', 'image/gif', 'image/webp'];
    if (!validTypes.includes(file.type)) {
      setErrors((prev) => ({ ...prev, image: '지원하지 않는 파일 형식입니다.' }));
      return;
    }
    if (file.size > 5 * 1024 * 1024) {
      setErrors((prev) => ({ ...prev, image: '파일 크기는 5MB 이하여야 합니다.' }));
      return;
    }

    setImageFile(file);
    setImagePreview(URL.createObjectURL(file));
    setErrors((prev) => ({ ...prev, image: '' }));
  };

  const validate = (): boolean => {
    const newErrors: Record<string, string> = {};

    if (!formData.name.trim()) {
      newErrors.name = '이름을 입력해주세요.';
    } else if (formData.name.length > 100) {
      newErrors.name = '이름은 100자 이하여야 합니다.';
    }

    if (!formData.species) {
      newErrors.species = '종류를 선택해주세요.';
    }

    if (formData.breed && formData.breed.length > 100) {
      newErrors.breed = '품종은 100자 이하여야 합니다.';
    }

    if (formData.memo && formData.memo.length > 1000) {
      newErrors.memo = '특이사항은 1000자 이하여야 합니다.';
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!validate()) return;

    try {
      await onSubmit(formData);
      if (imageFile && onImageUpload) {
        await onImageUpload(imageFile);
      }
      onSuccess?.();
    } catch {
      // 에러는 상위 컴포넌트에서 처리
    }
  };

  const speciesIcon = SPECIES_ICONS[formData.species] || '\uD83D\uDC3E';

  return (
    <Box component="form" onSubmit={handleSubmit}>
      {/* 프로필 이미지 */}
      <Box sx={{ display: 'flex', justifyContent: 'center', mb: 3 }}>
        <Box sx={{ position: 'relative' }}>
          <Avatar
            src={imagePreview || undefined}
            alt="프로필"
            sx={{
              width: 80,
              height: 80,
              backgroundColor: '#76BCA2',
              fontSize: '36px',
            }}
          >
            {speciesIcon}
          </Avatar>
          <IconButton
            size="small"
            onClick={() => fileInputRef.current?.click()}
            sx={{
              position: 'absolute',
              bottom: 0,
              right: -4,
              backgroundColor: '#FFFFFF',
              border: '1px solid #E0E0E0',
              width: 28,
              height: 28,
              '&:hover': { backgroundColor: '#F5F5F5' },
            }}
          >
            <PhotoCamera sx={{ fontSize: 16, color: '#404040' }} />
          </IconButton>
          <input
            ref={fileInputRef}
            type="file"
            accept="image/jpeg,image/png,image/gif,image/webp"
            onChange={handleImageChange}
            style={{ display: 'none' }}
          />
        </Box>
      </Box>
      {errors.image && (
        <Alert severity="error" sx={{ mb: 2 }}>
          {errors.image}
        </Alert>
      )}

      {/* 이름 */}
      <TextField
        fullWidth
        label="이름"
        value={formData.name}
        onChange={(e) => handleChange('name', e.target.value)}
        error={!!errors.name}
        helperText={errors.name}
        required
        inputProps={{ maxLength: 100 }}
        sx={{ mb: 2 }}
        size="small"
      />

      {/* 종류 */}
      <FormControl fullWidth size="small" sx={{ mb: 2 }}>
        <InputLabel>종류 *</InputLabel>
        <Select
          value={formData.species}
          label="종류 *"
          onChange={(e) => handleChange('species', e.target.value)}
        >
          {SPECIES_OPTIONS.map((species) => (
            <MenuItem key={species} value={species}>
              {SPECIES_NAMES[species]}
            </MenuItem>
          ))}
        </Select>
      </FormControl>

      {/* 품종 */}
      <TextField
        fullWidth
        label="품종"
        value={formData.breed}
        onChange={(e) => handleChange('breed', e.target.value)}
        error={!!errors.breed}
        helperText={errors.breed}
        placeholder="예: 말티즈"
        inputProps={{ maxLength: 100 }}
        sx={{ mb: 2 }}
        size="small"
      />

      {/* 생년월일 */}
      <TextField
        fullWidth
        label="생년월일"
        type="date"
        value={formData.birthDate}
        onChange={(e) => handleChange('birthDate', e.target.value)}
        InputLabelProps={{ shrink: true }}
        inputProps={{ max: new Date().toISOString().split('T')[0] }}
        sx={{ mb: 2 }}
        size="small"
      />

      {/* 성별 */}
      <Box sx={{ mb: 2 }}>
        <Typography
          sx={{
            fontFamily: 'Noto Sans KR, sans-serif',
            fontSize: '13px',
            color: '#404040',
            mb: 1,
          }}
        >
          성별
        </Typography>
        <RadioGroup
          row
          value={formData.gender || ''}
          onChange={(e) => handleChange('gender', e.target.value || undefined)}
        >
          {GENDER_OPTIONS.map((gender) => (
            <FormControlLabel
              key={gender}
              value={gender}
              control={<Radio size="small" sx={{ color: '#AEAEAE', '&.Mui-checked': { color: '#76BCA2' } }} />}
              label={
                <Typography sx={{ fontSize: '14px', fontFamily: 'Noto Sans KR, sans-serif' }}>
                  {GENDER_NAMES[gender]}
                </Typography>
              }
            />
          ))}
        </RadioGroup>
      </Box>

      {/* 중성화 */}
      <FormControlLabel
        control={
          <Checkbox
            checked={formData.isNeutered}
            onChange={(e) => handleChange('isNeutered', e.target.checked)}
            size="small"
            sx={{ color: '#AEAEAE', '&.Mui-checked': { color: '#76BCA2' } }}
          />
        }
        label={
          <Typography sx={{ fontSize: '14px', fontFamily: 'Noto Sans KR, sans-serif' }}>
            중성화 완료
          </Typography>
        }
        sx={{ mb: 2 }}
      />

      {/* 특이사항 */}
      <TextField
        fullWidth
        label="특이사항"
        value={formData.memo}
        onChange={(e) => handleChange('memo', e.target.value)}
        error={!!errors.memo}
        helperText={errors.memo}
        placeholder="특이사항을 입력해주세요"
        multiline
        rows={3}
        inputProps={{ maxLength: 1000 }}
        sx={{ mb: 3 }}
        size="small"
      />

      {/* 버튼 */}
      <Box sx={{ display: 'flex', gap: 1.5 }}>
        <AuthButton variant="secondary" fullWidth onClick={onCancel}>
          취소
        </AuthButton>
        <AuthButton variant="primary" fullWidth type="submit" loading={loading}>
          {pet ? '수정' : '등록'}
        </AuthButton>
      </Box>
    </Box>
  );
};

export default PetForm;
