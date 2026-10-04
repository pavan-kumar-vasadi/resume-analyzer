import { Link } from 'react-router-dom'
import { useAuth } from '../context/AuthContext.jsx'
import AppHeader from './AppHeader.jsx'

// Full app header for logged-in users, simple public header otherwise.
export default function SiteHeader() {
  const { user } = useAuth()
  if (user) return <AppHeader />
  return (
    <header className="border-b border-slate-100 bg-white">
      <nav className="mx-auto flex max-w-5xl items-center gap-4 px-6 py-3" aria-label="Public">
        <Link to="/" className="font-bold">Resume<span className="text-brand-600">Analyzer</span></Link>
        <Link to="/jobs" className="text-sm text-slate-600 hover:text-navy">Jobs</Link>
        <div className="ml-auto flex gap-3 text-sm">
          <Link to="/login" className="px-3 py-2 text-slate-700">Log in</Link>
          <Link to="/register" className="rounded-lg bg-brand-600 px-3 py-2 font-medium text-white hover:bg-brand-700">Sign up</Link>
        </div>
      </nav>
    </header>
  )
}
