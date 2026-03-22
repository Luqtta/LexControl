import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';
import { useI18n } from '../contexts/I18nContext';
import { ApiErrorHandler } from '../utils/errorHandler';

export default function LoginPage() {
  const { login } = useAuth();
  const { t } = useI18n();
  const navigate = useNavigate();
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (event: React.FormEvent) => {
    event.preventDefault();
    setError(null);
    setLoading(true);
    try {
      await login(email, password);
      navigate('/dashboard');
    } catch (err) {
      // Handle specific error cases
      if (ApiErrorHandler.isRateLimited(err)) {
        setError(t('auth.tooManyAttempts') || 'Too many login attempts. Please try again later.');
      } else if (ApiErrorHandler.isAuthError(err)) {
        setError(t('auth.invalid') || 'Invalid email or password.');
      } else {
        // Generic error message from error handler
        setError(ApiErrorHandler.getErrorMessage(err));
      }
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center px-4">
      <div className="card w-full max-w-md p-8 animate-fade-up">
        <h1 className="text-3xl font-display text-ink-900">{t('auth.welcome')}</h1>
        <p className="text-sm text-slate-400 mt-2">{t('auth.welcomeSubtitle')}</p>

        <form onSubmit={handleSubmit} className="mt-6 space-y-4">
          <div>
            <label className="label">{t('auth.email')}</label>
            <input
              className="input mt-2"
              type="email"
              required
              value={email}
              onChange={(event) => setEmail(event.target.value)}
              disabled={loading}
            />
          </div>
          <div>
            <label className="label">{t('auth.password')}</label>
            <div className="relative mt-2">
              <input
                className="input pr-20"
                type={showPassword ? 'text' : 'password'}
                required
                value={password}
                onChange={(event) => setPassword(event.target.value)}
                disabled={loading}
              />
              <button
                type="button"
                className="absolute right-3 top-1/2 -translate-y-1/2 text-xs font-semibold text-ink-900 disabled:opacity-50"
                onClick={() => setShowPassword((prev) => !prev)}
                disabled={loading}
              >
                {showPassword ? t('auth.hidePassword') : t('auth.showPassword')}
              </button>
            </div>
          </div>

          {error ? (
            <div className="p-3 rounded bg-red-50 border border-red-200">
              <p className="text-sm text-red-700">{error}</p>
            </div>
          ) : null}

          <button className="btn btn-primary w-full" disabled={loading}>
            {loading ? t('auth.loginLoading') : t('auth.login')}
          </button>
        </form>

        <p className="mt-6 text-sm text-slate-400">
          {t('auth.noAccount')}{' '}
          <Link className="text-ink-900 font-semibold" to="/register">
            {t('auth.createOne')}
          </Link>
        </p>
      </div>
    </div>
  );
}
