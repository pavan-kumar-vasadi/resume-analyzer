import { useEffect, useState } from 'react'
import { Link, useSearchParams } from 'react-router-dom'
import api, { errorMessage } from '../api/client.js'
import AppHeader from '../components/AppHeader.jsx'
import MatchScoreCard from '../components/MatchScoreCard.jsx'

export default function Recommendations() {
  const [params] = useSearchParams()
  const [resumes, setResumes] = useState(null)
  const [rid, setRid] = useState(params.get('resume') || '')
  const [data, setData] = useState(null)
  const [busy, setBusy] = useState(false)
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
    api.get(`/recommendations/${rid}`).then((r) => setData(r.data)).catch((e) => setError(errorMessage(e)))
  }, [rid])

  const analyze = async () => {
    setBusy(true); setError('')
    try { setData((await api.post(`/recommendations/analyze/${rid}`)).data) } catch (e) { setError(errorMessage(e)) } finally { setBusy(false) }
  }
  const toggleSave = async (rec) => {
    try {
      if (rec.saved) await api.delete(`/saved-jobs/${rec.jobId}`); else await api.post(`/saved-jobs/${rec.jobId}`)
      setData((d) => ({ ...d, items: d.items.map((x) => (x.jobId === rec.jobId ? { ...x, saved: !x.saved } : x)) }))
    } catch (e) { setError(errorMessage(e)) }
  }

  return (
    <div>
      <AppHeader />
      <main className="mx-auto max-w-4xl px-6 py-10">
        <h1 className="text-3xl font-bold">Recommended jobs</h1>
        {resumes && resumes.length === 0 && <div className="mt-8 rounded-2xl border border-dashed border-slate-200 p-10 text-center"><p className="font-medium">Upload a resume first</p><Link to="/resumes/upload" className="mt-2 inline-block text-brand-600 underline">Upload a resume</Link></div>}
        {resumes && resumes.length > 0 && (
          <div className="mt-5 flex flex-wrap items-end gap-3">
            <label className="text-sm font-medium">Resume
              <select value={rid} onChange={(e) => setRid(e.target.value)} className="mt-1 block rounded-lg border border-slate-200 px-3 py-2">
                {resumes.map((r) => <option key={r.id} value={r.id}>{r.fileName}{r.status !== 'ANALYZED' ? ` (${r.status})` : ''}</option>)}</select></label>
            <button onClick={analyze} disabled={busy || !rid} className="rounded-lg bg-brand-600 px-5 py-2 font-medium text-white hover:bg-brand-700 disabled:opacity-60">
              {busy ? 'Analyzing…' : data?.analyzed ? 'Re-analyze' : 'Find matching jobs'}</button>
            {data?.analyzed && <Link to={`/skill-gaps?resume=${rid}`} className="rounded-lg border border-slate-200 px-4 py-2 text-sm font-medium hover:bg-slate-50">Skill gap analysis</Link>}
          </div>)}
        {error && <p className="mt-5 rounded-lg bg-red-50 p-3 text-sm text-red-700" role="alert">{error}</p>}
        {data && (<>
          <div className="mt-5 rounded-xl bg-brand-50 p-4 text-sm text-slate-700"><p>{data.formula}</p><p className="mt-1">{data.disclaimer}</p>
            <p className="mt-1 text-xs text-slate-500">Your resume has {data.resumeSkillCount} detected skill(s).{data.analyzedAt ? ` Last analyzed: ${new Date(data.analyzedAt).toLocaleString()}.` : ''}</p></div>
          {!data.analyzed && <p className="mt-6 text-slate-600">Click "Find matching jobs" to compare this resume with the active jobs.</p>}
          {data.analyzed && data.items.length === 0 && <p className="mt-6 text-slate-600">There are no active jobs to compare yet.</p>}
          <div className="mt-6 grid gap-4">{data.items.map((x) => <MatchScoreCard key={x.jobId} rec={x} onToggleSave={toggleSave} />)}</div>
        </>)}
        {!data && !error && rid && <p className="mt-6 text-slate-500" role="status">Loading…</p>}
      </main>
    </div>
  )
}
