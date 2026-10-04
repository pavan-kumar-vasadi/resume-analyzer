import axios from 'axios'

// Token lives in sessionStorage (cleared when the tab closes). Limitation: any XSS bug
// could read it. HttpOnly cookies are safer but need extra CSRF handling; this is a
// reasonable trade-off for a student project.
const KEY = 'ra_token'
export const tokenStore = {
  get: () => sessionStorage.getItem(KEY),
  set: (t) => sessionStorage.setItem(KEY, t),
  clear: () => sessionStorage.removeItem(KEY),
}

const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api',
  timeout: 15000,
})

api.interceptors.request.use((cfg) => {
  const t = tokenStore.get()
  if (t) cfg.headers.Authorization = `Bearer ${t}`
  return cfg
})

api.interceptors.response.use((r) => r, (err) => {
  // Expired/invalid token on a logged-in session: clear it and go to login.
  if (err.response?.status === 401 && tokenStore.get()) {
    tokenStore.clear()
    window.location.assign('/login?expired=1')
  }
  return Promise.reject(err)
})

export const errorMessage = (err) => {
  const d = err.response?.data
  if (d?.errors) return Object.values(d.errors).join(' ')
  return d?.message || 'Cannot reach the server. Is the backend running?'
}

export const getHealth = () => api.get('/health').then((r) => r.data)
export default api
