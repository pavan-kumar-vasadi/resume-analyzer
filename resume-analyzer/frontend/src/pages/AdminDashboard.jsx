import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { Bar, BarChart, ResponsiveContainer, Tooltip, XAxis, YAxis } from 'recharts'
import { Briefcase, FileText, Target, Users } from 'lucide-react'
import api, { errorMessage } from '../api/client.js'

const Stat = ({ icon: Icon, label, value, hint }) => (
  <div className="rounded-2xl border border-slate-100 bg-white p-5 shadow-sm">
    <Icon className="h-5 w-5 text-brand-600" aria-hidden /><p className="mt-3 text-3xl font-bold">{value}</p>
    <p className="text-sm text-slate-600">{label}</p>{hint && <p className="text-xs text-slate-400">{hint}</p>}
  </div>
)

const Card = ({ title, sub, children }) => (
  <section className="rounded-2xl border border-slate-100 bg-white p-5 shadow-sm">
    <h2 className="font-semibold">{title}</h2>{sub && <p className="text-xs text-slate-500">{sub}</p>}{children}
  </section>
)

const Empty = ({ children }) => <p className="mt-6 text-sm text-slate-500">{children}</p>
const when = (t) => (t ? new Date(t).toLocaleString() : '-')
const dayLabel = (iso) => new Date(`${iso}T00:00:00`).toLocaleDateString(undefined, { day: 'numeric', month: 'short' })

