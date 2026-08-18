// API Response Types
export interface ApiResponse<T> {
  success: boolean;
  data: T;
  message?: string;
  errorCode?: string;
  timestamp: string;
  warnings?: string[];
}

export interface PageResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
  empty: boolean;
}

// Auth Types
export interface User {
  id: string;
  email: string;
  firstName: string;
  lastName: string;
  fullName: string;
  profileImageUrl?: string;
  status: UserStatus;
  roles: string[];
  lawFirmId: string;
  branchId?: string;
  departmentId?: string;
  phone?: string;
  mobile?: string;
  lawyerId?: string;
  createdAt?: string;
}

export type UserStatus = 'ACTIVE' | 'INACTIVE' | 'SUSPENDED' | 'PENDING' | 'LOCKED';

export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  expiresIn: number;
  user: User;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  email: string;
  password: string;
  firstName: string;
  lastName: string;
  isClient?: boolean;
}

// Case Types
export interface Case {
  id: string;
  caseNumber: string;
  matterNumber?: string;
  title: string;
  description?: string;
  caseType: CaseType;
  caseStatus: CaseStatus;
  casePriority: CasePriority;
  practiceArea?: string;
  clientId: string;
  clientName?: string;
  courtId?: string;
  courtName?: string;
  judgeId?: string;
  judgeName?: string;
  leadLawyerId?: string;
  leadLawyerName?: string;
  filingDate?: string;
  openingDate?: string;
  expectedClosingDate?: string;
  caseValue?: number;
  confidentialityLevel: ConfidentialityLevel;
  createdAt: string;
  updatedAt: string;
  createdBy?: string;
}

export type CaseType = 'CIVIL' | 'CRIMINAL' | 'COMMERCIAL' | 'FAMILY' | 'LABOR' | 'ADMINISTRATIVE' | 'REAL_ESTATE' | 'OTHER';
export type CaseStatus = 'NEW' | 'CONFLICT_CHECK' | 'OPEN' | 'PREPARATION' | 'FILED' | 'IN_PROGRESS' | 'HEARING' | 'JUDGMENT' | 'APPEAL' | 'SETTLEMENT' | 'CLOSED' | 'ARCHIVED';
export type CasePriority = 'LOW' | 'MEDIUM' | 'HIGH' | 'URGENT' | 'CRITICAL';

// Lawyer (for assignment dropdowns)
export interface Lawyer {
  id: string;
  userId?: string;
  email?: string;
  firstName?: string;
  lastName?: string;
  fullName?: string;
  barNumber?: string;
  hourlyRate?: number;
  availabilityStatus?: string;
  totalCases?: number;
}

// Case-Lawyer assignment
export interface CaseLawyer {
  id: string;
  caseId: string;
  lawyerId: string;
  lawyerName?: string;
  role?: string;
  isPrimary?: boolean;
  billingRate?: number;
  hoursAllocated?: number;
  hoursLogged?: number;
  assignedAt?: string;
  assignedBy?: string;
}

export type ConfidentialityLevel = 'PUBLIC' | 'INTERNAL' | 'CONFIDENTIAL' | 'STRICTLY_CONFIDENTIAL';

// Client Types
export interface Client {
  id: string;
  clientNumber: string;
  clientType: ClientType;
  firstName?: string;
  lastName?: string;
  companyName?: string;
  email?: string;
  phone?: string;
  mobile?: string;
  nationality?: string;
  riskLevel: RiskLevel;
  vip: boolean;
  status: ClientStatus;
  addressLine1?: string;
  city?: string;
  country?: string;
  assignedLawyerId?: string;
  assignedLawyerName?: string;
  createdAt: string;
  updatedAt: string;
}

export type ClientType = 'INDIVIDUAL' | 'COMPANY' | 'GOVERNMENT' | 'ORGANIZATION';
export type ClientStatus = 'ACTIVE' | 'INACTIVE' | 'PENDING' | 'BLACKLISTED';
export type RiskLevel = 'LOW' | 'MEDIUM' | 'HIGH';

// Document Types
export interface Document {
  id: string;
  documentNumber: string;
  title: string;
  description?: string;
  category: DocumentCategory;
  caseId?: string;
  caseNumber?: string;
  clientId?: string;
  fileName: string;
  fileSize: number;
  mimeType: string;
  version: number;
  isLatest: boolean;
  createdAt: string;
  createdBy?: string;
  createdByName?: string;
}

export type DocumentCategory = 'CONTRACT' | 'COURT_ORDER' | 'PLEADING' | 'EVIDENCE' | 'IDENTITY' | 'LEGAL_NOTICE' | 'AGREEMENT' | 'EMAIL' | 'REPORT' | 'JUDGMENT' | 'OTHER';

