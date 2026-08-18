import { useCallback, useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { Search, Plus, Users, Loader2 } from 'lucide-react';
import { clientService } from '@/services/clientService';
import type { Client } from '@/types';

const PAGE_SIZE = 10;

const statusColors: Record<string, string> = {
  ACTIVE: 'badge-success',
  INACTIVE: 'badge-neutral',
  PENDING: 'badge-warning',
  BLACKLISTED: 'badge-danger',
};

const fmtLabel = (s?: string) => (s ? s.charAt(0) + s.slice(1).toLowerCase() : '');

export default function ClientsPage() {
  const [clients, setClients] = useState<Client[]>([]);
  const [total, setTotal] = useState(0);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [loading, setLoading] = useState(true);
  const [searchQuery, setSearchQuery] = useState('');
  const [statusFilter, setStatusFilter] = useState('');

  const load = useCallback(async () => {
    setLoading(true);
    try {
      const res = await clientService.getClients({
        page,
        size: PAGE_SIZE,
        search: searchQuery || undefined,
        status: statusFilter || undefined,
      });
      if (res.success && res.data) {
        setClients(res.data.content);
        setTotal(res.data.totalElements);
        setTotalPages(res.data.totalPages);
      } else {
        setClients([]);
      }
    } catch {
      setClients([]);
    } finally {
      setLoading(false);
    }
  }, [page, searchQuery, statusFilter]);

  useEffect(() => {
    load();
  }, [load]);

  const start = total === 0 ? 0 : page * PAGE_SIZE + 1;
  const end = Math.min(total, (page + 1) * PAGE_SIZE);

  return (
    <div className="space-y-6 animate-in">
      <div className="page-header">
        <div>
          <h1 className="page-title">Clients</h1>
          <p className="page-subtitle">Manage your law firm's clients</p>
        </div>
        <Link to="/clients/new" className="btn-primary">
          <Plus className="w-4 h-4 mr-2" />
          New Client
        </Link>
      </div>

      <div className="card p-4">
        <div className="flex flex-col md:flex-row gap-4">
          <div className="flex-1 max-w-md">
            <div className="relative">
              <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-neutral-400" />
              <input
                type="text"
                placeholder="Search clients..."
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
            <option value="ACTIVE">Active</option>
            <option value="INACTIVE">Inactive</option>
            <option value="PENDING">Pending</option>
            <option value="BLACKLISTED">Blacklisted</option>
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
                    <th>Client No.</th>
                    <th>Name</th>
                    <th>Type</th>
                    <th>Email</th>
                    <th>Status</th>
                    <th>Lawyer</th>
                  </tr>
                </thead>
                <tbody>
                  {clients.map((client) => (
                    <tr key={client.id}>
                      <td>
                        <Link to={`/clients/${client.id}`} className="font-medium text-primary-600 hover:text-primary-700">
                          {client.clientNumber}
                        </Link>
                      </td>
                      <td>{client.companyName || `${client.firstName ?? ''} ${client.lastName ?? ''}`.trim()}</td>
                      <td>{fmtLabel(client.clientType)}</td>
                      <td className="text-neutral-500">{client.email || '—'}</td>
                      <td>
                        <span className={`badge ${statusColors[client.status] || 'badge-neutral'}`}>
                          {fmtLabel(client.status)}
                        </span>
                      </td>
                      <td>{client.assignedLawyerName || '—'}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>

            {clients.length === 0 && (
              <div className="text-center py-12">
                <Users className="w-12 h-12 text-neutral-300 mx-auto mb-4" />
                <p className="text-neutral-500">No clients found</p>
                <Link to="/clients/new" className="btn-primary mt-4">
                  Add your first client
                </Link>
              </div>
            )}
          </>
        )}
      </div>

      <div className="flex items-center justify-between">
        <p className="text-sm text-neutral-500">
          Showing {start}–{end} of {total} clients
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
