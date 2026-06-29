import { useState } from 'react';
import axios from 'axios';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';
import { useI18n } from '../contexts/I18nContext';
import AuthShell from '../components/AuthShell';

export default function RegisterPage() {
  const { register } = useAuth();
  const { t } = useI18n();
  const navigate = useNavigate();
  const [name, setName] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState<string | null>(null);
  const [isError, setIsError] = useState(false);

  const handleSubmit = async (event: React.FormEvent) => {
    event.preventDefault();
    setLoading(true);
    setMessage(null);
    setIsError(false);
    try {
      await register({ name, email, password });
      setMessage(t('auth.registerSuccess'));
      setIsError(false);
      setTimeout(() => navigate('/login'), 800);
    } catch (error) {
      if (axios.isAxiosError(error) && error.response?.status === 409) {
        setMessage(t('auth.emailInUse'));
      } else {
        setMessage(t('auth.registerFailed'));
      }
      setIsError(true);
    } finally {
      setLoading(false);
    }
  };

  return (
    <AuthShell>
      <div className="card p-8">
        <h1 className="text-3xl font-display text-ink-900 tracking-tight">{t('auth.createAccount')}</h1>
        <p className="text-sm text-slate-500 mt-2">{t('auth.createSubtitle')}</p>

        <form onSubmit={handleSubmit} className="mt-7 space-y-4">
          <div>
            <label className="label">{t('auth.name')}</label>
            <input
              className="input mt-2"
              type="text"
              required
              value={name}
              onChange={(event) => setName(event.target.value)}
            />
          </div>
          <div>
            <label className="label">{t('auth.email')}</label>
            <input
              className="input mt-2"
              type="email"
              required
              value={email}
              onChange={(event) => setEmail(event.target.value)}
            />
          </div>
          <div>
            <label className="label">{t('auth.password')}</label>
            <div className="relative mt-2">
              <input
                className="input pr-16"
                type={showPassword ? 'text' : 'password'}
                minLength={8}
                required
                value={password}
                onChange={(event) => setPassword(event.target.value)}
              />
              <button
                type="button"
                className="absolute right-3 top-1/2 -translate-y-1/2 text-xs font-semibold text-brand-600 hover:text-brand-700"
                onClick={() => setShowPassword((prev) => !prev)}
              >
                {showPassword ? t('auth.hidePassword') : t('auth.showPassword')}
              </button>
            </div>
          </div>

          {message ? (
            <div
              className={`rounded-xl border p-3 ${
                isError ? 'border-coral-500/30 bg-coral-50' : 'border-brand-500/30 bg-brand-50'
              }`}
            >
              <p className={`text-sm ${isError ? 'text-coral-600' : 'text-brand-700'}`}>{message}</p>
            </div>
          ) : null}

          <button className="btn btn-accent w-full" disabled={loading}>
            {loading ? t('auth.registerLoading') : t('auth.register')}
          </button>
        </form>

        <p className="mt-6 text-sm text-slate-500">
          {t('auth.alreadyRegistered')}{' '}
          <Link className="font-semibold text-brand-600 hover:text-brand-700" to="/login">
            {t('auth.loginLink')}
          </Link>
        </p>
      </div>
    </AuthShell>
  );
}
