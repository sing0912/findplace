/**
 * @fileoverview 반려동물 카드 컴포넌트
 */

import React from 'react';
import { PetSummary, SPECIES_NAMES, GENDER_NAMES } from '../../types/pet';

interface PetCardProps {
  pet: PetSummary;
  onEdit?: (id: number) => void;
  onDelete?: (id: number) => void;
  onViewMemorial?: (id: number) => void;
}

const SPECIES_ICONS: Record<string, string> = {
  DOG: '🐕',
  CAT: '🐈',
  BIRD: '🐦',
  HAMSTER: '🐹',
  RABBIT: '🐰',
  FISH: '🐟',
  REPTILE: '🦎',
  ETC: '🐾',
};

/**
 * 반려동물 카드 컴포넌트
 */
const PetCard: React.FC<PetCardProps> = ({ pet, onEdit, onDelete, onViewMemorial }) => {
  const speciesIcon = SPECIES_ICONS[pet.species] || '🐾';

  const handleEdit = () => {
    if (onEdit) {
      onEdit(pet.id);
    }
  };

  const handleDelete = () => {
    if (onDelete && window.confirm(`${pet.name}를 삭제하시겠습니까?`)) {
      onDelete(pet.id);
    }
  };

  const handleViewMemorial = () => {
    if (onViewMemorial) {
      onViewMemorial(pet.id);
    }
  };

  return (
    <div className={`pet-card ${pet.isDeceased ? 'pet-card--deceased' : ''}`}>
      {/* 프로필 이미지 */}
      <div className="pet-card__image">
        {pet.profileImageUrl ? (
          <img src={pet.profileImageUrl} alt={pet.name} />
        ) : (
          <span className="pet-card__icon">{speciesIcon}</span>
        )}
        {pet.isDeceased && <span className="pet-card__rainbow">🌈</span>}
      </div>

      {/* 정보 */}
      <div className="pet-card__info">
        <h3 className="pet-card__name">
          {pet.name}
          {pet.isDeceased && <span className="pet-card__memorial-badge">(추모 중)</span>}
        </h3>

        <p className="pet-card__details">
          {pet.speciesName}
          {pet.breed && ` · ${pet.breed}`}
          {pet.gender && ` · ${GENDER_NAMES[pet.gender]}`}
        </p>

        {pet.isDeceased && pet.deceasedAt ? (
          <p className="pet-card__dates">
            {pet.age !== null && `${pet.age}세`}
            {pet.deceasedAt && ` ~ ${pet.deceasedAt}`}
          </p>
        ) : (
          pet.age !== null && <p className="pet-card__age">{pet.age}세</p>
        )}
      </div>

      {/* 액션 버튼 */}
      <div className="pet-card__actions">
        {pet.isDeceased ? (
          <button type="button" onClick={handleViewMemorial} className="pet-card__btn pet-card__btn--memorial">
            추모관 보기
          </button>
        ) : (
          <>
            <button type="button" onClick={handleEdit} className="pet-card__btn pet-card__btn--edit">
              수정
            </button>
            <button type="button" onClick={handleDelete} className="pet-card__btn pet-card__btn--delete">
              삭제
            </button>
          </>
        )}
      </div>
    </div>
  );
};

export default PetCard;
