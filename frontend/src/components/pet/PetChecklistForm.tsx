/**
 * @fileoverview 반려동물 성향 체크리스트 폼 컴포넌트 (MUI)
 * @see docs/develop/pet/frontend.md
 */

import React, { useState, useEffect } from 'react';
import {
  Box,
  Typography,
  Slider,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  TextField,
  Alert,
} from '@mui/material';
import {
  PetChecklist,
  CreatePetChecklistRequest,
  UpdatePetChecklistRequest,
  EATING_HABIT_OPTIONS,
  WALK_PREFERENCE_OPTIONS,
  CHECKLIST_SLIDER_LABELS,
} from '../../types/petChecklist';
import { AuthButton } from '../auth';

interface PetChecklistFormProps {
  checklist?: PetChecklist | null;
  onSubmit: (data: CreatePetChecklistRequest | UpdatePetChecklistRequest) => Promise<void>;
  loading?: boolean;
  error?: string | null;
}

const SLIDER_MARKS = [
  { value: 1, label: '1' },
  { value: 2, label: '2' },
  { value: 3, label: '3' },
  { value: 4, label: '4' },
  { value: 5, label: '5' },
];

const SLIDER_FIELDS = [
  'friendlyToStrangers',
  'friendlyToDogs',
  'friendlyToCats',
  'activityLevel',
  'barkingLevel',
  'separationAnxiety',
  'houseTraining',
  'commandTraining',
] as const;

type SliderField = typeof SLIDER_FIELDS[number];

interface FormData {
  friendlyToStrangers: number;
  friendlyToDogs: number;
  friendlyToCats: number;
  activityLevel: number;
  barkingLevel: number;
  separationAnxiety: number;
  houseTraining: number;
  commandTraining: number;
  eatingHabit: string;
  walkPreference: string;
  fearItems: string;
  additionalNotes: string;
}

const defaultFormData: FormData = {
  friendlyToStrangers: 3,
  friendlyToDogs: 3,
  friendlyToCats: 3,
  activityLevel: 3,
  barkingLevel: 3,
  separationAnxiety: 3,
  houseTraining: 3,
  commandTraining: 3,
  eatingHabit: '',
  walkPreference: '',
  fearItems: '',
  additionalNotes: '',
};

