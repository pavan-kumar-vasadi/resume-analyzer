import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import api, { errorMessage } from '../api/client.js'
import AppHeader from '../components/AppHeader.jsx'
import ConfirmDialog from '../components/ConfirmDialog.jsx'
import { useToast } from '../context/ToastContext.jsx'

export default function AdminJobs() {
  const [q, setQ] = useState('')
  const [text, setText] = useState('')
  const [page, setPage] = useState(0)
  const [data, setData] = useState(null)
  const [error, setError] = useState('')
  const [target, setTarget] = useState(null)
  const [tick, setTick] = useState(0)
  const toast = useToast()

  useEffect(() => {
    api.get('/admin/jobs', { params: { ...(q && { q }), page, size: 8 } }).then((r) => setData(r.data)).catch((e) => setError(errorMessage(e)))
  }, [q, page, tick])

  const toggle = async (j) => {
    try { await api.patch(`/admin/jobs/${j.id}/active`, null, { params: { value: !j.active } }); setTick((t) => t + 1); toast.push(j.active ? 'Job deactivated' : 'Job activated') }
    catch (e) { setError(errorMessage(e)); toast.push('Could not update the job', 'error') }
  }
  const remove = async () => {
    const id = target.id; setTarget(null)
    try { await api.delete(`/admin/jobs/${id}`); setTick((t) => t + 1); toast.push('Job deleted') } catch (e) { setError(errorMessage(e)); toast.push('Could not delete the job', 'error') }
  }

  return (
    <div>
      <AppHeader />
      <main className="mx-auto max-w-5xl px-6 py-10">
        <div className="flex items-center justify-between"><h1 className="text-3xl font-bold">Manage jobs</h1>
          <Link to="/admin/jobs/new" className="rounded-lg bg-brand-600 px-4 py-2 text-sm font-medium text-white hover:bg-brand-700">Add job</Link></div>
        <form onSubmit={(e) => { e.preventDefault(); setQ(text); setPage(0) }} className="mt-5 flex gap-2" role="search">
          <input value={text} onChange={(e) => setText(e.target.value)} placeholder="Search title or company" aria-label="Search jobs" className="w-full rounded-lg border border-slate-200 px-3 py-2" />
          <button className="rounded-lg border border-slate-200 px-4 py-2 hover:bg-slate-50">Search</button>
        </form>
        {error && <p className="mt-4 rounded-lg bg-red-50 p-3 text-sm text-red-700" role="alert">{error}</p>}
        {!data && !error && <p className="mt-6 text-slate-500" role="status">Loading…</p>}
        {data && data.items.length === 0 && <p className="mt-8 rounded-2xl border border-dashed border-slate-200 p-10 text-center text-slate-600">No jobs found. Click "Add job" to create one.</p>}
        {data && data.items.length > 0 && (<>
          <div className="mt-5 overflow-x-auto rounded-xl border border-slate-100"><table className="w-full text-left text-sm">
            <thead className="bg-slate-50 text-slate-600"><tr><th className="p-3">Title</th><th className="p-3">Company</th><th className="p-3">Level</th><th className="p-3">Status</th><th className="p-3">Actions</th></tr></thead>
            <tbody>{data.items.map((j) => (
              <tr key={j.id} className="border-t border-slate-100">
                <td className="p-3 font-medium">{j.title}</td><td className="p-3">{j.companyName}</td><td className="p-3">{j.experienceLevel}</td>
                <td className="p-3"><span className={`rounded-full px-2 py-0.5 text-xs ${j.active ? 'bg-emerald-50 text-emerald-700' : 'bg-slate-100 text-slate-600'}`}>{j.active ? 'Active' : 'Inactive'}</span></td>
                <td className="space-x-3 p-3"><Link to={`/admin/jobs/${j.id}/edit`} className="text-brand-600 underline">Edit</Link>
                  <button onClick={() => toggle(j)} className="text-slate-700 underline">{j.active ? 'Deactivate' : 'Activate'}</button>
                  <button onClick={() => setTarget(j)} className="text-red-600 underline">Delete</button></td>
              </tr>))}</tbody></table></div>
          <div className="mt-5 flex items-center justify-between text-sm">
            <button disabled={page === 0} onClick={() => setPage(page - 1)} className="rounded-lg border border-slate-200 px-4 py-2 disabled:opacity-40">Previous</button>
            <span>Page {data.page + 1} of {Math.max(1, data.totalPages)} · {data.totalItems} jobs</span>
            <button disabled={page + 1 >= data.totalPages} onClick={() => setPage(page + 1)} className="rounded-lg border border-slate-200 px-4 py-2 disabled:opacity-40">Next</button>
          </div></>)}
      </main>
      <ConfirmDialog open={!!target} title="Delete this job?" message={target ? `"${target.title}" will be permanently deleted along with saved-job entries. Use Deactivate to hide it instead.` : ''}
        onConfirm={remove} onCancel={() => setTarget(null)} />
    </div>
  )
}
