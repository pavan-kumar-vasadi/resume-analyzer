import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { Bar, BarChart, ResponsiveContainer, Tooltip, XAxis, YAxis } from 'recharts'
import { FileText, Gauge, Layers, Target } from 'lucide-react'
import api, { errorMessage } from '../api/client.js'
import { useAuth } from '../context/AuthContext.jsx'

function Ring({ value }) {
  const c = 2 * Math.PI * 36
  return (
    <svg viewBox="0 0 90 90" className="h-24 w-24" role="img" aria-label={`Completeness score ${value} out of 100`}>
      <circle cx="45" cy="45" r="36" fill="none" stroke="#e2e8f0" strokeWidth="9" />
      <circle cx="45" cy="45" r="36" fill="none" stroke="#4f46e5" strokeWidth="9" strokeLinecap="round" strokeDasharray={c} strokeDashoffset={c * (1 - value / 100)} transform="rotate(-90 45 45)" />
      <text x="45" y="50" textAnchor="middle" className="fill-navy text-lg font-bold">{value}</text>
    </svg>
  )
}

const Stat = ({ icon: Icon, label, value, hint }) => (
  <div className="rounded-2xl border border-slate-100 bg-white p-5 shadow-sm">
    <Icon className="h-5 w-5 text-brand-600" aria-hidden /><p className="mt-3 text-3xl font-bold">{value}</p>
    <p className="text-sm text-slate-600">{label}</p>{hint && <p className="text-xs text-slate-400">{hint}</p>}
  </div>
)

