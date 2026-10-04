import { useEffect, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import api, { errorMessage } from '../api/client.js'
import AppHeader from '../components/AppHeader.jsx'
import SkillBadge from '../components/SkillBadge.jsx'

export default function ResumeAnalysis() {
  const { id } = useParams()
  const [a, setA] = useState(null)
  const [error, setError] = useState('')
  useEffect(() => { api.get(`/resumes/${id}/analysis`).then((r) => setA(r.data)).catch((e) => setError(errorMessage(e))) }, [id])

  const byCat = (a?.skills || []).reduce((m, s) => ((m[s.category] ||= []).push(s.name), m), {})
  return (
    <div>
      <AppHeader />
      <main className="mx-auto max-w-4xl px-6 py-12">
        {error && <p className="rounded-lg bg-red-50 p-3 text-sm text-red-700" role="alert">{error} <Link to="/resumes" className="underline">Back to my resumes</Link></p>}
        {!a && !error && <p className="text-slate-500" role="status">Loading…</p>}
        {a && (<>
          <h1 className="text-3xl font-bold">Resume analysis</h1>
          <p className="mt-1 text-slate-600">{a.fileName}</p>
          {a.message && <p className="mt-6 rounded-lg bg-amber-50 p-4 text-sm text-amber-800" role="alert">{a.message}</p>}
          {!a.message && (<>
            <section className="mt-8 rounded-2xl border border-slate-100 p-6 shadow-sm">
              <div className="flex items-end justify-between"><h2 className="text-lg font-semibold">Completeness score</h2><p className="text-4xl font-bold text-brand-600">{a.overallScore}<span className="text-lg text-slate-400">/100</span></p></div>
              <div className="mt-3 h-3 rounded-full bg-slate-100" role="progressbar" aria-valuenow={a.overallScore} aria-valuemin="0" aria-valuemax="100"><div className="h-3 rounded-full bg-brand-600" style={{ width: `${a.overallScore}%` }} /></div>
              <p className="mt-3 text-xs text-slate-500">{a.scoreNote}</p>
              <ul className="mt-5 grid gap-3 sm:grid-cols-2">{a.sections.map((s) => (
                <li key={s.name}><div className="flex justify-between text-sm"><span>{s.name}</span><span className={s.present ? 'text-emerald-700' : 'text-red-600'}>{s.score}/{s.max}</span></div>
                  <div className="mt-1 h-1.5 rounded-full bg-slate-100"><div className={`h-1.5 rounded-full ${s.present ? 'bg-emerald-500' : 'bg-red-400'}`} style={{ width: `${s.max ? (s.score / s.max) * 100 : 0}%` }} /></div></li>))}</ul>
            </section>
            <section className="mt-6 rounded-2xl border border-slate-100 p-6">
              <h2 className="text-lg font-semibold">Extracted details</h2>
              <dl className="mt-3 grid gap-2 text-sm sm:grid-cols-3">
                {[['Name', a.candidateName], ['Email', a.email], ['Phone', a.phone]].map(([k, v]) => (<div key={k}><dt className="text-slate-500">{k}</dt><dd className="font-medium">{v || 'Not detected'}</dd></div>))}
              </dl>
              <p className="mt-4 text-sm text-slate-500">Education keywords: <span className="text-navy">{a.educationKeywords.join(', ') || 'none found'}</span></p>
              <p className="mt-1 text-sm text-slate-500">Experience keywords: <span className="text-navy">{a.experienceKeywords.join(', ') || 'none found'}</span></p>
            </section>
            <section className="mt-6 rounded-2xl border border-slate-100 p-6">
              <h2 className="text-lg font-semibold">Detected skills ({a.skills.length})</h2>
              <p className="text-xs text-slate-500">Found by dictionary matching. Skills not in the dictionary are not detected.</p>
              {a.skills.length === 0 && <p className="mt-3 text-sm text-slate-600">No dictionary skills were found in this resume.</p>}
              {Object.entries(byCat).map(([cat, names]) => (<div key={cat} className="mt-4"><p className="text-sm font-medium text-slate-600">{cat}</p><div className="mt-2 flex flex-wrap gap-2">{names.map((n) => <SkillBadge key={n} name={n} />)}</div></div>))}
            </section>
            <section className="mt-6 rounded-2xl border border-slate-100 p-6">
              <h2 className="text-lg font-semibold">Suggestions</h2>
              <ul className="mt-3 list-disc space-y-1 pl-5 text-sm text-slate-700">{a.suggestions.map((s) => <li key={s}>{s}</li>)}</ul>
            </section>
            <details className="mt-6 rounded-2xl border border-slate-100 p-6">
              <summary className="cursor-pointer font-semibold">Extracted resume text</summary>
              <pre className="mt-4 max-h-96 overflow-auto whitespace-pre-wrap text-sm text-slate-700">{a.extractedText}</pre>
            </details>
          </>)}
          <div className="mt-8 flex gap-5">
            {!a.message && <Link to={`/recommendations?resume=${id}`} className="rounded-lg bg-brand-600 px-4 py-2 text-sm font-medium text-white hover:bg-brand-700">Find matching jobs</Link>}
            <Link to="/resumes" className="py-2 text-brand-600 underline">Back to my resumes</Link></div>
        </>)}
      </main>
    </div>
  )
}