export default function AdminDashboard() {
  const [d, setD] = useState(null)
  const [error, setError] = useState('')
  useEffect(() => { api.get('/admin/dashboard').then((r) => setD(r.data)).catch((e) => setError(errorMessage(e))) }, [])

  const signups = d ? d.registrationsByDay.reduce((n, x) => n + x.count, 0) : 0

  return (
    <main className="mx-auto max-w-5xl px-6 py-10">
      <h1 className="text-3xl font-bold">Admin dashboard</h1>
      <p className="mt-1 text-slate-600">Platform-wide figures, read live from the database.</p>
      <div className="mt-5 flex flex-wrap gap-3">
        <Link to="/admin/users" className="rounded-lg border border-slate-200 bg-white px-4 py-2 text-sm font-medium hover:bg-slate-50">View all users</Link>
        <Link to="/admin/jobs" className="rounded-lg border border-slate-200 bg-white px-4 py-2 text-sm font-medium hover:bg-slate-50">Manage jobs</Link>
        <Link to="/admin/jobs/new" className="rounded-lg bg-brand-600 px-4 py-2 text-sm font-medium text-white hover:bg-brand-700">Add job</Link>
      </div>

      {error && <p className="mt-6 rounded-lg bg-red-50 p-3 text-sm text-red-700" role="alert">{error}</p>}
      {!d && !error && <p className="mt-8 text-slate-500" role="status">Loading…</p>}

      {d && (<>
        <div className="mt-8 grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
          <Stat icon={Users} label="Registered users" value={d.totalUsers} hint={`${d.totalAdmins} admin${d.totalAdmins === 1 ? '' : 's'}`} />
          <Stat icon={FileText} label="Resumes uploaded" value={d.totalResumes} />
          <Stat icon={Briefcase} label="Active jobs" value={d.activeJobs} hint={`${d.totalJobs} in total`} />
          <Stat icon={Target} label="Resumes matched to jobs" value={d.analyzedResumes} hint="at least one job analysis run" />
        </div>

        <div className="mt-8 grid gap-6 lg:grid-cols-2">
          <Card title="New registrations" sub={`Last ${d.registrationsByDay.length} days (${signups} sign-up${signups === 1 ? '' : 's'})`}>
            {signups === 0 ? <Empty>No new registrations in this period.</Empty> : (
              <div className="mt-3 h-56" role="img" aria-label="New registrations per day bar chart"><ResponsiveContainer width="100%" height="100%">
                <BarChart data={d.registrationsByDay.map((x) => ({ day: dayLabel(x.date), count: x.count }))}>
                  <XAxis dataKey="day" tick={{ fontSize: 11 }} minTickGap={16} /><YAxis allowDecimals={false} width={28} /><Tooltip />
                  <Bar dataKey="count" name="Registrations" fill="#4f46e5" radius={[6, 6, 0, 0]} />
                </BarChart>
              </ResponsiveContainer></div>)}
          </Card>

          <Card title="Most common skills" sub="Number of resumes that contain each skill">
            {d.topSkills.length === 0 ? <Empty>No skills detected yet.</Empty> : (
              <div className="mt-3 h-56" role="img" aria-label="Most common skills bar chart"><ResponsiveContainer width="100%" height="100%">
                <BarChart data={d.topSkills} layout="vertical" margin={{ left: 10 }}>
                  <XAxis type="number" allowDecimals={false} /><YAxis type="category" dataKey="label" width={100} /><Tooltip />
                  <Bar dataKey="count" name="Resumes" fill="#0d9488" radius={[0, 6, 6, 0]} />
                </BarChart>
              </ResponsiveContainer></div>)}
          </Card>

          <Card title="Jobs by experience level" sub="All jobs, active and inactive">
            {d.totalJobs === 0 ? <Empty>No jobs yet.</Empty> : (
              <div className="mt-3 h-48" role="img" aria-label="Jobs by experience level bar chart"><ResponsiveContainer width="100%" height="100%">
                <BarChart data={d.jobsByLevel}>
                  <XAxis dataKey="label" tick={{ fontSize: 11 }} /><YAxis allowDecimals={false} width={28} /><Tooltip />
                  <Bar dataKey="count" name="Jobs" fill="#8b5cf6" radius={[6, 6, 0, 0]} />
                </BarChart>
              </ResponsiveContainer></div>)}
          </Card>

          <Card title="Resumes by status" sub="All uploaded resumes">
            {d.totalResumes === 0 ? <Empty>No resumes uploaded yet.</Empty> : (
              <div className="mt-3 h-48" role="img" aria-label="Resumes by status bar chart"><ResponsiveContainer width="100%" height="100%">
                <BarChart data={d.resumesByStatus}>
                  <XAxis dataKey="label" tick={{ fontSize: 11 }} /><YAxis allowDecimals={false} width={28} /><Tooltip />
                  <Bar dataKey="count" name="Resumes" fill="#f59e0b" radius={[6, 6, 0, 0]} />
                </BarChart>
              </ResponsiveContainer></div>)}
          </Card>

          <Card title="Recent registrations">
            {d.recentUsers.length === 0 ? <Empty>No users yet.</Empty> : (
              <ul className="mt-3 divide-y divide-slate-100 text-sm">{d.recentUsers.map((u) => (
                <li key={u.id} className="flex items-center justify-between gap-3 py-2">
                  <span className="min-w-0"><span className="block truncate font-medium">{u.fullName}</span><span className="block truncate text-xs text-slate-500">{u.email}</span></span>
                  <span className="shrink-0 text-right text-xs text-slate-400">{u.role}<br />{u.createdAt ? new Date(u.createdAt).toLocaleDateString() : '-'}</span>
                </li>))}</ul>)}
          </Card>

          <Card title="Recently uploaded resumes">
            {d.recentResumes.length === 0 ? <Empty>No resumes yet.</Empty> : (
              <ul className="mt-3 divide-y divide-slate-100 text-sm">{d.recentResumes.map((r) => (
                <li key={r.id} className="flex items-center justify-between gap-3 py-2">
                  <span className="min-w-0"><span className="block truncate font-medium">{r.fileName}</span><span className="block truncate text-xs text-slate-500">{r.userName} · {r.skillCount} skills · {r.status}</span></span>
                  <span className="shrink-0 text-xs text-slate-400">{when(r.uploadedAt)}</span>
                </li>))}</ul>)}
          </Card>

          <Card title="Recent job analyses">
            {d.recentAnalyses.length === 0 ? <Empty>No job analyses run yet.</Empty> : (
              <ul className="mt-3 divide-y divide-slate-100 text-sm">{d.recentAnalyses.map((a) => (
                <li key={a.resumeId} className="flex items-center justify-between gap-3 py-2">
                  <span className="min-w-0"><span className="block truncate font-medium">{a.fileName}</span>
                    <span className="block truncate text-xs text-slate-500">{a.userName} · {a.jobsMatched} jobs compared{a.bestJobTitle ? ` · best: ${a.bestJobTitle} (${Math.round(a.bestScore)}%)` : ''}</span></span>
                  <span className="shrink-0 text-xs text-slate-400">{when(a.analyzedAt)}</span>
                </li>))}</ul>)}
          </Card>
        </div>
      </>)}
    </main>
  )
}
