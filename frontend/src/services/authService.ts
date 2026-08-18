import { apiGet, apiPost } from './api';
import type { AuthResponse, LoginRequest, RegisterRequest, ApiResponse } from '@/types';

export const authService = {
  login: async (data: LoginRequest): Promise<ApiResponse<AuthResponse>> => {
    return apiPost<ApiResponse<AuthResponse>>('/v1/auth/login', data);
  },

  register: async (data: RegisterRequest): Promise<ApiResponse<AuthResponse>> => {
    return apiPost<ApiResponse<AuthResponse>>('/v1/auth/register', data);
  },

  refreshToken: async (refreshToken: string): Promise<ApiResponse<AuthResponse>> => {
    return apiPost<ApiResponse<AuthResponse>>('/v1/auth/refresh', { refreshToken });
  },

  logout: async (): Promise<ApiResponse<void>> => {
    return apiPost<ApiResponse<void>>('/v1/auth/logout');
  },

  forgotPassword: async (data: { email: string }): Promise<ApiResponse<void>> => {
    return apiPost<ApiResponse<void>>('/v1/auth/forgot-password', data);
  },

  resetPassword: async (data: {
    email: string;
    token: string;
    newPassword: string;
  }): Promise<ApiResponse<void>> => {
    return apiPost<ApiResponse<void>>('/v1/auth/reset-password', data);
  },

  getCurrentUser: async () => {
    return apiGet<ApiResponse<any>>('/v1/users/me');
  },
};