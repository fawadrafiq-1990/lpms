import { apiGet, apiPost, apiPut, apiDelete } from './api';
import type { Hearing, PageResponse, ApiResponse } from '@/types';

export interface HearingQueryParams {
  page?: number;
  size?: number;
  status?: string;
}

export interface CreateHearingRequest {
  hearingNumber?: string;
  caseId: string;
  caseNumber?: string;
  caseTitle?: string;
  courtId: string;
  courtName?: string;
  courtroomId?: string;
  courtroomName?: string;
  judgeId?: string;
  judgeName?: string;
  hearingType: string;
  scheduledDate: string;
  scheduledTime: string;
  durationMinutes?: number;
  status?: string;
  priority?: string;
  location?: string;
  agenda?: string;
  notes?: string;
}

export const hearingService = {
  getHearings: (params: HearingQueryParams = {}): Promise<ApiResponse<PageResponse<Hearing>>> => {
    return apiGet<ApiResponse<PageResponse<Hearing>>>('/v1/hearings', params);
  },

  getUpcoming: (): Promise<ApiResponse<Hearing[]>> => {
    return apiGet<ApiResponse<Hearing[]>>('/v1/hearings/upcoming');
  },

  getHearingsByCase: (caseId: string): Promise<ApiResponse<Hearing[]>> => {
    return apiGet<ApiResponse<Hearing[]>>(`/v1/hearings/case/${caseId}`);
  },

  getHearing: (id: string): Promise<ApiResponse<Hearing>> => {
    return apiGet<ApiResponse<Hearing>>(`/v1/hearings/${id}`);
  },

  createHearing: (data: CreateHearingRequest): Promise<ApiResponse<Hearing>> => {
    return apiPost<ApiResponse<Hearing>>('/v1/hearings', data);
  },

  updateHearing: (id: string, data: Partial<CreateHearingRequest>): Promise<ApiResponse<Hearing>> => {
    return apiPut<ApiResponse<Hearing>>(`/v1/hearings/${id}`, data);
  },

  deleteHearing: (id: string): Promise<ApiResponse<void>> => {
    return apiDelete<ApiResponse<void>>(`/v1/hearings/${id}`);
  },
};
