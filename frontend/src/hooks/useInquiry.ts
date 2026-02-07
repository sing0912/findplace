/**
 * @fileoverview 문의 게시판 관련 커스텀 훅
 * @see docs/develop/user/frontend.md - 섹션 10
 */

import { useState, useCallback } from 'react';

type InquiryStatus = 'WAITING' | 'ANSWERED';

interface InquiryAnswer {
  content: string;
  createdAt: string;
}

interface Inquiry {
  id: number;
  title: string;
  content?: string;
  status: InquiryStatus;
  createdAt: string;
  answer?: InquiryAnswer | null;
}

interface InquiryListResponse {
  content: Inquiry[];
  totalElements: number;
  totalPages: number;
  number: number;
}

interface CreateInquiryRequest {
  title: string;
  content: string;
}

interface UpdateInquiryRequest {
  title: string;
  content: string;
}

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
  const [currentPage, setCurrentPage] = useState(0);

  const getAuthHeader = (): HeadersInit => {
    const token = localStorage.getItem('accessToken');
    return token ? { Authorization: `Bearer ${token}` } : {};
  };

  const fetchInquiries = useCallback(async (page = 0) => {
    setIsLoading(true);
    setError(null);

    try {
      const response = await fetch(`/api/v1/inquiries?page=${page}&size=10`, {
        headers: {
          ...getAuthHeader(),
        },
      });

      if (!response.ok) {
        throw new Error('문의 목록을 불러오는데 실패했습니다.');
      }

      const data: InquiryListResponse = await response.json();

      if (page === 0) {
        setInquiries(data.content);
      } else {
        setInquiries((prev) => [...prev, ...data.content]);
      }

      setCurrentPage(data.number);
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
      const response = await fetch(`/api/v1/inquiries/${id}`, {
        headers: {
          ...getAuthHeader(),
        },
      });

      if (!response.ok) {
        throw new Error('문의를 불러오는데 실패했습니다.');
      }

      const data: Inquiry = await response.json();
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
      const response = await fetch('/api/v1/inquiries', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          ...getAuthHeader(),
        },
        body: JSON.stringify(data),
      });

      if (!response.ok) {
        const errorData = await response.json();
        throw new Error(errorData.message || '문의 작성에 실패했습니다.');
      }

      const inquiry: Inquiry = await response.json();
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
      const response = await fetch(`/api/v1/inquiries/${id}`, {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
          ...getAuthHeader(),
        },
        body: JSON.stringify(data),
      });

      if (!response.ok) {
        const errorData = await response.json();
        throw new Error(errorData.message || '문의 수정에 실패했습니다.');
      }

      const inquiry: Inquiry = await response.json();
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
      const response = await fetch(`/api/v1/inquiries/${id}`, {
        method: 'DELETE',
        headers: {
          ...getAuthHeader(),
        },
      });

      if (!response.ok) {
        const errorData = await response.json();
        throw new Error(errorData.message || '문의 삭제에 실패했습니다.');
      }

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
