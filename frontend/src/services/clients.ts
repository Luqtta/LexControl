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
}): Promise<Client[]> {
  const { data } = await api.get<Client[]>('/clients', { params });
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
