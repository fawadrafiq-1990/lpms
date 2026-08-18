import { apiGet } from './api';
import type {
  Case,
  Hearing,
  Task,
  PageResponse,
  ApiResponse,
} from '@/types';

/**
 * Dashboard data is composed client-side from existing list endpoints, since
 * the backend has no dedicated /v1/dashboard/stats endpoint.
 */
export interface DashboardData {
  activeCases: number;
  upcomingHearings: number;
  pendingTasks: number;
  upcomingHearingsList: Hearing[];
  pendingTasksList: Task[];
  recentCases: Case[];
}

export const dashboardService = {
  getStats: async (userId: string): Promise<ApiResponse<DashboardData>> => {
    // Fire the independent requests in parallel.
    const [casesRes, hearingsRes, tasksRes] = await Promise.allSettled([
      apiGet<ApiResponse<PageResponse<Case>>>('/v1/cases', { page: 0, size: 5 }),
      apiGet<ApiResponse<Hearing[]>>('/v1/hearings/upcoming'),
      apiGet<ApiResponse<Task[]>>('/v1/tasks/pending', { assignedTo: userId }),
    ]);

    const casesData = casesRes.status === 'fulfilled' ? casesRes.value.data : null;
    const hearingsData = hearingsRes.status === 'fulfilled' ? hearingsRes.value.data : null;
    const tasksData = tasksRes.status === 'fulfilled' ? tasksRes.value.data : null;

    const data: DashboardData = {
      activeCases: casesData?.totalElements ?? 0,
      upcomingHearings: Array.isArray(hearingsData) ? hearingsData.length : 0,
      pendingTasks: Array.isArray(tasksData) ? tasksData.length : 0,
      upcomingHearingsList: Array.isArray(hearingsData) ? hearingsData.slice(0, 5) : [],
      pendingTasksList: Array.isArray(tasksData) ? tasksData.slice(0, 5) : [],
      recentCases: casesData?.content ?? [],
    };

    return {
      success: true,
      data,
      timestamp: new Date().toISOString(),
    };
  },
};
