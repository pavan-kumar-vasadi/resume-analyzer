import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import api, { errorMessage } from '../api/client.js'
import AppHeader from '../components/AppHeader.jsx'
import JobCard from '../components/JobCard.jsx'

export default function SavedJobs() {
  const [jobs, setJobs] = useState(null)
  const [error, setError] = useState('')
  useEffect(() => { api.get('/saved-jobs').then((r) => setJobs(r.data)).catch((e) => setError(errorMessage(e))) }, [])
  const remove = async (job) => {
    try { await api.delete(`/saved-jobs/${job.id}`); setJobs((l) => l.filter((j) => j.id !== job.id)) }
    catch (e) { setError(errorMessage(e)) }
  }
  return (
    <div>
      <AppHeader />
      <main className="mx-auto max-w-5xl px-6 py-10">
        <h1 className="text-3xl font-bold">Saved jobs</h1>
        {error && <p className="mt-4 rounded-lg bg-red-50 p-3 text-sm text-red-700" role="alert">{error}</p>}
        {!jobs && !error && <p className="mt-6 text-slate-500" role="status">Loading…</p>}
        {jobs && jobs.length === 0 && <div className="mt-8 rounded-2xl border border-dashed border-slate-200 p-10 text-center"><p className="font-medium">No saved jobs yet</p><Link to="/jobs" className="mt-2 inline-block text-brand-600 underline">Browse jobs</Link></div>}
        <div className="mt-6 grid gap-4">{jobs?.map((j) => <JobCard key={j.id} job={j} onToggleSave={remove} />)}</div>
      </main>
    </div>
  )
}
