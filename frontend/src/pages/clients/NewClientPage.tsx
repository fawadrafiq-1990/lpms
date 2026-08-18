import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { ArrowLeft, Loader2 } from 'lucide-react';
import { clientService } from '@/services/clientService';
import { useAuthStore } from '@/store/authStore';
import toast from 'react-hot-toast';
import type { ClientType, ClientStatus, RiskLevel } from '@/types';

const clientTypes: ClientType[] = ['INDIVIDUAL', 'COMPANY', 'GOVERNMENT', 'ORGANIZATION'];
const statuses: ClientStatus[] = ['ACTIVE', 'INACTIVE', 'PENDING', 'BLACKLISTED'];
const riskLevels: RiskLevel[] = ['LOW', 'MEDIUM', 'HIGH'];

export default function NewClientPage() {
  const navigate = useNavigate();
  const { user } = useAuthStore();
  const [saving, setSaving] = useState(false);
  const [form, setForm] = useState({
    clientNumber: `CL-${Math.floor(1000 + Math.random() * 9000)}`,
    clientType: 'INDIVIDUAL' as ClientType,
    firstName: '',
    lastName: '',
    companyName: '',
    email: '',
    phone: '',
    mobile: '',
    nationality: '',
    riskLevel: 'LOW' as RiskLevel,
    vip: false,
    status: 'ACTIVE' as ClientStatus,
    addressLine1: '',
    city: '',
    country: '',
  });

  const set = (field: keyof typeof form, value: string | boolean) =>
    setForm((f) => ({ ...f, [field]: value }));

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setSaving(true);
    try {
      const res = await clientService.createClient({
        ...form,
        assignedLawyerId: user?.lawyerId,
      });
      if (res.success && res.data) {
        toast.success('Client created successfully');
        navigate(`/clients/${res.data.id}`);
      } else {
        toast.error(res.message || 'Failed to create client');
      }
    } catch (error: any) {
      const message = error.response?.data?.message || 'Failed to create client';
      toast.error(message);
    } finally {
      setSaving(false);
    }
  };

  const isCompany = form.clientType === 'COMPANY' || form.clientType === 'ORGANIZATION';

  return (
    <div className="space-y-6 animate-in max-w-3xl">
      <div className="flex items-center gap-4">
        <button
          onClick={() => navigate('/clients')}
          className="p-2 hover:bg-neutral-100 rounded-lg transition-colors"
        >
          <ArrowLeft className="w-5 h-5" />
        </button>
        <div>
          <h1 className="page-title">New Client</h1>
          <p className="page-subtitle">Add a new client to your firm</p>
        </div>
      </div>

      <form onSubmit={handleSubmit} className="card p-6 space-y-6">
        {/* Type & number */}
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div>
            <label className="label">Client Number *</label>
            <input
              className="input"
              value={form.clientNumber}
              onChange={(e) => set('clientNumber', e.target.value)}
              required
            />
          </div>
          <div>
            <label className="label">Client Type *</label>
            <select
              className="input"
              value={form.clientType}
              onChange={(e) => set('clientType', e.target.value)}
            >
              {clientTypes.map((t) => (
                <option key={t} value={t}>
                  {t.charAt(0) + t.slice(1).toLowerCase()}
                </option>
              ))}
            </select>
          </div>
        </div>

        {/* Name fields */}
        {isCompany ? (
          <div>
            <label className="label">Company Name *</label>
            <input
              className="input"
              value={form.companyName}
              onChange={(e) => set('companyName', e.target.value)}
              required
            />
          </div>
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div>
              <label className="label">First Name *</label>
              <input
                className="input"
                value={form.firstName}
                onChange={(e) => set('firstName', e.target.value)}
                required
              />
            </div>
            <div>
              <label className="label">Last Name *</label>
              <input
                className="input"
                value={form.lastName}
                onChange={(e) => set('lastName', e.target.value)}
                required
              />
            </div>
          </div>
        )}

        {/* Contact */}
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div>
            <label className="label">Email</label>
            <input
              type="email"
              className="input"
              value={form.email}
              onChange={(e) => set('email', e.target.value)}
            />
          </div>
          <div>
            <label className="label">Phone</label>
            <input
              className="input"
              value={form.phone}
              onChange={(e) => set('phone', e.target.value)}
            />
          </div>
        </div>

        {/* Risk / status / nationality */}
        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
          <div>
            <label className="label">Nationality</label>
            <input
              className="input"
              value={form.nationality}
              onChange={(e) => set('nationality', e.target.value)}
            />
          </div>
          <div>
            <label className="label">Risk Level</label>
            <select
              className="input"
              value={form.riskLevel}
              onChange={(e) => set('riskLevel', e.target.value)}
            >
              {riskLevels.map((r) => (
                <option key={r} value={r}>
                  {r.charAt(0) + r.slice(1).toLowerCase()}
                </option>
              ))}
            </select>
          </div>
          <div>
            <label className="label">Status</label>
            <select
              className="input"
              value={form.status}
              onChange={(e) => set('status', e.target.value)}
            >
              {statuses.map((s) => (
                <option key={s} value={s}>
                  {s.charAt(0) + s.slice(1).toLowerCase()}
                </option>
              ))}
            </select>
          </div>
        </div>

        {/* Address */}
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div>
            <label className="label">Address</label>
            <input
              className="input"
              value={form.addressLine1}
              onChange={(e) => set('addressLine1', e.target.value)}
            />
          </div>
          <div>
            <label className="label">City</label>
            <input
              className="input"
              value={form.city}
              onChange={(e) => set('city', e.target.value)}
            />
          </div>
        </div>

        <div className="flex items-center justify-end gap-3 pt-4 border-t border-neutral-200">
          <button
            type="button"
            onClick={() => navigate('/clients')}
            className="btn-secondary"
          >
            Cancel
          </button>
          <button type="submit" disabled={saving} className="btn-primary">
            {saving && <Loader2 className="w-4 h-4 mr-2 animate-spin" />}
            Create Client
          </button>
        </div>
      </form>
    </div>
  );
}
