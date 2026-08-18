import { Routes, Route, Navigate } from 'react-router-dom';
import { useAuthStore } from './store/authStore';
import MainLayout from './layouts/MainLayout';
import LoginPage from './pages/auth/LoginPage';
import DashboardPage from './pages/dashboard/DashboardPage';
import CasesPage from './pages/cases/CasesPage';
import NewCasePage from './pages/cases/NewCasePage';
import CaseDetailPage from './pages/cases/CaseDetailPage';
import ClientsPage from './pages/clients/ClientsPage';
import NewClientPage from './pages/clients/NewClientPage';
import ClientDetailPage from './pages/clients/ClientDetailPage';
import DocumentsPage from './pages/documents/DocumentsPage';
import HearingsPage from './pages/hearings/HearingsPage';
import CalendarPage from './pages/calendar/CalendarPage';
import TasksPage from './pages/tasks/TasksPage';
import BillingPage from './pages/billing/BillingPage';
import SettingsPage from './pages/settings/SettingsPage';
import AIAssistantPage from './pages/ai/AIAssistantPage';
import UsersPage from './pages/admin/UsersPage';

// Protected route wrapper
function ProtectedRoute({ children }: { children: React.ReactNode }) {
  const { isAuthenticated } = useAuthStore();

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  return <>{children}</>;
}

// Public route wrapper (redirects to dashboard if already logged in)
function PublicRoute({ children }: { children: React.ReactNode }) {
  const { isAuthenticated } = useAuthStore();

  if (isAuthenticated) {
    return <Navigate to="/" replace />;
  }

  return <>{children}</>;
}

function App() {
  return (
    <Routes>
      {/* Public routes */}
      <Route
        path="/login"
        element={
          <PublicRoute>
            <LoginPage />
          </PublicRoute>
        }
      />

      {/* Protected routes */}
      <Route
        path="/"
        element={
          <ProtectedRoute>
            <MainLayout />
          </ProtectedRoute>
        }
      >
        <Route index element={<DashboardPage />} />
        <Route path="dashboard" element={<DashboardPage />} />

        {/* Cases */}
        <Route path="cases" element={<CasesPage />} />
        <Route path="cases/new" element={<NewCasePage />} />
        <Route path="cases/:caseId" element={<CaseDetailPage />} />

        {/* Clients */}
        <Route path="clients" element={<ClientsPage />} />
        <Route path="clients/new" element={<NewClientPage />} />
        <Route path="clients/:clientId" element={<ClientDetailPage />} />

        {/* Documents */}
        <Route path="documents" element={<DocumentsPage />} />

        {/* Hearings */}
        <Route path="hearings" element={<HearingsPage />} />

        {/* Calendar */}
        <Route path="calendar" element={<CalendarPage />} />

        {/* Tasks */}
        <Route path="tasks" element={<TasksPage />} />

        {/* Billing */}
        <Route path="billing" element={<BillingPage />} />

        {/* AI Assistant */}
        <Route path="ai-assistant" element={<AIAssistantPage />} />

        {/* Admin */}
        <Route path="admin/users" element={<UsersPage />} />

        {/* Settings */}
        <Route path="settings" element={<SettingsPage />} />
      </Route>

      {/* Catch all */}
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
}

export default App;