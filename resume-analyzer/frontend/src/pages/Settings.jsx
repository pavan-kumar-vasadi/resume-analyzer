import { useState } from 'react'
import { useForm } from 'react-hook-form'
import api, { errorMessage } from '../api/client.js'
import FormField from '../components/FormField.jsx'
import { useToast } from '../context/ToastContext.jsx'

export default function Settings() {
  const toast = useToast()
  const [error, setError] = useState('')
  const { register, handleSubmit, watch, reset, formState: { errors, isSubmitting } } = useForm()
  const onSubmit = async (v) => {
    setError('')
    try { await api.put('/users/me/password', v); reset(); toast.push('Password changed') }
    catch (e) { setError(errorMessage(e)); toast.push('Could not change password', 'error') }
  }
  return (
    <main className="mx-auto max-w-2xl px-6 py-10">
      <h1 className="text-3xl font-bold">Settings</h1>
      <form onSubmit={handleSubmit(onSubmit)} className="mt-6 space-y-4 rounded-2xl border border-slate-100 bg-white p-5" noValidate>
        <h2 className="font-semibold">Change password</h2>
        {error && <p className="rounded-lg bg-red-50 p-3 text-sm text-red-700" role="alert">{error}</p>}
        <FormField label="Current password" id="currentPassword" type="password" autoComplete="current-password" error={errors.currentPassword?.message} {...register('currentPassword', { required: 'Current password is required' })} />
        <FormField label="New password" id="newPassword" type="password" autoComplete="new-password" error={errors.newPassword?.message}
          {...register('newPassword', { required: 'New password is required', minLength: { value: 8, message: 'At least 8 characters' }, validate: (v) => (/[A-Za-z]/.test(v) && /\d/.test(v)) || 'Use at least one letter and one digit' })} />
        <FormField label="Confirm new password" id="confirmPassword" type="password" autoComplete="new-password" error={errors.confirmPassword?.message}
          {...register('confirmPassword', { required: 'Confirm your new password', validate: (v) => v === watch('newPassword') || 'Passwords do not match' })} />
        <button disabled={isSubmitting} className="rounded-lg bg-brand-600 px-5 py-2.5 font-medium text-white hover:bg-brand-700 disabled:opacity-60">{isSubmitting ? 'Saving…' : 'Change password'}</button>
      </form>
      <p className="mt-4 text-xs text-slate-500">Sessions expire automatically after a while; you will be asked to log in again.</p>
    </main>
  )
}