// Hearing Types
export interface Hearing {
  id: string;
  hearingNumber: string;
  caseId: string;
  caseNumber?: string;
  caseTitle?: string;
  courtId: string;
  courtName?: string;
  courtroomId?: string;
  courtroomName?: string;
  judgeId?: string;
  judgeName?: string;
  hearingType: HearingType;
  scheduledDate: string;
  scheduledTime: string;
  durationMinutes: number;
  status: HearingStatus;
  location?: string;
  agenda?: string;
  outcome?: string;
  notes?: string;
  createdAt: string;
}

export type HearingType = 'INITIAL' | 'ARGUMENT' | 'EVIDENCE' | 'WITNESS' | 'CROSS_EXAMINATION' | 'FINAL_ARGUMENT' | 'JUDGMENT' | 'APPEAL' | 'OTHER';
export type HearingStatus = 'SCHEDULED' | 'IN_PROGRESS' | 'COMPLETED' | 'CANCELLED' | 'POSTPONED' | 'ADJOURNED';

// Task Types
export interface Task {
  id: string;
  taskNumber: string;
  caseId?: string;
  caseNumber?: string;
  title: string;
  description?: string;
  priority: TaskPriority;
  status: TaskStatus;
  assignedTo: string;
  assignedToName?: string;
  dueDate?: string;
  dueTime?: string;
  estimatedHours?: number;
  actualHours?: number;
  billable: boolean;
  progress: number;
  createdAt: string;
  createdBy?: string;
}

export type TaskPriority = 'LOW' | 'MEDIUM' | 'HIGH' | 'URGENT' | 'CRITICAL';
export type TaskStatus = 'TODO' | 'IN_PROGRESS' | 'BLOCKED' | 'REVIEW' | 'COMPLETED' | 'CANCELLED';

// Invoice Types
export interface Invoice {
  id: string;
  invoiceNumber: string;
  caseId: string;
  caseNumber?: string;
  clientId: string;
  clientName?: string;
  invoiceDate: string;
  dueDate: string;
  subtotal: number;
  taxAmount: number;
  total: number;
  amountPaid: number;
  amountOutstanding: number;
  currency: string;
  status: InvoiceStatus;
  createdAt: string;
}

export type InvoiceStatus = 'DRAFT' | 'ISSUED' | 'PARTIALLY_PAID' | 'PAID' | 'OVERDUE' | 'CANCELLED';

// Dashboard Types
export interface DashboardStats {
  activeCases: number;
  upcomingHearings: number;
  pendingTasks: number;
  overdueDeadlines: number;
  outstandingInvoices: number;
  totalOutstanding: number;
  recentDocuments: Document[];
  upcomingHearingsList: Hearing[];
  pendingTasksList: Task[];
  casesByStatus: { status: string; count: number }[];
  casesByPracticeArea: { area: string; count: number }[];
  monthlyRevenue: { month: string; revenue: number }[];
}

// Notification Types
export interface Notification {
  id: string;
  userId: string;
  notificationType: string;
  title: string;
  message: string;
  priority: NotificationPriority;
  status: NotificationStatus;
  actionUrl?: string;
  caseId?: string;
  createdAt: string;
  readAt?: string;
}

export type NotificationPriority = 'LOW' | 'NORMAL' | 'HIGH' | 'URGENT';
export type NotificationStatus = 'UNREAD' | 'READ' | 'ARCHIVED';

// AI Types
export interface AIConversation {
  id: string;
  title: string;
  conversationType: string;
  caseId?: string;
  messageCount: number;
  lastMessageAt?: string;
  createdAt: string;
}

export interface AIMessage {
  id: string;
  role: 'user' | 'assistant' | 'system';
  content: string;
  sources?: AISource[];
  agentChain?: string[];
  createdAt: string;
}

export interface AISource {
  title: string;
  url?: string;
  type: string;
  relevance: number;
  excerpt?: string;
}

// Calendar Types
export interface CalendarEvent {
  id: string;
  title: string;
  description?: string;
  startDate: string;
  endDate: string;
  allDay: boolean;
  eventType: CalendarEventType;
  caseId?: string;
  hearingId?: string;
  location?: string;
  attendees?: string[];
  color?: string;
  recurrenceRule?: string;
}

export type CalendarEventType = 'HEARING' | 'MEETING' | 'DEADLINE' | 'TASK' | 'COURT_DATE' | 'APPOINTMENT' | 'OTHER';

// Pagination and Filtering
export interface PaginationParams {
  page?: number;
  size?: number;
  sort?: string;
  direction?: 'asc' | 'desc';
}

export interface SearchParams extends PaginationParams {
  search?: string;
  status?: string;
  type?: string;
  dateFrom?: string;
  dateTo?: string;
}