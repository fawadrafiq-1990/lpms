import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { ArrowLeft, Loader2 } from 'lucide-react';
import { caseService } from '@/services/caseService';
import { clientService } from '@/services/clientService';
import { userService } from '@/services/userService';
import toast from 'react-hot-toast';
import type { Client, Lawyer } from '@/types';

const caseTypes = ['LITIGATION', 'CORPORATE', 'FAMILY', 'CIVIL', 'CRIMINAL', 'COMMERCIAL', 'REAL_ESTATE', 'LABOR', 'ADMINISTRATIVE', 'OTHER'];
const caseStatuses = ['NEW', 'CONFLICT_CHECK', 'OPEN', 'PREPARATION', 'FILED', 'IN_PROGRESS', 'HEARING', 'JUDGMENT', 'APPEAL', 'SETTLEMENT', 'CLOSED', 'ARCHIVED'];
const priorities = ['LOW', 'MEDIUM', 'HIGH', 'URGENT', 'CRITICAL'];

const fmt = (s?: string) => (s ? s.charAt(0) + s.slice(1).toLowerCase().replace(/_/g, ' ') : s);

export default function NewCasePage() {
  const navigate = useNavigate();
  const [saving, setSaving] = useState(false);
  const [loading, setLoading] = useState(true);
  const [clients, setClients] = useState<Client[]>([]);
  const [lawyers, setLawyers] = useState<Lawyer[]>([]);
  const [form, setForm] = useState({
    caseNumber: `CASE-${Math.floor(10000 + Math.random() * 89999)}`,
    title: '',
    description: '',
    clientId: '',
    caseType: 'LITIGATION',
    caseStatus: 'NEW',
    casePriority: 'MEDIUM',
    practiceArea: '',
    leadLawyerId: '',
    caseValue: '',
  });

  const set = (field: keyof typeof form, value: string) =>
    setForm((f) => ({ ...f, [field]: value }));

  useEffect(() => {
    (async () => {
      try {
        const [c, l] = await Promise.all([
          clientService.getActiveClients(),
          userService.getLawyers(),
        ]);
        if (c.success && c.data) setClients(c.data);
        if (l.success && l.data) setLawyers(l.data);
      } catch {
        /* handled by interceptor */
      } finally {
        setLoading(false);
      }
    })();
  }, []);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!form.clientId) {
      toast.error('Please select a client');
      return;
    }
    setSaving(true);
    try {
      const client = clients.find((c) => c.id === form.clientId);
      const lead = lawyers.find((l) => l.id === form.leadLawyerId);
      const res = await caseService.createCase({
        caseNumber: form.caseNumber,
        title: form.title,
        description: form.description || undefined,
        clientId: form.clientId,
        clientName: client?.companyName || `${client?.firstName ?? ''} ${client?.lastName ?? ''}`.trim() || undefined,
        caseType: form.caseType,
        caseStatus: form.caseStatus as any,
        casePriority: form.casePriority as any,
        practiceArea: form.practiceArea || undefined,
        leadLawyerId: form.leadLawyerId || undefined,
        leadLawyerName: lead?.fullName || undefined,
        caseValue: form.caseValue ? Number(form.caseValue) : undefined,
        confidentialityLevel: 'CONFIDENTIAL',
      } as any);
      if (res.success && res.data) {
        toast.success('Case created successfully');
        navigate(`/cases/${res.data.id}`);
      } else {
        toast.error(res.message || 'Failed to create case');
      }
    } catch (error: any) {
      toast.error(error.response?.data?.message || 'Failed to create case');
    } finally {
      setSaving(false);
    }
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center py-20">
        <Loader2 className="w-6 h-6 animate-spin text-neutral-400" />
      </div>
    );
  }

  return (
    <div className="space-y-6 animate-in max-w-3xl">
      <div className="flex items-center gap-4">
        <button
          onClick={() => navigate('/cases')}
          className="p-2 hover:bg-neutral-100 rounded-lg transition-colors"
        >
          <ArrowLeft className="w-5 h-5" />
        </button>
        <div>
          <h1 className="page-title">New Case</h1>
          <p className="page-subtitle">Open a new matter</p>
        </div>
      </div>

      <form onSubmit={handleSubmit} className="card p-6 space-y-6">
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div>
            <label className="label">Case Number *</label>
            <input className="input" value={form.caseNumber} onChange={(e) => set('caseNumber', e.target.value)} required />
          </div>
          <div>
            <label className="label">Client *</label>
            <select className="input" value={form.clientId} onChange={(e) => set('clientId', e.target.value)} required>
              <option value="">Select a client...</option>
              {clients.map((c) => (
                <option key={c.id} value={c.id}>
                  {c.companyName || `${c.firstName ?? ''} ${c.lastName ?? ''}`.trim()} — {c.clientNumber}
                </option>
              ))}
            </select>
          </div>
        </div>

        <div>
          <label className="label">Title *</label>
          <input className="input" value={form.title} onChange={(e) => set('title', e.target.value)} required placeholder="e.g. Smith vs. Johnson Corp" />
        </div>

        <div>
          <label className="label">Description</label>
          <textarea
            className="input"
            rows={3}
            value={form.description}
            onChange={(e) => set('description', e.target.value)}
            placeholder="Brief summary of the matter"
          />
        </div>

        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
          <div>
            <label className="label">Case Type *</label>
            <select className="input" value={form.caseType} onChange={(e) => set('caseType', e.target.value)}>
              {caseTypes.map((t) => (
                <option key={t} value={t}>{fmt(t)}</option>
              ))}
            </select>
          </div>
          <div>
            <label className="label">Status</label>
            <select className="input" value={form.caseStatus} onChange={(e) => set('caseStatus', e.target.value)}>
              {caseStatuses.map((s) => (
                <option key={s} value={s}>{fmt(s)}</option>
              ))}
            </select>
          </div>
          <div>
            <label className="label">Priority</label>
            <select className="input" value={form.casePriority} onChange={(e) => set('casePriority', e.target.value)}>
              {priorities.map((p) => (
                <option key={p} value={p}>{fmt(p)}</option>
              ))}
            </select>
          </div>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
          <div>
            <label className="label">Practice Area</label>
            <input className="input" value={form.practiceArea} onChange={(e) => set('practiceArea', e.target.value)} placeholder="e.g. Litigation" />
          </div>
          <div>
            <label className="label">Lead Lawyer</label>
            <select className="input" value={form.leadLawyerId} onChange={(e) => set('leadLawyerId', e.target.value)}>
              <option value="">Unassigned</option>
              {lawyers.map((l) => (
                <option key={l.id} value={l.id}>{l.fullName}</option>
              ))}
            </select>
          </div>
          <div>
            <label className="label">Case Value (AED)</label>
            <input className="input" type="number" min="0" value={form.caseValue} onChange={(e) => set('caseValue', e.target.value)} placeholder="0" />
          </div>
        </div>

        <div className="flex items-center justify-end gap-3 pt-4 border-t border-neutral-200">
          <button type="button" onClick={() => navigate('/cases')} className="btn-secondary">
            Cancel
          </button>
          <button type="submit" disabled={saving} className="btn-primary">
            {saving && <Loader2 className="w-4 h-4 mr-2 animate-spin" />}
            Create Case
          </button>
        </div>
      </form>
    </div>
  );
}
