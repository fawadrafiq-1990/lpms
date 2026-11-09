import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Bell, Search, ChevronDown, LogOut, User, Settings } from 'lucide-react';
import { useAuthStore } from '@/store/authStore';
import { authService } from '@/services/authService';
import toast from 'react-hot-toast';

export default function Header() {
  const navigate = useNavigate();
  const { user, logout } = useAuthStore();
  const [showUserMenu, setShowUserMenu] = useState(false);
  const [showNotifications, setShowNotifications] = useState(false);
  const [searchQuery, setSearchQuery] = useState('');

  const handleLogout = async () => {
    try {
      await authService.logout();
    } catch {
      // Continue with logout even if API call fails
    }
    logout();
    toast.success('Logged out successfully');
    navigate('/login');
  };

  const handleSearch = (e: React.FormEvent) => {
    e.preventDefault();
    if (searchQuery.trim()) {
      navigate(`/search?q=${encodeURIComponent(searchQuery)}`);
    }
  };

  return (
    <header className="sticky top-0 z-30 bg-white border-b border-neutral-200">
      <div className="flex items-center justify-between px-6 py-3">
        {/* Search */}
        <form onSubmit={handleSearch} className="flex-1 max-w-md">
          <div className="relative">
            <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-neutral-400" />
            <input
              type="text"
              placeholder="Search cases, clients, documents..."
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              className="w-full pl-10 pr-4 py-2 text-sm bg-neutral-100 border-0 rounded-lg
                         placeholder:text-neutral-400 focus:outline-none focus:ring-2
                         focus:ring-primary-500 focus:bg-white transition-all"
            />
          </div>
        </form>

        {/* Right side */}
        <div className="flex items-center gap-4">
          {/* Notifications */}
          <div className="relative">
            <button
              onClick={() => setShowNotifications(!showNotifications)}
              className="relative p-2 text-neutral-500 hover:text-neutral-700 hover:bg-neutral-100
                         rounded-lg transition-colors"
            >
              <Bell className="w-5 h-5" />
              <span className="absolute top-1 right-1 w-2 h-2 bg-danger-500 rounded-full" />
            </button>

            {showNotifications && (
              <div className="dropdown right-0 w-80">
                <div className="px-4 py-3 border-b border-neutral-200">
                  <h3 className="font-semibold text-sm">Notifications</h3>
                </div>
                <div className="max-h-96 overflow-y-auto">
                  <div className="px-4 py-8 text-center text-sm text-neutral-500">
                    No new notifications
                  </div>
                </div>
                <div className="px-4 py-2 border-t border-neutral-200">
                  <button className="text-sm text-primary-600 hover:text-primary-700">
                    View all notifications
                  </button>
                </div>
              </div>
            )}
          </div>

          {/* User menu */}
          <div className="relative">
            <button
              onClick={() => setShowUserMenu(!showUserMenu)}
              className="flex items-center gap-2 p-2 hover:bg-neutral-100 rounded-lg transition-colors"
            >
              <div className="w-8 h-8 rounded-full bg-primary-100 flex items-center justify-center">
                <span className="text-sm font-medium text-primary-700">
                  {user?.firstName?.charAt(0)}
                  {user?.lastName?.charAt(0)}
                </span>
              </div>
              <div className="text-left hidden sm:block">
                <p className="text-sm font-medium text-neutral-900">{user?.fullName}</p>
                <p className="text-xs text-neutral-500">{user?.roles?.[0]}</p>
              </div>
              <ChevronDown className="w-4 h-4 text-neutral-400" />
            </button>

            {showUserMenu && (
              <div className="dropdown right-0">
                <button
                  onClick={() => navigate('/settings')}
                  className="dropdown-item w-full"
                >
                  <User className="w-4 h-4 mr-3" />
                  Profile
                </button>
                <button
                  onClick={() => navigate('/settings')}
                  className="dropdown-item w-full"
                >
                  <Settings className="w-4 h-4 mr-3" />
                  Settings
                </button>
                <div className="border-t border-neutral-200 my-1" />
                <button
                  onClick={handleLogout}
                  className="dropdown-item w-full text-danger-600 hover:bg-danger-50"
                >
                  <LogOut className="w-4 h-4 mr-3" />
                  Logout
                </button>
              </div>
            )}
          </div>
        </div>
      </div>
    </header>
  );
}