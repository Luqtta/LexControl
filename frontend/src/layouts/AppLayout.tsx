import { NavLink, Outlet, useNavigate } from 'react-router-dom';
import type { ReactNode } from 'react';
import { useAuth } from '../contexts/AuthContext';
import { useI18n } from '../contexts/I18nContext';

const icons: Record<string, ReactNode> = {
  dashboard: (
    <>
      <rect x="3" y="3" width="7" height="9" rx="1.5" />
      <rect x="14" y="3" width="7" height="5" rx="1.5" />
      <rect x="14" y="12" width="7" height="9" rx="1.5" />
      <rect x="3" y="16" width="7" height="5" rx="1.5" />
    </>
  ),
  clients: (
    <>
      <circle cx="9" cy="8" r="3.2" />
      <path d="M3.5 20a5.5 5.5 0 0 1 11 0" />
      <path d="M16 5.5a3 3 0 0 1 0 5.4M17 20a5.5 5.5 0 0 0-3-4.9" />
    </>
  ),
  transactions: (
    <>
      <path d="M7 7h13l-3-3M17 17H4l3 3" />
    </>
  )
};

export default function AppLayout() {
  const { user, logout } = useAuth();
  const { t } = useI18n();
  const navigate = useNavigate();

  const navItems = [
    { key: 'dashboard', label: t('nav.dashboard'), path: '/dashboard' },
    { key: 'clients', label: t('nav.clients'), path: '/clients' },
    { key: 'transactions', label: t('nav.transactions'), path: '/transactions' }
  ];

  const initials = (user?.name ?? '?')
    .split(' ')
    .map((part) => part[0])
    .filter(Boolean)
    .slice(0, 2)
    .join('')
    .toUpperCase();

  const handleLogout = async () => {
    await logout();
    navigate('/login');
  };

  return (
    <div className="min-h-screen grid grid-cols-1 lg:grid-cols-[268px_1fr]">
      <aside className="flex flex-col gap-2 bg-gradient-to-b from-ink-900 to-ink-950 text-white px-5 py-7 lg:sticky lg:top-0 lg:h-screen border-r border-white/5">
        <div className="flex items-center gap-3 px-1 mb-8">
          <div className="grid h-11 w-11 place-items-center rounded-xl bg-gradient-to-br from-brand-400 to-brand-600 font-display text-lg font-bold shadow-glow">
            LC
          </div>
          <div className="leading-tight">
            <p className="text-[10px] uppercase tracking-[0.32em] text-white/45">{t('app.title')}</p>
            <h1 className="font-display text-lg">{t('app.subtitle')}</h1>
          </div>
        </div>

        <nav className="space-y-1">
          {navItems.map((item) => (
            <NavLink
              key={item.path}
              to={item.path}
              className={({ isActive }) =>
                `group relative flex items-center gap-3 rounded-xl px-3.5 py-2.5 text-sm font-semibold transition ${
                  isActive ? 'bg-white/[0.07] text-white' : 'text-white/55 hover:text-white hover:bg-white/[0.04]'
                }`
              }
            >
              {({ isActive }) => (
                <>
                  <span
                    className={`absolute left-0 top-1/2 h-5 w-1 -translate-y-1/2 rounded-r-full bg-brand-400 transition-opacity ${
                      isActive ? 'opacity-100' : 'opacity-0'
                    }`}
                  />
                  <svg
                    viewBox="0 0 24 24"
                    className="h-[18px] w-[18px] shrink-0"
                    fill="none"
                    stroke="currentColor"
                    strokeWidth="1.7"
                    strokeLinecap="round"
                    strokeLinejoin="round"
                  >
                    {icons[item.key]}
                  </svg>
                  {item.label}
                </>
              )}
            </NavLink>
          ))}
        </nav>

        <div className="mt-auto space-y-4 pt-6">
          <div className="flex items-center gap-3 rounded-2xl border border-white/10 bg-white/[0.04] p-3.5">
            <div className="grid h-10 w-10 shrink-0 place-items-center rounded-full bg-white/10 text-sm font-bold">
              {initials}
            </div>
            <div className="min-w-0">
              <p className="truncate text-sm font-semibold">{user?.name}</p>
              <p className="truncate text-xs text-white/45">{user?.email}</p>
            </div>
          </div>
          <button
            onClick={handleLogout}
            className="btn w-full border border-white/10 bg-transparent text-white/80 hover:bg-white/[0.06] hover:text-white"
          >
            {t('nav.logout')}
          </button>
        </div>
      </aside>

      <main className="px-5 py-8 sm:px-8 lg:px-12">
        <div className="mx-auto w-full max-w-6xl">
          <Outlet />
        </div>
      </main>
    </div>
  );
}
