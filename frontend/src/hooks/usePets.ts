/**
 * @fileoverview 반려동물 관련 커스텀 훅
 */

import { useState, useEffect, useCallback } from 'react';
import {
  getMyPets,
  getPet,
  createPet,
  updatePet,
  deletePet,
  markAsDeceased,
  uploadPetImage,
} from '../api/pet';
import {
  Pet,
  PetSummary,
  PetListResponse,
  CreatePetRequest,
  UpdatePetRequest,
  DeceasedRequest,
} from '../types/pet';

interface UseMyPetsResult {
  pets: PetSummary[];
  totalCount: number;
  aliveCount: number;
  deceasedCount: number;
  loading: boolean;
  error: string | null;
  refetch: () => Promise<void>;
}

/**
 * 내 반려동물 목록 조회 훅
 */
export const useMyPets = (): UseMyPetsResult => {
  const [data, setData] = useState<PetListResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const fetchPets = useCallback(async () => {
    try {
      setLoading(true);
      setError(null);
      const response = await getMyPets();
      setData(response);
    } catch (err) {
      setError('반려동물 목록을 불러오는데 실패했습니다.');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchPets();
  }, [fetchPets]);

  return {
    pets: data?.content ?? [],
    totalCount: data?.totalCount ?? 0,
    aliveCount: data?.aliveCount ?? 0,
    deceasedCount: data?.deceasedCount ?? 0,
    loading,
    error,
    refetch: fetchPets,
  };
};

interface UsePetResult {
  pet: Pet | null;
  loading: boolean;
  error: string | null;
  refetch: () => Promise<void>;
}

/**
 * 반려동물 상세 조회 훅
 */
export const usePet = (id: number | null): UsePetResult => {
  const [pet, setPet] = useState<Pet | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const fetchPet = useCallback(async () => {
    if (!id) {
      setPet(null);
      return;
    }

    try {
      setLoading(true);
      setError(null);
      const response = await getPet(id);
      setPet(response);
    } catch (err) {
      setError('반려동물 정보를 불러오는데 실패했습니다.');
    } finally {
      setLoading(false);
    }
  }, [id]);

  useEffect(() => {
    fetchPet();
  }, [fetchPet]);

  return { pet, loading, error, refetch: fetchPet };
};

interface UsePetMutationsResult {
  createPet: (request: CreatePetRequest) => Promise<Pet>;
  updatePet: (id: number, request: UpdatePetRequest) => Promise<Pet>;
  deletePet: (id: number) => Promise<void>;
  markAsDeceased: (id: number, request: DeceasedRequest) => Promise<Pet>;
  uploadImage: (id: number, file: File) => Promise<Pet>;
  loading: boolean;
  error: string | null;
}

/**
 * 반려동물 CRUD 작업 훅
 */
export const usePetMutations = (): UsePetMutationsResult => {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const handleCreatePet = useCallback(async (request: CreatePetRequest): Promise<Pet> => {
    setLoading(true);
    setError(null);
    try {
      return await createPet(request);
    } catch (err) {
      setError('반려동물 등록에 실패했습니다.');
      throw err;
    } finally {
      setLoading(false);
    }
  }, []);

  const handleUpdatePet = useCallback(
    async (id: number, request: UpdatePetRequest): Promise<Pet> => {
      setLoading(true);
      setError(null);
      try {
        return await updatePet(id, request);
      } catch (err) {
        setError('반려동물 정보 수정에 실패했습니다.');
        throw err;
      } finally {
        setLoading(false);
      }
    },
    []
  );

  const handleDeletePet = useCallback(async (id: number): Promise<void> => {
    setLoading(true);
    setError(null);
    try {
      await deletePet(id);
    } catch (err) {
      setError('반려동물 삭제에 실패했습니다.');
      throw err;
    } finally {
      setLoading(false);
    }
  }, []);

  const handleMarkAsDeceased = useCallback(
    async (id: number, request: DeceasedRequest): Promise<Pet> => {
      setLoading(true);
      setError(null);
      try {
        return await markAsDeceased(id, request);
      } catch (err) {
        setError('사망 처리에 실패했습니다.');
        throw err;
      } finally {
        setLoading(false);
      }
    },
    []
  );

  const handleUploadImage = useCallback(async (id: number, file: File): Promise<Pet> => {
    setLoading(true);
    setError(null);
    try {
      return await uploadPetImage(id, file);
    } catch (err) {
      setError('이미지 업로드에 실패했습니다.');
      throw err;
    } finally {
      setLoading(false);
    }
  }, []);

  return {
    createPet: handleCreatePet,
    updatePet: handleUpdatePet,
    deletePet: handleDeletePet,
    markAsDeceased: handleMarkAsDeceased,
    uploadImage: handleUploadImage,
    loading,
    error,
  };
};
