import { forwardRef } from 'react'

// forwardRef is required so react-hook-form's register() can reach the <input>.
const FormField = forwardRef(function FormField({ label, id, error, ...props }, ref) {
  return (
    <div>
      <label htmlFor={id} className="block text-sm font-medium">{label}</label>
      <input id={id} ref={ref} aria-invalid={!!error} {...props}
        className={`mt-1 w-full rounded-lg border px-3 py-2 outline-none focus:ring-2 focus:ring-brand-600 ${error ? 'border-red-400' : 'border-slate-200'}`} />
      {error && <p className="mt-1 text-sm text-red-600" role="alert">{error}</p>}
    </div>
  )
})

export default FormField
