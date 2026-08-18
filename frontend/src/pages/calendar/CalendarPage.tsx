import { useCallback, useEffect, useState } from 'react';
import { ChevronLeft, ChevronRight, Plus, Loader2 } from 'lucide-react';
import { calendarService } from '@/services/calendarService';
import { hearingService } from '@/services/hearingService';
import { caseService } from '@/services/caseService';
import Modal from '@/components/common/Modal';
import toast from 'react-hot-toast';
import type { CalendarEvent, Hearing, Case } from '@/types';

const eventTypes = ['MEETING', 'DEADLINE', 'COURT_DATE', 'APPOINTMENT', 'TASK', 'OTHER'];
const fmtLabel = (s?: string) => (s ? s.charAt(0) + s.slice(1).toLowerCase().replace(/_/g, ' ') : '');

const typeColors: Record<string, string> = {
  HEARING: 'bg-warning-100 text-warning-700 border-warning-300',
  MEETING: 'bg-primary-100 text-primary-700 border-primary-300',
  DEADLINE: 'bg-danger-100 text-danger-700 border-danger-300',
  COURT_DATE: 'bg-warning-100 text-warning-700 border-warning-300',
  APPOINTMENT: 'bg-success-100 text-success-700 border-success-300',
  TASK: 'bg-neutral-100 text-neutral-700 border-neutral-300',
  OTHER: 'bg-neutral-100 text-neutral-700 border-neutral-300',
};

const WEEKDAYS = ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'];

interface DayItem {
  date: Date;
  label: string;
  inMonth: boolean;
  isToday: boolean;
}

