import { Link } from 'react-router-dom'
export default function ComingSoon({ title, phase }) {
  return (
    <div className="min-h-screen grid place-items-center px-6 text-center">
      <div>
        <h1 className="text-3xl font-bold">{title}</h1>
        <p className="mt-2 text-slate-600">This page is built in {phase} of the implementation plan.</p>
        <Link to="/" className="mt-6 inline-block rounded-lg bg-brand-600 px-5 py-2.5 text-white font-medium hover:bg-brand-700">Back to home</Link>
      </div>
    </div>
  )
}
