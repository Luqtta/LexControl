import { ReactNode } from 'react';

type Tone = 'neutral' | 'income' | 'expense' | 'brand';

const toneBar: Record<Tone, string> = {
  neutral: 'bg-slate-300',
  income: 'bg-brand-500',
  expense: 'bg-coral-500',
  brand: 'bg-gradient-to-b from-brand-400 to-brand-600'
};

const toneValue: Record<Tone, string> = {
  neutral: 'text-ink-900',
  income: 'text-brand-600',
  expense: 'text-coral-600',
  brand: 'text-ink-900'
};

export default function StatCard({
  label,
  value,
  footer,
  tone = 'neutral'
}: {
  label: string;
  value: string;
  footer?: ReactNode;
  tone?: Tone;
}) {
  return (
    <div className="card card-interactive relative overflow-hidden p-5 pl-6 animate-fade-up">
      <span className={`absolute left-0 top-5 bottom-5 w-1 rounded-r-full ${toneBar[tone]}`} />
      <p className="card-header">{label}</p>
      <p className={`card-value mt-3 ${toneValue[tone]}`}>{value}</p>
      {footer ? <div className="mt-4 text-xs text-slate-400">{footer}</div> : null}
    </div>
  );
}