const PetChecklistForm: React.FC<PetChecklistFormProps> = ({
  checklist,
  onSubmit,
  loading = false,
  error,
}) => {
  const [formData, setFormData] = useState<FormData>(defaultFormData);

  useEffect(() => {
    if (checklist) {
      setFormData({
        friendlyToStrangers: checklist.friendlyToStrangers,
        friendlyToDogs: checklist.friendlyToDogs,
        friendlyToCats: checklist.friendlyToCats,
        activityLevel: checklist.activityLevel,
        barkingLevel: checklist.barkingLevel,
        separationAnxiety: checklist.separationAnxiety,
        houseTraining: checklist.houseTraining,
        commandTraining: checklist.commandTraining,
        eatingHabit: checklist.eatingHabit || '',
        walkPreference: checklist.walkPreference || '',
        fearItems: checklist.fearItems || '',
        additionalNotes: checklist.additionalNotes || '',
      });
    }
  }, [checklist]);

  const handleSliderChange = (field: SliderField, value: number | number[]) => {
    setFormData((prev) => ({ ...prev, [field]: value as number }));
  };

  const handleChange = (field: string, value: string) => {
    setFormData((prev) => ({ ...prev, [field]: value }));
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    const request: CreatePetChecklistRequest = {
      friendlyToStrangers: formData.friendlyToStrangers,
      friendlyToDogs: formData.friendlyToDogs,
      friendlyToCats: formData.friendlyToCats,
      activityLevel: formData.activityLevel,
      barkingLevel: formData.barkingLevel,
      separationAnxiety: formData.separationAnxiety,
      houseTraining: formData.houseTraining,
      commandTraining: formData.commandTraining,
      eatingHabit: formData.eatingHabit || undefined,
      walkPreference: formData.walkPreference || undefined,
      fearItems: formData.fearItems || undefined,
      additionalNotes: formData.additionalNotes || undefined,
    };

    await onSubmit(request);
  };

  return (
    <Box component="form" onSubmit={handleSubmit}>
      {error && (
        <Alert severity="error" sx={{ mb: 2 }}>
          {error}
        </Alert>
      )}

      {/* 수치형 항목 (Slider) */}
      <Typography
        sx={{
          fontFamily: 'Noto Sans, sans-serif',
          fontSize: '14px',
          fontWeight: 600,
          color: '#000000',
          mb: 2,
        }}
      >
        성향 평가 (1: 매우 낮음 ~ 5: 매우 높음)
      </Typography>

      {SLIDER_FIELDS.map((field) => (
        <Box key={field} sx={{ mb: 2.5, px: 1 }}>
          <Typography
            sx={{
              fontFamily: 'Noto Sans KR, sans-serif',
              fontSize: '13px',
              color: '#404040',
              mb: 1,
            }}
          >
            {CHECKLIST_SLIDER_LABELS[field]}
          </Typography>
          <Slider
            value={formData[field]}
            onChange={(_, value) => handleSliderChange(field, value)}
            min={1}
            max={5}
            step={1}
            marks={SLIDER_MARKS}
            valueLabelDisplay="auto"
            sx={{
              color: '#76BCA2',
              '& .MuiSlider-markLabel': {
                fontSize: '11px',
                color: '#AEAEAE',
              },
            }}
          />
        </Box>
      ))}

      {/* 식사 습관 */}
      <FormControl fullWidth size="small" sx={{ mb: 2 }}>
        <InputLabel>식사 습관</InputLabel>
        <Select
          value={formData.eatingHabit}
          label="식사 습관"
          onChange={(e) => handleChange('eatingHabit', e.target.value)}
        >
          <MenuItem value="">
            <em>선택 안함</em>
          </MenuItem>
          {EATING_HABIT_OPTIONS.map((option) => (
            <MenuItem key={option.value} value={option.value}>
              {option.label}
            </MenuItem>
          ))}
        </Select>
      </FormControl>

      {/* 산책 선호도 */}
      <FormControl fullWidth size="small" sx={{ mb: 2 }}>
        <InputLabel>산책 선호도</InputLabel>
        <Select
          value={formData.walkPreference}
          label="산책 선호도"
          onChange={(e) => handleChange('walkPreference', e.target.value)}
        >
          <MenuItem value="">
            <em>선택 안함</em>
          </MenuItem>
          {WALK_PREFERENCE_OPTIONS.map((option) => (
            <MenuItem key={option.value} value={option.value}>
              {option.label}
            </MenuItem>
          ))}
        </Select>
      </FormControl>

      {/* 무서워하는 것 */}
      <TextField
        fullWidth
        label="무서워하는 것"
        value={formData.fearItems}
        onChange={(e) => handleChange('fearItems', e.target.value)}
        placeholder="예: 천둥, 진공청소기"
        inputProps={{ maxLength: 500 }}
        sx={{ mb: 2 }}
        size="small"
      />

      {/* 추가 메모 */}
      <TextField
        fullWidth
        label="추가 메모"
        value={formData.additionalNotes}
        onChange={(e) => handleChange('additionalNotes', e.target.value)}
        placeholder="시터에게 전달하고 싶은 추가 정보"
        multiline
        rows={3}
        inputProps={{ maxLength: 1000 }}
        sx={{ mb: 3 }}
        size="small"
      />

      {/* 저장 버튼 */}
      <AuthButton variant="primary" fullWidth type="submit" loading={loading}>
        {checklist ? '수정' : '저장'}
      </AuthButton>
    </Box>
  );
};

export default PetChecklistForm;
