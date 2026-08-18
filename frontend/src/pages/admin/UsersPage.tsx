import { useCallback, useEffect, useState } from 'react';
import { Plus, Shield, Loader2 } from 'lucide-react';
import { userService, type CreateUserRequest } from '@/services/userService';
import Modal from '@/components/common/Modal';
import toast from 'react-hot-toast';
import type { User } from '@/types';

const PAGE_SIZE = 10;

const roleOptions: { value: string; label: string }[] = [
  { value: 'ROLE-LAWYER', label: 'Lawyer' },
  { value: 'ROLE-SENIOR-LAWYER', label: 'Senior Lawyer' },
  { value: 'ROLE-MANAGING-PARTNER', label: 'Managing Partner' },
  { value: 'ROLE-PARALEGAL', label: 'Paralegal' },
  { value: 'ROLE-LEGAL-ASSISTANT', label: 'Legal Assistant' },
  { value: 'ROLE-RECEPTIONIST', label: 'Receptionist' },
  { value: 'ROLE-ACCOUNTANT', label: 'Accountant' },
  { value: 'ROLE-FIRM-ADMIN', label: 'Firm Administrator' },
];

const fmtLabel = (s?: string) => (s ? s.charAt(0) + s.slice(1).toLowerCase().replace(/[-_]/g, ' ') : '');

const statusColors: Record<string, string> = {
  ACTIVE: 'badge-success',
  INACTIVE: 'badge-neutral',
  SUSPENDED: 'badge-danger',
  PENDING: 'badge-warning',
  LOCKED: 'badge-danger',
};

