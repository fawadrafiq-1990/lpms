import { useCallback, useEffect, useState } from 'react';
import { Plus, DollarSign, Loader2, Trash2, Send, CreditCard, Ban } from 'lucide-react';
import { invoiceService } from '@/services/invoiceService';
import { caseService } from '@/services/caseService';
import { clientService } from '@/services/clientService';
import Modal from '@/components/common/Modal';
import toast from 'react-hot-toast';
import type { Invoice, Case, Client } from '@/types';

const PAGE_SIZE = 10;
const statuses = ['DRAFT', 'ISSUED', 'PARTIALLY_PAID', 'PAID', 'OVERDUE', 'CANCELLED'];
const fmtLabel = (s?: string) => (s ? s.charAt(0) + s.slice(1).toLowerCase().replace(/_/g, ' ') : '');

const statusColors: Record<string, string> = {
  DRAFT: 'badge-neutral',
  ISSUED: 'badge-primary',
  PARTIALLY_PAID: 'badge-warning',
  PAID: 'badge-success',
  OVERDUE: 'badge-danger',
  CANCELLED: 'badge-neutral',
};

const money = (n?: number, cur = 'PKR') => `${cur} ${Number(n || 0).toLocaleString()}`;

export default function BillingPage() {
  const [invoices, setInvoices] = useState<Invoice[]>([]);
  const [total, setTotal] = useState(0);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [loading, setLoading] = useState(true);
  const [statusFilter, setStatusFilter] = useState('');
  const [outstanding, setOutstanding] = useState(0);

  const [cases, setCases] = useState<Case[]>([]);
  const [clients, setClients] = useState<Client[]>([]);

  const [modalOpen, setModalOpen] = useState(false);
  const [saving, setSaving] = useState(false);
  const [form, setForm] = useState({ caseId: '', clientId: '', subtotal: '', taxAmount: '', dueDate: '', notes: '' });

  const [payFor, setPayFor] = useState<Invoice | null>(null);
  const [payAmount, setPayAmount] = useState('');
  const [paying, setPaying] = useState(false);

  const load = useCallback(async () => {
    setLoading(true);
    try {
      const res = await invoiceService.getInvoices({ page, size: PAGE_SIZE, status: statusFilter || undefined });
      if (res.success && res.data) {
        setInvoices(res.data.content);
        setTotal(res.data.totalElements);
        setTotalPages(res.data.totalPages);
      } else {
        setInvoices([]);
      }
    } catch {
      setInvoices([]);
    } finally {
      setLoading(false);
    }
  }, [page, statusFilter]);

  useEffect(() => {
    load();
    invoiceService.getOutstanding().then((r) => { if (r.success) setOutstanding(r.data); }).catch(() => {});
  }, [load]);

  useEffect(() => {
    caseService.getCases({ size: 100 }).then((res) => { if (res.success && res.data) setCases(res.data.content); }).catch(() => {});
    clientService.getActiveClients().then((res) => { if (res.success && res.data) setClients(res.data); }).catch(() => {});
  }, []);

  const openModal = () => {
    setForm({ caseId: '', clientId: '', subtotal: '', taxAmount: '', dueDate: '', notes: '' });
    setModalOpen(true);
  };

  const handleCreate = async () => {
    if (!form.caseId || !form.clientId) { toast.error('Select a case and client'); return; }
    setSaving(true);
    try {
      const c = cases.find((x) => x.id === form.caseId);
      const cl = clients.find((x) => x.id === form.clientId);
      const subtotal = Number(form.subtotal) || 0;
      const tax = Number(form.taxAmount) || 0;
      const res = await invoiceService.createInvoice({
        caseId: form.caseId,
        caseNumber: c?.caseNumber,
        clientId: form.clientId,
        clientName: cl?.companyName || `${cl?.firstName ?? ''} ${cl?.lastName ?? ''}`.trim(),
        invoiceDate: new Date().toISOString().slice(0, 10),
        dueDate: form.dueDate || undefined,
        subtotal,
        taxAmount: tax,
        total: subtotal + tax,
        currency: 'PKR',
        notes: form.notes || undefined,
      });
      if (res.success) {
        toast.success('Invoice created');
        setModalOpen(false);
        load();
      } else {
        toast.error(res.message || 'Failed to create invoice');
      }
    } catch (e: any) {
      toast.error(e.response?.data?.message || 'Failed to create invoice');
    } finally {
      setSaving(false);
    }
  };

  const handleIssue = async (id: string) => {
    try {
      const res = await invoiceService.issueInvoice(id);
      if (res.success) { toast.success('Invoice issued'); load(); }
      else toast.error(res.message || 'Failed to issue');
    } catch (e: any) { toast.error(e.response?.data?.message || 'Failed to issue'); }
  };

  const handleCancel = async (id: string) => {
    try {
      const res = await invoiceService.cancelInvoice(id);
      if (res.success) { toast.success('Invoice cancelled'); load(); }
      else toast.error(res.message || 'Failed to cancel');
    } catch (e: any) { toast.error(e.response?.data?.message || 'Failed to cancel'); }
  };

  const handlePayment = async () => {
    if (!payFor) return;
    const amt = Number(payAmount);
    if (!amt || amt <= 0) { toast.error('Enter a valid amount'); return; }
    setPaying(true);
    try {
      const res = await invoiceService.recordPayment(payFor.id, amt);
      if (res.success) { toast.success('Payment recorded'); setPayFor(null); setPayAmount(''); load(); }
      else toast.error(res.message || 'Failed to record payment');
    } catch (e: any) {
      toast.error(e.response?.data?.message || 'Failed to record payment');
    } finally {
      setPaying(false);
    }
  };

  const handleDelete = async (id: string) => {
    try {
      const res = await invoiceService.deleteInvoice(id);
      if (res.success) { toast.success('Invoice deleted'); load(); }
      else toast.error(res.message || 'Failed to delete');
    } catch (e: any) { toast.error(e.response?.data?.message || 'Failed to delete'); }
  };

  const start = total === 0 ? 0 : page * PAGE_SIZE + 1;
  const end = Math.min(total, (page + 1) * PAGE_SIZE);

  return (
    <div className="space-y-6 animate-in">
      <div className="page-header">
        <div>
          <h1 className="page-title">Billing</h1>
          <p className="page-subtitle">Invoices and payments</p>
        </div>
        <button className="btn-primary" onClick={openModal}>
          <Plus className="w-4 h-4 mr-2" />New Invoice
        </button>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        <div className="card p-6">
          <p className="text-sm text-neutral-500 font-medium">Total Outstanding</p>
          <p className="text-3xl font-bold text-neutral-900 mt-1">{money(outstanding)}</p>
        </div>
        <div className="card p-6">
          <p className="text-sm text-neutral-500 font-medium">Total Invoices</p>
          <p className="text-3xl font-bold text-neutral-900 mt-1">{total}</p>
        </div>
        <div className="card p-6">
          <p className="text-sm text-neutral-500 font-medium">Currency</p>
          <p className="text-3xl font-bold text-neutral-900 mt-1">PKR</p>
        </div>
      </div>

      <div className="card p-4">
        <select value={statusFilter} onChange={(e) => { setStatusFilter(e.target.value); setPage(0); }} className="input w-auto">
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
                    <th>Invoice No.</th>
                    <th>Client</th>
                    <th>Case</th>
                    <th>Issued</th>
                    <th>Due</th>
                    <th>Total</th>
                    <th>Outstanding</th>
                    <th>Status</th>
                    <th></th>
                  </tr>
                </thead>
                <tbody>
                  {invoices.map((inv) => (
                    <tr key={inv.id}>
                      <td className="font-medium">{inv.invoiceNumber}</td>
                      <td>{inv.clientName || '—'}</td>
                      <td className="text-neutral-500">{inv.caseNumber || '—'}</td>
                      <td className="text-neutral-500">{inv.invoiceDate ? new Date(inv.invoiceDate).toLocaleDateString() : '—'}</td>
                      <td className="text-neutral-500">{inv.dueDate ? new Date(inv.dueDate).toLocaleDateString() : '—'}</td>
                      <td>{money(inv.total, inv.currency)}</td>
                      <td className={Number(inv.amountOutstanding) > 0 ? 'text-danger-600 font-medium' : 'text-neutral-500'}>
                        {money(inv.amountOutstanding, inv.currency)}
                      </td>
                      <td><span className={`badge ${statusColors[inv.status] || 'badge-neutral'}`}>{fmtLabel(inv.status)}</span></td>
                      <td>
                        <div className="flex items-center gap-1 justify-end">
                          {inv.status === 'DRAFT' && (
                            <button onClick={() => handleIssue(inv.id)} className="p-1.5 hover:bg-primary-50 rounded text-primary-600" title="Issue"><Send className="w-4 h-4" /></button>
                          )}
                          {(inv.status === 'ISSUED' || inv.status === 'PARTIALLY_PAID' || inv.status === 'OVERDUE') && (
                            <button onClick={() => { setPayFor(inv); setPayAmount(String(inv.amountOutstanding)); }} className="p-1.5 hover:bg-success-50 rounded text-success-600" title="Record Payment"><CreditCard className="w-4 h-4" /></button>
                          )}
                          {inv.status !== 'PAID' && inv.status !== 'CANCELLED' && (
                            <button onClick={() => handleCancel(inv.id)} className="p-1.5 hover:bg-neutral-100 rounded text-neutral-500" title="Cancel"><Ban className="w-4 h-4" /></button>
                          )}
                          <button onClick={() => handleDelete(inv.id)} className="p-1.5 hover:bg-danger-50 rounded text-danger-600" title="Delete"><Trash2 className="w-4 h-4" /></button>
                        </div>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
            {invoices.length === 0 && (
              <div className="text-center py-12">
                <DollarSign className="w-12 h-12 text-neutral-300 mx-auto mb-4" />
                <p className="text-neutral-500">No invoices yet</p>
              </div>
            )}
          </>
        )}
      </div>

      <div className="flex items-center justify-between">
        <p className="text-sm text-neutral-500">Showing {start}–{end} of {total} invoices</p>
        <div className="flex gap-2">
          <button className="btn-secondary btn-sm" disabled={page === 0 || loading} onClick={() => setPage((p) => p - 1)}>Previous</button>
          <button className="btn-secondary btn-sm" disabled={page >= totalPages - 1 || loading} onClick={() => setPage((p) => p + 1)}>Next</button>
        </div>
      </div>

      {/* New Invoice Modal */}
      <Modal open={modalOpen} onClose={() => setModalOpen(false)} title="New Invoice" size="md">
        <div className="space-y-4">
          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="label">Case *</label>
              <select className="input" value={form.caseId} onChange={(e) => setForm((f) => ({ ...f, caseId: e.target.value }))}>
                <option value="">Select…</option>
                {cases.map((c) => <option key={c.id} value={c.id}>{c.caseNumber} — {c.title}</option>)}
              </select>
            </div>
            <div>
              <label className="label">Client *</label>
              <select className="input" value={form.clientId} onChange={(e) => setForm((f) => ({ ...f, clientId: e.target.value }))}>
                <option value="">Select…</option>
                {clients.map((c) => <option key={c.id} value={c.id}>{c.companyName || `${c.firstName ?? ''} ${c.lastName ?? ''}`.trim()}</option>)}
              </select>
            </div>
          </div>
          <div className="grid grid-cols-3 gap-4">
            <div>
              <label className="label">Subtotal (PKR)</label>
              <input type="number" min="0" className="input" value={form.subtotal} onChange={(e) => setForm((f) => ({ ...f, subtotal: e.target.value }))} />
            </div>
            <div>
              <label className="label">Tax (PKR)</label>
              <input type="number" min="0" className="input" value={form.taxAmount} onChange={(e) => setForm((f) => ({ ...f, taxAmount: e.target.value }))} />
            </div>
            <div>
              <label className="label">Due Date</label>
              <input type="date" className="input" value={form.dueDate} onChange={(e) => setForm((f) => ({ ...f, dueDate: e.target.value }))} />
            </div>
          </div>
          <div>
            <label className="label">Notes</label>
            <textarea className="input" rows={2} value={form.notes} onChange={(e) => setForm((f) => ({ ...f, notes: e.target.value }))} />
          </div>
          <div className="flex items-center justify-end gap-3 pt-4 border-t border-neutral-200">
            <button className="btn-secondary" onClick={() => setModalOpen(false)}>Cancel</button>
            <button className="btn-primary" onClick={handleCreate} disabled={saving}>
              {saving && <Loader2 className="w-4 h-4 mr-2 animate-spin" />}Create Invoice
            </button>
          </div>
        </div>
      </Modal>

      {/* Payment Modal */}
      <Modal open={!!payFor} onClose={() => setPayFor(null)} title="Record Payment" size="sm">
        {payFor && (
          <div className="space-y-4">
            <div className="bg-neutral-50 rounded-lg p-4 text-sm">
              <p className="font-medium">{payFor.invoiceNumber} — {payFor.clientName}</p>
              <p className="text-neutral-500 mt-1">Outstanding: {money(payFor.amountOutstanding, payFor.currency)}</p>
            </div>
            <div>
              <label className="label">Payment Amount (PKR) *</label>
              <input type="number" min="0" className="input" value={payAmount} onChange={(e) => setPayAmount(e.target.value)} />
            </div>
            <div className="flex items-center justify-end gap-3 pt-4 border-t border-neutral-200">
              <button className="btn-secondary" onClick={() => setPayFor(null)}>Cancel</button>
              <button className="btn-primary" onClick={handlePayment} disabled={paying}>
                {paying && <Loader2 className="w-4 h-4 mr-2 animate-spin" />}Record Payment
              </button>
            </div>
          </div>
        )}
      </Modal>
    </div>
  );
}
