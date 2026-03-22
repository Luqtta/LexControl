import { NavLink, Outlet, useNavigate } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';
import { useI18n } from '../contexts/I18nContext';

export default function AppLayout() {
  const { user, logout } = useAuth();
  const { t } = useI18n();
  const navigate = useNavigate();

  const navItems = [
    { label: t('nav.dashboard'), path: '/dashboard' },
    { label: t('nav.clients'), path: '/clients' },
    { label: t('nav.transactions'), path: '/transactions' }
  ];

  const handleLogout = async () => {
    await logout();
    navigate('/login');
  };

  return (
    <div className="min-h-screen grid grid-cols-1 lg:grid-cols-[260px_1fr]">
      <aside className="bg-ink-900 text-white px-6 py-8">
        <div className="mb-10">
          <p className="text-xs uppercase tracking-[0.3em] text-white/60">{t('app.title')}</p>
          <h1 className="mt-2 text-2xl font-display">{t('app.subtitle')}</h1>
        </div>
        <nav className="space-y-2">
          {navItems.map((item) => (
            <NavLink
              key={item.path}
              to={item.path}
              className={({ isActive }) =>
                `block rounded-xl px-4 py-3 text-sm font-semibold transition ${
                  isActive ? 'bg-white text-ink-900' : 'text-white/70 hover:bg-white/10'
                }`
              }
            >
              {item.label}
            </NavLink>
          ))}
        </nav>
        <div className="mt-10 rounded-2xl bg-white/10 p-4 text-sm">
          <p className="text-white/60">{t('nav.signedInAs')}</p>
          <p className="font-semibold">{user?.name}</p>
          <p className="text-white/60 text-xs break-all">{user?.email}</p>
        </div>
        <button onClick={handleLogout} className="btn btn-ghost mt-6 w-full text-white border-white/30">
          {t('nav.logout')}
        </button>
      </aside>

      <main className="px-6 py-8 lg:px-10">
        <Outlet />
      </main>
    </div>
  );
}
