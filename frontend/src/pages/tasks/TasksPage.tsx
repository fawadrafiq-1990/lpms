import { useCallback, useEffect, useState } from 'react';
import { Plus, CheckSquare, Loader2, Trash2 } from 'lucide-react';
import { taskService } from '@/services/taskService';
import { userService } from '@/services/userService';
import { caseService } from '@/services/caseService';
import { useAuthStore } from '@/store/authStore';
import Modal from '@/components/common/Modal';
import toast from 'react-hot-toast';
import type { Task, Case, User } from '@/types';

const PAGE_SIZE = 10;
const priorities = ['LOW', 'MEDIUM', 'HIGH', 'URGENT', 'CRITICAL'];
const statuses = ['TODO', 'IN_PROGRESS', 'BLOCKED', 'REVIEW', 'COMPLETED', 'CANCELLED'];

const fmtLabel = (s?: string) => (s ? s.charAt(0) + s.slice(1).toLowerCase().replace(/_/g, ' ') : '');

const priorityColors: Record<string, string> = {
  LOW: 'badge-neutral',
  MEDIUM: 'badge-warning',
  HIGH: 'badge-warning',
  URGENT: 'badge-danger',
  CRITICAL: 'badge-danger',
};

const statusColors: Record<string, string> = {
  TODO: 'badge-neutral',
  IN_PROGRESS: 'badge-primary',
  BLOCKED: 'badge-danger',
  REVIEW: 'badge-warning',
  COMPLETED: 'badge-success',
  CANCELLED: 'badge-neutral',
};

