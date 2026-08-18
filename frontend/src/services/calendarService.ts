import { apiGet, apiPost, apiPut, apiDelete } from './api';
import type { CalendarEvent, ApiResponse } from '@/types';

export interface CalendarEventQueryParams {
  start?: string;
  end?: string;
}

export interface CreateCalendarEventRequest {
  title: string;
  description?: string;
  startDate: string;
  endDate: string;
  allDay?: boolean;
  eventType: string;
  caseId?: string;
  location?: string;
  color?: string;
  notes?: string;
}

export const calendarService = {
  getEvents: (params: CalendarEventQueryParams = {}): Promise<ApiResponse<CalendarEvent[]>> => {
    return apiGet<ApiResponse<CalendarEvent[]>>('/v1/calendar/events', params);
  },

  getEvent: (id: string): Promise<ApiResponse<CalendarEvent>> => {
    return apiGet<ApiResponse<CalendarEvent>>(`/v1/calendar/events/${id}`);
  },

  createEvent: (data: CreateCalendarEventRequest): Promise<ApiResponse<CalendarEvent>> => {
    return apiPost<ApiResponse<CalendarEvent>>('/v1/calendar/events', data);
  },

  updateEvent: (id: string, data: Partial<CreateCalendarEventRequest>): Promise<ApiResponse<CalendarEvent>> => {
    return apiPut<ApiResponse<CalendarEvent>>(`/v1/calendar/events/${id}`, data);
  },

  deleteEvent: (id: string): Promise<ApiResponse<void>> => {
    return apiDelete<ApiResponse<void>>(`/v1/calendar/events/${id}`);
  },
};
