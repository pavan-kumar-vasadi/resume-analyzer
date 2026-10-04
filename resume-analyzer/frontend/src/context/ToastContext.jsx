import { createContext, useCallback, useContext, useState } from 'react'
import ToastNotification from '../components/ToastNotification.jsx'

const Ctx = createContext({ push: () => {} })
export const useToast = () => useContext(Ctx)

export function ToastProvider({ children }) {
  const [items, setItems] = useState([])
  const close = useCallback((id) => setItems((l) => l.filter((x) => x.id !== id)), [])
  const push = useCallback((message, type = 'success') => {
    const id = Date.now() + Math.random()
    setItems((l) => [...l, { id, message, type }])
    setTimeout(() => close(id), 4000)
  }, [close])
  return <Ctx.Provider value={{ push }}>{children}<ToastNotification items={items} onClose={close} /></Ctx.Provider>
}
