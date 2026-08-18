import { apiGet, apiPost, apiPut, apiDelete } from './api';
import { api } from './api';
import type { Invoice, PageResponse, ApiResponse } from '@/types';

export interface InvoiceQueryParams {
  page?: number;
  size?: number;
  status?: string;
  caseId?: string;
}

export interface CreateInvoiceRequest {
  caseId: string;
  caseNumber?: string;
  clientId: string;
  clientName?: string;
  invoiceDate?: string;
  dueDate?: string;
  subtotal?: number;
  taxAmount?: number;
  discountAmount?: number;
  total?: number;
  currency?: string;
  billingModel?: string;
  notes?: string;
}

export const invoiceService = {
  getInvoices: (params: InvoiceQueryParams = {}): Promise<ApiResponse<PageResponse<Invoice>>> => {
    return apiGet<ApiResponse<PageResponse<Invoice>>>('/v1/invoices', params);
  },

  getInvoice: (id: string): Promise<ApiResponse<Invoice>> => {
    return apiGet<ApiResponse<Invoice>>(`/v1/invoices/${id}`);
  },

  getInvoicesByCase: (caseId: string): Promise<ApiResponse<Invoice[]>> => {
    return apiGet<ApiResponse<Invoice[]>>(`/v1/invoices/case/${caseId}`);
  },

  getOverdue: (): Promise<ApiResponse<Invoice[]>> => {
    return apiGet<ApiResponse<Invoice[]>>('/v1/invoices/overdue');
  },

  getOutstanding: (): Promise<ApiResponse<number>> => {
    return apiGet<ApiResponse<number>>('/v1/invoices/outstanding');
  },

  createInvoice: (data: CreateInvoiceRequest): Promise<ApiResponse<Invoice>> => {
    return apiPost<ApiResponse<Invoice>>('/v1/invoices', data);
  },

  updateInvoice: (id: string, data: Partial<CreateInvoiceRequest>): Promise<ApiResponse<Invoice>> => {
    return apiPut<ApiResponse<Invoice>>(`/v1/invoices/${id}`, data);
  },

  issueInvoice: (id: string): Promise<ApiResponse<Invoice>> => {
    return api.patch<ApiResponse<Invoice>>(`/v1/invoices/${id}/issue`).then((r) => r.data);
  },

  recordPayment: (id: string, amount: number): Promise<ApiResponse<Invoice>> => {
    return api
      .patch<ApiResponse<Invoice>>(`/v1/invoices/${id}/payment`, null, { params: { amount } })
      .then((r) => r.data);
  },

  cancelInvoice: (id: string): Promise<ApiResponse<Invoice>> => {
    return api.patch<ApiResponse<Invoice>>(`/v1/invoices/${id}/cancel`).then((r) => r.data);
  },

  deleteInvoice: (id: string): Promise<ApiResponse<void>> => {
    return apiDelete<ApiResponse<void>>(`/v1/invoices/${id}`);
  },
};
