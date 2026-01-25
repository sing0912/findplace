/**
 * @fileoverview 반려동물 등록/수정 폼 컴포넌트
 */

import React, { useState, useEffect } from 'react';
import {
  Species,
  Gender,
  SPECIES_NAMES,
  GENDER_NAMES,
  CreatePetRequest,
  UpdatePetRequest,
  Pet,
} from '../../types/pet';

interface PetFormProps {
  /** 수정 모드일 경우 기존 반려동물 정보 */
  pet?: Pet | null;
  /** 제출 핸들러 */
  onSubmit: (data: CreatePetRequest | UpdatePetRequest) => Promise<void>;
  /** 취소 핸들러 */
  onCancel: () => void;
  /** 이미지 업로드 핸들러 */
  onImageUpload?: (file: File) => Promise<void>;
  /** 로딩 상태 */
  loading?: boolean;
}

const SPECIES_OPTIONS: Species[] = ['DOG', 'CAT', 'BIRD', 'HAMSTER', 'RABBIT', 'FISH', 'REPTILE', 'ETC'];
const GENDER_OPTIONS: Gender[] = ['MALE', 'FEMALE', 'UNKNOWN'];

/**
 * 반려동물 등록/수정 폼 컴포넌트
 */
const PetForm: React.FC<PetFormProps> = ({
  pet,
  onSubmit,
  onCancel,
  onImageUpload,
  loading = false,
}) => {
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

  // 수정 모드일 경우 기존 데이터 로드
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

  const handleChange = (
    e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement | HTMLTextAreaElement>
  ) => {
    const { name, value, type } = e.target;
    const checked = (e.target as HTMLInputElement).checked;

    setFormData((prev) => ({
      ...prev,
      [name]: type === 'checkbox' ? checked : value,
    }));

    // 에러 클리어
    if (errors[name]) {
      setErrors((prev) => ({ ...prev, [name]: '' }));
    }
  };

  const handleImageChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (file) {
      // 파일 유효성 검사
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
    }
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
      newErrors.memo = '메모는 1000자 이하여야 합니다.';
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!validate()) {
      return;
    }

    try {
      await onSubmit(formData);

      // 이미지 업로드 (수정 모드이고 새 이미지가 있는 경우)
      if (imageFile && onImageUpload) {
        await onImageUpload(imageFile);
      }
    } catch (err) {
      // 에러는 상위 컴포넌트에서 처리
    }
  };

  return (
    <form className="pet-form" onSubmit={handleSubmit}>
      {/* 프로필 이미지 */}
      <div className="pet-form__field">
        <label className="pet-form__label">프로필 이미지</label>
        <div className="pet-form__image-upload">
          {imagePreview && (
            <img src={imagePreview} alt="미리보기" className="pet-form__image-preview" />
          )}
          <input
            type="file"
            accept="image/jpeg,image/png,image/gif,image/webp"
            onChange={handleImageChange}
            className="pet-form__file-input"
          />
        </div>
        {errors.image && <span className="pet-form__error">{errors.image}</span>}
      </div>

      {/* 이름 */}
      <div className="pet-form__field">
        <label htmlFor="name" className="pet-form__label">
          이름 <span className="pet-form__required">*</span>
        </label>
        <input
          type="text"
          id="name"
          name="name"
          value={formData.name}
          onChange={handleChange}
          placeholder="반려동물 이름"
          className="pet-form__input"
          maxLength={100}
        />
        {errors.name && <span className="pet-form__error">{errors.name}</span>}
      </div>

      {/* 종류 */}
      <div className="pet-form__field">
        <label htmlFor="species" className="pet-form__label">
          종류 <span className="pet-form__required">*</span>
        </label>
        <select
          id="species"
          name="species"
          value={formData.species}
          onChange={handleChange}
          className="pet-form__select"
        >
          {SPECIES_OPTIONS.map((species) => (
            <option key={species} value={species}>
              {SPECIES_NAMES[species]}
            </option>
          ))}
        </select>
        {errors.species && <span className="pet-form__error">{errors.species}</span>}
      </div>

      {/* 품종 */}
      <div className="pet-form__field">
        <label htmlFor="breed" className="pet-form__label">
          품종
        </label>
        <input
          type="text"
          id="breed"
          name="breed"
          value={formData.breed}
          onChange={handleChange}
          placeholder="품종 (예: 말티즈)"
          className="pet-form__input"
          maxLength={100}
        />
        {errors.breed && <span className="pet-form__error">{errors.breed}</span>}
      </div>

      {/* 생년월일 */}
      <div className="pet-form__field">
        <label htmlFor="birthDate" className="pet-form__label">
          생년월일
        </label>
        <input
          type="date"
          id="birthDate"
          name="birthDate"
          value={formData.birthDate}
          onChange={handleChange}
          max={new Date().toISOString().split('T')[0]}
          className="pet-form__input"
        />
      </div>

      {/* 성별 */}
      <div className="pet-form__field">
        <label className="pet-form__label">성별</label>
        <div className="pet-form__radio-group">
          {GENDER_OPTIONS.map((gender) => (
            <label key={gender} className="pet-form__radio-label">
              <input
                type="radio"
                name="gender"
                value={gender}
                checked={formData.gender === gender}
                onChange={handleChange}
                className="pet-form__radio"
              />
              {GENDER_NAMES[gender]}
            </label>
          ))}
        </div>
      </div>

      {/* 중성화 */}
      <div className="pet-form__field">
        <label className="pet-form__checkbox-label">
          <input
            type="checkbox"
            name="isNeutered"
            checked={formData.isNeutered}
            onChange={handleChange}
            className="pet-form__checkbox"
          />
          중성화 완료
        </label>
      </div>

      {/* 메모 */}
      <div className="pet-form__field">
        <label htmlFor="memo" className="pet-form__label">
          특이사항
        </label>
        <textarea
          id="memo"
          name="memo"
          value={formData.memo}
          onChange={handleChange}
          placeholder="특이사항을 입력해주세요"
          className="pet-form__textarea"
          maxLength={1000}
          rows={3}
        />
        {errors.memo && <span className="pet-form__error">{errors.memo}</span>}
      </div>

      {/* 버튼 */}
      <div className="pet-form__actions">
        <button type="button" onClick={onCancel} className="pet-form__btn pet-form__btn--cancel">
          취소
        </button>
        <button type="submit" disabled={loading} className="pet-form__btn pet-form__btn--submit">
          {loading ? '처리 중...' : pet ? '수정' : '등록'}
        </button>
      </div>
    </form>
  );
};

export default PetForm;
