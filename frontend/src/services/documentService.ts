import { apiGet, apiPost, apiDelete } from './api';
import { api } from './api';
import type { Document, PageResponse, ApiResponse } from '@/types';

export interface DocumentQueryParams {
  page?: number;
  size?: number;
  caseId?: string;
  clientId?: string;
}

export interface CreateDocumentRequest {
  documentNumber: string;
  title: string;
  category?: string;
  caseId?: string;
  clientId?: string;
  description?: string;
}

export const documentService = {
  getDocuments: (params: DocumentQueryParams = {}): Promise<ApiResponse<PageResponse<Document>>> => {
    if (params.caseId) {
      return apiGet<ApiResponse<PageResponse<Document>>>(`/v1/documents/case/${params.caseId}`, {
        page: params.page,
        size: params.size,
      });
    }
    if (params.clientId) {
      return apiGet<ApiResponse<PageResponse<Document>>>(`/v1/documents/client/${params.clientId}`, {
        page: params.page,
        size: params.size,
      });
    }
    return apiGet<ApiResponse<PageResponse<Document>>>('/v1/documents', {
      page: params.page,
      size: params.size,
    });
  },

  getDocument: (id: string): Promise<ApiResponse<Document>> => {
    return apiGet<ApiResponse<Document>>(`/v1/documents/${id}`);
  },

  createDocument: (data: CreateDocumentRequest): Promise<ApiResponse<Document>> => {
    return apiPost<ApiResponse<Document>>('/v1/documents', data);
  },

  uploadFile: (id: string, file: File): Promise<ApiResponse<Document>> => {
    const formData = new FormData();
    formData.append('file', file);
    return api
      .post<ApiResponse<Document>>(`/v1/documents/${id}/upload`, formData, {
        headers: { 'Content-Type': 'multipart/form-data' },
      })
      .then((response) => response.data);
  },

  deleteDocument: (id: string): Promise<ApiResponse<void>> => {
    return apiDelete<ApiResponse<void>>(`/v1/documents/${id}`);
  },
};
