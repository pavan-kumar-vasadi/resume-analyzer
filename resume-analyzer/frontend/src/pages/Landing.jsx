import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { FileText, Target, BarChart3, ShieldCheck, Upload, Cpu, Briefcase, ChevronDown } from 'lucide-react'
import { getHealth } from '../api/client.js'

const features = [
  { icon: FileText, title: 'Resume parsing', text: 'Extracts text from PDF and DOCX files and shows exactly what was found.' },
  { icon: Cpu, title: 'Skill detection', text: 'A configurable, rule-based dictionary with aliases, not a generative model.' },
  { icon: Target, title: 'Job matching', text: 'Compares your skills with job requirements and reports a skill-overlap estimate.' },
  { icon: BarChart3, title: 'Skill gap analysis', text: 'See which skills are missing most often across the jobs you target.' },
  { icon: ShieldCheck, title: 'Private by design', text: 'Resumes are stored server-side and only accessible to their owner.' },
  { icon: Briefcase, title: 'Admin-managed jobs', text: 'Only jobs entered by administrators are shown. Demo records are labeled.' },
]
const steps = [
  { icon: Upload, title: 'Upload', text: 'Add your resume as PDF or DOCX (max 5 MB).' },
  { icon: Cpu, title: 'Analyze', text: 'We detect skills and check resume completeness.' },
  { icon: Target, title: 'Match', text: 'Review matched and missing skills per job.' },
]
const faqs = [
  ['Does this use generative AI?', 'No. Skill extraction and matching are rule-based. The design is modular so an LLM could be added later.'],
  ['Is the match score my chance of being hired?', 'No. It is a skill-overlap estimate between your resume and a job\'s listed skills.'],
  ['Are the jobs real vacancies?', 'Only jobs added by administrators appear. Sample records are clearly marked as demonstrations.'],
  ['What about scanned PDFs?', 'OCR is not configured, so image-only PDFs will show a clear message instead of invented content.'],
]

function ApiStatus() {
  const [s, setS] = useState('checking')
  useEffect(() => {
    getHealth().then((d) => setS(d.database === 'UP' ? 'up' : 'degraded')).catch(() => setS('down'))
  }, [])
  const map = {
    checking: ['bg-slate-300', 'Checking backend…'],
    up: ['bg-emerald-500', 'Backend and database connected'],
    degraded: ['bg-amber-500', 'Backend up, database unavailable'],
    down: ['bg-red-500', 'Backend not reachable'],
  }
  const [dot, label] = map[s]
  return (
    <span role="status" className="inline-flex items-center gap-2 text-xs text-slate-500">
      <span className={`h-2 w-2 rounded-full ${dot}`} />{label}
    </span>
  )
}

