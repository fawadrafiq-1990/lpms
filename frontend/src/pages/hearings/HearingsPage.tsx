import { useCallback, useEffect, useState } from 'react';
import { Plus, Gavel, Loader2, Trash2 } from 'lucide-react';
import { hearingService } from '@/services/hearingService';
import { caseService } from '@/services/caseService';
import Modal from '@/components/common/Modal';
import toast from 'react-hot-toast';
import type { Hearing, Case } from '@/types';

const PAGE_SIZE = 10;
const hearingTypes = ['INITIAL', 'ARGUMENT', 'EVIDENCE', 'WITNESS', 'CROSS_EXAMINATION', 'FINAL_ARGUMENT', 'JUDGMENT', 'APPEAL', 'OTHER'];
const statuses = ['SCHEDULED', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED', 'POSTPONED', 'ADJOURNED'];

const fmtLabel = (s?: string) => (s ? s.charAt(0) + s.slice(1).toLowerCase().replace(/_/g, ' ') : '');

const statusColors: Record<string, string> = {
  SCHEDULED: 'badge-primary',
  IN_PROGRESS: 'badge-warning',
  COMPLETED: 'badge-success',
  CANCELLED: 'badge-danger',
  POSTPONED: 'badge-warning',
  ADJOURNED: 'badge-warning',
};

export default function HearingsPage() {
  const [hearings, setHearings] = useState<Hearing[]>([]);
  const [total, setTotal] = useState(0);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [loading, setLoading] = useState(true);
  const [statusFilter, setStatusFilter] = useState('');
  const [cases, setCases] = useState<Case[]>([]);

  const [modalOpen, setModalOpen] = useState(false);
  const [saving, setSaving] = useState(false);
  const [form, setForm] = useState({
    caseId: '',
    hearingType: 'INITIAL',
    scheduledDate: '',
    scheduledTime: '09:00',
    durationMinutes: '60',
    courtName: 'Primary Court',
    location: '',
    agenda: '',
  });

  const load = useCallback(async () => {
    setLoading(true);
    try {
      const res = await hearingService.getHearings({ page, size: PAGE_SIZE, status: statusFilter || undefined });
      if (res.success && res.data) {
        setHearings(res.data.content);
        setTotal(res.data.totalElements);
        setTotalPages(res.data.totalPages);
      } else {
        setHearings([]);
      }
    } catch {
      setHearings([]);
    } finally {
      setLoading(false);
    }
  }, [page, statusFilter]);

  useEffect(() => {
    load();
  }, [load]);

  useEffect(() => {
    caseService.getCases({ size: 100 }).then((res) => {
      if (res.success && res.data) setCases(res.data.content);
    }).catch(() => {});
  }, []);

  const openModal = () => {
    setForm({ caseId: '', hearingType: 'INITIAL', scheduledDate: '', scheduledTime: '09:00', durationMinutes: '60', courtName: 'Primary Court', location: '', agenda: '' });
    setModalOpen(true);
  };

  const handleCreate = async () => {
    if (!form.caseId) { toast.error('Select a case'); return; }
    if (!form.scheduledDate) { toast.error('Select a date'); return; }
    setSaving(true);
    try {
      const c = cases.find((x) => x.id === form.caseId);
      const res = await hearingService.createHearing({
        caseId: form.caseId,
        caseNumber: c?.caseNumber,
        caseTitle: c?.title,
        courtId: 'court-001',
        courtName: form.courtName || 'Primary Court',
        hearingType: form.hearingType,
        scheduledDate: form.scheduledDate,
        scheduledTime: form.scheduledTime,
        durationMinutes: Number(form.durationMinutes) || 60,
        status: 'SCHEDULED',
        location: form.location || undefined,
        agenda: form.agenda || undefined,
      });
      if (res.success) {
        toast.success('Hearing scheduled');
        setModalOpen(false);
        load();
      } else {
        toast.error(res.message || 'Failed to schedule hearing');
      }
    } catch (e: any) {
      toast.error(e.response?.data?.message || 'Failed to schedule hearing');
    } finally {
      setSaving(false);
    }
  };

  const handleDelete = async (id: string) => {
    try {
      const res = await hearingService.deleteHearing(id);
      if (res.success) { toast.success('Hearing deleted'); load(); }
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
          <h1 className="page-title">Hearings</h1>
          <p className="page-subtitle">Court hearing schedule</p>
        </div>
        <button className="btn-primary" onClick={openModal}>
          <Plus className="w-4 h-4 mr-2" />Schedule Hearing
        </button>
      </div>

      <div className="card p-4">
        <select
          value={statusFilter}
          onChange={(e) => { setStatusFilter(e.target.value); setPage(0); }}
          className="input w-auto"
        >
          <option value="">All Statuses</option>
          {statuses.map((s) => <option key={s} value={s}>{fmtLabel(s)}</option>)}
        </select>
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
                    <th>Hearing No.</th>
                    <th>Case</th>
                    <th>Type</th>
                    <th>Date</th>
                    <th>Time</th>
                    <th>Court</th>
                    <th>Status</th>
                    <th></th>
                  </tr>
                </thead>
                <tbody>
                  {hearings.map((h) => (
                    <tr key={h.id}>
                      <td className="font-medium">{h.hearingNumber}</td>
                      <td className="max-w-xs truncate">{h.caseTitle || h.caseNumber || h.caseId}</td>
                      <td>{fmtLabel(h.hearingType)}</td>
                      <td>{h.scheduledDate ? new Date(h.scheduledDate).toLocaleDateString() : '—'}</td>
                      <td>{h.scheduledTime}</td>
                      <td className="text-neutral-500">{h.courtName || h.location || '—'}</td>
                      <td><span className={`badge ${statusColors[h.status] || 'badge-neutral'}`}>{fmtLabel(h.status)}</span></td>
                      <td className="text-right">
                        <button onClick={() => handleDelete(h.id)} className="p-1.5 hover:bg-danger-50 rounded text-danger-600" title="Delete">
                          <Trash2 className="w-4 h-4" />
                        </button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
            {hearings.length === 0 && (
              <div className="text-center py-12">
                <Gavel className="w-12 h-12 text-neutral-300 mx-auto mb-4" />
                <p className="text-neutral-500">No hearings scheduled</p>
              </div>
            )}
          </>
        )}
      </div>

      <div className="flex items-center justify-between">
        <p className="text-sm text-neutral-500">Showing {start}–{end} of {total} hearings</p>
        <div className="flex gap-2">
          <button className="btn-secondary btn-sm" disabled={page === 0 || loading} onClick={() => setPage((p) => p - 1)}>Previous</button>
          <button className="btn-secondary btn-sm" disabled={page >= totalPages - 1 || loading} onClick={() => setPage((p) => p + 1)}>Next</button>
        </div>
      </div>

      <Modal open={modalOpen} onClose={() => setModalOpen(false)} title="Schedule Hearing" size="md">
        <div className="space-y-4">
          <div>
            <label className="label">Case *</label>
            <select className="input" value={form.caseId} onChange={(e) => setForm((f) => ({ ...f, caseId: e.target.value }))}>
              <option value="">Select a case...</option>
              {cases.map((c) => <option key={c.id} value={c.id}>{c.caseNumber} — {c.title}</option>)}
            </select>
          </div>
          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="label">Type</label>
              <select className="input" value={form.hearingType} onChange={(e) => setForm((f) => ({ ...f, hearingType: e.target.value }))}>
                {hearingTypes.map((t) => <option key={t} value={t}>{fmtLabel(t)}</option>)}
              </select>
            </div>
            <div>
              <label className="label">Duration (min)</label>
              <input type="number" min="5" className="input" value={form.durationMinutes} onChange={(e) => setForm((f) => ({ ...f, durationMinutes: e.target.value }))} />
            </div>
          </div>
          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="label">Date *</label>
              <input type="date" className="input" value={form.scheduledDate} onChange={(e) => setForm((f) => ({ ...f, scheduledDate: e.target.value }))} />
            </div>
            <div>
              <label className="label">Time</label>
              <input type="time" className="input" value={form.scheduledTime} onChange={(e) => setForm((f) => ({ ...f, scheduledTime: e.target.value }))} />
            </div>
          </div>
          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="label">Court Name</label>
              <input className="input" value={form.courtName} onChange={(e) => setForm((f) => ({ ...f, courtName: e.target.value }))} />
            </div>
            <div>
              <label className="label">Location</label>
              <input className="input" value={form.location} onChange={(e) => setForm((f) => ({ ...f, location: e.target.value }))} placeholder="Courtroom / address" />
            </div>
          </div>
          <div>
            <label className="label">Agenda</label>
            <textarea className="input" rows={2} value={form.agenda} onChange={(e) => setForm((f) => ({ ...f, agenda: e.target.value }))} />
          </div>
          <div className="flex items-center justify-end gap-3 pt-4 border-t border-neutral-200">
            <button className="btn-secondary" onClick={() => setModalOpen(false)}>Cancel</button>
            <button className="btn-primary" onClick={handleCreate} disabled={saving}>
              {saving && <Loader2 className="w-4 h-4 mr-2 animate-spin" />}Schedule
            </button>
          </div>
        </div>
      </Modal>
    </div>
  );
}
