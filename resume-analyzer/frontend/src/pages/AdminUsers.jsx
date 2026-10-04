import { useEffect, useMemo, useState } from 'react'
import api, { errorMessage } from '../api/client.js'

export default function AdminUsers() {
  const [users, setUsers] = useState(null)
  const [error, setError] = useState('')
  const [q, setQ] = useState('')
  useEffect(() => {
    api.get('/admin/users').then((r) => setUsers(r.data)).catch((e) => setError(errorMessage(e)))
  }, [])
  const shown = useMemo(() => {
    const t = q.trim().toLowerCase()
    return (users || []).filter((u) => !t || u.fullName.toLowerCase().includes(t) || u.email.toLowerCase().includes(t))
  }, [users, q])

  return (
    <main className="mx-auto max-w-5xl px-6 py-10">
      <h1 className="text-3xl font-bold">Registered users</h1>
      <p className="mt-1 text-slate-600">{users ? `${users.length} account${users.length === 1 ? '' : 's'}` : ' '}</p>
      <input value={q} onChange={(e) => setQ(e.target.value)} placeholder="Search name or email" aria-label="Search users" className="mt-4 w-full rounded-lg border border-slate-200 px-3 py-2 sm:max-w-sm" />
      {error && <p className="mt-6 rounded-lg bg-red-50 p-3 text-sm text-red-700" role="alert">{error}</p>}
      {!users && !error && <p className="mt-6 text-slate-500" role="status">Loading…</p>}
      {users && (
        <div className="mt-6 overflow-x-auto rounded-xl border border-slate-100 bg-white">
          <table className="w-full text-left text-sm">
            <thead className="bg-slate-50 text-slate-600"><tr><th className="p-3">Name</th><th className="p-3">Email</th><th className="p-3">Role</th><th className="p-3">Joined</th></tr></thead>
            <tbody>
              {shown.length === 0 && <tr><td colSpan={4} className="p-4 text-center text-slate-500">No users match your search.</td></tr>}
              {shown.map((u) => (
                <tr key={u.id} className="border-t border-slate-100">
                  <td className="p-3">{u.fullName}</td><td className="p-3">{u.email}</td><td className="p-3">{u.role}</td>
                  <td className="p-3">{u.createdAt ? new Date(u.createdAt).toLocaleDateString() : '-'}</td>
                </tr>))}
            </tbody>
          </table>
        </div>
      )}
    </main>
  )
}
