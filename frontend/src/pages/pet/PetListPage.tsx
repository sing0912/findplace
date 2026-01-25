/**
 * @fileoverview 반려동물 목록 페이지
 */

import React, { useState } from 'react';
import { useMyPets, usePetMutations, usePet } from '../../hooks/usePets';
import PetCard from '../../components/pet/PetCard';
import PetForm from '../../components/pet/PetForm';
import { CreatePetRequest, UpdatePetRequest } from '../../types/pet';

/**
 * 반려동물 목록 페이지
 */
const PetListPage: React.FC = () => {
  const { pets, totalCount, aliveCount, deceasedCount, loading, error, refetch } = useMyPets();
  const { createPet, updatePet, deletePet, uploadImage, loading: mutationLoading } = usePetMutations();

  const [showForm, setShowForm] = useState(false);
  const [editingPetId, setEditingPetId] = useState<number | null>(null);

  const { pet: editingPet } = usePet(editingPetId);

  const handleCreate = async (data: CreatePetRequest | UpdatePetRequest) => {
    await createPet(data as CreatePetRequest);
    setShowForm(false);
    refetch();
  };

  const handleUpdate = async (data: CreatePetRequest | UpdatePetRequest) => {
    if (editingPetId) {
      await updatePet(editingPetId, data as UpdatePetRequest);
      setEditingPetId(null);
      refetch();
    }
  };

  const handleImageUpload = async (file: File) => {
    if (editingPetId) {
      await uploadImage(editingPetId, file);
    }
  };

  const handleEdit = (id: number) => {
    setEditingPetId(id);
  };

  const handleDelete = async (id: number) => {
    await deletePet(id);
    refetch();
  };

  const handleCancel = () => {
    setShowForm(false);
    setEditingPetId(null);
  };

  if (loading) {
    return <div className="pet-list-page__loading">로딩 중...</div>;
  }

  if (error) {
    return (
      <div className="pet-list-page__error">
        <p>{error}</p>
        <button onClick={refetch}>다시 시도</button>
      </div>
    );
  }

  return (
    <div className="pet-list-page">
      <header className="pet-list-page__header">
        <h1>내 반려동물</h1>
        <button
          onClick={() => setShowForm(true)}
          className="pet-list-page__add-btn"
          disabled={showForm || editingPetId !== null}
        >
          + 반려동물 등록
        </button>
      </header>

      {/* 통계 */}
      <div className="pet-list-page__stats">
        <span>전체 {totalCount}마리</span>
        <span>함께하는 {aliveCount}마리</span>
        <span>무지개다리 {deceasedCount}마리</span>
      </div>

      {/* 등록 폼 */}
      {showForm && (
        <div className="pet-list-page__form-container">
          <h2>반려동물 등록</h2>
          <PetForm
            onSubmit={handleCreate}
            onCancel={handleCancel}
            loading={mutationLoading}
          />
        </div>
      )}

      {/* 수정 폼 */}
      {editingPetId && editingPet && (
        <div className="pet-list-page__form-container">
          <h2>반려동물 정보 수정</h2>
          <PetForm
            pet={editingPet}
            onSubmit={handleUpdate}
            onCancel={handleCancel}
            onImageUpload={handleImageUpload}
            loading={mutationLoading}
          />
        </div>
      )}

      {/* 반려동물 목록 */}
      {pets.length === 0 ? (
        <div className="pet-list-page__empty">
          <p>등록된 반려동물이 없습니다.</p>
          <p>새로운 반려동물을 등록해주세요!</p>
        </div>
      ) : (
        <div className="pet-list-page__grid">
          {pets.map((pet) => (
            <PetCard
              key={pet.id}
              pet={pet}
              onEdit={handleEdit}
              onDelete={handleDelete}
            />
          ))}
        </div>
      )}
    </div>
  );
};

export default PetListPage;
