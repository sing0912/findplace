/**
 * @fileoverview 문의 게시판 관련 커스텀 훅
 * @see docs/develop/user/frontend.md - 섹션 10
 */

import { useState, useCallback } from 'react';
import { inquiryApi, Inquiry, CreateInquiryRequest, UpdateInquiryRequest } from '../api/inquiry';

interface UseInquiryReturn {
  inquiries: Inquiry[];
  currentInquiry: Inquiry | null;
  isLoading: boolean;
  error: string | null;
  hasMore: boolean;
  fetchInquiries: (page?: number) => Promise<void>;
  fetchInquiry: (id: number) => Promise<void>;
  createInquiry: (data: CreateInquiryRequest) => Promise<Inquiry>;
  updateInquiry: (id: number, data: UpdateInquiryRequest) => Promise<Inquiry>;
  deleteInquiry: (id: number) => Promise<void>;
  clearError: () => void;
}

export const useInquiry = (): UseInquiryReturn => {
  const [inquiries, setInquiries] = useState<Inquiry[]>([]);
  const [currentInquiry, setCurrentInquiry] = useState<Inquiry | null>(null);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [hasMore, setHasMore] = useState(true);

  const fetchInquiries = useCallback(async (page = 0) => {
    setIsLoading(true);
    setError(null);

    try {
      const data = await inquiryApi.getList(page);

      if (page === 0) {
        setInquiries(data.content);
      } else {
        setInquiries((prev) => [...prev, ...data.content]);
      }

      setHasMore(data.number < data.totalPages - 1);
    } catch (err) {
      setError(err instanceof Error ? err.message : '알 수 없는 오류가 발생했습니다.');
    } finally {
      setIsLoading(false);
    }
  }, []);

  const fetchInquiry = useCallback(async (id: number) => {
    setIsLoading(true);
    setError(null);

    try {
      const data = await inquiryApi.getDetail(id);
      setCurrentInquiry(data);
    } catch (err) {
      setError(err instanceof Error ? err.message : '알 수 없는 오류가 발생했습니다.');
    } finally {
      setIsLoading(false);
    }
  }, []);

  const createInquiry = useCallback(async (data: CreateInquiryRequest): Promise<Inquiry> => {
    setIsLoading(true);
    setError(null);

    try {
      const inquiry = await inquiryApi.create(data);
      return inquiry;
    } catch (err) {
      const message = err instanceof Error ? err.message : '알 수 없는 오류가 발생했습니다.';
      setError(message);
      throw new Error(message);
    } finally {
      setIsLoading(false);
    }
  }, []);

  const updateInquiry = useCallback(async (id: number, data: UpdateInquiryRequest): Promise<Inquiry> => {
    setIsLoading(true);
    setError(null);

    try {
      const inquiry = await inquiryApi.update(id, data);
      setCurrentInquiry(inquiry);
      return inquiry;
    } catch (err) {
      const message = err instanceof Error ? err.message : '알 수 없는 오류가 발생했습니다.';
      setError(message);
      throw new Error(message);
    } finally {
      setIsLoading(false);
    }
  }, []);

  const deleteInquiry = useCallback(async (id: number): Promise<void> => {
    setIsLoading(true);
    setError(null);

    try {
      await inquiryApi.delete(id);
      setInquiries((prev) => prev.filter((i) => i.id !== id));
      setCurrentInquiry(null);
    } catch (err) {
      const message = err instanceof Error ? err.message : '알 수 없는 오류가 발생했습니다.';
      setError(message);
      throw new Error(message);
    } finally {
      setIsLoading(false);
    }
  }, []);

  const clearError = useCallback(() => {
    setError(null);
  }, []);

  return {
    inquiries,
    currentInquiry,
    isLoading,
    error,
    hasMore,
    fetchInquiries,
    fetchInquiry,
    createInquiry,
    updateInquiry,
    deleteInquiry,
    clearError,
  };
};

export default useInquiry;
