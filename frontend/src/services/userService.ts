import { apiGet, apiPost, apiPut, apiDelete } from './api';
import type { User, Lawyer, PageResponse, ApiResponse } from '@/types';

export interface UserQueryParams {
  role?: string;
  page?: number;
  size?: number;
  search?: string;
}

export interface CreateUserRequest {
  email: string;
  password: string;
  firstName: string;
  lastName: string;
  phone?: string;
  roleId: string;
  status?: string;
  barNumber?: string;
  licenseNumber?: string;
  hourlyRate?: number;
}

export const userService = {
  getUsers: (params: UserQueryParams = {}): Promise<ApiResponse<PageResponse<User>>> => {
    return apiGet<ApiResponse<PageResponse<User>>>('/v1/users', params);
  },

  getUser: (id: string): Promise<ApiResponse<User>> => {
    return apiGet<ApiResponse<User>>(`/v1/users/${id}`);
  },

  createUser: (data: CreateUserRequest): Promise<ApiResponse<User>> => {
    return apiPost<ApiResponse<User>>('/v1/users', data);
  },

  updateUser: (id: string, data: Partial<CreateUserRequest>): Promise<ApiResponse<User>> => {
    return apiPut<ApiResponse<User>>(`/v1/users/${id}`, data);
  },

  deleteUser: (id: string): Promise<ApiResponse<void>> => {
    return apiDelete<ApiResponse<void>>(`/v1/users/${id}`);
  },

  getLawyers: (): Promise<ApiResponse<Lawyer[]>> => {
    return apiGet<ApiResponse<Lawyer[]>>('/v1/users/lawyers');
  },
};
