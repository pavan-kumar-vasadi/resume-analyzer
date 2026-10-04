import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import api, { errorMessage } from '../api/client.js'
import AppHeader from '../components/AppHeader.jsx'
import ConfirmDialog from '../components/ConfirmDialog.jsx'
import { useToast } from '../context/ToastContext.jsx'

export default function MyResumes() {
  const [items, setItems] = useState(null)
  const [error, setError] = useState('')
  const [target, setTarget] = useState(null)
  const toast = useToast()

  useEffect(() => { api.get('/resumes/my').then((r) => setItems(r.data)).catch((e) => setError(errorMessage(e))) }, [])

  const remove = async () => {
    const id = target.id
    setTarget(null)
    try { await api.delete(`/resumes/${id}`); setItems((l) => l.filter((x) => x.id !== id)); toast.push('Resume deleted') }
    catch (e) { setError(errorMessage(e)) }
  }

  return (
    <div>
      <AppHeader />
      <main className="mx-auto max-w-5xl px-6 py-12">
        <div className="flex items-center justify-between">
          <h1 className="text-3xl font-bold">My resumes</h1>
          <Link to="/resumes/upload" className="rounded-lg bg-brand-600 px-4 py-2 text-sm font-medium text-white hover:bg-brand-700">Upload new</Link>
        </div>
        {error && <p className="mt-6 rounded-lg bg-red-50 p-3 text-sm text-red-700" role="alert">{error}</p>}
        {!items && !error && <p className="mt-6 text-slate-500" role="status">Loading…</p>}
        {items && items.length === 0 && (
          <div className="mt-10 rounded-2xl border border-dashed border-slate-200 p-10 text-center">
            <p className="font-medium">No resumes yet</p>
            <p className="text-sm text-slate-500">Upload your first resume to see detected skills and a completeness score.</p>
            <Link to="/resumes/upload" className="mt-4 inline-block text-brand-600 underline">Upload a resume</Link>
          </div>
        )}
        {items && items.length > 0 && (
          <div className="mt-6 overflow-x-auto rounded-xl border border-slate-100">
            <table className="w-full text-left text-sm">
              <thead className="bg-slate-50 text-slate-600"><tr><th className="p-3">File</th><th className="p-3">Status</th><th className="p-3">Skills</th><th className="p-3">Uploaded</th><th className="p-3">Actions</th></tr></thead>
              <tbody>{items.map((r) => (
                <tr key={r.id} className="border-t border-slate-100">
                  <td className="p-3 font-medium">{r.fileName}</td>
                  <td className="p-3"><span className={`rounded-full px-2 py-0.5 text-xs ${r.status === 'ANALYZED' ? 'bg-emerald-50 text-emerald-700' : 'bg-amber-50 text-amber-700'}`}>{r.status}</span></td>
                  <td className="p-3">{r.skillCount}</td>
                  <td className="p-3">{r.uploadedAt ? new Date(r.uploadedAt).toLocaleString() : '-'}</td>
                  <td className="space-x-3 p-3"><Link to={`/resumes/${r.id}`} className="text-brand-600 underline">View</Link>
                    <button onClick={() => setTarget(r)} className="text-red-600 underline">Delete</button></td>
                </tr>))}
              </tbody>
            </table>
          </div>
        )}
      </main>
      <ConfirmDialog open={!!target} title="Delete this resume?" message={target ? `"${target.fileName}" and its detected skills will be permanently removed.` : ''}
        onConfirm={remove} onCancel={() => setTarget(null)} />
    </div>
  )
}
