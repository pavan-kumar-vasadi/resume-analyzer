import { Link } from 'react-router-dom'
import { Bookmark } from 'lucide-react'

const chip = (cls) => `rounded-full px-2.5 py-1 text-xs ${cls}`

export default function MatchScoreCard({ rec, onToggleSave }) {
  const pct = Math.round(rec.matchScore)
  return (
    <article className="rounded-2xl border border-slate-100 bg-white p-5 shadow-sm">
      <div className="flex items-start justify-between gap-4">
        <div>
          <Link to={`/jobs/${rec.jobId}`} className="text-lg font-semibold hover:text-brand-600">{rec.title}</Link>
          <p className="text-sm text-slate-600">{rec.companyName}{rec.location ? ` · ${rec.location}` : ''} · {rec.employmentType} · {rec.experienceLevel}
            {rec.demo && <span className="ml-2 rounded-full bg-amber-50 px-2 py-0.5 text-xs text-amber-700">Demo record</span>}</p>
        </div>
        <div className="text-right"><p className="text-3xl font-bold text-brand-600">{pct}%</p><p className="text-xs text-slate-500">skill overlap estimate</p></div>
      </div>
      <div className="mt-3 h-2 rounded-full bg-slate-100" role="progressbar" aria-valuenow={pct} aria-valuemin="0" aria-valuemax="100"><div className="h-2 rounded-full bg-brand-600" style={{ width: `${pct}%` }} /></div>
      {rec.requiredCoverage !== null && <p className="mt-2 text-xs text-slate-500">Required skills covered: {rec.matchedSkills.length} of {rec.matchedSkills.length + rec.missingSkills.length} ({rec.requiredCoverage}%)</p>}
      {rec.note && <p className="mt-2 text-xs text-slate-500">{rec.note}</p>}

      {rec.matchedSkills.length > 0 && <div className="mt-3"><p className="text-xs font-medium text-slate-600">Matched skills</p>
        <div className="mt-1 flex flex-wrap gap-1.5">{rec.matchedSkills.map((s) => <span key={s} className={chip('bg-emerald-50 text-emerald-700')}>{s}</span>)}</div></div>}
      {rec.missingSkills.length > 0 && <div className="mt-3"><p className="text-xs font-medium text-slate-600">Missing required skills</p>
        <div className="mt-1 flex flex-wrap gap-1.5">{rec.missingSkills.map((s) => <span key={s} className={chip('bg-rose-50 text-rose-700')}>{s}</span>)}</div></div>}
      {(rec.preferredMatched.length > 0 || rec.preferredMissing.length > 0) && <div className="mt-3"><p className="text-xs font-medium text-slate-600">Preferred skills</p>
        <div className="mt-1 flex flex-wrap gap-1.5">{rec.preferredMatched.map((s) => <span key={s} className={chip('bg-teal-50 text-teal-700')}>{s} ✓</span>)}
          {rec.preferredMissing.map((s) => <span key={s} className={chip('bg-slate-100 text-slate-600')}>{s}</span>)}</div></div>}

      {rec.learningTopics.length > 0 && (
        <details className="mt-3 rounded-lg bg-slate-50 p-3 text-sm">
          <summary className="cursor-pointer font-medium">Suggested learning topics ({rec.learningTopics.length})</summary>
          <ul className="mt-2 list-disc space-y-1 pl-5 text-slate-700">{rec.learningTopics.map((t) => <li key={t.skill}><b>{t.skill}:</b> {t.suggestion}</li>)}</ul>
        </details>)}

      <div className="mt-4 flex gap-3">
        <Link to={`/jobs/${rec.jobId}`} className="rounded-lg bg-brand-600 px-4 py-2 text-sm font-medium text-white hover:bg-brand-700">Job details</Link>
        <button onClick={() => onToggleSave(rec)} aria-pressed={rec.saved} className={`inline-flex items-center gap-1 rounded-lg border px-4 py-2 text-sm font-medium ${rec.saved ? 'border-brand-600 bg-brand-50 text-brand-700' : 'border-slate-200 hover:bg-slate-50'}`}>
          <Bookmark className="h-4 w-4" fill={rec.saved ? 'currentColor' : 'none'} aria-hidden />{rec.saved ? 'Saved' : 'Save job'}</button>
      </div>
    </article>
  )
}
