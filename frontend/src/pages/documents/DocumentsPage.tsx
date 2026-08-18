import { useCallback, useEffect, useState } from 'react';
import { Plus, FileText, Loader2, Trash2 } from 'lucide-react';
import { documentService } from '@/services/documentService';
import { caseService } from '@/services/caseService';
import Modal from '@/components/common/Modal';
import toast from 'react-hot-toast';
import type { Document, Case } from '@/types';

const PAGE_SIZE = 10;
const categories = ['CONTRACT', 'COURT_ORDER', 'PLEADING', 'EVIDENCE', 'IDENTITY', 'LEGAL_NOTICE', 'AGREEMENT', 'EMAIL', 'REPORT', 'JUDGMENT', 'OTHER'];
const fmtLabel = (s?: string) => (s ? s.charAt(0) + s.slice(1).toLowerCase().replace(/_/g, ' ') : '');

const formatBytes = (n?: number) => {
  if (!n) return '—';
  if (n < 1024) return `${n} B`;
  if (n < 1024 * 1024) return `${(n / 1024).toFixed(1)} KB`;
  return `${(n / (1024 * 1024)).toFixed(1)} MB`;
};

export default function DocumentsPage() {
  const [docs, setDocs] = useState<Document[]>([]);
  const [total, setTotal] = useState(0);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [loading, setLoading] = useState(true);
  const [cases, setCases] = useState<Case[]>([]);

  const [modalOpen, setModalOpen] = useState(false);
  const [saving, setSaving] = useState(false);
  const [form, setForm] = useState({ title: '', category: 'OTHER', caseId: '', description: '' });

  const load = useCallback(async () => {
    setLoading(true);
    try {
      const res = await documentService.getDocuments({ page, size: PAGE_SIZE });
      if (res.success && res.data) {
        setDocs(res.data.content);
        setTotal(res.data.totalElements);
        setTotalPages(res.data.totalPages);
      } else {
        setDocs([]);
      }
    } catch {
      setDocs([]);
    } finally {
      setLoading(false);
    }
  }, [page]);

  useEffect(() => {
    load();
  }, [load]);

  useEffect(() => {
    caseService.getCases({ size: 100 }).then((res) => {
      if (res.success && res.data) setCases(res.data.content);
    }).catch(() => {});
  }, []);

  const openModal = () => {
    setForm({ title: '', category: 'OTHER', caseId: '', description: '' });
    setModalOpen(true);
  };

  const handleCreate = async () => {
    if (!form.title.trim()) {
      toast.error('Title is required');
      return;
    }
    setSaving(true);
    try {
      const docNumber = `DOC-${Date.now()}`;
      const created = await documentService.createDocument({
        documentNumber: docNumber,
        title: form.title,
        category: form.category,
        caseId: form.caseId || undefined,
        description: form.description || undefined,
      });
      if (created.success && created.data) {
        toast.success('Document created');
        setModalOpen(false);
        setPage(0);
        load();
      } else {
        toast.error(created.message || 'Failed to create document');
      }
    } catch (e: any) {
      toast.error(e.response?.data?.message || 'Failed to create document');
    } finally {
      setSaving(false);
    }
  };

  const handleDelete = async (id: string) => {
    try {
      const res = await documentService.deleteDocument(id);
      if (res.success) {
        toast.success('Document deleted');
        load();
      } else {
        toast.error(res.message || 'Failed to delete');
      }
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
          <h1 className="page-title">Documents</h1>
          <p className="page-subtitle">Manage legal documents</p>
        </div>
        <button className="btn-primary" onClick={openModal}>
          <Plus className="w-4 h-4 mr-2" />
          Upload Document
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
                    <th>Title</th>
                    <th>Doc No.</th>
                    <th>Category</th>
                    <th>Case</th>
                    <th>Size</th>
                    <th>Version</th>
                    <th>Created</th>
                    <th></th>
                  </tr>
                </thead>
                <tbody>
                  {docs.map((doc) => (
                    <tr key={doc.id}>
                      <td className="font-medium">{doc.title}</td>
                      <td className="text-neutral-500">{doc.documentNumber}</td>
                      <td>{fmtLabel(doc.category)}</td>
                      <td className="text-neutral-500">{doc.caseNumber || doc.caseId || '—'}</td>
                      <td>{formatBytes(doc.fileSize)}</td>
                      <td>v{doc.version}</td>
                      <td className="text-neutral-500">{doc.createdAt ? new Date(doc.createdAt).toLocaleDateString() : '—'}</td>
                      <td className="text-right">
                        <button onClick={() => handleDelete(doc.id)} className="p-1.5 hover:bg-danger-50 rounded text-danger-600" title="Delete">
                          <Trash2 className="w-4 h-4" />
                        </button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>

            {docs.length === 0 && (
              <div className="text-center py-12">
                <FileText className="w-12 h-12 text-neutral-300 mx-auto mb-4" />
                <p className="text-neutral-500">No documents yet</p>
              </div>
            )}
          </>
        )}
      </div>

      <div className="flex items-center justify-between">
        <p className="text-sm text-neutral-500">Showing {start}–{end} of {total} documents</p>
        <div className="flex gap-2">
          <button className="btn-secondary btn-sm" disabled={page === 0 || loading} onClick={() => setPage((p) => p - 1)}>Previous</button>
          <button className="btn-secondary btn-sm" disabled={page >= totalPages - 1 || loading} onClick={() => setPage((p) => p + 1)}>Next</button>
        </div>
      </div>

      <Modal open={modalOpen} onClose={() => setModalOpen(false)} title="Upload Document" size="md">
        <div className="space-y-4">
          <div>
            <label className="label">Title *</label>
            <input className="input" value={form.title} onChange={(e) => setForm((f) => ({ ...f, title: e.target.value }))} placeholder="Document title" />
          </div>
          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="label">Category</label>
              <select className="input" value={form.category} onChange={(e) => setForm((f) => ({ ...f, category: e.target.value }))}>
                {categories.map((c) => (
                  <option key={c} value={c}>{fmtLabel(c)}</option>
                ))}
              </select>
            </div>
            <div>
              <label className="label">Linked Case</label>
              <select className="input" value={form.caseId} onChange={(e) => setForm((f) => ({ ...f, caseId: e.target.value }))}>
                <option value="">None</option>
                {cases.map((c) => (
                  <option key={c.id} value={c.id}>{c.caseNumber} — {c.title}</option>
                ))}
              </select>
            </div>
          </div>
          <div>
            <label className="label">Description</label>
            <textarea className="input" rows={3} value={form.description} onChange={(e) => setForm((f) => ({ ...f, description: e.target.value }))} />
          </div>

          <div className="flex items-center justify-end gap-3 pt-4 border-t border-neutral-200">
            <button className="btn-secondary" onClick={() => setModalOpen(false)}>Cancel</button>
            <button className="btn-primary" onClick={handleCreate} disabled={saving}>
              {saving && <Loader2 className="w-4 h-4 mr-2 animate-spin" />}
              Create Document
            </button>
          </div>
        </div>
      </Modal>
    </div>
  );
}
