import { useState } from 'react'
import { useForm } from 'react-hook-form'
import { Link, Navigate, useNavigate, useSearchParams } from 'react-router-dom'
import AuthLayout from '../layouts/AuthLayout.jsx'
import FormField from '../components/FormField.jsx'
import { useAuth } from '../context/AuthContext.jsx'
import { errorMessage } from '../api/client.js'

export default function Login() {
  const { user, login } = useAuth()
  const nav = useNavigate()
  const [params] = useSearchParams()
  const [serverError, setServerError] = useState('')
  const { register, handleSubmit, formState: { errors, isSubmitting } } = useForm()

  if (user) return <Navigate to={user.role === 'ADMIN' ? '/admin' : '/dashboard'} replace />

  const onSubmit = async (v) => {
    setServerError('')
    try {
      const u = await login(v)
      nav(u.role === 'ADMIN' ? '/admin' : '/dashboard', { replace: true })
    } catch (e) { setServerError(errorMessage(e)) }
  }

  return (
    <AuthLayout title="Welcome back" subtitle="Log in to continue."
      footer={<>No account? <Link to="/register" className="font-medium text-brand-600">Sign up</Link></>}>
      {params.get('expired') && <p className="mb-4 rounded-lg bg-amber-50 p-3 text-sm text-amber-800" role="alert">Your session expired. Please log in again.</p>}
      {serverError && <p className="mb-4 rounded-lg bg-red-50 p-3 text-sm text-red-700" role="alert">{serverError}</p>}
      <form onSubmit={handleSubmit(onSubmit)} className="space-y-4" noValidate>
        <FormField label="Email" id="email" type="email" autoComplete="email" error={errors.email?.message}
          {...register('email', { required: 'Email is required', pattern: { value: /^\S+@\S+\.\S+$/, message: 'Enter a valid email' } })} />
        <FormField label="Password" id="password" type="password" autoComplete="current-password" error={errors.password?.message}
          {...register('password', { required: 'Password is required' })} />
        <button disabled={isSubmitting} className="w-full rounded-lg bg-brand-600 py-2.5 font-medium text-white hover:bg-brand-700 disabled:opacity-60">
          {isSubmitting ? 'Logging in…' : 'Log in'}
        </button>
      </form>
    </AuthLayout>
  )
}
