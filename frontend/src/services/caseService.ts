import { apiGet, apiPost, apiPut, apiDelete } from './api';
import type { Case, CaseLawyer, PageResponse, ApiResponse } from '@/types';

export interface CaseQueryParams {
  page?: number;
  size?: number;
  search?: string;
  status?: string;
  type?: string;
}

export interface AssignLawyerRequest {
  lawyerId: string;
  role?: string;
  isPrimary?: boolean;
  billingRate?: number;
  hoursAllocated?: number;
}

export const caseService = {
  getCases: (params: CaseQueryParams = {}): Promise<ApiResponse<PageResponse<Case>>> => {
    return apiGet<ApiResponse<PageResponse<Case>>>('/v1/cases', params);
  },

  getCase: (id: string): Promise<ApiResponse<Case>> => {
    return apiGet<ApiResponse<Case>>(`/v1/cases/${id}`);
  },

  getCasesByClient: (clientId: string): Promise<ApiResponse<Case[]>> => {
    return apiGet<ApiResponse<Case[]>>(`/v1/cases/client/${clientId}`);
  },

  createCase: (data: Partial<Case>): Promise<ApiResponse<Case>> => {
    return apiPost<ApiResponse<Case>>('/v1/cases', data);
  },

  updateCase: (id: string, data: Partial<Case>): Promise<ApiResponse<Case>> => {
    return apiPut<ApiResponse<Case>>(`/v1/cases/${id}`, data);
  },

  deleteCase: (id: string): Promise<ApiResponse<void>> => {
    return apiDelete<ApiResponse<void>>(`/v1/cases/${id}`);
  },

  getCaseLawyers: (caseId: string): Promise<ApiResponse<CaseLawyer[]>> => {
    return apiGet<ApiResponse<CaseLawyer[]>>(`/v1/cases/${caseId}/lawyers`);
  },

  assignLawyer: (caseId: string, data: AssignLawyerRequest): Promise<ApiResponse<CaseLawyer>> => {
    return apiPost<ApiResponse<CaseLawyer>>(`/v1/cases/${caseId}/lawyers`, data);
  },

  removeLawyer: (caseId: string, lawyerId: string): Promise<ApiResponse<void>> => {
    return apiDelete<ApiResponse<void>>(`/v1/cases/${caseId}/lawyers/${lawyerId}`);
  },
};
