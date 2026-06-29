import type { ReactNode } from 'react';
import { useI18n } from '../contexts/I18nContext';

// Shared split-screen frame for login/register: dark brand panel + form card.
export default function AuthShell({ children }: { children: ReactNode }) {
  const { t } = useI18n();
  const points = [t('auth.brandPoint1'), t('auth.brandPoint2'), t('auth.brandPoint3')];

  return (
    <div className="min-h-screen grid lg:grid-cols-2">
      <aside className="relative hidden overflow-hidden bg-gradient-to-br from-ink-800 to-ink-950 p-12 text-white lg:flex lg:flex-col lg:justify-between">
        <div className="pointer-events-none absolute -right-24 -top-24 h-80 w-80 rounded-full bg-brand-500/20 blur-3xl" />
        <div className="pointer-events-none absolute -bottom-32 -left-10 h-80 w-80 rounded-full bg-brand-400/10 blur-3xl" />

        <div className="relative flex items-center gap-3">
          <div className="grid h-12 w-12 place-items-center rounded-xl bg-gradient-to-br from-brand-400 to-brand-600 font-display text-xl font-bold shadow-glow">
            LC
          </div>
          <div className="leading-tight">
            <p className="text-[10px] uppercase tracking-[0.34em] text-white/45">{t('app.title')}</p>
            <h1 className="font-display text-xl">{t('app.subtitle')}</h1>
          </div>
        </div>

        <div className="relative max-w-sm">
          <h2 className="font-display text-3xl leading-tight tracking-tight text-balance">{t('auth.brandTagline')}</h2>
          <ul className="mt-8 space-y-3.5">
            {points.map((point) => (
              <li key={point} className="flex items-start gap-3 text-sm text-white/70">
                <svg viewBox="0 0 24 24" className="mt-0.5 h-5 w-5 shrink-0 text-brand-400" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                  <path d="M20 6L9 17l-5-5" />
                </svg>
                {point}
              </li>
            ))}
          </ul>
        </div>

        <p className="relative text-xs text-white/35">© {t('app.title')}</p>
      </aside>

      <main className="flex items-center justify-center px-4 py-12">
        <div className="w-full max-w-md animate-fade-up">{children}</div>
      </main>
    </div>
  );
}
