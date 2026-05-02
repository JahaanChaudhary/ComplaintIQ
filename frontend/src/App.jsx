import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import { Toaster } from 'react-hot-toast'
import { AuthProvider, useAuth } from './context/AuthContext'
import { PageSpinner } from './components/ui/Spinner'
import { Layout } from './components/layout/Layout'

// Auth
import Login from './pages/auth/Login'
import Register from './pages/auth/Register'

// Customer
import CustomerDashboard from './pages/customer/CustomerDashboard'
import SubmitComplaint from './pages/customer/SubmitComplaint'
import MyComplaints from './pages/customer/MyComplaints'
import TrackComplaint from './pages/customer/TrackComplaint'
import ComplaintDetail from './pages/customer/ComplaintDetail'

// Agent
import AgentDashboard from './pages/agent/AgentDashboard'
import AgentComplaintList from './pages/agent/AgentComplaintList'
import AgentComplaintDetail from './pages/agent/AgentComplaintDetail'

// Admin
import AdminDashboard from './pages/admin/AdminDashboard'
import AdminComplaints from './pages/admin/AdminComplaints'
import AdminAgents from './pages/admin/AdminAgents'
import AdminAnalytics from './pages/admin/AdminAnalytics'

function ProtectedRoute({ children, roles }) {
  const { user, loading } = useAuth()
  if (loading) return <PageSpinner />
  if (!user) return <Navigate to="/login" replace />
  if (roles && !roles.includes(user.role)) return <Navigate to="/" replace />
  return children
}

function RootRedirect() {
  const { user, loading } = useAuth()
  if (loading) return <PageSpinner />
  if (!user) return <Navigate to="/login" replace />
  if (user.role === 'CUSTOMER') return <Navigate to="/customer" replace />
  if (user.role === 'AGENT' || user.role === 'TEAM_LEAD') return <Navigate to="/agent" replace />
  return <Navigate to="/admin" replace />
}

function AppRoutes() {
  return (
    <Routes>
      <Route path="/" element={<RootRedirect />} />
      <Route path="/login" element={<Login />} />
      <Route path="/register" element={<Register />} />

      {/* Customer routes */}
      <Route path="/customer" element={
        <ProtectedRoute roles={['CUSTOMER']}><Layout /></ProtectedRoute>}>
        <Route index element={<CustomerDashboard />} />
        <Route path="submit" element={<SubmitComplaint />} />
        <Route path="my-complaints" element={<MyComplaints />} />
        <Route path="track" element={<TrackComplaint />} />
        <Route path="complaint/:ticketId" element={<ComplaintDetail />} />
      </Route>

      {/* Agent routes */}
      <Route path="/agent" element={
        <ProtectedRoute roles={['AGENT', 'TEAM_LEAD']}><Layout /></ProtectedRoute>}>
        <Route index element={<AgentDashboard />} />
        <Route path="complaints" element={<AgentComplaintList />} />
        <Route path="complaint/:ticketId" element={<AgentComplaintDetail />} />
      </Route>

      {/* Admin/Manager routes */}
      <Route path="/admin" element={
        <ProtectedRoute roles={['MANAGER', 'ADMIN']}><Layout /></ProtectedRoute>}>
        <Route index element={<AdminDashboard />} />
        <Route path="complaints" element={<AdminComplaints />} />
        <Route path="complaint/:ticketId" element={<AgentComplaintDetail />} />
        <Route path="agents" element={<AdminAgents />} />
        <Route path="analytics" element={<AdminAnalytics />} />
      </Route>

      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  )
}

export default function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <AppRoutes />
        <Toaster position="top-right"
          toastOptions={{ duration: 3500, style: { borderRadius: '10px', fontSize: '14px' } }} />
      </AuthProvider>
    </BrowserRouter>
  )
}
