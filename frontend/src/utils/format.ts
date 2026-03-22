export function formatCurrency(value: number) {
  return new Intl.NumberFormat('pt-BR', {
    style: 'currency',
    currency: 'BRL'
  }).format(value ?? 0);
}

export function formatDate(value: string) {
  if (!value) return '-';
  const date = new Date(value);
  return new Intl.DateTimeFormat('pt-BR').format(date);
}
