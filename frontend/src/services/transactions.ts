import { api } from './api';
import type { Transaction } from '../types';

export type TransactionPayload = {
  type: 'INCOME' | 'EXPENSE';
  amount: number;
  date: string;
  description?: string;
  clientId?: string | null;
};

export async function listTransactions(params?: {
  type?: 'INCOME' | 'EXPENSE';
  clientId?: string;
  from?: string;
  to?: string;
  sort?: string;
  page?: number;
  size?: number;
}): Promise<Transaction[]> {
  // Default to the first page with a large size so the current "show everything"
  // behaviour is preserved until visual pagination is implemented. Backend caps size at 100.
  const { data } = await api.get<Transaction[]>('/transactions', {
    params: { page: 0, size: 100, ...params }
  });
  return data;
}

export async function createTransaction(payload: TransactionPayload): Promise<Transaction> {
  const { data } = await api.post<Transaction>('/transactions', payload);
  return data;
}

export async function updateTransaction(id: string, payload: TransactionPayload): Promise<Transaction> {
  const { data } = await api.put<Transaction>(`/transactions/${id}`, payload);
  return data;
}

export async function deleteTransaction(id: string): Promise<void> {
  await api.delete(`/transactions/${id}`);
}
