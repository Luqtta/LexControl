import { useState } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { listTransactions, createTransaction, deleteTransaction, updateTransaction, type TransactionPayload } from '../services/transactions';
import { listClients } from '../services/clients';
import type { Transaction } from '../types';
import Modal from '../components/Modal';
import { formatCurrency, formatDate } from '../utils/format';
import { useI18n } from '../contexts/I18nContext';

const emptyForm = {
  type: 'INCOME' as const,
  amount: '' as unknown as number,
  date: new Date().toISOString().slice(0, 10),
  description: '',
  clientId: ''
};

export default function TransactionsPage() {
  const { t } = useI18n();
  const queryClient = useQueryClient();
  const [typeFilter, setTypeFilter] = useState('all');
  const [from, setFrom] = useState('');
  const [to, setTo] = useState('');
  const [isFormOpen, setFormOpen] = useState(false);
  const [editing, setEditing] = useState<Transaction | null>(null);
  const [formData, setFormData] = useState({ ...emptyForm });
  const [formError, setFormError] = useState<string | null>(null);

  const { data: transactions = [], isLoading } = useQuery({
    queryKey: ['transactions', typeFilter, from, to],
    queryFn: () =>
      listTransactions({
        type: typeFilter === 'all' ? undefined : (typeFilter as 'INCOME' | 'EXPENSE'),
        from: from || undefined,
        to: to || undefined
      })
  });

  const { data: clients = [] } = useQuery({
    queryKey: ['clients', 'select'],
    queryFn: () => listClients()
  });

  const createMutation = useMutation({
    mutationFn: createTransaction,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['transactions'] });
      queryClient.invalidateQueries({ queryKey: ['dashboard'] });
      queryClient.invalidateQueries({ queryKey: ['clients'] });
      closeForm();
    },
    onError: (error: any) => {
      setFormError(error?.message || t('common.error'));
    }
  });

  const updateMutation = useMutation({
    mutationFn: ({ id, payload }: { id: string; payload: TransactionPayload }) => updateTransaction(id, payload),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['transactions'] });
      queryClient.invalidateQueries({ queryKey: ['dashboard'] });
      queryClient.invalidateQueries({ queryKey: ['clients'] });
      closeForm();
    },
    onError: (error: any) => {
      setFormError(error?.message || t('common.error'));
    }
  });

  const deleteMutation = useMutation({
    mutationFn: deleteTransaction,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['transactions'] });
      queryClient.invalidateQueries({ queryKey: ['dashboard'] });
      queryClient.invalidateQueries({ queryKey: ['clients'] });
    }
  });

  const openCreate = () => {
    setEditing(null);
    setFormData({ ...emptyForm });
    setFormOpen(true);
  };

  const openEdit = (transaction: Transaction) => {
    setEditing(transaction);
    setFormData({
      type: transaction.type,
      amount: transaction.amount,
      date: transaction.date,
      description: transaction.description || '',
      clientId: transaction.clientId || ''
    });
    setFormOpen(true);
  };

  const closeForm = () => {
    setFormOpen(false);
    setEditing(null);
    setFormError(null);
  };

  const handleSubmit = (event: React.FormEvent) => {
    event.preventDefault();
    const payload = {
      type: formData.type,
      amount: Number(formData.amount),
      date: formData.date,
      description: formData.description,
      clientId: formData.clientId || null
    };
    if (editing) {
      updateMutation.mutate({ id: editing.id, payload });
    } else {
      createMutation.mutate(payload);
    }
  };

  return (
    <div className="space-y-8">
      <div className="flex flex-wrap items-end justify-between gap-4">
        <div>
          <p className="text-sm uppercase tracking-[0.3em] text-slate-400">{t('transactions.title')}</p>
          <h2 className="text-3xl font-display text-ink-900 mt-2">{t('transactions.subtitle')}</h2>
        </div>
        <button className="btn btn-primary" onClick={openCreate}>
          {t('transactions.new')}
        </button>
      </div>

      <div className="card p-4 flex flex-wrap gap-4">
        <select className="input w-full md:w-48" value={typeFilter} onChange={(event) => setTypeFilter(event.target.value)}>
          <option value="all">{t('transactions.allTypes')}</option>
          <option value="INCOME">{t('transactions.income')}</option>
          <option value="EXPENSE">{t('transactions.expense')}</option>
        </select>
        <input className="input w-full md:w-48" type="date" value={from} onChange={(event) => setFrom(event.target.value)} />
        <input className="input w-full md:w-48" type="date" value={to} onChange={(event) => setTo(event.target.value)} />
      </div>

      <div className="card overflow-hidden">
        {isLoading ? (
          <div className="p-6">{t('transactions.loading')}</div>
        ) : (
          <table className="w-full text-sm">
            <thead className="bg-white/70 text-left">
              <tr>
                <th className="px-5 py-3 text-slate-400 uppercase tracking-[0.2em] text-xs">
                  {t('transactions.header.type')}
                </th>
                <th className="px-5 py-3 text-slate-400 uppercase tracking-[0.2em] text-xs">
                  {t('transactions.header.amount')}
                </th>
                <th className="px-5 py-3 text-slate-400 uppercase tracking-[0.2em] text-xs">
                  {t('transactions.header.date')}
                </th>
                <th className="px-5 py-3 text-slate-400 uppercase tracking-[0.2em] text-xs">
                  {t('transactions.header.client')}
                </th>
                <th className="px-5 py-3"></th>
              </tr>
            </thead>
            <tbody>
              {transactions.map((item) => (
                <tr key={item.id} className="border-t border-white/60">
                  <td className="px-5 py-4 font-semibold text-ink-900">{item.type}</td>
                  <td className="px-5 py-4 text-ink-900">{formatCurrency(item.amount)}</td>
                  <td className="px-5 py-4 text-ink-900">{formatDate(item.date)}</td>
                  <td className="px-5 py-4 text-slate-400">{item.clientName || t('transactions.general')}</td>
                  <td className="px-5 py-4">
                    <div className="flex gap-2">
                      <button className="btn btn-ghost" onClick={() => openEdit(item)}>
                        {t('clients.edit')}
                      </button>
                      <button className="btn btn-ghost" onClick={() => deleteMutation.mutate(item.id)}>
                        {t('clients.delete')}
                      </button>
                    </div>
                  </td>
                </tr>
              ))}
              {transactions.length === 0 ? (
                <tr>
                  <td colSpan={5} className="px-5 py-8 text-center text-slate-400">
                    {t('transactions.empty')}
                  </td>
                </tr>
              ) : null}
            </tbody>
          </table>
        )}
      </div>

      <Modal open={isFormOpen} onClose={closeForm} title={editing ? t('transactions.modal.edit') : t('transactions.modal.new')}>
        <form className="space-y-4" onSubmit={handleSubmit}>
          <div>
            <label className="label">{t('transactions.form.type')}</label>
            <select
              className="input mt-2"
              value={formData.type}
              onChange={(event) => setFormData({ ...formData, type: event.target.value as 'INCOME' | 'EXPENSE' })}
            >
              <option value="INCOME">{t('transactions.income')}</option>
              <option value="EXPENSE">{t('transactions.expense')}</option>
            </select>
          </div>
          <div>
            <label className="label">{t('transactions.form.amount')}</label>
            <input
              className="input mt-2"
              type="number"
              min="0.01"
              step="0.01"
              required
              value={formData.amount}
              onChange={(event) => setFormData({ ...formData, amount: Number(event.target.value) })}
            />
          </div>
          <div>
            <label className="label">{t('transactions.form.date')}</label>
            <input
              className="input mt-2"
              type="date"
              value={formData.date}
              onChange={(event) => setFormData({ ...formData, date: event.target.value })}
            />
          </div>
          <div>
            <label className="label">{t('transactions.form.client')}</label>
            <select
              className="input mt-2"
              value={formData.clientId}
              onChange={(event) => setFormData({ ...formData, clientId: event.target.value })}
            >
              <option value="">{t('transactions.general')}</option>
              {clients.map((client) => (
                <option key={client.id} value={client.id}>
                  {client.name}
                </option>
              ))}
            </select>
          </div>
          <div>
            <label className="label">{t('transactions.form.description')}</label>
            <textarea
              className="input mt-2 min-h-[90px]"
              value={formData.description}
              onChange={(event) => setFormData({ ...formData, description: event.target.value })}
            />
          </div>
          {formError && (
            <p className="text-sm text-red-500">{formError}</p>
          )}
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
    </div>
  );
}
