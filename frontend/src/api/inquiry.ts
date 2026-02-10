/**
 * @fileoverview 문의 API 모듈
 *
 * 1:1 문의 관련 API 호출 함수들을 정의합니다.
 * apiClient 기반으로 통일된 패턴을 사용합니다.
 */

import apiClient from './client';
import { ApiResponse } from '../types/api';

type InquiryStatus = 'WAITING' | 'ANSWERED';

interface InquiryAnswer {
  content: string;
  createdAt: string;
}

export interface Inquiry {
  id: number;
  title: string;
  content?: string;
  status: InquiryStatus;
  createdAt: string;
  answer?: InquiryAnswer | null;
}

export interface InquiryListResponse {
  content: Inquiry[];
  totalElements: number;
  totalPages: number;
  number: number;
}

export interface CreateInquiryRequest {
  title: string;
  content: string;
}

export interface UpdateInquiryRequest {
  title: string;
  content: string;
}

export const inquiryApi = {
  getList: async (page = 0, size = 10): Promise<InquiryListResponse> => {
    const response = await apiClient.get<ApiResponse<InquiryListResponse>>('/v1/inquiries', {
      params: { page, size },
    });
    if (response.data.success && response.data.data) {
      return response.data.data;
    }
    throw new Error(response.data.error?.message || '문의 목록을 불러오는데 실패했습니다.');
  },

  getDetail: async (id: number): Promise<Inquiry> => {
    const response = await apiClient.get<ApiResponse<Inquiry>>(`/v1/inquiries/${id}`);
    if (response.data.success && response.data.data) {
      return response.data.data;
    }
    throw new Error(response.data.error?.message || '문의를 불러오는데 실패했습니다.');
  },

  create: async (data: CreateInquiryRequest): Promise<Inquiry> => {
    const response = await apiClient.post<ApiResponse<Inquiry>>('/v1/inquiries', data);
    if (response.data.success && response.data.data) {
      return response.data.data;
    }
    throw new Error(response.data.error?.message || '문의 작성에 실패했습니다.');
  },

  update: async (id: number, data: UpdateInquiryRequest): Promise<Inquiry> => {
    const response = await apiClient.put<ApiResponse<Inquiry>>(`/v1/inquiries/${id}`, data);
    if (response.data.success && response.data.data) {
      return response.data.data;
    }
    throw new Error(response.data.error?.message || '문의 수정에 실패했습니다.');
  },

  delete: async (id: number): Promise<void> => {
    const response = await apiClient.delete<ApiResponse<void>>(`/v1/inquiries/${id}`);
    if (!response.data.success) {
      throw new Error(response.data.error?.message || '문의 삭제에 실패했습니다.');
    }
  },
};
