/**
 * @fileoverview 반려동물 성향 체크리스트 커스텀 훅
 * @see docs/develop/pet/frontend.md
 */

import { useState, useEffect, useCallback } from 'react';
import { getChecklist, createChecklist, updateChecklist } from '../api/petChecklist';
import {
  PetChecklist,
  CreatePetChecklistRequest,
  UpdatePetChecklistRequest,
} from '../types/petChecklist';

interface UsePetChecklistResult {
  checklist: PetChecklist | null;
  loading: boolean;
  error: string | null;
  notFound: boolean;
  refetch: () => Promise<void>;
}

/**
 * 반려동물 성향 체크리스트 조회 훅
 */
export const usePetChecklist = (petId: number | null): UsePetChecklistResult => {
  const [checklist, setChecklist] = useState<PetChecklist | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [notFound, setNotFound] = useState(false);

  const fetchChecklist = useCallback(async () => {
    if (!petId) {
      setChecklist(null);
      return;
    }

    try {
      setLoading(true);
      setError(null);
      setNotFound(false);
      const response = await getChecklist(petId);
      setChecklist(response);
    } catch (err: any) {
      if (err?.response?.status === 404) {
        setNotFound(true);
        setChecklist(null);
      } else {
        setError('성향 체크리스트를 불러오는데 실패했습니다.');
      }
    } finally {
      setLoading(false);
    }
  }, [petId]);

  useEffect(() => {
    fetchChecklist();
  }, [fetchChecklist]);

  return { checklist, loading, error, notFound, refetch: fetchChecklist };
};

interface UsePetChecklistMutationsResult {
  createChecklist: (petId: number, request: CreatePetChecklistRequest) => Promise<PetChecklist>;
  updateChecklist: (petId: number, request: UpdatePetChecklistRequest) => Promise<PetChecklist>;
  loading: boolean;
  error: string | null;
}

/**
 * 성향 체크리스트 생성/수정 훅
 */
export const usePetChecklistMutations = (): UsePetChecklistMutationsResult => {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const handleCreate = useCallback(
    async (petId: number, request: CreatePetChecklistRequest): Promise<PetChecklist> => {
      setLoading(true);
      setError(null);
      try {
        return await createChecklist(petId, request);
      } catch (err) {
        setError('성향 체크리스트 저장에 실패했습니다.');
        throw err;
      } finally {
        setLoading(false);
      }
    },
    []
  );

  const handleUpdate = useCallback(
    async (petId: number, request: UpdatePetChecklistRequest): Promise<PetChecklist> => {
      setLoading(true);
      setError(null);
      try {
        return await updateChecklist(petId, request);
      } catch (err) {
        setError('성향 체크리스트 수정에 실패했습니다.');
        throw err;
      } finally {
        setLoading(false);
      }
    },
    []
  );

  return {
    createChecklist: handleCreate,
    updateChecklist: handleUpdate,
    loading,
    error,
  };
};