export default function Landing() {
  return (
    <div>
      <header className="sticky top-0 z-10 border-b border-slate-100 bg-white/90 backdrop-blur">
        <nav className="mx-auto flex max-w-6xl items-center justify-between px-6 py-4" aria-label="Main">
          <Link to="/" className="font-bold text-lg">Resume<span className="text-brand-600">Analyzer</span></Link>
          <div className="hidden gap-6 text-sm text-slate-600 md:flex">
            <a href="#features" className="hover:text-navy">Features</a>
            <a href="#how" className="hover:text-navy">How it works</a>
            <a href="#faq" className="hover:text-navy">FAQ</a>
            <Link to="/jobs" className="hover:text-navy">Jobs</Link>
          </div>
          <div className="flex items-center gap-3 text-sm">
            <Link to="/login" className="text-slate-700 hover:text-navy">Log in</Link>
            <Link to="/register" className="rounded-lg bg-brand-600 px-4 py-2 font-medium text-white hover:bg-brand-700">Sign up</Link>
          </div>
        </nav>
      </header>

      <section className="bg-gradient-to-b from-brand-50 to-white">
        <div className="mx-auto grid max-w-6xl items-center gap-12 px-6 py-20 md:grid-cols-2">
          <div>
            <h1 className="text-4xl font-bold leading-tight md:text-5xl">Turn Your Resume Into Your Career Roadmap</h1>
            <p className="mt-5 text-lg text-slate-600">Upload your resume, see which skills we can detect, check how complete it is, and get job recommendations ranked by skill overlap.</p>
            <div className="mt-8 flex flex-wrap gap-3">
              <Link to="/register" className="rounded-lg bg-brand-600 px-6 py-3 font-medium text-white hover:bg-brand-700">Analyze My Resume</Link>
              <Link to="/jobs" className="rounded-lg border border-slate-200 bg-white px-6 py-3 font-medium hover:bg-slate-50">Explore Jobs</Link>
            </div>
            <div className="mt-6"><ApiStatus /></div>
          </div>
          <div className="rounded-2xl border border-slate-100 bg-white p-6 shadow-lg" aria-label="Product preview (illustrative)">
            <p className="text-xs uppercase tracking-wide text-slate-400">Preview · illustrative example</p>
            <div className="mt-4 flex items-center justify-between"><span className="font-semibold">Junior Java Developer</span><span className="rounded-full bg-brand-100 px-3 py-1 text-sm font-semibold text-brand-700">60% match</span></div>
            <div className="mt-3 h-2 rounded-full bg-slate-100"><div className="h-2 w-3/5 rounded-full bg-brand-600" /></div>
            <div className="mt-5 flex flex-wrap gap-2 text-xs">
              {['Java', 'Spring Boot', 'MySQL'].map((x) => <span key={x} className="rounded-full bg-emerald-50 px-3 py-1 text-emerald-700">{x}</span>)}
              {['React', 'REST API'].map((x) => <span key={x} className="rounded-full bg-rose-50 px-3 py-1 text-rose-700">Missing: {x}</span>)}
            </div>
          </div>
        </div>
      </section>

      <section id="features" className="mx-auto max-w-6xl px-6 py-20">
        <h2 className="text-center text-3xl font-bold">Features</h2>
        <div className="mt-10 grid gap-6 sm:grid-cols-2 lg:grid-cols-3">
          {features.map(({ icon: Icon, title, text }) => (
            <div key={title} className="rounded-2xl border border-slate-100 bg-white p-6 shadow-sm">
              <Icon className="h-6 w-6 text-brand-600" aria-hidden />
              <h3 className="mt-3 font-semibold">{title}</h3>
              <p className="mt-1 text-sm text-slate-600">{text}</p>
            </div>
          ))}
        </div>
      </section>

      <section id="how" className="bg-slate-50 py-20">
        <div className="mx-auto max-w-6xl px-6">
          <h2 className="text-center text-3xl font-bold">How it works</h2>
          <div className="mt-10 grid gap-6 md:grid-cols-3">
            {steps.map(({ icon: Icon, title, text }, i) => (
              <div key={title} className="rounded-2xl bg-white p-6 shadow-sm">
                <span className="text-sm font-semibold text-brand-600">Step {i + 1}</span>
                <Icon className="mt-2 h-6 w-6" aria-hidden />
                <h3 className="mt-2 font-semibold">{title}</h3>
                <p className="mt-1 text-sm text-slate-600">{text}</p>
              </div>
            ))}
          </div>
        </div>
      </section>

      <section className="mx-auto max-w-6xl px-6 py-20">
        <h2 className="text-center text-3xl font-bold">Benefits</h2>
        <ul className="mx-auto mt-8 max-w-2xl list-disc space-y-2 pl-6 text-slate-700">
          <li>Know which skills are visible in your resume text.</li>
          <li>See exactly which required skills a job expects that you haven't listed.</li>
          <li>Get clear, actionable suggestions to improve resume completeness.</li>
        </ul>
      </section>

      <section id="faq" className="mx-auto max-w-3xl px-6 pb-20">
        <h2 className="text-center text-3xl font-bold">FAQ</h2>
        <div className="mt-8 divide-y divide-slate-100 rounded-2xl border border-slate-100">
          {faqs.map(([q, a]) => (
            <details key={q} className="group p-5">
              <summary className="flex cursor-pointer list-none items-center justify-between font-medium">{q}<ChevronDown className="h-4 w-4 transition group-open:rotate-180" aria-hidden /></summary>
              <p className="mt-2 text-sm text-slate-600">{a}</p>
            </details>
          ))}
        </div>
      </section>

      <footer className="border-t border-slate-100 bg-slate-50">
        <div className="mx-auto flex max-w-6xl flex-col items-center justify-between gap-3 px-6 py-8 text-sm text-slate-500 md:flex-row">
          <p>© {new Date().getFullYear()} AI-Based Resume Analyzer &amp; Job Recommendation System · B.Tech project</p>
          <div className="flex gap-5">
            <a href="#features" className="hover:text-navy">Features</a>
            <a href="#faq" className="hover:text-navy">FAQ</a>
            <Link to="/jobs" className="hover:text-navy">Jobs</Link>
            <a href="http://localhost:8080/swagger-ui.html" className="hover:text-navy">API docs</a>
          </div>
        </div>
      </footer>
    </div>
  )
}
