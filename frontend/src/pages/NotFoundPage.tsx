import { Link } from 'react-router-dom';
import { useI18n } from '../contexts/I18nContext';

export default function NotFoundPage() {
  const { t } = useI18n();

  return (
    <div className="min-h-screen flex items-center justify-center px-4">
      <div className="card p-8 text-center">
        <h1 className="text-3xl font-display text-ink-900">{t('notfound.title')}</h1>
        <p className="text-sm text-slate-400 mt-2">{t('notfound.subtitle')}</p>
        <Link className="btn btn-primary mt-6 inline-flex" to="/dashboard">
          {t('notfound.cta')}
        </Link>
      </div>
    </div>
  );
}
