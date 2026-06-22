import { api } from './api';
import type { Client } from '../types';

export type ClientPayload = {
  name: string;
  description?: string;
  totalHonorarios: number;
  valorRecebido?: number;
  valorPrevistoSentenca?: number;
  valorPagoSentenca?: number;
};

export async function listClients(params?: {
  search?: string;
  status?: string;
  sort?: string;
  page?: number;
  size?: number;
}): Promise<Client[]> {
  // Default to the first page with a large size so the current "show everything"
  // behaviour is preserved until visual pagination is implemented. Backend caps size at 100.
  const { data } = await api.get<Client[]>('/clients', {
    params: { page: 0, size: 100, ...params }
  });
  return data;
}

export async function createClient(payload: ClientPayload): Promise<Client> {
  const { data } = await api.post<Client>('/clients', payload);
  return data;
}

export async function updateClient(id: string, payload: ClientPayload): Promise<Client> {
  const { data } = await api.put<Client>(`/clients/${id}`, payload);
  return data;
}

export async function deleteClient(id: string): Promise<void> {
  await api.delete(`/clients/${id}`);
}
