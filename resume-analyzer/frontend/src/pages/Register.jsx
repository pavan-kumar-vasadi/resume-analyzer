import { useState } from 'react'
import { useForm } from 'react-hook-form'
import { Link, Navigate, useNavigate } from 'react-router-dom'
import AuthLayout from '../layouts/AuthLayout.jsx'
import FormField from '../components/FormField.jsx'
import { useAuth } from '../context/AuthContext.jsx'
import { errorMessage } from '../api/client.js'

export default function Register() {
  const { user, register: signUp } = useAuth()
  const nav = useNavigate()
  const [serverError, setServerError] = useState('')
  const { register, handleSubmit, watch, formState: { errors, isSubmitting } } = useForm()

  if (user) return <Navigate to="/dashboard" replace />

  const onSubmit = async (v) => {
    setServerError('')
    try { await signUp(v); nav('/dashboard', { replace: true }) }
    catch (e) { setServerError(errorMessage(e)) }
  }

  return (
    <AuthLayout title="Create your account" subtitle="Start analyzing your resume."
      footer={<>Already registered? <Link to="/login" className="font-medium text-brand-600">Log in</Link></>}>
      {serverError && <p className="mb-4 rounded-lg bg-red-50 p-3 text-sm text-red-700" role="alert">{serverError}</p>}
      <form onSubmit={handleSubmit(onSubmit)} className="space-y-4" noValidate>
        <FormField label="Full name" id="fullName" autoComplete="name" error={errors.fullName?.message}
          {...register('fullName', { required: 'Full name is required', maxLength: { value: 100, message: 'Too long' } })} />
        <FormField label="Email" id="email" type="email" autoComplete="email" error={errors.email?.message}
          {...register('email', { required: 'Email is required', pattern: { value: /^\S+@\S+\.\S+$/, message: 'Enter a valid email' } })} />
        <FormField label="Password" id="password" type="password" autoComplete="new-password" error={errors.password?.message}
          {...register('password', {
            required: 'Password is required',
            minLength: { value: 8, message: 'At least 8 characters' },
            validate: (v) => (/[A-Za-z]/.test(v) && /\d/.test(v)) || 'Use at least one letter and one digit',
          })} />
        <FormField label="Confirm password" id="confirmPassword" type="password" autoComplete="new-password" error={errors.confirmPassword?.message}
          {...register('confirmPassword', { required: 'Confirm your password', validate: (v) => v === watch('password') || 'Passwords do not match' })} />
        <button disabled={isSubmitting} className="w-full rounded-lg bg-brand-600 py-2.5 font-medium text-white hover:bg-brand-700 disabled:opacity-60">
          {isSubmitting ? 'Creating account…' : 'Create account'}
        </button>
      </form>
    </AuthLayout>
  )
}
