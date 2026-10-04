import { createContext, useContext, useEffect, useState } from 'react'
import api, { tokenStore } from '../api/client.js'

const Ctx = createContext(null)
export const useAuth = () => useContext(Ctx)

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null)
  const [loading, setLoading] = useState(!!tokenStore.get())

  useEffect(() => {
    if (!tokenStore.get()) return
    api.get('/auth/me').then((r) => setUser(r.data)).catch(() => tokenStore.clear()).finally(() => setLoading(false))
  }, [])

  const finish = (d) => { tokenStore.set(d.token); setUser(d.user); return d.user }
  const login = (body) => api.post('/auth/login', body).then((r) => finish(r.data))
  const register = (body) => api.post('/auth/register', body).then((r) => finish(r.data))
  const logout = () => { tokenStore.clear(); setUser(null) }
  const updateUser = (u) => setUser(u)

  return <Ctx.Provider value={{ user, loading, login, register, logout, updateUser }}>{children}</Ctx.Provider>
}
