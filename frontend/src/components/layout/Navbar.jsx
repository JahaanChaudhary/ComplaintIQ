import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../../context/AuthContext'
import { logout as apiLogout } from '../../api/auth'
import { LogOut, LayoutDashboard, User } from 'lucide-react'
import toast from 'react-hot-toast'

export function Navbar() {
  const { user, logout, isRole } = useAuth()
  const navigate = useNavigate()

  const handleLogout = async () => {
    try { await apiLogout() } catch {}
    logout()
    navigate('/login')
    toast.success('Logged out successfully')
  }

  const homeLink = () => {
    if (isRole('CUSTOMER')) return '/customer'
    if (isRole('AGENT', 'TEAM_LEAD')) return '/agent'
    if (isRole('MANAGER', 'ADMIN')) return '/admin'
    return '/'
  }

  return (
    <nav className="bg-white border-b border-gray-200 sticky top-0 z-40">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex items-center justify-between h-16">
          <Link to={homeLink()} className="flex items-center gap-2">
            <div className="w-8 h-8 bg-primary-600 rounded-lg flex items-center justify-center">
              <span className="text-white text-sm font-bold">CQ</span>
            </div>
            <span className="font-bold text-gray-900 text-lg">ComplaintIQ</span>
          </Link>

          {user && (
            <div className="flex items-center gap-4">
              <div className="hidden sm:flex items-center gap-2 text-sm text-gray-600">
                <User size={15} />
                <span className="font-medium">{user.name}</span>
                <span className="bg-primary-100 text-primary-700 text-xs px-2 py-0.5 rounded-full font-medium">
                  {user.role?.replace('_', ' ')}
                </span>
              </div>
              <button onClick={handleLogout}
                className="flex items-center gap-1.5 text-sm text-gray-500 hover:text-gray-800 transition-colors">
                <LogOut size={16} /> Logout
              </button>
            </div>
          )}
        </div>
      </div>
    </nav>
  )
}