export default function Dashboard() {
  const { user } = useAuth()
  const [d, setD] = useState(null)
  const [error, setError] = useState('')
  useEffect(() => { api.get('/dashboard').then((r) => setD(r.data)).catch((e) => setError(errorMessage(e))) }, [])

  return (
    <main className="mx-auto max-w-5xl px-6 py-10">
      <h1 className="text-3xl font-bold">Welcome, {user.fullName}</h1>
      <p className="mt-1 text-slate-600">Here is a summary of your resumes and job matches.</p>
      <div className="mt-5 flex flex-wrap gap-3">
        <Link to="/resumes/upload" className="rounded-lg bg-brand-600 px-4 py-2 text-sm font-medium text-white hover:bg-brand-700">Upload resume</Link>
        <Link to="/recommendations" className="rounded-lg border border-slate-200 bg-white px-4 py-2 text-sm font-medium hover:bg-slate-50">Find matching jobs</Link>
        <Link to="/jobs" className="rounded-lg border border-slate-200 bg-white px-4 py-2 text-sm font-medium hover:bg-slate-50">Browse jobs</Link>
        <Link to="/skill-gaps" className="rounded-lg border border-slate-200 bg-white px-4 py-2 text-sm font-medium hover:bg-slate-50">Skills analysis</Link>
      </div>
      {error && <p className="mt-6 rounded-lg bg-red-50 p-3 text-sm text-red-700" role="alert">{error}</p>}
      {!d && !error && <p className="mt-8 text-slate-500" role="status">Loading…</p>}
      {d && (<>
        <div className="mt-8 grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
          <Stat icon={FileText} label="Resumes uploaded" value={d.totalResumes} />
          <div className="flex items-center gap-3 rounded-2xl border border-slate-100 bg-white p-5 shadow-sm">
            {d.latestScore !== null ? <Ring value={d.latestScore} /> : <Gauge className="h-8 w-8 text-slate-300" aria-hidden />}
            <div><p className="text-sm text-slate-600">Latest completeness score</p><p className="text-xs text-slate-400">{d.latestResumeName || 'No analyzed resume yet'}</p></div>
          </div>
          <Stat icon={Layers} label="Detected skills" value={d.totalSkills} hint="distinct, across your resumes" />
          <Stat icon={Target} label="Recommended jobs" value={d.recommendedJobs} hint="with at least one matching skill" />
        </div>

        {d.totalResumes === 0 && <div className="mt-8 rounded-2xl border border-dashed border-slate-200 bg-white p-10 text-center"><p className="font-medium">No data yet</p><p className="text-sm text-slate-500">Upload your first resume to see skills, scores and job matches here.</p><Link to="/resumes/upload" className="mt-3 inline-block text-brand-600 underline">Upload a resume</Link></div>}

        {d.totalResumes > 0 && (<div className="mt-8 grid gap-6 lg:grid-cols-2">
          <section className="rounded-2xl border border-slate-100 bg-white p-5 shadow-sm"><h2 className="font-semibold">Skills distribution</h2><p className="text-xs text-slate-500">Skills in your latest analyzed resume, by category</p>
            {d.skillsByCategory.length === 0 ? <p className="mt-6 text-sm text-slate-500">No skills detected yet.</p> : (
              <div className="mt-3 h-56" role="img" aria-label="Skills by category bar chart"><ResponsiveContainer width="100%" height="100%">
                <BarChart data={d.skillsByCategory} layout="vertical" margin={{ left: 10 }}><XAxis type="number" allowDecimals={false} /><YAxis type="category" dataKey="category" width={90} /><Tooltip /><Bar dataKey="count" name="Skills" fill="#4f46e5" radius={[0, 6, 6, 0]} /></BarChart>
              </ResponsiveContainer></div>)}</section>
          <section className="rounded-2xl border border-slate-100 bg-white p-5 shadow-sm"><h2 className="font-semibold">Best job matches</h2><p className="text-xs text-slate-500">Skill overlap estimate (not a hiring probability)</p>
            {d.topMatches.length === 0 ? <p className="mt-6 text-sm text-slate-500">No matches yet. <Link to="/recommendations" className="text-brand-600 underline">Run a job analysis</Link>.</p> : (<>
              <div className="mt-3 h-44" role="img" aria-label="Top job match scores bar chart"><ResponsiveContainer width="100%" height="100%">
                <BarChart data={d.topMatches.map((m) => ({ name: m.title.replace('[DEMO] ', '').slice(0, 22), score: Math.round(m.score) }))} layout="vertical" margin={{ left: 10 }}><XAxis type="number" domain={[0, 100]} /><YAxis type="category" dataKey="name" width={140} /><Tooltip formatter={(v) => `${v}%`} /><Bar dataKey="score" name="Match" fill="#0d9488" radius={[0, 6, 6, 0]} /></BarChart>
              </ResponsiveContainer></div>
              <ul className="mt-2 space-y-1 text-sm">{d.topMatches.map((m) => <li key={m.jobId}><Link to={`/jobs/${m.jobId}`} className="text-brand-600 underline">{m.title}</Link> · {m.companyName}</li>)}</ul></>)}</section>
          <section className="rounded-2xl border border-slate-100 bg-white p-5 shadow-sm"><div className="flex items-center justify-between"><h2 className="font-semibold">Recently analyzed resumes</h2><Link to="/resumes" className="text-sm text-brand-600 underline">View all</Link></div>
            <ul className="mt-3 divide-y divide-slate-100 text-sm">{d.recentResumes.map((r) => (<li key={r.id} className="flex items-center justify-between py-2"><Link to={`/resumes/${r.id}`} className="font-medium hover:text-brand-600">{r.fileName}</Link><span className="text-slate-500">{r.skillCount} skills · {r.status}</span></li>))}</ul></section>
          <section className="rounded-2xl border border-slate-100 bg-white p-5 shadow-sm"><h2 className="font-semibold">Recent activity</h2>
            <ul className="mt-3 space-y-2 text-sm">{d.recentActivity.map((a, i) => (<li key={i} className="flex justify-between gap-3"><span>{a.text}</span><span className="shrink-0 text-xs text-slate-400">{new Date(a.at).toLocaleString()}</span></li>))}</ul></section>
        </div>)}
      </>)}
    </main>
  )
}
