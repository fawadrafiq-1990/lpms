import { apiGet, apiPost, apiPut, apiDelete } from './api';
import type { Client, PageResponse, ApiResponse } from '@/types';

export interface ClientQueryParams {
  page?: number;
  size?: number;
  search?: string;
  status?: string;
  type?: string;
}

export const clientService = {
  getClients: (params: ClientQueryParams = {}): Promise<ApiResponse<PageResponse<Client>>> => {
    return apiGet<ApiResponse<PageResponse<Client>>>('/v1/clients', params);
  },

  getActiveClients: (): Promise<ApiResponse<Client[]>> => {
    return apiGet<ApiResponse<Client[]>>('/v1/clients/active');
  },

  getClient: (id: string): Promise<ApiResponse<Client>> => {
    return apiGet<ApiResponse<Client>>(`/v1/clients/${id}`);
  },

  createClient: (data: Partial<Client>): Promise<ApiResponse<Client>> => {
    return apiPost<ApiResponse<Client>>('/v1/clients', data);
  },

  updateClient: (id: string, data: Partial<Client>): Promise<ApiResponse<Client>> => {
    return apiPut<ApiResponse<Client>>(`/v1/clients/${id}`, data);
  },

  deleteClient: (id: string): Promise<ApiResponse<void>> => {
    return apiDelete<ApiResponse<void>>(`/v1/clients/${id}`);
  },
};
