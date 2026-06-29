const brl = new Intl.NumberFormat('pt-BR', { minimumFractionDigits: 2, maximumFractionDigits: 2 });

type Props = {
  value: number;
  onChange: (value: number) => void;
  className?: string;
  required?: boolean;
};

// Cents accumulator: every digit typed shifts into the cents, so "16500000" -> 16500,00.
// Solves both the "can't clear the 0" (text field, empty = 0) and BR formatting bugs that
// type="number" caused. ponytail: digits-only, no locale parsing to get wrong.
export default function MoneyInput({ value, onChange, className, required }: Props) {
  const handle = (raw: string) => {
    const cents = raw.replace(/\D/g, '');
    onChange(cents ? Number(cents) / 100 : 0);
  };
  return (
    <input
      className={className}
      inputMode="decimal"
      placeholder="0,00"
      value={value ? brl.format(value) : ''}
      onChange={(event) => handle(event.target.value)}
      required={required}
    />
  );
}