export default function UsersPage() {
  const [users, setUsers] = useState<User[]>([]);
  const [total, setTotal] = useState(0);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [loading, setLoading] = useState(true);

  const [modalOpen, setModalOpen] = useState(false);
  const [saving, setSaving] = useState(false);
  const [form, setForm] = useState({
    email: '',
    password: '',
    firstName: '',
    lastName: '',
    phone: '',
    roleId: 'ROLE-LAWYER',
    barNumber: '',
    hourlyRate: '',
  });

  const load = useCallback(async () => {
    setLoading(true);
    try {
      const res = await userService.getUsers({ page, size: PAGE_SIZE });
      if (res.success && res.data) {
        setUsers(res.data.content);
        setTotal(res.data.totalElements);
        setTotalPages(res.data.totalPages);
      } else {
        setUsers([]);
      }
    } catch {
      setUsers([]);
    } finally {
      setLoading(false);
    }
  }, [page]);

  useEffect(() => {
    load();
  }, [load]);

  const openModal = () => {
    setForm({ email: '', password: '', firstName: '', lastName: '', phone: '', roleId: 'ROLE-LAWYER', barNumber: '', hourlyRate: '' });
    setModalOpen(true);
  };

  const handleCreate = async () => {
    if (!form.email || !form.password || !form.firstName || !form.lastName) {
      toast.error('Email, password, and name are required');
      return;
    }
    setSaving(true);
    try {
      const payload: CreateUserRequest = {
        email: form.email,
        password: form.password,
        firstName: form.firstName,
        lastName: form.lastName,
        phone: form.phone || undefined,
        roleId: form.roleId,
        barNumber: form.barNumber || undefined,
        hourlyRate: form.hourlyRate ? Number(form.hourlyRate) : undefined,
      };
      const res = await userService.createUser(payload);
      if (res.success) {
        toast.success('User created');
        setModalOpen(false);
        setPage(0);
        load();
      } else {
        toast.error(res.message || 'Failed to create user');
      }
    } catch (e: any) {
      toast.error(e.response?.data?.message || 'Failed to create user');
    } finally {
      setSaving(false);
    }
  };

  const start = total === 0 ? 0 : page * PAGE_SIZE + 1;
  const end = Math.min(total, (page + 1) * PAGE_SIZE);

  return (
    <div className="space-y-6 animate-in">
      <div className="page-header">
        <div>
          <h1 className="page-title">User Management</h1>
          <p className="page-subtitle">Manage system users and roles</p>
        </div>
        <button className="btn-primary" onClick={openModal}>
          <Plus className="w-4 h-4 mr-2" />
          Add User
        </button>
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
                    <th>Name</th>
                    <th>Email</th>
                    <th>Phone</th>
                    <th>Roles</th>
                    <th>Status</th>
                    <th>Created</th>
                  </tr>
                </thead>
                <tbody>
                  {users.map((u) => (
                    <tr key={u.id}>
                      <td className="font-medium">{u.fullName}</td>
                      <td className="text-neutral-500">{u.email}</td>
                      <td>{u.phone || '—'}</td>
                      <td>
                        <div className="flex flex-wrap gap-1">
                          {(u.roles || []).map((r) => (
                            <span key={r} className="badge badge-primary">{fmtLabel(r)}</span>
                          ))}
                        </div>
                      </td>
                      <td>
                        <span className={`badge ${statusColors[u.status] || 'badge-neutral'}`}>{fmtLabel(u.status)}</span>
                      </td>
                      <td className="text-neutral-500">{u.createdAt ? new Date(u.createdAt).toLocaleDateString() : '—'}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>

            {users.length === 0 && (
              <div className="text-center py-12">
                <Shield className="w-12 h-12 text-neutral-300 mx-auto mb-4" />
                <p className="text-neutral-500">No users found</p>
              </div>
            )}
          </>
        )}
      </div>

      <div className="flex items-center justify-between">
        <p className="text-sm text-neutral-500">Showing {start}–{end} of {total} users</p>
        <div className="flex gap-2">
          <button className="btn-secondary btn-sm" disabled={page === 0 || loading} onClick={() => setPage((p) => p - 1)}>Previous</button>
          <button className="btn-secondary btn-sm" disabled={page >= totalPages - 1 || loading} onClick={() => setPage((p) => p + 1)}>Next</button>
        </div>
      </div>

      <Modal open={modalOpen} onClose={() => setModalOpen(false)} title="Add User" size="md">
        <div className="space-y-4">
          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="label">First Name *</label>
              <input className="input" value={form.firstName} onChange={(e) => setForm((f) => ({ ...f, firstName: e.target.value }))} />
            </div>
            <div>
              <label className="label">Last Name *</label>
              <input className="input" value={form.lastName} onChange={(e) => setForm((f) => ({ ...f, lastName: e.target.value }))} />
            </div>
          </div>
          <div>
            <label className="label">Email *</label>
            <input type="email" className="input" value={form.email} onChange={(e) => setForm((f) => ({ ...f, email: e.target.value }))} />
          </div>
          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="label">Password *</label>
              <input type="password" className="input" value={form.password} onChange={(e) => setForm((f) => ({ ...f, password: e.target.value }))} />
            </div>
            <div>
              <label className="label">Phone</label>
              <input className="input" value={form.phone} onChange={(e) => setForm((f) => ({ ...f, phone: e.target.value }))} />
            </div>
          </div>
          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="label">Role</label>
              <select className="input" value={form.roleId} onChange={(e) => setForm((f) => ({ ...f, roleId: e.target.value }))}>
                {roleOptions.map((r) => (
                  <option key={r.value} value={r.value}>{r.label}</option>
                ))}
              </select>
            </div>
            <div>
              <label className="label">Bar Number</label>
              <input className="input" value={form.barNumber} onChange={(e) => setForm((f) => ({ ...f, barNumber: e.target.value }))} />
            </div>
          </div>
          <div>
            <label className="label">Hourly Rate (AED)</label>
            <input type="number" min="0" className="input" value={form.hourlyRate} onChange={(e) => setForm((f) => ({ ...f, hourlyRate: e.target.value }))} />
          </div>

          <div className="flex items-center justify-end gap-3 pt-4 border-t border-neutral-200">
            <button className="btn-secondary" onClick={() => setModalOpen(false)}>Cancel</button>
            <button className="btn-primary" onClick={handleCreate} disabled={saving}>
              {saving && <Loader2 className="w-4 h-4 mr-2 animate-spin" />}
              Create User
            </button>
          </div>
        </div>
      </Modal>
    </div>
  );
}
