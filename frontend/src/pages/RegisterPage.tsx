import { useState } from 'react';
import axios from 'axios';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';
import { useI18n } from '../contexts/I18nContext';

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
    <div className="min-h-screen flex items-center justify-center px-4">
      <div className="card w-full max-w-md p-8 animate-fade-up">
        <h1 className="text-3xl font-display text-ink-900">{t('auth.createAccount')}</h1>
        <p className="text-sm text-slate-400 mt-2">{t('auth.createSubtitle')}</p>

        <form onSubmit={handleSubmit} className="mt-6 space-y-4">
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
                className="input pr-20"
                type={showPassword ? 'text' : 'password'}
                minLength={8}
                required
                value={password}
                onChange={(event) => setPassword(event.target.value)}
              />
              <button
                type="button"
                className="absolute right-3 top-1/2 -translate-y-1/2 text-xs font-semibold text-ink-900"
                onClick={() => setShowPassword((prev) => !prev)}
              >
                {showPassword ? t('auth.hidePassword') : t('auth.showPassword')}
              </button>
            </div>
          </div>

          {message ? (
            <p className={`text-sm ${isError ? 'text-coral-500' : 'text-ink-900'}`}>{message}</p>
          ) : null}

          <button className="btn btn-primary w-full" disabled={loading}>
            {loading ? t('auth.registerLoading') : t('auth.register')}
          </button>
        </form>

        <p className="mt-6 text-sm text-slate-400">
          {t('auth.alreadyRegistered')}{' '}
          <Link className="text-ink-900 font-semibold" to="/login">
            {t('auth.loginLink')}
          </Link>
        </p>
      </div>
    </div>
  );
}