export default function CalendarPage() {
  const [cursor, setCursor] = useState(() => {
    const d = new Date();
    return new Date(d.getFullYear(), d.getMonth(), 1);
  });
  const [events, setEvents] = useState<CalendarEvent[]>([]);
  const [hearings, setHearings] = useState<Hearing[]>([]);
  const [cases, setCases] = useState<Case[]>([]);
  const [loading, setLoading] = useState(true);

  const [modalOpen, setModalOpen] = useState(false);
  const [saving, setSaving] = useState(false);
  const [form, setForm] = useState({ title: '', eventType: 'MEETING', startDate: '', startTime: '09:00', endDate: '', endTime: '10:00', allDay: false, location: '', caseId: '', description: '' });

  // Build the month grid (6 weeks = 42 cells)
  const grid: DayItem[] = [];
  const firstOfMonth = new Date(cursor.getFullYear(), cursor.getMonth(), 1);
  const startDay = firstOfMonth.getDay();
  const gridStart = new Date(firstOfMonth);
  gridStart.setDate(firstOfMonth.getDate() - startDay);
  const today = new Date();
  today.setHours(0, 0, 0, 0);
  for (let i = 0; i < 42; i++) {
    const d = new Date(gridStart);
    d.setDate(gridStart.getDate() + i);
    grid.push({
      date: d,
      label: String(d.getDate()),
      inMonth: d.getMonth() === cursor.getMonth(),
      isToday: d.getTime() === today.getTime(),
    });
  }

  // Month range for fetching
  const rangeStart = new Date(grid[0].date);
  rangeStart.setHours(0, 0, 0, 0);
  const rangeEnd = new Date(grid[41].date);
  rangeEnd.setHours(23, 59, 59, 999);

  const load = useCallback(async () => {
    setLoading(true);
    try {
      const [evRes, hearRes] = await Promise.all([
        calendarService.getEvents({
          start: rangeStart.toISOString(),
          end: rangeEnd.toISOString(),
        }).catch(() => ({ success: false, data: [] as CalendarEvent[] })),
        hearingService.getHearings({ size: 100 }).catch(() => ({ success: false, data: { content: [] as Hearing[] } })),
      ]);
      if (evRes.success && Array.isArray(evRes.data)) setEvents(evRes.data);
      if (hearRes.success && hearRes.data) setHearings(hearRes.data.content);
    } catch {
      /* handled */
    } finally {
      setLoading(false);
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [cursor]);

  useEffect(() => {
    load();
  }, [load]);

  useEffect(() => {
    caseService.getCases({ size: 100 }).then((res) => { if (res.success && res.data) setCases(res.data.content); }).catch(() => {});
  }, []);

  // Index items by yyyy-mm-dd
  const sameDay = (a: Date, b: Date) =>
    a.getFullYear() === b.getFullYear() && a.getMonth() === b.getMonth() && a.getDate() === b.getDate();

  const itemsForDay = (day: Date) => {
    const evs = events
      .filter((e) => e.startDate && sameDay(new Date(e.startDate), day))
      .map((e) => ({ id: e.id, kind: e.eventType, title: e.title, time: e.allDay ? 'All day' : (e.startDate ? new Date(e.startDate).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }) : '') }));
    const hrs = hearings
      .filter((h) => h.scheduledDate && sameDay(new Date(h.scheduledDate), day))
      .map((h) => ({ id: h.id, kind: 'HEARING', title: h.caseTitle || h.caseNumber || 'Hearing', time: h.scheduledTime || '' }));
    return [...hrs, ...evs];
  };

  const openModal = (day?: Date) => {
    const d = day || new Date();
    const ds = d.toISOString().slice(0, 10);
    setForm({ title: '', eventType: 'MEETING', startDate: ds, startTime: '09:00', endDate: ds, endTime: '10:00', allDay: false, location: '', caseId: '', description: '' });
    setModalOpen(true);
  };

  const handleCreate = async () => {
    if (!form.title.trim()) { toast.error('Title is required'); return; }
    if (!form.startDate) { toast.error('Start date is required'); return; }
    setSaving(true);
    try {
      const start = form.allDay ? `${form.startDate}T00:00:00` : `${form.startDate}T${form.startTime || '09:00'}:00`;
      const end = form.allDay ? `${form.endDate || form.startDate}T23:59:00` : `${form.endDate || form.startDate}T${form.endTime || '10:00'}:00`;
      const res = await calendarService.createEvent({
        title: form.title,
        description: form.description || undefined,
        startDate: start,
        endDate: end,
        allDay: form.allDay,
        eventType: form.eventType,
        caseId: form.caseId || undefined,
        location: form.location || undefined,
      });
      if (res.success) {
        toast.success('Event created');
        setModalOpen(false);
        load();
      } else {
        toast.error(res.message || 'Failed to create event');
      }
    } catch (e: any) {
      toast.error(e.response?.data?.message || 'Failed to create event');
    } finally {
      setSaving(false);
    }
  };

  const handleDelete = async (id: string) => {
    try {
      const res = await calendarService.deleteEvent(id);
      if (res.success) { toast.success('Event deleted'); load(); }
      else toast.error(res.message || 'Failed to delete');
    } catch (e: any) {
      toast.error(e.response?.data?.message || 'Failed to delete');
    }
  };

  const monthLabel = cursor.toLocaleDateString('en-US', { month: 'long', year: 'numeric' });

  return (
    <div className="space-y-6 animate-in">
      <div className="page-header">
        <div>
          <h1 className="page-title">Calendar</h1>
          <p className="page-subtitle">Schedule and events</p>
        </div>
        <button className="btn-primary" onClick={() => openModal()}>
          <Plus className="w-4 h-4 mr-2" />New Event
        </button>
      </div>

      <div className="card p-6">
        <div className="flex items-center justify-between mb-4">
          <h2 className="text-lg font-semibold text-neutral-900">{monthLabel}</h2>
          <div className="flex items-center gap-2">
            <button onClick={() => setCursor((c) => new Date(c.getFullYear(), c.getMonth() - 1, 1))} className="p-2 hover:bg-neutral-100 rounded-lg">
              <ChevronLeft className="w-5 h-5" />
            </button>
            <button onClick={() => { const d = new Date(); setCursor(new Date(d.getFullYear(), d.getMonth(), 1)); }} className="btn-secondary btn-sm">Today</button>
            <button onClick={() => setCursor((c) => new Date(c.getFullYear(), c.getMonth() + 1, 1))} className="p-2 hover:bg-neutral-100 rounded-lg">
              <ChevronRight className="w-5 h-5" />
            </button>
          </div>
        </div>

        {loading ? (
          <div className="flex items-center justify-center py-16"><Loader2 className="w-6 h-6 animate-spin text-neutral-400" /></div>
        ) : (
          <>
            {/* Weekday header */}
            <div className="grid grid-cols-7 mb-2">
              {WEEKDAYS.map((d) => (
                <div key={d} className="text-center text-xs font-semibold text-neutral-400 uppercase py-2">{d}</div>
              ))}
            </div>
            {/* Day grid */}
            <div className="grid grid-cols-7 gap-1">
              {grid.map((day, i) => {
                const items = itemsForDay(day.date);
                return (
                  <div
                    key={i}
                    onClick={() => openModal(day.date)}
                    className={`min-h-[96px] p-1.5 rounded-lg border cursor-pointer transition-colors ${
                      day.isToday ? 'border-primary-400 bg-primary-50' : 'border-neutral-200 hover:bg-neutral-50'
                    } ${!day.inMonth ? 'opacity-40' : ''}`}
                  >
                    <div className={`text-xs font-medium mb-1 ${day.isToday ? 'text-primary-700' : 'text-neutral-600'}`}>
                      {day.label}
                    </div>
                    <div className="space-y-1">
                      {items.slice(0, 3).map((it) => (
                        <div
                          key={it.id}
                          onClick={(e) => { e.stopPropagation(); if (it.kind !== 'HEARING') handleDelete(it.id); }}
                          className={`text-[10px] px-1 py-0.5 rounded border truncate ${typeColors[it.kind] || typeColors.OTHER}`}
                          title={`${it.time ? it.time + ' ' : ''}${it.title}`}
                        >
                          {it.time ? `${it.time} ` : ''}{it.title}
                        </div>
                      ))}
                      {items.length > 3 && (
                        <div className="text-[10px] text-neutral-400 px-1">+{items.length - 3} more</div>
                      )}
                    </div>
                  </div>
                );
              })}
            </div>

            {/* Legend */}
            <div className="flex flex-wrap items-center gap-3 mt-4 pt-4 border-t border-neutral-200">
              {['HEARING', 'MEETING', 'DEADLINE', 'APPOINTMENT', 'TASK'].map((t) => (
                <div key={t} className="flex items-center gap-1.5">
                  <span className={`w-3 h-3 rounded border ${typeColors[t]}`} />
                  <span className="text-xs text-neutral-500">{fmtLabel(t)}</span>
                </div>
              ))}
            </div>
          </>
        )}
      </div>

      <Modal open={modalOpen} onClose={() => setModalOpen(false)} title="New Event" size="md">
        <div className="space-y-4">
          <div>
            <label className="label">Title *</label>
            <input className="input" value={form.title} onChange={(e) => setForm((f) => ({ ...f, title: e.target.value }))} />
          </div>
          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="label">Type</label>
              <select className="input" value={form.eventType} onChange={(e) => setForm((f) => ({ ...f, eventType: e.target.value }))}>
                {eventTypes.map((t) => <option key={t} value={t}>{fmtLabel(t)}</option>)}
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
          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="label">Start Date *</label>
              <input type="date" className="input" value={form.startDate} onChange={(e) => setForm((f) => ({ ...f, startDate: e.target.value }))} />
            </div>
            <div>
              <label className="label">End Date</label>
              <input type="date" className="input" value={form.endDate} onChange={(e) => setForm((f) => ({ ...f, endDate: e.target.value }))} />
            </div>
          </div>
          <label className="flex items-center gap-2 cursor-pointer">
            <input type="checkbox" checked={form.allDay} onChange={(e) => setForm((f) => ({ ...f, allDay: e.target.checked }))} className="w-4 h-4" />
            <span className="text-sm text-neutral-700">All day event</span>
          </label>
          {!form.allDay && (
            <div className="grid grid-cols-2 gap-4">
              <div>
                <label className="label">Start Time</label>
                <input type="time" className="input" value={form.startTime} onChange={(e) => setForm((f) => ({ ...f, startTime: e.target.value }))} />
              </div>
              <div>
                <label className="label">End Time</label>
                <input type="time" className="input" value={form.endTime} onChange={(e) => setForm((f) => ({ ...f, endTime: e.target.value }))} />
              </div>
            </div>
          )}
          <div>
            <label className="label">Location</label>
            <input className="input" value={form.location} onChange={(e) => setForm((f) => ({ ...f, location: e.target.value }))} />
          </div>
          <div>
            <label className="label">Description</label>
            <textarea className="input" rows={2} value={form.description} onChange={(e) => setForm((f) => ({ ...f, description: e.target.value }))} />
          </div>
          <div className="flex items-center justify-end gap-3 pt-4 border-t border-neutral-200">
            <button className="btn-secondary" onClick={() => setModalOpen(false)}>Cancel</button>
            <button className="btn-primary" onClick={handleCreate} disabled={saving}>
              {saving && <Loader2 className="w-4 h-4 mr-2 animate-spin" />}Create Event
            </button>
          </div>
        </div>
      </Modal>
    </div>
  );
}
