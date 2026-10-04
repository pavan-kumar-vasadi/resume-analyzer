import { Routes, Route, Link } from 'react-router-dom'
import Landing from './pages/Landing.jsx'
import Login from './pages/Login.jsx'
import Register from './pages/Register.jsx'
import Dashboard from './pages/Dashboard.jsx'
import Profile from './pages/Profile.jsx'
import Settings from './pages/Settings.jsx'
import History from './pages/History.jsx'
import AdminDashboard from './pages/AdminDashboard.jsx'
import AdminUsers from './pages/AdminUsers.jsx'
import AdminJobs from './pages/AdminJobs.jsx'
import AdminJobForm from './pages/AdminJobForm.jsx'
import ResumeUpload from './pages/ResumeUpload.jsx'
import MyResumes from './pages/MyResumes.jsx'
import ResumeAnalysis from './pages/ResumeAnalysis.jsx'
import Jobs from './pages/Jobs.jsx'
import JobDetails from './pages/JobDetails.jsx'
import SavedJobs from './pages/SavedJobs.jsx'
import Recommendations from './pages/Recommendations.jsx'
import SkillGaps from './pages/SkillGaps.jsx'
import ProtectedRoute from './routes/ProtectedRoute.jsx'
import StudentLayout from './layouts/StudentLayout.jsx'
import { useAuth } from './context/AuthContext.jsx'

function NotFound() {
  return (
    <div className="min-h-screen grid place-items-center text-center px-6">
      <div>
        <p className="text-6xl font-bold text-brand-600">404</p>
        <p className="mt-2 text-slate-600">This page doesn't exist.</p>
        <Link to="/" className="mt-6 inline-block rounded-lg bg-brand-600 px-5 py-2.5 text-white font-medium hover:bg-brand-700">Back to home</Link>
      </div>
    </div>
  )
}

// Public job pages show the sidebar only when someone is logged in.
function MaybeLayout({ children }) {
  const { user } = useAuth()
  return user ? <StudentLayout>{children}</StudentLayout> : children
}

export default function App() {
  return (
    <Routes>
      <Route path="/" element={<Landing />} />
      <Route path="/login" element={<Login />} />
      <Route path="/register" element={<Register />} />
      <Route path="/jobs" element={<MaybeLayout><Jobs /></MaybeLayout>} />
      <Route path="/jobs/:id" element={<MaybeLayout><JobDetails /></MaybeLayout>} />
      <Route element={<ProtectedRoute><StudentLayout /></ProtectedRoute>}>
        <Route path="/dashboard" element={<Dashboard />} />
        <Route path="/profile" element={<Profile />} />
        <Route path="/resumes/upload" element={<ResumeUpload />} />
        <Route path="/resumes" element={<MyResumes />} />
        <Route path="/resumes/:id" element={<ResumeAnalysis />} />
        <Route path="/recommendations" element={<Recommendations />} />
        <Route path="/skill-gaps" element={<SkillGaps />} />
        <Route path="/saved-jobs" element={<SavedJobs />} />
        <Route path="/history" element={<History />} />
        <Route path="/settings" element={<Settings />} />
      </Route>
      <Route element={<ProtectedRoute adminOnly><StudentLayout /></ProtectedRoute>}>
        <Route path="/admin" element={<AdminDashboard />} />
        <Route path="/admin/users" element={<AdminUsers />} />
        <Route path="/admin/jobs" element={<AdminJobs />} />
        <Route path="/admin/jobs/new" element={<AdminJobForm />} />
        <Route path="/admin/jobs/:id/edit" element={<AdminJobForm />} />
      </Route>
      <Route path="*" element={<NotFound />} />
    </Routes>
  )
}
