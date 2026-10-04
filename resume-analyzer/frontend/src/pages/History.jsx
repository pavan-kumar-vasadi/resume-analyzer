import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import api, { errorMessage } from '../api/client.js'

export default function History() {
  const [rows, setRows] = useState(null)
  const [error, setError] = useState('')
  useEffect(() => { api.get('/history').then((r) => setRows(r.data)).catch((e) => setError(errorMessage(e))) }, [])
  const fmt = (x) => (x ? new Date(x).toLocaleString() : '-')
  return (
    <main className="mx-auto max-w-5xl px-6 py-10">
      <h1 className="text-3xl font-bold">Analysis history</h1>
      <p className="mt-1 text-sm text-slate-500">Each resume with its completeness score and its latest job analysis. Re-analyzing replaces the previous job results.</p>
      {error && <p className="mt-5 rounded-lg bg-red-50 p-3 text-sm text-red-700" role="alert">{error}</p>}
      {!rows && !error && <p className="mt-6 text-slate-500" role="status">Loading…</p>}
      {rows && rows.length === 0 && <div className="mt-8 rounded-2xl border border-dashed border-slate-200 bg-white p-10 text-center"><p className="font-medium">No analyses yet</p><Link to="/resumes/upload" className="mt-2 inline-block text-brand-600 underline">Upload a resume</Link></div>}
      {rows && rows.length > 0 && (
        <div className="mt-6 overflow-x-auto rounded-xl border border-slate-100 bg-white"><table className="w-full text-left text-sm">
          <thead className="bg-slate-50 text-slate-600"><tr><th className="p-3">Resume</th><th className="p-3">Uploaded</th><th className="p-3">Skills</th><th className="p-3">Completeness</th><th className="p-3">Job analysis</th><th className="p-3">Best match</th></tr></thead>
          <tbody>{rows.map((r) => (<tr key={r.resumeId} className="border-t border-slate-100">
            <td className="p-3 font-medium"><Link to={`/resumes/${r.resumeId}`} className="hover:text-brand-600">{r.fileName}</Link></td>
            <td className="p-3">{fmt(r.uploadedAt)}</td><td className="p-3">{r.skillCount}</td>
            <td className="p-3">{r.completenessScore !== null ? `${r.completenessScore}/100` : r.status}</td>
            <td className="p-3">{fmt(r.jobAnalysisAt)}</td>
            <td className="p-3">{r.bestMatchTitle ? `${r.bestMatchTitle} (${Math.round(r.bestMatchScore)}%)` : '-'}</td></tr>))}</tbody></table></div>)}
    </main>
  )
}
