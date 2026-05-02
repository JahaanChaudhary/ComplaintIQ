import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../../context/AuthContext'
import { register as apiRegister } from '../../api/auth'
import { getErrorMsg } from '../../utils/helpers'
import { Spinner } from '../../components/ui/Spinner'
import toast from 'react-hot-toast'

export default function Register() {
  const [form, setForm] = useState({ name: '', email: '', password: '', phone: '' })
  const [loading, setLoading] = useState(false)
  const { login } = useAuth()
  const navigate = useNavigate()

  const handleSubmit = async (e) => {
    e.preventDefault()
    setLoading(true)
    try {
      const { data } = await apiRegister(form)
      const { accessToken, refreshToken, role, name, email } = data.data
      login({ name, email, role }, { accessToken, refreshToken })
      toast.success('Account created! Welcome to ComplaintIQ.')
      navigate('/customer')
    } catch (err) {
      toast.error(getErrorMsg(err))
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="min-h-screen bg-gradient-to-br from-primary-50 to-white flex items-center justify-center p-4">
      <div className="w-full max-w-md">
        <div className="text-center mb-8">
          <div className="w-14 h-14 bg-primary-600 rounded-2xl flex items-center justify-center mx-auto mb-4">
            <span className="text-white text-xl font-bold">CQ</span>
          </div>
          <h1 className="text-2xl font-bold text-gray-900">Create Account</h1>
          <p className="text-gray-500 text-sm mt-1">Register as a customer</p>
        </div>

        <div className="card">
          <form onSubmit={handleSubmit} className="space-y-4">
            {[
              ['Name', 'name', 'text', 'John Doe'],
              ['Email', 'email', 'email', 'you@example.com'],
              ['Password (min 8 chars)', 'password', 'password', '••••••••'],
              ['Phone (optional)', 'phone', 'tel', '+91-9876543210'],
            ].map(([label, field, type, ph]) => (
              <div key={field}>
                <label className="label">{label}</label>
                <input className="input" type={type} placeholder={ph}
                  required={field !== 'phone'} value={form[field]}
                  onChange={e => setForm(f => ({ ...f, [field]: e.target.value }))} />
              </div>
            ))}
            <button type="submit" disabled={loading} className="btn-primary w-full flex items-center justify-center gap-2">
              {loading ? <Spinner size="sm" /> : 'Create Account'}
            </button>
          </form>
          <p className="text-sm text-center text-gray-500 mt-4">
            Already have an account? <Link to="/login" className="text-primary-600 font-medium hover:underline">Sign in</Link>
          </p>
        </div>
      </div>
    </div>
  )
}
