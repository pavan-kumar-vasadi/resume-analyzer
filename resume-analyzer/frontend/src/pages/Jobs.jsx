import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import api, { errorMessage } from '../api/client.js'
import { useAuth } from '../context/AuthContext.jsx'
import SiteHeader from '../components/SiteHeader.jsx'
import JobCard from '../components/JobCard.jsx'

const LEVELS = ['Entry', 'Mid', 'Senior']
const TYPES = ['Full-time', 'Part-time', 'Internship', 'Contract']
const sel = 'mt-1 w-full rounded-lg border border-slate-200 px-3 py-2 text-sm'

export default function Jobs() {
  const { user } = useAuth()
  const nav = useNavigate()
  const [f, setF] = useState({ q: '', location: '', experienceLevel: '', employmentType: '', skill: '' })
  const [text, setText] = useState('')
  const [page, setPage] = useState(0)
  const [data, setData] = useState(null)
  const [skills, setSkills] = useState([])
  const [error, setError] = useState('')

  useEffect(() => { api.get('/skills').then((r) => setSkills(r.data)).catch(() => {}) }, [])
  useEffect(() => {
    const params = Object.fromEntries(Object.entries({ ...f, page, size: 6 }).filter(([, v]) => v !== ''))
    setError('')
    api.get('/jobs', { params }).then((r) => setData(r.data)).catch((e) => setError(errorMessage(e)))
  }, [f, page, user])

  const change = (k, v) => { setF((o) => ({ ...o, [k]: v })); setPage(0) }
  const clear = () => { setF({ q: '', location: '', experienceLevel: '', employmentType: '', skill: '' }); setText(''); setPage(0) }
  const toggleSave = async (job) => {
    if (!user) return nav('/login')
    try {
      if (job.saved) await api.delete(`/saved-jobs/${job.id}`); else await api.post(`/saved-jobs/${job.id}`)
      setData((d) => ({ ...d, items: d.items.map((j) => (j.id === job.id ? { ...j, saved: !j.saved } : j)) }))
    } catch (e) { setError(errorMessage(e)) }
  }

  return (
    <div>
      <SiteHeader />
      <main className="mx-auto max-w-5xl px-6 py-10">
        <h1 className="text-3xl font-bold">Browse jobs</h1>
        <p className="mt-1 text-sm text-slate-500">Only jobs entered by administrators are shown. Records marked "Demo" are sample data, not real vacancies.</p>
        <form onSubmit={(e) => { e.preventDefault(); change('q', text) }} className="mt-6 flex gap-2" role="search">
          <input value={text} onChange={(e) => setText(e.target.value)} placeholder="Search by job title or company" aria-label="Search jobs"
            className="w-full rounded-lg border border-slate-200 px-3 py-2" />
          <button className="rounded-lg bg-brand-600 px-5 py-2 font-medium text-white hover:bg-brand-700">Search</button>
        </form>
        <div className="mt-4 grid gap-3 sm:grid-cols-4">
          <label className="text-sm font-medium">Location<input value={f.location} onChange={(e) => change('location', e.target.value)} className={sel} placeholder="e.g. Remote" /></label>
          <label className="text-sm font-medium">Experience level<select value={f.experienceLevel} onChange={(e) => change('experienceLevel', e.target.value)} className={sel}><option value="">Any</option>{LEVELS.map((x) => <option key={x}>{x}</option>)}</select></label>
          <label className="text-sm font-medium">Employment type<select value={f.employmentType} onChange={(e) => change('employmentType', e.target.value)} className={sel}><option value="">Any</option>{TYPES.map((x) => <option key={x}>{x}</option>)}</select></label>
          <label className="text-sm font-medium">Skill<select value={f.skill} onChange={(e) => change('skill', e.target.value)} className={sel}><option value="">Any</option>{skills.map((s) => <option key={s.id}>{s.name}</option>)}</select></label>
        </div>
        <button onClick={clear} className="mt-3 text-sm text-brand-600 underline">Clear filters</button>
        {error && <p className="mt-6 rounded-lg bg-red-50 p-3 text-sm text-red-700" role="alert">{error}</p>}
        {!data && !error && <p className="mt-8 text-slate-500" role="status">Loading…</p>}
        {data && data.items.length === 0 && <div className="mt-8 rounded-2xl border border-dashed border-slate-200 p-10 text-center text-slate-600">No jobs match your filters.</div>}
        {data && data.items.length > 0 && (<>
          <p className="mt-6 text-sm text-slate-500">{data.totalItems} job{data.totalItems === 1 ? '' : 's'} found</p>
          <div className="mt-3 grid gap-4">{data.items.map((j) => <JobCard key={j.id} job={j} onToggleSave={toggleSave} />)}</div>
          <div className="mt-6 flex items-center justify-between text-sm">
            <button disabled={page === 0} onClick={() => setPage(page - 1)} className="rounded-lg border border-slate-200 px-4 py-2 disabled:opacity-40">Previous</button>
            <span>Page {data.page + 1} of {Math.max(1, data.totalPages)}</span>
            <button disabled={page + 1 >= data.totalPages} onClick={() => setPage(page + 1)} className="rounded-lg border border-slate-200 px-4 py-2 disabled:opacity-40">Next</button>
          </div>
        </>)}
      </main>
    </div>
  )
}
