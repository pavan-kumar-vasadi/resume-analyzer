import { useEffect, useState } from 'react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import api, { errorMessage } from '../api/client.js'
import { useAuth } from '../context/AuthContext.jsx'
import SiteHeader from '../components/SiteHeader.jsx'
import SkillBadge from '../components/SkillBadge.jsx'

export default function JobDetails() {
  const { id } = useParams()
  const { user } = useAuth()
  const nav = useNavigate()
  const [job, setJob] = useState(null)
  const [error, setError] = useState('')
  useEffect(() => { api.get(`/jobs/${id}`).then((r) => setJob(r.data)).catch((e) => setError(errorMessage(e))) }, [id, user])

  const toggle = async () => {
    if (!user) return nav('/login')
    try {
      if (job.saved) await api.delete(`/saved-jobs/${id}`); else await api.post(`/saved-jobs/${id}`)
      setJob({ ...job, saved: !job.saved })
    } catch (e) { setError(errorMessage(e)) }
  }

  return (
    <div>
      <SiteHeader />
      <main className="mx-auto max-w-3xl px-6 py-10">
        {error && <p className="rounded-lg bg-red-50 p-3 text-sm text-red-700" role="alert">{error} <Link to="/jobs" className="underline">Back to jobs</Link></p>}
        {!job && !error && <p className="text-slate-500" role="status">Loading…</p>}
        {job && (<>
          <h1 className="text-3xl font-bold">{job.title}</h1>
          <p className="mt-1 text-slate-600">{job.companyName}{job.location ? ` · ${job.location}` : ''}
            {job.demo && <span className="ml-2 rounded-full bg-amber-50 px-2 py-0.5 text-xs text-amber-700">Demo record, not a real vacancy</span>}</p>
          <p className="mt-2 text-sm text-slate-500">{job.employmentType} · {job.experienceLevel} level · {job.minExperienceYears}+ yrs experience{job.salaryRange ? ` · ${job.salaryRange}` : ''}</p>
          <div className="mt-5 flex gap-3">
            <button onClick={toggle} className="rounded-lg border border-slate-200 px-4 py-2 text-sm font-medium hover:bg-slate-50">{job.saved ? 'Saved ✓ (click to remove)' : 'Save job'}</button>
            {job.applicationUrl && <a href={job.applicationUrl} target="_blank" rel="noopener noreferrer" className="rounded-lg bg-brand-600 px-4 py-2 text-sm font-medium text-white hover:bg-brand-700">Apply (external site)</a>}
          </div>
          <section className="mt-8"><h2 className="font-semibold">Description</h2><p className="mt-2 whitespace-pre-wrap text-slate-700">{job.description}</p></section>
          <section className="mt-6"><h2 className="font-semibold">Required skills</h2>
            <div className="mt-2 flex flex-wrap gap-2">{job.requiredSkills.length ? job.requiredSkills.map((s) => <SkillBadge key={s.id} name={s.name} />) : <span className="text-sm text-slate-500">None listed</span>}</div></section>
          <section className="mt-6"><h2 className="font-semibold">Preferred skills</h2>
            <div className="mt-2 flex flex-wrap gap-2">{job.preferredSkills.length ? job.preferredSkills.map((s) => <SkillBadge key={s.id} name={s.name} />) : <span className="text-sm text-slate-500">None listed</span>}</div></section>
          <div className="mt-8"><Link to="/jobs" className="text-brand-600 underline">Back to jobs</Link></div>
        </>)}
      </main>
    </div>
  )
}
