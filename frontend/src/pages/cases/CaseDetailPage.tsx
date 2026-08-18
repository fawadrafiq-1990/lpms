import { useCallback, useEffect, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { ArrowLeft, Users, Plus, Trash2, Loader2, CheckCircle2 } from 'lucide-react';
import { caseService, type AssignLawyerRequest } from '@/services/caseService';
import { userService } from '@/services/userService';
import { documentService } from '@/services/documentService';
import Modal from '@/components/common/Modal';
import toast from 'react-hot-toast';
import type { Case, CaseLawyer, Lawyer, Document } from '@/types';

const fmtLabel = (s?: string) => (s ? s.charAt(0) + s.slice(1).toLowerCase().replace(/_/g, ' ') : '');

function Info({ label, value }: { label: string; value?: string | number }) {
  return (
    <div>
      <p className="text-xs uppercase tracking-wide text-neutral-400 font-medium">{label}</p>
      <p className="text-neutral-900 mt-0.5">{value ? String(value) : '—'}</p>
    </div>
  );
}

export default function CaseDetailPage() {
  const { caseId } = useParams<{ caseId: string }>();
  const [tab, setTab] = useState<'overview' | 'lawyers' | 'documents'>('overview');

  const [cas, setCase] = useState<Case | null>(null);
  const [lawyers, setLawyers] = useState<CaseLawyer[]>([]);
  const [docs, setDocs] = useState<Document[]>([]);
  const [loading, setLoading] = useState(true);

  // Assign lawyer modal
  const [modalOpen, setModalOpen] = useState(false);
  const [allLawyers, setAllLawyers] = useState<Lawyer[]>([]);
  const [saving, setSaving] = useState(false);
  const [assign, setAssign] = useState<AssignLawyerRequest>({ lawyerId: '', role: 'ASSOCIATE', isPrimary: false });

  const load = useCallback(async () => {
    if (!caseId) return;
    try {
      const [c, cl, d] = await Promise.all([
        caseService.getCase(caseId),
        caseService.getCaseLawyers(caseId).catch(() => ({ success: false, data: [] })),
        documentService.getDocuments({ caseId }).catch(() => ({ success: false, data: null })),
      ]);
      if (c.success && c.data) setCase(c.data);
      else toast.error(c.message || 'Case not found');
      if (Array.isArray(cl.data)) setLawyers(cl.data);
      if (d.success && d.data) setDocs(d.data.content);
    } catch {
      toast.error('Failed to load case');
    } finally {
      setLoading(false);
    }
  }, [caseId]);

  useEffect(() => {
    load();
  }, [load]);

  const openAssignModal = async () => {
    setModalOpen(true);
    setAssign({ lawyerId: '', role: 'ASSOCIATE', isPrimary: false });
    try {
      const res = await userService.getLawyers();
      if (res.success && res.data) setAllLawyers(res.data);
    } catch {
      /* handled */
    }
  };

  const handleAssign = async () => {
    if (!caseId || !assign.lawyerId) {
      toast.error('Select a lawyer');
      return;
    }
    setSaving(true);
    try {
      const res = await caseService.assignLawyer(caseId, assign);
      if (res.success) {
        toast.success('Lawyer assigned');
        setModalOpen(false);
        load();
      } else {
        toast.error(res.message || 'Failed to assign lawyer');
      }
    } catch (e: any) {
      toast.error(e.response?.data?.message || 'Failed to assign lawyer');
    } finally {
      setSaving(false);
    }
  };

  const handleRemove = async (lawyerId: string) => {
    if (!caseId) return;
    try {
      const res = await caseService.removeLawyer(caseId, lawyerId);
      if (res.success) {
        toast.success('Lawyer removed');
        load();
      } else {
        toast.error(res.message || 'Failed to remove lawyer');
      }
    } catch (e: any) {
      toast.error(e.response?.data?.message || 'Failed to remove lawyer');
    }
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center py-20">
        <Loader2 className="w-6 h-6 animate-spin text-neutral-400" />
      </div>
    );
  }

  if (!cas) {
    return (
      <div className="space-y-6 animate-in">
        <div className="flex items-center gap-4">
          <Link to="/cases" className="p-2 hover:bg-neutral-100 rounded-lg"><ArrowLeft className="w-5 h-5" /></Link>
          <h1 className="page-title">Case Details</h1>
        </div>
        <div className="card p-6 text-neutral-500">Case not found.</div>
      </div>
    );
  }

  return (
    <div className="space-y-6 animate-in">
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-4">
          <Link to="/cases" className="p-2 hover:bg-neutral-100 rounded-lg transition-colors"><ArrowLeft className="w-5 h-5" /></Link>
          <div>
            <h1 className="page-title">{cas.title}</h1>
            <p className="page-subtitle">{cas.caseNumber} · {fmtLabel(cas.caseType)}</p>
          </div>
        </div>
        <button className="btn-primary" onClick={openAssignModal}>
          <Plus className="w-4 h-4 mr-2" />
          Assign Lawyer
        </button>
      </div>

      {/* Tabs */}
      <div className="border-b border-neutral-200 flex gap-1">
        {(['overview', 'lawyers', 'documents'] as const).map((t) => (
          <button
            key={t}
            onClick={() => setTab(t)}
            className={`px-4 py-2 text-sm font-medium rounded-t-lg transition-colors ${
              tab === t
                ? 'border-b-2 border-primary-600 text-primary-600'
                : 'text-neutral-500 hover:text-neutral-700'
            }`}
          >
            {t.charAt(0).toUpperCase() + t.slice(1)}
          </button>
        ))}
      </div>

      {tab === 'overview' && (
        <div className="card p-6">
          <h2 className="font-semibold mb-4">Case Information</h2>
          <div className="grid grid-cols-2 md:grid-cols-3 gap-4">
            <Info label="Status" value={fmtLabel(cas.caseStatus)} />
            <Info label="Priority" value={cas.casePriority} />
            <Info label="Client" value={cas.clientName} />
            <Info label="Practice Area" value={cas.practiceArea} />
            <Info label="Lead Lawyer" value={cas.leadLawyerName} />
            <Info label="Confidentiality" value={fmtLabel(cas.confidentialityLevel)} />
            <Info label="Court" value={cas.courtName} />
            <Info label="Opening Date" value={cas.openingDate} />
            <Info label="Expected Closing" value={cas.expectedClosingDate} />
            <Info label="Case Value" value={cas.caseValue ? `AED ${Number(cas.caseValue).toLocaleString()}` : undefined} />
          </div>
          {cas.description && (
            <div className="mt-6">
              <p className="text-xs uppercase tracking-wide text-neutral-400 font-medium mb-2">Description</p>
              <p className="text-neutral-700 whitespace-pre-wrap">{cas.description}</p>
            </div>
          )}
        </div>
      )}

      {tab === 'lawyers' && (
        <div className="card overflow-hidden">
          <div className="flex items-center justify-between p-4 border-b border-neutral-200">
            <h2 className="font-semibold flex items-center gap-2"><Users className="w-4 h-4" /> Assigned Lawyers ({lawyers.length})</h2>
            <button className="btn-secondary btn-sm" onClick={openAssignModal}>
              <Plus className="w-4 h-4 mr-1" /> Assign
            </button>
          </div>
          {lawyers.length === 0 ? (
            <div className="py-12 text-center text-neutral-500">No lawyers assigned to this case yet.</div>
          ) : (
            <table className="table">
              <thead>
                <tr>
                  <th>Lawyer</th>
                  <th>Role</th>
                  <th>Primary</th>
                  <th>Billing Rate</th>
                  <th>Hours Logged</th>
                  <th></th>
                </tr>
              </thead>
              <tbody>
                {lawyers.map((cl) => (
                  <tr key={cl.id}>
                    <td className="font-medium">{cl.lawyerName || cl.lawyerId}</td>
                    <td>{cl.role ? fmtLabel(cl.role) : '—'}</td>
                    <td>{cl.isPrimary ? <CheckCircle2 className="w-4 h-4 text-success-600" /> : '—'}</td>
                    <td>{cl.billingRate ? `AED ${cl.billingRate}` : '—'}</td>
                    <td>{cl.hoursLogged ? `${cl.hoursLogged}h` : '—'}</td>
                    <td className="text-right">
                      <button onClick={() => handleRemove(cl.lawyerId)} className="p-1.5 hover:bg-danger-50 rounded text-danger-600" title="Remove">
                        <Trash2 className="w-4 h-4" />
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>
      )}

      {tab === 'documents' && (
        <div className="card overflow-hidden">
          <div className="p-4 border-b border-neutral-200">
            <h2 className="font-semibold">Documents ({docs.length})</h2>
          </div>
          {docs.length === 0 ? (
            <div className="py-12 text-center text-neutral-500">No documents linked to this case.</div>
          ) : (
            <table className="table">
              <thead>
                <tr>
                  <th>Title</th>
                  <th>Category</th>
                  <th>File</th>
                  <th>Version</th>
                  <th>Uploaded</th>
                </tr>
              </thead>
              <tbody>
                {docs.map((doc) => (
                  <tr key={doc.id}>
                    <td className="font-medium">{doc.title}</td>
                    <td>{fmtLabel(doc.category)}</td>
                    <td className="text-neutral-500">{doc.fileName}</td>
                    <td>v{doc.version}</td>
                    <td className="text-neutral-500">{doc.createdAt ? new Date(doc.createdAt).toLocaleDateString() : '—'}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>
      )}

      {/* Assign Lawyer Modal */}
      <Modal open={modalOpen} onClose={() => setModalOpen(false)} title="Assign Lawyer" size="sm">
        <div className="space-y-4">
          <div>
            <label className="label">Lawyer *</label>
            <select
              className="input"
              value={assign.lawyerId}
              onChange={(e) => setAssign((a) => ({ ...a, lawyerId: e.target.value }))}
            >
              <option value="">Select a lawyer...</option>
              {allLawyers.map((l) => (
                <option key={l.id} value={l.id}>{l.fullName}</option>
              ))}
            </select>
          </div>
          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="label">Role</label>
              <select
                className="input"
                value={assign.role}
                onChange={(e) => setAssign((a) => ({ ...a, role: e.target.value }))}
              >
                <option value="LEAD">Lead</option>
                <option value="ASSOCIATE">Associate</option>
                <option value="SENIOR_ASSOCIATE">Senior Associate</option>
                <option value="CONSULTANT">Consultant</option>
              </select>
            </div>
            <div>
              <label className="label">Billing Rate (AED/hr)</label>
              <input
                type="number"
                min="0"
                className="input"
                value={assign.billingRate ?? ''}
                onChange={(e) => setAssign((a) => ({ ...a, billingRate: e.target.value ? Number(e.target.value) : undefined }))}
              />
            </div>
          </div>
          <label className="flex items-center gap-2 cursor-pointer">
            <input
              type="checkbox"
              checked={!!assign.isPrimary}
              onChange={(e) => setAssign((a) => ({ ...a, isPrimary: e.target.checked }))}
              className="w-4 h-4"
            />
            <span className="text-sm text-neutral-700">Set as primary lawyer</span>
          </label>

          <div className="flex items-center justify-end gap-3 pt-4 border-t border-neutral-200">
            <button className="btn-secondary" onClick={() => setModalOpen(false)}>Cancel</button>
            <button className="btn-primary" onClick={handleAssign} disabled={saving}>
              {saving && <Loader2 className="w-4 h-4 mr-2 animate-spin" />}
              Assign
            </button>
          </div>
        </div>
      </Modal>
    </div>
  );
}
