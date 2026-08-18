import { useCallback, useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { Search, Plus, Briefcase, Loader2 } from 'lucide-react';
import { caseService } from '@/services/caseService';
import type { Case } from '@/types';

const PAGE_SIZE = 10;

const statusColors: Record<string, string> = {
  NEW: 'badge-neutral',
  CONFLICT_CHECK: 'badge-warning',
  OPEN: 'badge-primary',
  PREPARATION: 'badge-primary',
  FILED: 'badge-success',
  IN_PROGRESS: 'badge-primary',
  HEARING: 'badge-warning',
  JUDGMENT: 'badge-warning',
  APPEAL: 'badge-warning',
  SETTLEMENT: 'badge-success',
  CLOSED: 'badge-neutral',
  ARCHIVED: 'badge-neutral',
};

const fmtLabel = (s?: string) => (s ? s.charAt(0) + s.slice(1).toLowerCase().replace(/_/g, ' ') : '');

export default function CasesPage() {
  const [cases, setCases] = useState<Case[]>([]);
  const [total, setTotal] = useState(0);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [loading, setLoading] = useState(true);
  const [searchQuery, setSearchQuery] = useState('');
  const [statusFilter, setStatusFilter] = useState('');
  const [typeFilter, setTypeFilter] = useState('');

  const load = useCallback(async () => {
    setLoading(true);
    try {
      const res = await caseService.getCases({
        page,
        size: PAGE_SIZE,
        search: searchQuery || undefined,
        status: statusFilter || undefined,
        type: typeFilter || undefined,
      });
      if (res.success && res.data) {
        setCases(res.data.content);
        setTotal(res.data.totalElements);
        setTotalPages(res.data.totalPages);
      } else {
        setCases([]);
      }
    } catch {
      setCases([]);
    } finally {
      setLoading(false);
    }
  }, [page, searchQuery, statusFilter, typeFilter]);

  useEffect(() => {
    load();
  }, [load]);

  const start = total === 0 ? 0 : page * PAGE_SIZE + 1;
  const end = Math.min(total, (page + 1) * PAGE_SIZE);

  return (
    <div className="space-y-6 animate-in">
      <div className="page-header">
        <div>
          <h1 className="page-title">Cases</h1>
          <p className="page-subtitle">Manage your law firm's cases and matters</p>
        </div>
        <Link to="/cases/new" className="btn-primary">
          <Plus className="w-4 h-4 mr-2" />
          New Case
        </Link>
      </div>

      <div className="card p-4">
        <div className="flex flex-col md:flex-row gap-4">
          <div className="flex-1">
            <div className="relative">
              <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-neutral-400" />
              <input
                type="text"
                placeholder="Search by case number, title, or client..."
                value={searchQuery}
                onChange={(e) => {
                  setSearchQuery(e.target.value);
                  setPage(0);
                }}
                className="input pl-10"
              />
            </div>
          </div>

          <select
            value={statusFilter}
            onChange={(e) => {
              setStatusFilter(e.target.value);
              setPage(0);
            }}
            className="input w-auto"
          >
            <option value="">All Statuses</option>
            <option value="NEW">New</option>
            <option value="OPEN">Open</option>
            <option value="IN_PROGRESS">In Progress</option>
            <option value="HEARING">Hearing</option>
            <option value="CLOSED">Closed</option>
          </select>

          <select
            value={typeFilter}
            onChange={(e) => {
              setTypeFilter(e.target.value);
              setPage(0);
            }}
            className="input w-auto"
          >
            <option value="">All Types</option>
            <option value="LITIGATION">Litigation</option>
            <option value="CORPORATE">Corporate</option>
            <option value="FAMILY">Family</option>
            <option value="CIVIL">Civil</option>
            <option value="CRIMINAL">Criminal</option>
            <option value="COMMERCIAL">Commercial</option>
            <option value="REAL_ESTATE">Real Estate</option>
          </select>
        </div>
      </div>

      <div className="card overflow-hidden">
        {loading ? (
          <div className="flex items-center justify-center py-16">
            <Loader2 className="w-6 h-6 animate-spin text-neutral-400" />
          </div>
        ) : (
          <>
            <div className="overflow-x-auto">
              <table className="table">
                <thead>
                  <tr>
                    <th>Case Number</th>
                    <th>Title</th>
                    <th>Client</th>
                    <th>Status</th>
                    <th>Priority</th>
                    <th>Lawyer</th>
                    <th>Value</th>
                  </tr>
                </thead>
                <tbody>
                  {cases.map((caseItem) => (
                    <tr key={caseItem.id}>
                      <td>
                        <Link to={`/cases/${caseItem.id}`} className="font-medium text-primary-600 hover:text-primary-700">
                          {caseItem.caseNumber}
                        </Link>
                      </td>
                      <td className="max-w-xs truncate">{caseItem.title}</td>
                      <td>{caseItem.clientName || '—'}</td>
                      <td>
                        <span className={`badge ${statusColors[caseItem.caseStatus] || 'badge-neutral'}`}>
                          {fmtLabel(caseItem.caseStatus)}
                        </span>
                      </td>
                      <td>
                        <span className={`badge ${
                          caseItem.casePriority === 'CRITICAL' || caseItem.casePriority === 'URGENT'
                            ? 'badge-danger'
                            : caseItem.casePriority === 'HIGH'
                            ? 'badge-warning'
                            : 'badge-neutral'
                        }`}>
                          {caseItem.casePriority}
                        </span>
                      </td>
                      <td>{caseItem.leadLawyerName || '—'}</td>
                      <td>{caseItem.caseValue ? `AED ${Number(caseItem.caseValue).toLocaleString()}` : '-'}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>

            {cases.length === 0 && (
              <div className="text-center py-12">
                <Briefcase className="w-12 h-12 text-neutral-300 mx-auto mb-4" />
                <p className="text-neutral-500">No cases found</p>
                <Link to="/cases/new" className="btn-primary mt-4">
                  Create your first case
                </Link>
              </div>
            )}
          </>
        )}
      </div>

      <div className="flex items-center justify-between">
        <p className="text-sm text-neutral-500">
          Showing {start}–{end} of {total} cases
        </p>
        <div className="flex gap-2">
          <button className="btn-secondary btn-sm" disabled={page === 0 || loading} onClick={() => setPage((p) => p - 1)}>
            Previous
          </button>
          <button className="btn-secondary btn-sm" disabled={page >= totalPages - 1 || loading} onClick={() => setPage((p) => p + 1)}>
            Next
          </button>
        </div>
      </div>
    </div>
  );
}
