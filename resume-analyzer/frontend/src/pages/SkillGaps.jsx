import { useEffect, useState } from 'react'
import { Link, useSearchParams } from 'react-router-dom'
import api, { errorMessage } from '../api/client.js'
import AppHeader from '../components/AppHeader.jsx'

export default function SkillGaps() {
  const [params] = useSearchParams()
  const [resumes, setResumes] = useState(null)
  const [rid, setRid] = useState(params.get('resume') || '')
  const [scope, setScope] = useState('top')
  const [data, setData] = useState(null)
  const [error, setError] = useState('')

  useEffect(() => {
    api.get('/resumes/my').then(({ data: list }) => {
      setResumes(list)
      if (!rid && list.length) setRid(String((list.find((x) => x.status === 'ANALYZED') || list[0]).id))
    }).catch((e) => setError(errorMessage(e)))
  }, []) // eslint-disable-line
  useEffect(() => {
    if (!rid) return
    setData(null); setError('')
    api.get(`/recommendations/${rid}/skill-gaps`, { params: { scope } }).then((r) => setData(r.data)).catch((e) => setError(errorMessage(e)))
  }, [rid, scope])

  const tab = (v, label) => <button onClick={() => setScope(v)} aria-pressed={scope === v} className={`rounded-lg px-4 py-2 text-sm font-medium ${scope === v ? 'bg-brand-600 text-white' : 'border border-slate-200 hover:bg-slate-50'}`}>{label}</button>
  return (
    <div>
      <AppHeader />
      <main className="mx-auto max-w-4xl px-6 py-10">
        <h1 className="text-3xl font-bold">Skill gap analysis</h1>
        <p className="mt-1 text-sm text-slate-500">Required skills that are missing most often across your target jobs.</p>
        {resumes && resumes.length === 0 && <div className="mt-8 rounded-2xl border border-dashed border-slate-200 p-10 text-center"><Link to="/resumes/upload" className="text-brand-600 underline">Upload a resume first</Link></div>}
        {resumes && resumes.length > 0 && (
          <div className="mt-5 flex flex-wrap items-end gap-3">
            <label className="text-sm font-medium">Resume
              <select value={rid} onChange={(e) => setRid(e.target.value)} className="mt-1 block rounded-lg border border-slate-200 px-3 py-2">
                {resumes.map((r) => <option key={r.id} value={r.id}>{r.fileName}</option>)}</select></label>
            <div className="flex gap-2">{tab('top', 'Top 10 matching jobs')}{tab('saved', 'My saved jobs')}</div>
          </div>)}
        {error && <p className="mt-5 rounded-lg bg-red-50 p-3 text-sm text-red-700" role="alert">{error}</p>}
        {!data && !error && rid && <p className="mt-6 text-slate-500" role="status">Loading…</p>}
        {data && (<>
          <p className="mt-5 text-sm text-slate-600">Based on {data.targetJobCount} job(s).</p>
          {data.note && <p className="mt-3 rounded-lg bg-amber-50 p-3 text-sm text-amber-800">{data.note} <Link to={`/recommendations?resume=${rid}`} className="underline">Go to recommendations</Link></p>}
          <ul className="mt-4 space-y-3">{data.items.map((g) => (
            <li key={g.skill} className="rounded-2xl border border-slate-100 p-4">
              <div className="flex items-center justify-between"><p className="font-semibold">{g.skill} <span className="text-xs font-normal text-slate-500">{g.category}</span></p>
                <p className="text-sm text-slate-600">missing in {g.missingInJobs} of {data.targetJobCount} jobs ({g.percentOfTargets}%)</p></div>
              <div className="mt-2 h-2 rounded-full bg-slate-100"><div className="h-2 rounded-full bg-rose-400" style={{ width: `${g.percentOfTargets}%` }} /></div>
              <p className="mt-2 text-sm text-slate-600">{g.suggestion}</p>
            </li>))}</ul>
        </>)}
      </main>
    </div>
  )
}
