const brl = new Intl.NumberFormat('pt-BR', { minimumFractionDigits: 2, maximumFractionDigits: 2 });

type Props = {
  value: number;
  onChange: (value: number) => void;
  className?: string;
  required?: boolean;
};

// Cents accumulator: every digit typed shifts into the cents, so "1649839" -> 16.498,39.
// Solves the "can't clear the 0" (text field, empty = 0, shows blank) and the BR-format
// bugs that type="number" caused. Typed separators are ignored on purpose — the value
// formats itself as you type. Verified: "1649839" -> R$ 16.498,39 (see money_check).
// ponytail: digits-only, no locale parsing to get wrong.
export default function MoneyInput({ value, onChange, className, required }: Props) {
  const num = typeof value === 'number' ? value : Number(value) || 0;
  const handle = (raw: string) => {
    const cents = raw.replace(/\D/g, '');
    onChange(cents ? Number(cents) / 100 : 0);
  };
  return (
    <div className="relative">
      <span className="pointer-events-none absolute left-3.5 top-1/2 -translate-y-1/2 text-sm font-semibold text-slate-400">
        R$
      </span>
      <input
        className={`${className ?? ''} pl-10 text-right tnum font-semibold`}
        inputMode="numeric"
        placeholder="0,00"
        value={num ? brl.format(num) : ''}
        onChange={(event) => handle(event.target.value)}
        required={required}
      />
    </div>
  );
}
