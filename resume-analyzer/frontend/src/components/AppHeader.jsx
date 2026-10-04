import { useContext } from 'react'
import { NavLink, useNavigate } from 'react-router-dom'
import { LayoutCtx } from '../context/LayoutContext.js'
import { useAuth } from '../context/AuthContext.jsx'

export default function AppHeader() {
  const { user, logout } = useAuth()
  const nav = useNavigate()
  const inLayout = useContext(LayoutCtx)
  const link = ({ isActive }) => `rounded-lg px-3 py-2 text-sm ${isActive ? 'bg-brand-50 font-medium text-brand-700' : 'text-slate-600 hover:bg-slate-50'}`
  if (inLayout) return null
  return (
    <header className="border-b border-slate-100 bg-white">
      <nav className="mx-auto flex max-w-5xl flex-wrap items-center gap-1 px-6 py-3" aria-label="App">
        <span className="mr-4 font-bold">Resume<span className="text-brand-600">Analyzer</span></span>
        <NavLink to="/dashboard" className={link}>Dashboard</NavLink>
        <NavLink to="/resumes/upload" className={link}>Upload</NavLink>
        <NavLink to="/resumes" end className={link}>My resumes</NavLink>
        <NavLink to="/recommendations" className={link}>Recommended</NavLink>
        <NavLink to="/skill-gaps" className={link}>Skill gaps</NavLink>
        <NavLink to="/jobs" end className={link}>Jobs</NavLink>
        <NavLink to="/saved-jobs" className={link}>Saved</NavLink>
        {user?.role === 'ADMIN' && <>
          <NavLink to="/admin" end className={link}>Admin</NavLink>
          <NavLink to="/admin/users" className={link}>Users</NavLink>
          <NavLink to="/admin/jobs" className={link}>Manage jobs</NavLink>
        </>}
        <button onClick={() => { logout(); nav('/login', { replace: true }) }}
          className="ml-auto rounded-lg bg-brand-600 px-3 py-2 text-sm font-medium text-white hover:bg-brand-700">Log out</button>
      </nav>
    </header>
  )
}
