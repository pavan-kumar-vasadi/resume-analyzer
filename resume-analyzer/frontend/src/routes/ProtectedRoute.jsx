import { Navigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext.jsx'

export default function ProtectedRoute({ children, adminOnly = false }) {
  const { user, loading } = useAuth()
  if (loading) return <p className="p-10 text-center text-slate-500" role="status">Loading…</p>
  if (!user) return <Navigate to="/login" replace />
  // UI-only convenience: the backend enforces roles on every admin endpoint.
  if (adminOnly && user.role !== 'ADMIN') return <Navigate to="/dashboard" replace />
  return children
}
