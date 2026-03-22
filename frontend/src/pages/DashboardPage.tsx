import { useQuery } from '@tanstack/react-query';
import StatCard from '../components/StatCard';
import { getDashboardSummary } from '../services/dashboard';
import { formatCurrency } from '../utils/format';
import { useI18n } from '../contexts/I18nContext';

export default function DashboardPage() {
  const { t } = useI18n();
  const { data, isLoading } = useQuery({
    queryKey: ['dashboard'],
    queryFn: getDashboardSummary
  });

  return (
    <div className="space-y-8">
      <div>
        <p className="text-sm uppercase tracking-[0.3em] text-slate-400">{t('dashboard.title')}</p>
        <h2 className="text-3xl font-display text-ink-900 mt-2">{t('dashboard.subtitle')}</h2>
      </div>

      {isLoading || !data ? (
        <div className="card p-6">{t('dashboard.loading')}</div>
      ) : (
        <>
          <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-3">
            <StatCard label={t('dashboard.totalHonorarios')} value={formatCurrency(data.totalHonorarios)} />
            <StatCard label={t('dashboard.totalRecebido')} value={formatCurrency(data.totalRecebido)} />
            <StatCard label={t('dashboard.totalPendente')} value={formatCurrency(data.totalPendente)} />
            <StatCard label={t('dashboard.recebidoMes')} value={formatCurrency(data.recebidoMes)} />
            <StatCard label={t('dashboard.gastosMes')} value={formatCurrency(data.gastosMes)} />
            <StatCard label={t('dashboard.saldoAtual')} value={formatCurrency(data.saldoAtual)} />
          </div>

          <div className="grid gap-4 md:grid-cols-2">
            <div className="card p-6">
              <p className="card-header">{t('dashboard.sentencaTitle')}</p>
              <div className="mt-4 space-y-2 text-sm">
                <div className="flex items-center justify-between">
                  <span className="text-slate-400">{t('dashboard.previsto')}</span>
                  <span className="font-semibold text-ink-900">{formatCurrency(data.sentencaPrevista)}</span>
                </div>
                <div className="flex items-center justify-between">
                  <span className="text-slate-400">{t('dashboard.pago')}</span>
                  <span className="font-semibold text-ink-900">{formatCurrency(data.sentencaPaga)}</span>
                </div>
                <div className="flex items-center justify-between">
                  <span className="text-slate-400">{t('dashboard.pendente')}</span>
                  <span className="font-semibold text-ink-900">{formatCurrency(data.sentencaPendente)}</span>
                </div>
              </div>
            </div>

            <div className="card p-6">
              <p className="card-header">{t('dashboard.totaisTitle')}</p>
              <div className="mt-4 space-y-2 text-sm">
                <div className="flex items-center justify-between">
                  <span className="text-slate-400">{t('dashboard.totalGastos')}</span>
                  <span className="font-semibold text-ink-900">{formatCurrency(data.totalGastos)}</span>
                </div>
                <div className="flex items-center justify-between">
                  <span className="text-slate-400">{t('dashboard.totalCreditos')}</span>
                  <span className="font-semibold text-ink-900">{formatCurrency(data.totalCreditos)}</span>
                </div>
              </div>
            </div>
          </div>
        </>
      )}
    </div>
  );
}
