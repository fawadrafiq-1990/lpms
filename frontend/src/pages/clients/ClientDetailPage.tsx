import { useEffect, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { ArrowLeft, Loader2 } from 'lucide-react';
import { clientService } from '@/services/clientService';
import { caseService } from '@/services/caseService';
import toast from 'react-hot-toast';
import type { Client, Case } from '@/types';

const fmtLabel = (s?: string) => (s ? s.charAt(0) + s.slice(1).toLowerCase().replace(/_/g, ' ') : '');

function Field({ label, value }: { label: string; value?: string | number | boolean }) {
  return (
    <div>
      <p className="text-xs uppercase tracking-wide text-neutral-400 font-medium">{label}</p>
      <p className="text-neutral-900 mt-0.5">
        {typeof value === 'boolean' ? (value ? 'Yes' : 'No') : value ? String(value) : '—'}
      </p>
    </div>
  );
}

export default function ClientDetailPage() {
  const { clientId } = useParams<{ clientId: string }>();
  const [client, setClient] = useState<Client | null>(null);
  const [cases, setCases] = useState<Case[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (!clientId) return;
    (async () => {
      setLoading(true);
      try {
        const [c, cs] = await Promise.all([
          clientService.getClient(clientId),
          caseService.getCasesByClient(clientId).catch(() => ({ success: false, data: [] })),
        ]);
        if (c.success && c.data) setClient(c.data);
        else toast.error(c.message || 'Client not found');
        if (Array.isArray(cs.data)) setCases(cs.data);
      } catch {
        toast.error('Failed to load client');
      } finally {
        setLoading(false);
      }
    })();
  }, [clientId]);

  if (loading) {
    return (
      <div className="flex items-center justify-center py-20">
        <Loader2 className="w-6 h-6 animate-spin text-neutral-400" />
      </div>
    );
  }

  if (!client) {
    return (
      <div className="space-y-6 animate-in">
        <div className="flex items-center gap-4">
          <Link to="/clients" className="p-2 hover:bg-neutral-100 rounded-lg"><ArrowLeft className="w-5 h-5" /></Link>
          <h1 className="page-title">Client Details</h1>
        </div>
        <div className="card p-6 text-neutral-500">Client not found.</div>
      </div>
    );
  }

  return (
    <div className="space-y-6 animate-in">
      <div className="flex items-center gap-4">
        <Link to="/clients" className="p-2 hover:bg-neutral-100 rounded-lg transition-colors"><ArrowLeft className="w-5 h-5" /></Link>
        <div>
          <h1 className="page-title">{client.companyName || `${client.firstName ?? ''} ${client.lastName ?? ''}`.trim()}</h1>
          <p className="page-subtitle">{client.clientNumber} · {fmtLabel(client.clientType)} client</p>
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        <div className="card p-6">
          <h2 className="font-semibold mb-4">Profile</h2>
          <div className="grid grid-cols-2 gap-4">
            <Field label="Status" value={fmtLabel(client.status)} />
            <Field label="Risk Level" value={fmtLabel(client.riskLevel)} />
            <Field label="VIP" value={client.vip} />
            <Field label="Nationality" value={client.nationality} />
            <Field label="Email" value={client.email} />
            <Field label="Phone" value={client.phone} />
            <Field label="City" value={client.city} />
            <Field label="Country" value={client.country} />
            <Field label="Assigned Lawyer" value={client.assignedLawyerName} />
          </div>
        </div>

        <div className="lg:col-span-2 card p-6">
          <h2 className="font-semibold mb-4">Cases ({cases.length})</h2>
          {cases.length === 0 ? (
            <p className="text-neutral-500">No cases for this client yet.</p>
          ) : (
            <div className="overflow-x-auto">
              <table className="table">
                <thead>
                  <tr>
                    <th>Case No.</th>
                    <th>Title</th>
                    <th>Type</th>
                    <th>Status</th>
                    <th>Lead Lawyer</th>
                  </tr>
                </thead>
                <tbody>
                  {cases.map((c) => (
                    <tr key={c.id}>
                      <td>
                        <Link to={`/cases/${c.id}`} className="font-medium text-primary-600 hover:text-primary-700">
                          {c.caseNumber}
                        </Link>
                      </td>
                      <td className="max-w-xs truncate">{c.title}</td>
                      <td>{fmtLabel(c.caseType)}</td>
                      <td>{fmtLabel(c.caseStatus)}</td>
                      <td>{c.leadLawyerName || '—'}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
