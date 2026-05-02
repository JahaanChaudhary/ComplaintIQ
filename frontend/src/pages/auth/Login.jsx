import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../../context/AuthContext'
import { login as apiLogin } from '../../api/auth'
import { getErrorMsg } from '../../utils/helpers'
import { Spinner } from '../../components/ui/Spinner'
import toast from 'react-hot-toast'

export default function Login() {
  const [form, setForm] = useState({ email: '', password: '' })
  const [loading, setLoading] = useState(false)
  const { login } = useAuth()
  const navigate = useNavigate()

  const handleSubmit = async (e) => {
    e.preventDefault()
    setLoading(true)
    try {
      const { data } = await apiLogin(form)
      const { accessToken, refreshToken, role, name, email } = data.data
      login({ name, email, role }, { accessToken, refreshToken })
      toast.success(`Welcome back, ${name}!`)
      const redirectMap = { CUSTOMER: '/customer', AGENT: '/agent', TEAM_LEAD: '/agent', MANAGER: '/admin', ADMIN: '/admin' }
      navigate(redirectMap[role] || '/')
    } catch (err) {
      toast.error(getErrorMsg(err))
    } finally {
      setLoading(false)
    }
  }

  const quickLogin = (email, password) => setForm({ email, password })

  return (
    <div className="min-h-screen bg-gradient-to-br from-primary-50 to-white flex items-center justify-center p-4">
      <div className="w-full max-w-md">
        <div className="text-center mb-8">
          <div className="w-14 h-14 bg-primary-600 rounded-2xl flex items-center justify-center mx-auto mb-4">
            <span className="text-white text-xl font-bold">CQ</span>
          </div>
          <h1 className="text-2xl font-bold text-gray-900">ComplaintIQ</h1>
          <p className="text-gray-500 text-sm mt-1">AI-Powered Complaint Management</p>
        </div>

        <div className="card">
          <h2 className="text-lg font-semibold mb-5">Sign In</h2>
          <form onSubmit={handleSubmit} className="space-y-4">
            <div>
              <label className="label">Email</label>
              <input className="input" type="email" value={form.email} required
                onChange={e => setForm(f => ({ ...f, email: e.target.value }))} placeholder="you@example.com" />
            </div>
            <div>
              <label className="label">Password</label>
              <input className="input" type="password" value={form.password} required
                onChange={e => setForm(f => ({ ...f, password: e.target.value }))} placeholder="••••••••" />
            </div>
            <button type="submit" disabled={loading} className="btn-primary w-full flex items-center justify-center gap-2">
              {loading ? <Spinner size="sm" /> : 'Sign In'}
            </button>
          </form>

          <div className="mt-4 pt-4 border-t">
            <p className="text-xs text-gray-500 mb-2 font-medium">Quick Login (Demo)</p>
            <div className="grid grid-cols-2 gap-2">
              {[
                ['Admin', 'admin@complaintiq.com', 'Admin@123'],
                ['Manager', 'manager1@complaintiq.com', 'Manager@123'],
                ['Agent', 'priya.sharma@complaintiq.com', 'Agent@123'],
                ['Customer', 'aarav.shah@gmail.com', 'Customer@123'],
              ].map(([role, email, pw]) => (
                <button key={role} type="button" onClick={() => quickLogin(email, pw)}
                  className="text-xs bg-gray-50 hover:bg-gray-100 text-gray-600 px-2 py-1.5 rounded border transition-colors">
                  {role}
                </button>
              ))}
            </div>
          </div>

          <p className="text-sm text-center text-gray-500 mt-4">
            No account? <Link to="/register" className="text-primary-600 font-medium hover:underline">Register</Link>
          </p>
        </div>
      </div>
    </div>
  )
}
