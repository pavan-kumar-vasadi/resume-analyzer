import { useEffect, useState } from 'react'
import { useForm } from 'react-hook-form'
import { Link, useNavigate, useParams } from 'react-router-dom'
import api, { errorMessage } from '../api/client.js'
import AppHeader from '../components/AppHeader.jsx'
import FormField from '../components/FormField.jsx'
import { useToast } from '../context/ToastContext.jsx'

const box = 'mt-1 w-full rounded-lg border border-slate-200 px-3 py-2'

export default function AdminJobForm() {
  const { id } = useParams()
  const edit = !!id
  const nav = useNavigate()
  const toast = useToast()
  const [skills, setSkills] = useState([])
  const [sel, setSel] = useState({}) // skillId -> 'R' (required) | 'P' (preferred)
  const [serverError, setServerError] = useState('')
  const [loaded, setLoaded] = useState(!edit)
  const { register, handleSubmit, reset, formState: { errors, isSubmitting } } = useForm({
    defaultValues: { employmentType: 'Full-time', experienceLevel: 'Entry', minExperienceYears: 0, active: true },
  })

  useEffect(() => { api.get('/skills').then((r) => setSkills(r.data)).catch((e) => setServerError(errorMessage(e))) }, [])
  useEffect(() => {
    if (!edit) return
    api.get(`/admin/jobs/${id}`).then(({ data: j }) => {
      reset({ ...j, location: j.location || '', salaryRange: j.salaryRange || '', applicationUrl: j.applicationUrl || '' })
      const m = {}
      j.requiredSkills.forEach((s) => (m[s.id] = 'R')); j.preferredSkills.forEach((s) => (m[s.id] = 'P'))
      setSel(m); setLoaded(true)
    }).catch((e) => setServerError(errorMessage(e)))
  }, [id, edit, reset])

  const cycle = (sid) => setSel((m) => { const n = { ...m }; if (!n[sid]) n[sid] = 'R'; else if (n[sid] === 'R') n[sid] = 'P'; else delete n[sid]; return n })

  const onSubmit = async (v) => {
    setServerError('')
    const ids = (t) => Object.entries(sel).filter(([, x]) => x === t).map(([k]) => Number(k))
    const body = { ...v, minExperienceYears: Number(v.minExperienceYears), requiredSkillIds: ids('R'), preferredSkillIds: ids('P') }
    try { if (edit) await api.put(`/admin/jobs/${id}`, body); else await api.post('/admin/jobs', body); toast.push(edit ? 'Job updated' : 'Job created'); nav('/admin/jobs') }
    catch (e) { setServerError(errorMessage(e)); toast.push('Could not save the job', 'error') }
  }

  return (
    <div>
      <AppHeader />
      <main className="mx-auto max-w-2xl px-6 py-10">
        <h1 className="text-3xl font-bold">{edit ? 'Edit job' : 'Add job'}</h1>
        <p className="mt-1 text-sm text-slate-500">Enter only verified, real information.</p>
        {serverError && <p className="mt-4 rounded-lg bg-red-50 p-3 text-sm text-red-700" role="alert">{serverError}</p>}
        {!loaded ? <p className="mt-6 text-slate-500" role="status">Loading…</p> : (
          <form onSubmit={handleSubmit(onSubmit)} className="mt-6 space-y-4" noValidate>
            <FormField label="Job title" id="title" error={errors.title?.message} {...register('title', { required: 'Title is required' })} />
            <FormField label="Company name" id="companyName" error={errors.companyName?.message} {...register('companyName', { required: 'Company name is required' })} />
            <div><label htmlFor="description" className="block text-sm font-medium">Description</label>
              <textarea id="description" rows={5} className={box} {...register('description', { required: 'Description is required' })} />
              {errors.description && <p className="mt-1 text-sm text-red-600" role="alert">{errors.description.message}</p>}</div>
            <div className="grid gap-4 sm:grid-cols-2">
              <FormField label="Location" id="location" {...register('location')} />
              <FormField label="Salary range (optional)" id="salaryRange" {...register('salaryRange')} />
              <div><label htmlFor="employmentType" className="block text-sm font-medium">Employment type</label>
                <select id="employmentType" className={box} {...register('employmentType')}>{['Full-time', 'Part-time', 'Internship', 'Contract'].map((x) => <option key={x}>{x}</option>)}</select></div>
              <div><label htmlFor="experienceLevel" className="block text-sm font-medium">Experience level</label>
                <select id="experienceLevel" className={box} {...register('experienceLevel')}>{['Entry', 'Mid', 'Senior'].map((x) => <option key={x}>{x}</option>)}</select></div>
              <FormField label="Minimum experience (years)" id="minExperienceYears" type="number" min="0" error={errors.minExperienceYears?.message}
                {...register('minExperienceYears', { min: { value: 0, message: 'Cannot be negative' } })} />
              <FormField label="Application URL (optional)" id="applicationUrl" error={errors.applicationUrl?.message}
                {...register('applicationUrl', { pattern: { value: /^$|^https?:\/\/\S+$/, message: 'Must start with http:// or https://' } })} />
            </div>
            <fieldset><legend className="text-sm font-medium">Skills</legend>
              <p className="text-xs text-slate-500">Click a skill to cycle: none → <b>required</b> → <b>preferred</b> → none.</p>
              <div className="mt-2 flex flex-wrap gap-2">{skills.map((s) => (
                <button type="button" key={s.id} onClick={() => cycle(s.id)} aria-label={`${s.name}: ${sel[s.id] === 'R' ? 'required' : sel[s.id] === 'P' ? 'preferred' : 'not selected'}`}
                  className={`rounded-full border px-3 py-1 text-sm ${sel[s.id] === 'R' ? 'border-brand-600 bg-brand-600 text-white' : sel[s.id] === 'P' ? 'border-emerald-500 bg-emerald-50 text-emerald-700' : 'border-slate-200 text-slate-600 hover:bg-slate-50'}`}>
                  {s.name}{sel[s.id] === 'R' ? ' (R)' : sel[s.id] === 'P' ? ' (P)' : ''}</button>))}</div>
            </fieldset>
            <label className="flex items-center gap-2 text-sm"><input type="checkbox" {...register('active')} /> Active (visible to students)</label>
            <div className="flex gap-3">
              <button disabled={isSubmitting} className="rounded-lg bg-brand-600 px-5 py-2.5 font-medium text-white hover:bg-brand-700 disabled:opacity-60">{isSubmitting ? 'Saving…' : 'Save job'}</button>
              <Link to="/admin/jobs" className="rounded-lg border border-slate-200 px-5 py-2.5 font-medium hover:bg-slate-50">Cancel</Link>
            </div>
          </form>)}
      </main>
    </div>
  )
}
