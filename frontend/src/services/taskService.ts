import { apiGet, apiPost, apiPut, apiDelete } from './api';
import type { Task, PageResponse, ApiResponse } from '@/types';

export interface TaskQueryParams {
  assignedTo: string;
  page?: number;
  size?: number;
  status?: string;
}

export interface CreateTaskRequest {
  taskNumber?: string;
  caseId?: string;
  caseNumber?: string;
  clientId?: string;
  title: string;
  description?: string;
  taskType?: string;
  priority?: string;
  status?: string;
  assignedTo: string;
  assignedToName?: string;
  dueDate?: string;
  dueTime?: string;
  estimatedHours?: number;
  billable?: boolean;
  billingRate?: number;
  notes?: string;
}

export const taskService = {
  getTasks: (params: TaskQueryParams): Promise<ApiResponse<PageResponse<Task>>> => {
    return apiGet<ApiResponse<PageResponse<Task>>>('/v1/tasks', params);
  },

  getPendingTasks: (assignedTo: string): Promise<ApiResponse<Task[]>> => {
    return apiGet<ApiResponse<Task[]>>('/v1/tasks/pending', { assignedTo });
  },

  getTasksByCase: (caseId: string): Promise<ApiResponse<Task[]>> => {
    return apiGet<ApiResponse<Task[]>>(`/v1/tasks/case/${caseId}`);
  },

  getTask: (id: string): Promise<ApiResponse<Task>> => {
    return apiGet<ApiResponse<Task>>(`/v1/tasks/${id}`);
  },

  createTask: (data: CreateTaskRequest): Promise<ApiResponse<Task>> => {
    return apiPost<ApiResponse<Task>>('/v1/tasks', data);
  },

  updateTask: (id: string, data: Partial<CreateTaskRequest>): Promise<ApiResponse<Task>> => {
    return apiPut<ApiResponse<Task>>(`/v1/tasks/${id}`, data);
  },

  deleteTask: (id: string): Promise<ApiResponse<void>> => {
    return apiDelete<ApiResponse<void>>(`/v1/tasks/${id}`);
  },
};
