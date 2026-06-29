import { Link } from 'react-router-dom';
import { useI18n } from '../contexts/I18nContext';

export default function NotFoundPage() {
  const { t } = useI18n();

  return (
    <div className="min-h-screen flex items-center justify-center px-4">
      <div className="card p-10 text-center max-w-md animate-fade-up">
        <p className="font-display text-6xl tracking-tight text-brand-500">404</p>
        <h1 className="text-2xl font-display text-ink-900 mt-4 tracking-tight">{t('notfound.title')}</h1>
        <p className="text-sm text-slate-500 mt-2">{t('notfound.subtitle')}</p>
        <Link className="btn btn-accent mt-7 inline-flex" to="/dashboard">
          {t('notfound.cta')}
        </Link>
      </div>
    </div>
  );
}
