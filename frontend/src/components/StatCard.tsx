import { ReactNode } from 'react';

export default function StatCard({
  label,
  value,
  footer,
  accent
}: {
  label: string;
  value: string;
  footer?: ReactNode;
  accent?: string;
}) {
  return (
    <div className={`card p-5 animate-fade-up ${accent ?? ''}`}>
      <p className="card-header">{label}</p>
      <p className="card-value mt-3">{value}</p>
      {footer ? <div className="mt-4 text-xs text-slate-400">{footer}</div> : null}
    </div>
  );
}
