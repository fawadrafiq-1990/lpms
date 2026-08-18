import { useEffect, useState } from 'react';
import {
  Briefcase,
  CheckSquare,
  ArrowRight,
  Gavel,
  Clock,
  Scale,
} from 'lucide-react';
import { Link } from 'react-router-dom';
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer } from 'recharts';
import { dashboardService, type DashboardData } from '@/services/dashboardService';
import { useAuthStore } from '@/store/authStore';
import toast from 'react-hot-toast';

interface StatCardProps {
  title: string;
  value: string | number;
  subtitle?: string;
  icon: React.ReactNode;
  variant?: 'default' | 'success' | 'warning' | 'danger';
}

function StatCard({ title, value, subtitle, icon, variant = 'default' }: StatCardProps) {
  const variantStyles = {
    default: 'bg-white border-neutral-200',
    success: 'bg-success-50 border-success-200',
    warning: 'bg-warning-50 border-warning-200',
    danger: 'bg-danger-50 border-danger-200',
  };
  const iconStyles = {
    default: 'bg-primary-100 text-primary-600',
    success: 'bg-success-100 text-success-600',
    warning: 'bg-warning-100 text-warning-600',
    danger: 'bg-danger-100 text-danger-600',
  };
  return (
    <div className={`card p-6 ${variantStyles[variant]}`}>
      <div className="flex items-start justify-between">
        <div>
          <p className="text-sm text-neutral-500 font-medium">{title}</p>
          <p className="text-3xl font-bold text-neutral-900 mt-1">{value}</p>
          {subtitle && <p className="text-sm text-neutral-500 mt-1">{subtitle}</p>}
        </div>
        <div className={`p-3 rounded-xl ${iconStyles[variant]}`}>{icon}</div>
      </div>
    </div>
  );
}

const fmtLabel = (s?: string) => (s ? s.charAt(0) + s.slice(1).toLowerCase().replace(/_/g, ' ') : '');

