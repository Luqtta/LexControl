import { useQuery } from '@tanstack/react-query';
import StatCard from '../components/StatCard';
import { getDashboardSummary } from '../services/dashboard';
import { formatCurrency } from '../utils/format';
import { useI18n } from '../contexts/I18nContext';

function Row({ label, value, tone }: { label: string; value: number; tone?: string }) {
  return (
    <div className="flex items-center justify-between border-b border-slate-100 py-2.5 last:border-0">
      <span className="text-sm text-slate-500">{label}</span>
      <span className={`text-sm font-semibold tnum ${tone ?? 'text-ink-900'}`}>{formatCurrency(value)}</span>
    </div>
  );
}

export default function DashboardPage() {
  const { t } = useI18n();
  const { data, isLoading } = useQuery({
    queryKey: ['dashboard'],
    queryFn: getDashboardSummary
  });

  return (
    <div className="space-y-8">
      <div>
        <p className="text-xs uppercase tracking-[0.3em] text-slate-400">{t('dashboard.title')}</p>
        <h2 className="text-3xl font-display text-ink-900 mt-2 tracking-tight">{t('dashboard.subtitle')}</h2>
      </div>

      {isLoading || !data ? (
        <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-3">
          {Array.from({ length: 6 }).map((_, i) => (
            <div key={i} className="card p-5 animate-pulse">
              <div className="h-3 w-24 rounded bg-slate-200" />
              <div className="mt-4 h-7 w-32 rounded bg-slate-200" />
            </div>
          ))}
        </div>
      ) : (
        <>
          <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-3">
            <StatCard label={t('dashboard.totalHonorarios')} value={formatCurrency(data.totalHonorarios)} tone="brand" />
            <StatCard label={t('dashboard.totalRecebido')} value={formatCurrency(data.totalRecebido)} tone="income" />
            <StatCard label={t('dashboard.totalPendente')} value={formatCurrency(data.totalPendente)} />
            <StatCard label={t('dashboard.recebidoMes')} value={formatCurrency(data.recebidoMes)} tone="income" />
            <StatCard label={t('dashboard.gastosMes')} value={formatCurrency(data.gastosMes)} tone="expense" />
            <StatCard label={t('dashboard.saldoAtual')} value={formatCurrency(data.saldoAtual)} tone="brand" />
          </div>

          <div className="grid gap-4 md:grid-cols-2">
            <div className="card p-6">
              <p className="card-header">{t('dashboard.sentencaTitle')}</p>
              <div className="mt-4">
                <Row label={t('dashboard.previsto')} value={data.sentencaPrevista} />
                <Row label={t('dashboard.pago')} value={data.sentencaPaga} tone="text-brand-600" />
                <Row label={t('dashboard.pendente')} value={data.sentencaPendente} />
              </div>
            </div>

            <div className="card p-6">
              <p className="card-header">{t('dashboard.totaisTitle')}</p>
              <div className="mt-4">
                <Row label={t('dashboard.totalGastos')} value={data.totalGastos} tone="text-coral-600" />
                <Row label={t('dashboard.totalCreditos')} value={data.totalCreditos} tone="text-brand-600" />
              </div>
            </div>
          </div>
        </>
      )}
    </div>
  );
}
