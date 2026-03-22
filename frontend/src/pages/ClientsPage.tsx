import { useMemo, useState } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import type { Client } from '../types';
import { createClient, deleteClient, listClients, updateClient, type ClientPayload } from '../services/clients';
import { createTransaction } from '../services/transactions';
import Modal from '../components/Modal';
import { formatCurrency } from '../utils/format';
import { useI18n } from '../contexts/I18nContext';

const emptyForm: ClientPayload = {
  name: '',
  description: '',
  totalHonorarios: 0,
  valorRecebido: 0,
  valorPrevistoSentenca: 0,
  valorPagoSentenca: 0
};

export default function ClientsPage() {
  const { t } = useI18n();
  const queryClient = useQueryClient();
  const [search, setSearch] = useState('');
  const [status, setStatus] = useState('all');
  const [sort, setSort] = useState('createdAt');
  const [isFormOpen, setFormOpen] = useState(false);
  const [editing, setEditing] = useState<Client | null>(null);
  const [formData, setFormData] = useState<ClientPayload>(emptyForm);
  const [paymentClient, setPaymentClient] = useState<Client | null>(null);
  const [paymentAmount, setPaymentAmount] = useState(0);
  const [paymentDate, setPaymentDate] = useState(() => new Date().toISOString().slice(0, 10));
  const [paymentDescription, setPaymentDescription] = useState('');

  const { data = [], isLoading } = useQuery({
    queryKey: ['clients', search, status, sort],
    queryFn: () =>
      listClients({
        search: search || undefined,
        status: status === 'all' ? undefined : status,
        sort
      })
  });

  const createMutation = useMutation({
    mutationFn: createClient,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['clients'] });
      queryClient.invalidateQueries({ queryKey: ['dashboard'] });
      closeForm();
    }
  });

  const updateMutation = useMutation({
    mutationFn: ({ id, payload }: { id: string; payload: ClientPayload }) => updateClient(id, payload),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['clients'] });
      queryClient.invalidateQueries({ queryKey: ['dashboard'] });
      closeForm();
    }
  });

  const deleteMutation = useMutation({
    mutationFn: deleteClient,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['clients'] });
      queryClient.invalidateQueries({ queryKey: ['dashboard'] });
    }
  });

  const paymentMutation = useMutation({
    mutationFn: createTransaction,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['clients'] });
      queryClient.invalidateQueries({ queryKey: ['dashboard'] });
      queryClient.invalidateQueries({ queryKey: ['transactions'] });
      setPaymentClient(null);
    }
  });

  const openCreate = () => {
    setEditing(null);
    setFormData(emptyForm);
    setFormOpen(true);
  };

  const openEdit = (client: Client) => {
    setEditing(client);
    setFormData({
      name: client.name,
      description: client.description || '',
      totalHonorarios: client.totalHonorarios,
      valorRecebido: client.valorRecebido,
      valorPrevistoSentenca: client.valorPrevistoSentenca,
      valorPagoSentenca: client.valorPagoSentenca
    });
    setFormOpen(true);
  };

  const openPayment = (client: Client) => {
    setPaymentClient(client);
    setPaymentAmount(0);
    setPaymentDate(new Date().toISOString().slice(0, 10));
    setPaymentDescription('');
  };

  const closeForm = () => {
    setFormOpen(false);
    setEditing(null);
  };

  const handleSubmit = (event: React.FormEvent) => {
    event.preventDefault();
    if (editing) {
      updateMutation.mutate({ id: editing.id, payload: formData });
    } else {
      createMutation.mutate(formData);
    }
  };

  const handlePayment = () => {
    if (!paymentClient) return;
    paymentMutation.mutate({
      type: 'INCOME',
      amount: paymentAmount,
      date: paymentDate,
      description: paymentDescription,
      clientId: paymentClient.id
    });
  };

  const tableRows = useMemo(() => data, [data]);

  return (
    <div className="space-y-8">
      <div className="flex flex-wrap items-end justify-between gap-4">
        <div>
          <p className="text-sm uppercase tracking-[0.3em] text-slate-400">{t('clients.title')}</p>
          <h2 className="text-3xl font-display text-ink-900 mt-2">{t('clients.subtitle')}</h2>
        </div>
        <button className="btn btn-primary" onClick={openCreate}>
          {t('clients.new')}
        </button>
      </div>

      <div className="card p-4 flex flex-wrap gap-4">
        <input
          className="input w-full md:w-64"
          placeholder={t('clients.search')}
          value={search}
          onChange={(event) => setSearch(event.target.value)}
        />
        <select className="input w-full md:w-48" value={status} onChange={(event) => setStatus(event.target.value)}>
          <option value="all">{t('clients.all')}</option>
          <option value="PENDING">{t('clients.pending')}</option>
          <option value="PAID">{t('clients.paid')}</option>
        </select>
        <select className="input w-full md:w-48" value={sort} onChange={(event) => setSort(event.target.value)}>
          <option value="createdAt">{t('clients.newest')}</option>
          <option value="name">{t('clients.form.name')}</option>
          <option value="totalHonorarios">{t('clients.totalHonorarios')}</option>
          <option value="valorRecebido">{t('clients.received')}</option>
        </select>
      </div>

      <div className="card overflow-hidden">
        {isLoading ? (
          <div className="p-6">{t('clients.loading')}</div>
        ) : (
          <table className="w-full text-sm">
            <thead className="bg-white/70 text-left">
              <tr>
                <th className="px-5 py-3 text-slate-400 uppercase tracking-[0.2em] text-xs">
                  {t('clients.header.client')}
                </th>
                <th className="px-5 py-3 text-slate-400 uppercase tracking-[0.2em] text-xs">
                  {t('clients.header.honorarios')}
                </th>
                <th className="px-5 py-3 text-slate-400 uppercase tracking-[0.2em] text-xs">
                  {t('clients.header.received')}
                </th>
                <th className="px-5 py-3 text-slate-400 uppercase tracking-[0.2em] text-xs">
                  {t('clients.header.pending')}
                </th>
                <th className="px-5 py-3"></th>
              </tr>
            </thead>
            <tbody>
              {tableRows.map((client) => (
                <tr key={client.id} className="border-t border-white/60">
                  <td className="px-5 py-4">
                    <p className="font-semibold text-ink-900">{client.name}</p>
                    <p className="text-xs text-slate-400">{client.description || t('clients.noDescription')}</p>
                  </td>
                  <td className="px-5 py-4 text-ink-900">{formatCurrency(client.totalHonorarios)}</td>
                  <td className="px-5 py-4 text-ink-900">{formatCurrency(client.valorRecebido)}</td>
                  <td className="px-5 py-4 text-ink-900">{formatCurrency(client.valorPendente)}</td>
                  <td className="px-5 py-4">
                    <div className="flex flex-wrap gap-2">
                      <button className="btn btn-ghost" onClick={() => openEdit(client)}>
                        {t('clients.edit')}
                      </button>
                      <button className="btn btn-ghost" onClick={() => openPayment(client)}>
                        {t('clients.addPayment')}
                      </button>
                      <button className="btn btn-ghost" onClick={() => deleteMutation.mutate(client.id)}>
                        {t('clients.delete')}
                      </button>
                    </div>
                  </td>
                </tr>
              ))}
              {tableRows.length === 0 ? (
                <tr>
                  <td colSpan={5} className="px-5 py-8 text-center text-slate-400">
                    {t('clients.empty')}
                  </td>
                </tr>
              ) : null}
            </tbody>
          </table>
        )}
      </div>

      <Modal open={isFormOpen} onClose={closeForm} title={editing ? t('clients.modal.edit') : t('clients.modal.new')}>
        <form className="space-y-4" onSubmit={handleSubmit}>
          <div>
            <label className="label">{t('clients.form.name')}</label>
            <input
              className="input mt-2"
              required
              value={formData.name}
              onChange={(event) => setFormData({ ...formData, name: event.target.value })}
            />
          </div>
          <div>
            <label className="label">{t('clients.form.description')}</label>
            <textarea
              className="input mt-2 min-h-[90px]"
              value={formData.description}
              onChange={(event) => setFormData({ ...formData, description: event.target.value })}
            />
          </div>
          <div className="grid gap-4 md:grid-cols-2">
            <div>
              <label className="label">{t('clients.form.totalHonorarios')}</label>
              <input
                className="input mt-2"
                type="number"
                min="0"
                step="0.01"
                value={formData.totalHonorarios}
                onChange={(event) => setFormData({ ...formData, totalHonorarios: Number(event.target.value) })}
                required
              />
            </div>
            <div>
              <label className="label">{t('clients.form.valorRecebido')}</label>
              <input
                className="input mt-2"
                type="number"
                min="0"
                step="0.01"
                value={formData.valorRecebido ?? 0}
                onChange={(event) => setFormData({ ...formData, valorRecebido: Number(event.target.value) })}
              />
            </div>
            <div>
              <label className="label">{t('clients.form.previstoSentenca')}</label>
              <input
                className="input mt-2"
                type="number"
                min="0"
                step="0.01"
                value={formData.valorPrevistoSentenca ?? 0}
                onChange={(event) =>
                  setFormData({ ...formData, valorPrevistoSentenca: Number(event.target.value) })
                }
              />
            </div>
            <div>
              <label className="label">{t('clients.form.pagoSentenca')}</label>
              <input
                className="input mt-2"
                type="number"
                min="0"
                step="0.01"
                value={formData.valorPagoSentenca ?? 0}
                onChange={(event) =>
                  setFormData({ ...formData, valorPagoSentenca: Number(event.target.value) })
                }
              />
            </div>
          </div>
          <div className="flex justify-end gap-2">
            <button type="button" className="btn btn-ghost" onClick={closeForm}>
              {t('common.cancel')}
            </button>
            <button type="submit" className="btn btn-primary">
              {editing ? t('common.saveChanges') : t('common.create')}
            </button>
          </div>
        </form>
      </Modal>

      <Modal
        open={!!paymentClient}
        onClose={() => setPaymentClient(null)}
        title={paymentClient ? `${t('clients.payment.title')} - ${paymentClient.name}` : t('clients.payment.title')}
      >
        <div className="space-y-4">
          <div>
            <label className="label">{t('clients.payment.amount')}</label>
            <input
              className="input mt-2"
              type="number"
              min="0"
              step="0.01"
              value={paymentAmount}
              onChange={(event) => setPaymentAmount(Number(event.target.value))}
            />
          </div>
          <div>
            <label className="label">{t('clients.payment.date')}</label>
            <input
              className="input mt-2"
              type="date"
              value={paymentDate}
              onChange={(event) => setPaymentDate(event.target.value)}
            />
          </div>
          <div>
            <label className="label">{t('clients.payment.description')}</label>
            <textarea
              className="input mt-2 min-h-[90px]"
              value={paymentDescription}
              onChange={(event) => setPaymentDescription(event.target.value)}
            />
          </div>
          <div className="flex justify-end gap-2">
            <button className="btn btn-ghost" onClick={() => setPaymentClient(null)}>
              {t('common.cancel')}
            </button>
            <button className="btn btn-primary" onClick={handlePayment}>
              {t('clients.payment.save')}
            </button>
          </div>
        </div>
      </Modal>
    </div>
  );
}
