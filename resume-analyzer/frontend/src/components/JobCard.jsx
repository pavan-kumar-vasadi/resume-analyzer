import { Link } from 'react-router-dom'
import { Bookmark, MapPin } from 'lucide-react'
import SkillBadge from './SkillBadge.jsx'

export default function JobCard({ job, onToggleSave }) {
  return (
    <article className="rounded-2xl border border-slate-100 bg-white p-5 shadow-sm">
      <div className="flex items-start justify-between gap-3">
        <div>
          <Link to={`/jobs/${job.id}`} className="text-lg font-semibold hover:text-brand-600">{job.title}</Link>
          <p className="text-sm text-slate-600">{job.companyName}
            {job.demo && <span className="ml-2 rounded-full bg-amber-50 px-2 py-0.5 text-xs text-amber-700">Demo record</span>}</p>
        </div>
        {onToggleSave && (
          <button onClick={() => onToggleSave(job)} aria-pressed={job.saved} aria-label={job.saved ? 'Remove from saved jobs' : 'Save job'}
            className={`rounded-lg border p-2 ${job.saved ? 'border-brand-600 bg-brand-50 text-brand-600' : 'border-slate-200 text-slate-500 hover:bg-slate-50'}`}>
            <Bookmark className="h-4 w-4" fill={job.saved ? 'currentColor' : 'none'} aria-hidden />
          </button>
        )}
      </div>
      <p className="mt-2 flex flex-wrap items-center gap-x-4 text-sm text-slate-500">
        {job.location && <span className="inline-flex items-center gap-1"><MapPin className="h-3.5 w-3.5" aria-hidden />{job.location}</span>}
        <span>{job.employmentType}</span><span>{job.experienceLevel} level</span>
        {job.salaryRange && <span>{job.salaryRange}</span>}
      </p>
      {job.requiredSkills.length > 0 && (
        <div className="mt-3 flex flex-wrap gap-2">{job.requiredSkills.slice(0, 6).map((s) => <SkillBadge key={s.id} name={s.name} />)}</div>
      )}
    </article>
  )
}