export default function DashboardPage() {
  const { user } = useAuthStore();
  const [stats, setStats] = useState<DashboardData | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    (async () => {
      if (!user?.id) {
        setLoading(false);
        return;
      }
      try {
        const res = await dashboardService.getStats(user.id);
        if (res.success && res.data) setStats(res.data);
        else toast.error(res.message || 'Failed to load dashboard');
      } catch {
        toast.error('Failed to load dashboard');
      } finally {
        setLoading(false);
      }
    })();
  }, [user?.id]);

  if (loading) {
    return (
      <div className="space-y-6 animate-in">
        <div className="page-header">
          <div>
            <h1 className="page-title">Dashboard</h1>
            <p className="page-subtitle">Loading your overview…</p>
          </div>
        </div>
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
          {[0, 1, 2, 3].map((i) => (
            <div key={i} className="card p-6 h-28 bg-neutral-50 animate-pulse" />
          ))}
        </div>
      </div>
    );
  }

  const data = stats;
  // Derive a small cases-by-status breakdown from recent cases for the chart.
  const casesByStatus = data?.recentCases.reduce<{ status: string; count: number }[]>((acc, c) => {
    const key = fmtLabel(c.caseStatus);
    const found = acc.find((x) => x.status === key);
    if (found) found.count += 1;
    else acc.push({ status: key, count: 1 });
    return acc;
  }, []) ?? [];

  return (
    <div className="space-y-6 animate-in">
      <div className="page-header">
        <div>
          <h1 className="page-title">Dashboard</h1>
          <p className="page-subtitle">Welcome back! Here's what's happening today.</p>
        </div>
        <Link to="/cases/new" className="btn-primary">
          <Briefcase className="w-4 h-4 mr-2" />
          New Case
        </Link>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
        <StatCard title="Active Cases" value={data?.activeCases ?? 0} subtitle="Across your firm" icon={<Briefcase className="w-6 h-6" />} />
        <StatCard title="Upcoming Hearings" value={data?.upcomingHearings ?? 0} subtitle="Scheduled" icon={<Gavel className="w-6 h-6" />} variant="warning" />
        <StatCard title="Pending Tasks" value={data?.pendingTasks ?? 0} subtitle="Assigned to you" icon={<CheckSquare className="w-6 h-6" />} />
        <StatCard title="Recent Cases" value={data?.recentCases.length ?? 0} subtitle="Most recent matters" icon={<Scale className="w-6 h-6" />} />
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <div className="card">
          <div className="card-header">
            <h2 className="font-semibold text-neutral-900">Cases by Status (recent)</h2>
          </div>
          <div className="card-body">
            <div className="h-64">
              {casesByStatus.length > 0 ? (
                <ResponsiveContainer width="100%" height="100%">
                  <BarChart data={casesByStatus}>
                    <CartesianGrid strokeDasharray="3 3" stroke="#e5e5e5" />
                    <XAxis dataKey="status" tick={{ fontSize: 12 }} />
                    <YAxis allowDecimals={false} tick={{ fontSize: 12 }} />
                    <Tooltip />
                    <Bar dataKey="count" fill="#0ea5e9" radius={[4, 4, 0, 0]} />
                  </BarChart>
                </ResponsiveContainer>
              ) : (
                <div className="h-full flex items-center justify-center text-neutral-400 text-sm">No case data yet</div>
              )}
            </div>
          </div>
        </div>

        <div className="card">
          <div className="card-header flex items-center justify-between">
            <h2 className="font-semibold text-neutral-900">Upcoming Hearings</h2>
            <Link to="/hearings" className="text-sm text-primary-600 hover:text-primary-700 flex items-center gap-1">
              View all <ArrowRight className="w-4 h-4" />
            </Link>
          </div>
          <div className="divide-y divide-neutral-200">
            {(data?.upcomingHearingsList ?? []).map((hearing) => (
              <div key={hearing.id} className="px-6 py-4 hover:bg-neutral-50 transition-colors">
                <div className="flex items-start justify-between">
                  <div>
                    <p className="font-medium text-neutral-900">{hearing.caseTitle || hearing.caseNumber || hearing.hearingNumber}</p>
                    <p className="text-sm text-neutral-500 mt-1">{hearing.courtName} · {fmtLabel(hearing.hearingType)}</p>
                  </div>
                  <div className="text-right">
                    <p className="text-sm font-medium text-neutral-900">{new Date(hearing.scheduledDate).toLocaleDateString()}</p>
                    <p className="text-sm text-neutral-500">{hearing.scheduledTime}</p>
                  </div>
                </div>
              </div>
            ))}
            {(data?.upcomingHearingsList.length ?? 0) === 0 && (
              <div className="px-6 py-8 text-center text-neutral-500">No upcoming hearings</div>
            )}
          </div>
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <div className="card">
          <div className="card-header flex items-center justify-between">
            <h2 className="font-semibold text-neutral-900">Pending Tasks</h2>
            <Link to="/tasks" className="text-sm text-primary-600 hover:text-primary-700 flex items-center gap-1">
              View all <ArrowRight className="w-4 h-4" />
            </Link>
          </div>
          <div className="divide-y divide-neutral-200">
            {(data?.pendingTasksList ?? []).map((task) => (
              <div key={task.id} className="px-6 py-4 hover:bg-neutral-50 transition-colors">
                <div className="flex items-start justify-between">
                  <div className="flex-1">
                    <p className="font-medium text-neutral-900">{task.title}</p>
                    <p className="text-sm text-neutral-500 mt-1">{task.caseNumber || 'General'} · {task.assignedToName || 'Unassigned'}</p>
                    {task.dueDate && (
                      <div className="flex items-center gap-1 mt-2">
                        <Clock className="w-4 h-4 text-neutral-400" />
                        <span className={`text-xs ${new Date(task.dueDate) < new Date() ? 'text-danger-600' : 'text-neutral-500'}`}>
                          Due {new Date(task.dueDate).toLocaleDateString()}
                        </span>
                      </div>
                    )}
                  </div>
                  <span className={`badge ${
                    task.priority === 'HIGH' || task.priority === 'URGENT'
                      ? 'badge-danger'
                      : task.priority === 'MEDIUM'
                      ? 'badge-warning'
                      : 'badge-neutral'
                  }`}>{task.priority}</span>
                </div>
              </div>
            ))}
            {(data?.pendingTasksList.length ?? 0) === 0 && (
              <div className="px-6 py-8 text-center text-neutral-500">No pending tasks</div>
            )}
          </div>
        </div>

        <div className="card">
          <div className="card-header flex items-center justify-between">
            <h2 className="font-semibold text-neutral-900">Recent Cases</h2>
            <Link to="/cases" className="text-sm text-primary-600 hover:text-primary-700 flex items-center gap-1">
              View all <ArrowRight className="w-4 h-4" />
            </Link>
          </div>
          <div className="divide-y divide-neutral-200">
            {(data?.recentCases ?? []).map((c) => (
              <Link key={c.id} to={`/cases/${c.id}`} className="px-6 py-4 hover:bg-neutral-50 transition-colors block">
                <div className="flex items-start justify-between">
                  <div>
                    <p className="font-medium text-neutral-900">{c.title}</p>
                    <p className="text-sm text-neutral-500 mt-1">{c.caseNumber} · {c.clientName || 'No client'}</p>
                  </div>
                  <span className={`badge ${
                    c.caseStatus === 'CLOSED' || c.caseStatus === 'ARCHIVED' ? 'badge-neutral' : 'badge-primary'
                  }`}>{fmtLabel(c.caseStatus)}</span>
                </div>
              </Link>
            ))}
            {(data?.recentCases.length ?? 0) === 0 && (
              <div className="px-6 py-8 text-center text-neutral-500">No cases yet</div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}
