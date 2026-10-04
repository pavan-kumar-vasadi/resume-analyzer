import { useRef, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { Upload } from 'lucide-react'
import api, { errorMessage } from '../api/client.js'
import AppHeader from '../components/AppHeader.jsx'

const MAX = 5 * 1024 * 1024

function check(f) {
  const n = f.name.toLowerCase()
  if (!n.endsWith('.pdf') && !n.endsWith('.docx')) return 'Only PDF and DOCX files are supported.'
  if (f.size === 0) return 'The file is empty.'
  if (f.size > MAX) return 'File is larger than 5 MB.'
  return ''
}

export default function ResumeUpload() {
  const nav = useNavigate()
  const input = useRef(null)
  const [file, setFile] = useState(null)
  const [error, setError] = useState('')
  const [progress, setProgress] = useState(0)
  const [busy, setBusy] = useState(false)
  const [drag, setDrag] = useState(false)

  const pick = (f) => {
    if (!f) return
    const err = check(f)
    setError(err)
    setFile(err ? null : f)
  }

  const submit = async () => {
    setBusy(true); setError(''); setProgress(0)
    const fd = new FormData()
    fd.append('file', file)
    try {
      const { data } = await api.post('/resumes/upload', fd, {
        timeout: 60000,
        onUploadProgress: (e) => setProgress(Math.round((e.loaded * 100) / (e.total || file.size))),
      })
      nav(`/resumes/${data.id}`)
    } catch (e) { setError(errorMessage(e)); setBusy(false) }
  }

  return (
    <div>
      <AppHeader />
      <main className="mx-auto max-w-2xl px-6 py-12">
        <h1 className="text-3xl font-bold">Analyze your resume</h1>
        <p className="mt-2 text-slate-600">Upload a PDF or DOCX (max 5 MB). Text is extracted and checked with rule-based skill matching.</p>
        <div onDragOver={(e) => { e.preventDefault(); setDrag(true) }} onDragLeave={() => setDrag(false)}
          onDrop={(e) => { e.preventDefault(); setDrag(false); pick(e.dataTransfer.files[0]) }}
          className={`mt-8 rounded-2xl border-2 border-dashed p-10 text-center ${drag ? 'border-brand-600 bg-brand-50' : 'border-slate-200'}`}>
          <Upload className="mx-auto h-8 w-8 text-brand-600" aria-hidden />
          <p className="mt-3 font-medium">Drag and drop your resume here</p>
          <p className="text-sm text-slate-500">or</p>
          <button type="button" onClick={() => input.current.click()} disabled={busy}
            className="mt-2 rounded-lg border border-slate-200 px-4 py-2 text-sm font-medium hover:bg-slate-50">Browse files</button>
          <input ref={input} type="file" accept=".pdf,.docx" className="hidden" aria-label="Choose resume file" onChange={(e) => pick(e.target.files[0])} />
        </div>
        {error && <p className="mt-4 rounded-lg bg-red-50 p-3 text-sm text-red-700" role="alert">{error}</p>}
        {file && (
          <div className="mt-4 rounded-xl border border-slate-100 p-4">
            <p className="font-medium">{file.name}</p>
            <p className="text-sm text-slate-500">{(file.size / 1024).toFixed(1)} KB</p>
            {busy && <div className="mt-3 h-2 rounded-full bg-slate-100" role="progressbar" aria-valuenow={progress}><div className="h-2 rounded-full bg-brand-600" style={{ width: `${progress}%` }} /></div>}
            <button onClick={submit} disabled={busy} className="mt-4 rounded-lg bg-brand-600 px-5 py-2.5 font-medium text-white hover:bg-brand-700 disabled:opacity-60">
              {busy ? (progress < 100 ? `Uploading ${progress}%` : 'Analyzing…') : 'Upload and analyze'}
            </button>
          </div>
        )}
      </main>
    </div>
  )
}
