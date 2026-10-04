import { useState } from 'react'
import { useForm } from 'react-hook-form'
import api, { errorMessage } from '../api/client.js'
import FormField from '../components/FormField.jsx'
import { useAuth } from '../context/AuthContext.jsx'
import { useToast } from '../context/ToastContext.jsx'

export default function Profile() {
  const { user, updateUser } = useAuth()
  const toast = useToast()
  const [error, setError] = useState('')
  const { register, handleSubmit, formState: { errors, isSubmitting } } = useForm({ defaultValues: { fullName: user.fullName } })
  const onSubmit = async (v) => {
    setError('')
    try { updateUser((await api.put('/users/me', v)).data); toast.push('Profile updated') }
    catch (e) { setError(errorMessage(e)); toast.push('Could not update profile', 'error') }
  }
  return (
    <main className="mx-auto max-w-2xl px-6 py-10">
      <h1 className="text-3xl font-bold">My profile</h1>
      <dl className="mt-6 grid gap-3 rounded-2xl border border-slate-100 bg-white p-5 text-sm sm:grid-cols-3">
        <div><dt className="text-slate-500">Email</dt><dd className="font-medium">{user.email}</dd></div>
        <div><dt className="text-slate-500">Role</dt><dd className="font-medium">{user.role}</dd></div>
        <div><dt className="text-slate-500">Member since</dt><dd className="font-medium">{user.createdAt ? new Date(user.createdAt).toLocaleDateString() : '-'}</dd></div>
      </dl>
      <form onSubmit={handleSubmit(onSubmit)} className="mt-6 space-y-4 rounded-2xl border border-slate-100 bg-white p-5" noValidate>
        <h2 className="font-semibold">Edit profile</h2>
        {error && <p className="rounded-lg bg-red-50 p-3 text-sm text-red-700" role="alert">{error}</p>}
        <FormField label="Full name" id="fullName" error={errors.fullName?.message} {...register('fullName', { required: 'Full name is required', maxLength: { value: 100, message: 'Name is too long' } })} />
        <p className="text-xs text-slate-500">Your email is your login and cannot be changed here.</p>
        <button disabled={isSubmitting} className="rounded-lg bg-brand-600 px-5 py-2.5 font-medium text-white hover:bg-brand-700 disabled:opacity-60">{isSubmitting ? 'Saving…' : 'Save changes'}</button>
      </form>
    </main>
  )
}
