import { useState } from 'react'
import { NavLink, Outlet, useNavigate } from 'react-router-dom'
import { LayoutDashboard, User, Upload, FileText, BarChart3, Target, Briefcase, Bookmark, History, Settings, LogOut, Menu, X, Users, ClipboardList, ShieldCheck } from 'lucide-react'
import { LayoutCtx } from '../context/LayoutContext.js'
import { useAuth } from '../context/AuthContext.jsx'

const ITEMS = [
  ['/dashboard', 'Overview', LayoutDashboard, true], ['/profile', 'My Profile', User, true], ['/resumes/upload', 'Resume Analyzer', Upload, true],
  ['/resumes', 'My Resumes', FileText, true], ['/skill-gaps', 'Skills Analysis', BarChart3, true], ['/recommendations', 'Recommended Jobs', Target, true],
  ['/jobs', 'Browse Jobs', Briefcase, false], ['/saved-jobs', 'Saved Jobs', Bookmark, true], ['/history', 'Analysis History', History, true], ['/settings', 'Settings', Settings, true],
]

export default function StudentLayout({ children }) {
  const { user, logout } = useAuth()
  const nav = useNavigate()
  const [open, setOpen] = useState(false)
  const link = ({ isActive }) => `flex items-center gap-3 rounded-lg px-3 py-2 text-sm ${isActive ? 'bg-brand-50 font-medium text-brand-700' : 'text-slate-600 hover:bg-slate-50'}`
  const out = () => { logout(); nav('/login', { replace: true }) }
  return (
    <LayoutCtx.Provider value={true}>
      <div className="min-h-screen bg-slate-50 md:flex">
        <aside className={`fixed inset-y-0 left-0 z-40 flex w-64 flex-col border-r border-slate-100 bg-white p-4 transition-transform md:sticky md:top-0 md:h-screen md:translate-x-0 ${open ? 'translate-x-0' : '-translate-x-full'}`} aria-label="Sidebar">
          <p className="px-3 py-2 text-lg font-bold">Resume<span className="text-brand-600">Analyzer</span></p>
          <p className="truncate px-3 pb-3 text-xs text-slate-500">{user?.fullName}</p>
          <nav className="flex-1 space-y-1 overflow-y-auto">
            {ITEMS.map(([to, label, Icon, end]) => <NavLink key={to} to={to} end={end} onClick={() => setOpen(false)} className={link}><Icon className="h-4 w-4" aria-hidden />{label}</NavLink>)}
            {user?.role === 'ADMIN' && (<>
              <p className="px-3 pt-4 text-xs font-semibold uppercase text-slate-400">Admin</p>
              <NavLink to="/admin" end onClick={() => setOpen(false)} className={link}><ShieldCheck className="h-4 w-4" aria-hidden />Admin dashboard</NavLink>
              <NavLink to="/admin/users" onClick={() => setOpen(false)} className={link}><Users className="h-4 w-4" aria-hidden />Users</NavLink>
              <NavLink to="/admin/jobs" onClick={() => setOpen(false)} className={link}><ClipboardList className="h-4 w-4" aria-hidden />Manage jobs</NavLink>
            </>)}
          </nav>
          <button onClick={out} className="flex items-center gap-3 rounded-lg px-3 py-2 text-sm text-red-600 hover:bg-red-50"><LogOut className="h-4 w-4" aria-hidden />Logout</button>
        </aside>
        {open && <div className="fixed inset-0 z-30 bg-black/30 md:hidden" onClick={() => setOpen(false)} aria-hidden />}
        <div className="min-w-0 flex-1">
          <div className="flex items-center gap-3 border-b border-slate-100 bg-white px-4 py-3 md:hidden">
            <button onClick={() => setOpen(!open)} aria-label={open ? 'Close menu' : 'Open menu'} aria-expanded={open} className="rounded-lg border border-slate-200 p-2">{open ? <X className="h-4 w-4" /> : <Menu className="h-4 w-4" />}</button>
            <span className="font-bold">Resume<span className="text-brand-600">Analyzer</span></span>
          </div>
          {children ?? <Outlet />}
        </div>
      </div>
    </LayoutCtx.Provider>
  )
}
