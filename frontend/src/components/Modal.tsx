import { ReactNode } from 'react';
import { useI18n } from '../contexts/I18nContext';

export default function Modal({
  open,
  onClose,
  title,
  children
}: {
  open: boolean;
  onClose: () => void;
  title: string;
  children: ReactNode;
}) {
  const { t } = useI18n();
  if (!open) return null;
  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 px-4">
      <div className="card w-full max-w-xl p-6">
        <div className="flex items-center justify-between">
          <h3 className="text-lg font-display">{title}</h3>
          <button className="btn btn-ghost" onClick={onClose}>
            {t('modal.close')}
          </button>
        </div>
        <div className="mt-4">{children}</div>
      </div>
    </div>
  );
}