export default function TasksPage() {
  const { user } = useAuthStore();
  const [tasks, setTasks] = useState<Task[]>([]);
  const [total, setTotal] = useState(0);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [loading, setLoading] = useState(true);
  const [assignee, setAssignee] = useState(user?.id || '');
  const [statusFilter, setStatusFilter] = useState('');
  const [users, setUsers] = useState<User[]>([]);
  const [cases, setCases] = useState<Case[]>([]);

  const [modalOpen, setModalOpen] = useState(false);
  const [saving, setSaving] = useState(false);
  const [form, setForm] = useState({ title: '', description: '', caseId: '', priority: 'MEDIUM', status: 'TODO', dueDate: '', assignedTo: '' });

  const load = useCallback(async () => {
    if (!assignee) { setLoading(false); return; }
    setLoading(true);
    try {
      const res = await taskService.getTasks({ assignedTo: assignee, page, size: PAGE_SIZE, status: statusFilter || undefined });
      if (res.success && res.data) {
        setTasks(res.data.content);
        setTotal(res.data.totalElements);
        setTotalPages(res.data.totalPages);
      } else {
        setTasks([]);
      }
    } catch {
      setTasks([]);
    } finally {
      setLoading(false);
    }
  }, [assignee, page, statusFilter]);

  useEffect(() => {
    load();
  }, [load]);

  useEffect(() => {
    userService.getUsers({ size: 100 }).then((res) => {
      if (res.success && res.data) setUsers(res.data.content);
    }).catch(() => {});
    caseService.getCases({ size: 100 }).then((res) => {
      if (res.success && res.data) setCases(res.data.content);
    }).catch(() => {});
  }, []);

  const openModal = () => {
    setForm({ title: '', description: '', caseId: '', priority: 'MEDIUM', status: 'TODO', dueDate: '', assignedTo: assignee || user?.id || '' });
    setModalOpen(true);
  };

  const handleCreate = async () => {
    if (!form.title.trim()) { toast.error('Title is required'); return; }
    if (!form.assignedTo) { toast.error('Select an assignee'); return; }
    setSaving(true);
    try {
      const c = cases.find((x) => x.id === form.caseId);
      const res = await taskService.createTask({
        title: form.title,
        description: form.description || undefined,
        caseId: form.caseId || undefined,
        caseNumber: c?.caseNumber,
        priority: form.priority,
        status: form.status,
        assignedTo: form.assignedTo,
        assignedToName: users.find((u) => u.id === form.assignedTo)?.fullName,
        dueDate: form.dueDate || undefined,
      });
      if (res.success) {
        toast.success('Task created');
        setModalOpen(false);
        setAssignee(form.assignedTo);
        setPage(0);
        load();
      } else {
        toast.error(res.message || 'Failed to create task');
      }
    } catch (e: any) {
      toast.error(e.response?.data?.message || 'Failed to create task');
    } finally {
      setSaving(false);
    }
  };

  const handleDelete = async (id: string) => {
    try {
      const res = await taskService.deleteTask(id);
      if (res.success) { toast.success('Task deleted'); load(); }
      else toast.error(res.message || 'Failed to delete');
    } catch (e: any) {
      toast.error(e.response?.data?.message || 'Failed to delete');
    }
  };

  const start = total === 0 ? 0 : page * PAGE_SIZE + 1;
  const end = Math.min(total, (page + 1) * PAGE_SIZE);

  return (
    <div className="space-y-6 animate-in">
      <div className="page-header">
        <div>
          <h1 className="page-title">Tasks</h1>
          <p className="page-subtitle">Track work items and deadlines</p>
        </div>
        <button className="btn-primary" onClick={openModal}>
          <Plus className="w-4 h-4 mr-2" />New Task
        </button>
      </div>

      <div className="card p-4">
        <div className="flex flex-col md:flex-row gap-4">
          <select value={assignee} onChange={(e) => { setAssignee(e.target.value); setPage(0); }} className="input w-auto">
            <option value="">Select assignee…</option>
            {users.map((u) => <option key={u.id} value={u.id}>{u.fullName}</option>)}
          </select>
          <select value={statusFilter} onChange={(e) => { setStatusFilter(e.target.value); setPage(0); }} className="input w-auto">
            <option value="">All Statuses</option>
            {statuses.map((s) => <option key={s} value={s}>{fmtLabel(s)}</option>)}
          </select>
        </div>
      </div>

      <div className="card overflow-hidden">
        {loading ? (
          <div className="flex items-center justify-center py-16"><Loader2 className="w-6 h-6 animate-spin text-neutral-400" /></div>
        ) : (
          <>
            <div className="overflow-x-auto">
              <table className="table">
                <thead>
                  <tr>
                    <th>Task No.</th>
                    <th>Title</th>
                    <th>Case</th>
                    <th>Priority</th>
                    <th>Status</th>
                    <th>Due</th>
                    <th>Progress</th>
                    <th></th>
                  </tr>
                </thead>
                <tbody>
                  {tasks.map((t) => (
                    <tr key={t.id}>
                      <td className="text-neutral-500">{t.taskNumber}</td>
                      <td className="font-medium">{t.title}</td>
                      <td className="text-neutral-500">{t.caseNumber || '—'}</td>
                      <td><span className={`badge ${priorityColors[t.priority] || 'badge-neutral'}`}>{t.priority}</span></td>
                      <td><span className={`badge ${statusColors[t.status] || 'badge-neutral'}`}>{fmtLabel(t.status)}</span></td>
                      <td className={t.dueDate && new Date(t.dueDate) < new Date() ? 'text-danger-600' : 'text-neutral-500'}>
                        {t.dueDate ? new Date(t.dueDate).toLocaleDateString() : '—'}
                      </td>
                      <td>
                        <div className="flex items-center gap-2">
                          <div className="w-16 h-1.5 bg-neutral-200 rounded-full overflow-hidden">
                            <div className="h-full bg-primary-500" style={{ width: `${t.progress || 0}%` }} />
                          </div>
                          <span className="text-xs text-neutral-500">{t.progress || 0}%</span>
                        </div>
                      </td>
                      <td className="text-right">
                        <button onClick={() => handleDelete(t.id)} className="p-1.5 hover:bg-danger-50 rounded text-danger-600" title="Delete">
                          <Trash2 className="w-4 h-4" />
                        </button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
            {tasks.length === 0 && (
              <div className="text-center py-12">
                <CheckSquare className="w-12 h-12 text-neutral-300 mx-auto mb-4" />
                <p className="text-neutral-500">No tasks found for this assignee</p>
              </div>
            )}
          </>
        )}
      </div>

      <div className="flex items-center justify-between">
        <p className="text-sm text-neutral-500">Showing {start}–{end} of {total} tasks</p>
        <div className="flex gap-2">
          <button className="btn-secondary btn-sm" disabled={page === 0 || loading} onClick={() => setPage((p) => p - 1)}>Previous</button>
          <button className="btn-secondary btn-sm" disabled={page >= totalPages - 1 || loading} onClick={() => setPage((p) => p + 1)}>Next</button>
        </div>
      </div>

      <Modal open={modalOpen} onClose={() => setModalOpen(false)} title="New Task" size="md">
        <div className="space-y-4">
          <div>
            <label className="label">Title *</label>
            <input className="input" value={form.title} onChange={(e) => setForm((f) => ({ ...f, title: e.target.value }))} />
          </div>
          <div>
            <label className="label">Description</label>
            <textarea className="input" rows={2} value={form.description} onChange={(e) => setForm((f) => ({ ...f, description: e.target.value }))} />
          </div>
          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="label">Assignee *</label>
              <select className="input" value={form.assignedTo} onChange={(e) => setForm((f) => ({ ...f, assignedTo: e.target.value }))}>
                <option value="">Select…</option>
                {users.map((u) => <option key={u.id} value={u.id}>{u.fullName}</option>)}
              </select>
            </div>
            <div>
              <label className="label">Linked Case</label>
              <select className="input" value={form.caseId} onChange={(e) => setForm((f) => ({ ...f, caseId: e.target.value }))}>
                <option value="">None</option>
                {cases.map((c) => <option key={c.id} value={c.id}>{c.caseNumber} — {c.title}</option>)}
              </select>
            </div>
          </div>
          <div className="grid grid-cols-3 gap-4">
            <div>
              <label className="label">Priority</label>
              <select className="input" value={form.priority} onChange={(e) => setForm((f) => ({ ...f, priority: e.target.value }))}>
                {priorities.map((p) => <option key={p} value={p}>{p}</option>)}
              </select>
            </div>
            <div>
              <label className="label">Status</label>
              <select className="input" value={form.status} onChange={(e) => setForm((f) => ({ ...f, status: e.target.value }))}>
                {statuses.map((s) => <option key={s} value={s}>{fmtLabel(s)}</option>)}
              </select>
            </div>
            <div>
              <label className="label">Due Date</label>
              <input type="date" className="input" value={form.dueDate} onChange={(e) => setForm((f) => ({ ...f, dueDate: e.target.value }))} />
            </div>
          </div>
          <div className="flex items-center justify-end gap-3 pt-4 border-t border-neutral-200">
            <button className="btn-secondary" onClick={() => setModalOpen(false)}>Cancel</button>
            <button className="btn-primary" onClick={handleCreate} disabled={saving}>
              {saving && <Loader2 className="w-4 h-4 mr-2 animate-spin" />}Create Task
            </button>
          </div>
        </div>
      </Modal>
    </div>
  );
}
