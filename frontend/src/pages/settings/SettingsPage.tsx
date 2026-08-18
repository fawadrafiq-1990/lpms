import { Settings as SettingsIcon } from 'lucide-react';
export default function SettingsPage() {
  return (
    <div className="space-y-6 animate-in">
      <div className="page-header">
        <div><h1 className="page-title">Settings</h1><p className="page-subtitle">System configuration</p></div>
      </div>
      <div className="card p-6"><SettingsIcon className="w-12 h-12 text-neutral-300 mx-auto mb-4" /><p className="text-neutral-500">Settings will be displayed here.</p></div>
    </div>
  );
}
