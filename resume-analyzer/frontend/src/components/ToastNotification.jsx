export default function ToastNotification({ items, onClose }) {
  return (
    <div className="fixed bottom-4 right-4 z-[60] space-y-2" aria-live="polite">
      {items.map((t) => (
        <div key={t.id} role="status" className={`flex items-start gap-3 rounded-xl px-4 py-3 text-sm shadow-lg ${t.type === 'error' ? 'bg-red-600 text-white' : 'bg-navy text-white'}`}>
          <span>{t.message}</span>
          <button onClick={() => onClose(t.id)} aria-label="Dismiss notification" className="ml-2 opacity-80 hover:opacity-100">×</button>
        </div>))}
    </div>
  )
}
