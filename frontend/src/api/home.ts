/**
 * @fileoverview Home API 모듈
 */

import apiClient from './client';
import { ApiResponse } from '../types/api';
import { CustomerHomeResponse, PartnerHomeResponse } from '../types/home';

export const homeApi = {
  getCustomerHome: async (params?: { latitude?: number; longitude?: number }): Promise<CustomerHomeResponse> => {
    const response = await apiClient.get<ApiResponse<CustomerHomeResponse>>('/v1/home/customer', {
      params,
    });
    if (response.data.success && response.data.data) {
      return response.data.data;
    }
    throw new Error(response.data.error?.message || '홈 데이터를 불러오는데 실패했습니다.');
  },

  getPartnerHome: async (): Promise<PartnerHomeResponse> => {
    const response = await apiClient.get<ApiResponse<PartnerHomeResponse>>('/v1/home/partner');
    if (response.data.success && response.data.data) {
      return response.data.data;
    }
    throw new Error(response.data.error?.message || '홈 데이터를 불러오는데 실패했습니다.');
  },
};
