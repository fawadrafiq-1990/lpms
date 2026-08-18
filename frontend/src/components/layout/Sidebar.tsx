import { NavLink } from 'react-router-dom';
import {
  LayoutDashboard,
  Briefcase,
  Users,
  FileText,
  Calendar,
  CheckSquare,
  DollarSign,
  Gavel,
  Bot,
  Settings,
  Shield,
} from 'lucide-react';
import { useAuthStore } from '@/store/authStore';

interface NavItem {
  label: string;
  path: string;
  icon: React.ReactNode;
  children?: NavItem[];
  roles?: string[];
}

const navItems: NavItem[] = [
  {
    label: 'Dashboard',
    path: '/dashboard',
    icon: <LayoutDashboard className="w-5 h-5" />,
  },
  {
    label: 'Cases',
    path: '/cases',
    icon: <Briefcase className="w-5 h-5" />,
  },
  {
    label: 'Clients',
    path: '/clients',
    icon: <Users className="w-5 h-5" />,
  },
  {
    label: 'Documents',
    path: '/documents',
    icon: <FileText className="w-5 h-5" />,
  },
  {
    label: 'Calendar',
    path: '/calendar',
    icon: <Calendar className="w-5 h-5" />,
  },
  {
    label: 'Tasks',
    path: '/tasks',
    icon: <CheckSquare className="w-5 h-5" />,
  },
  {
    label: 'Hearings',
    path: '/hearings',
    icon: <Gavel className="w-5 h-5" />,
  },
  {
    label: 'Billing',
    path: '/billing',
    icon: <DollarSign className="w-5 h-5" />,
  },
  {
    label: 'AI Assistant',
    path: '/ai-assistant',
    icon: <Bot className="w-5 h-5" />,
  },
];

const adminItems: NavItem[] = [
  {
    label: 'Users',
    path: '/admin/users',
    icon: <Shield className="w-5 h-5" />,
  },
  {
    label: 'Settings',
    path: '/settings',
    icon: <Settings className="w-5 h-5" />,
  },
];

function NavSection({ title, items }: { title?: string; items: NavItem[] }) {
  return (
    <div className="space-y-1">
      {title && (
        <div className="px-4 py-2 text-xs font-semibold text-neutral-400 uppercase tracking-wider">
          {title}
        </div>
      )}
      {items.map((item) => (
        <NavLink
          key={item.path}
          to={item.path}
          className={({ isActive }) =>
            `sidebar-item ${isActive ? 'sidebar-item-active' : ''}`
          }
        >
          {item.icon}
          <span>{item.label}</span>
        </NavLink>
      ))}
    </div>
  );
}

export default function Sidebar() {
  const { user } = useAuthStore();
  const isAdmin = user?.roles?.some((r) =>
    ['SUPER_ADMIN', 'FIRM_ADMIN', 'MANAGING_PARTNER'].includes(r)
  );

  return (
    <aside className="sidebar">
      {/* Logo */}
      <div className="flex items-center gap-3 px-4 py-5 border-b border-neutral-200">
        <div className="w-10 h-10 rounded-lg bg-primary-600 flex items-center justify-center">
          <Gavel className="w-6 h-6 text-white" />
        </div>
        <div>
          <h1 className="font-bold text-lg text-neutral-900">LPMS</h1>
          <p className="text-xs text-neutral-500">Legal Practice Management</p>
        </div>
      </div>

      {/* Navigation */}
      <nav className="flex-1 overflow-y-auto py-4">
        <NavSection items={navItems} />

        {isAdmin && (
          <>
            <div className="my-4 mx-4 border-t border-neutral-200" />
            <NavSection title="Administration" items={adminItems} />
          </>
        )}
      </nav>

      {/* Footer */}
      <div className="border-t border-neutral-200 p-4">
        <div className="flex items-center gap-3">
          <div className="w-8 h-8 rounded-full bg-primary-100 flex items-center justify-center">
            <span className="text-sm font-medium text-primary-700">
              {user?.firstName?.charAt(0)}
              {user?.lastName?.charAt(0)}
            </span>
          </div>
          <div className="flex-1 min-w-0">
            <p className="text-sm font-medium text-neutral-900 truncate">
              {user?.fullName}
            </p>
            <p className="text-xs text-neutral-500 truncate">{user?.email}</p>
          </div>
        </div>
      </div>
    </aside>
  );
}