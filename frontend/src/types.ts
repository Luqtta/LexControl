export type User = {
  id: string;
  name: string;
  email: string;
  createdAt: string;
};

export type AuthResponse = {
  accessToken: string;
  expiresAt: number;
  tokenType: string;
  user: User;
};

export type Client = {
  id: string;
  name: string;
  description?: string | null;
  totalHonorarios: number;
  valorRecebido: number;
  valorPendente: number;
  valorPrevistoSentenca: number;
  valorPagoSentenca: number;
  valorPendenteSentenca: number;
  createdAt: string;
};

export type Transaction = {
  id: string;
  type: 'INCOME' | 'EXPENSE';
  amount: number;
  description?: string | null;
  date: string;
  clientId?: string | null;
  clientName?: string | null;
  createdAt: string;
};

export type DashboardSummary = {
  totalHonorarios: number;
  totalRecebido: number;
  totalPendente: number;
  recebidoMes: number;
  gastosMes: number;
  saldoAtual: number;
  totalGastos: number;
  totalCreditos: number;
  sentencaPrevista: number;
  sentencaPaga: number;
  sentencaPendente: number;
};
