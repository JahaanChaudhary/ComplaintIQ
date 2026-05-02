import { NavLink } from 'react-router-dom'
import { useAuth } from '../../context/AuthContext'
import {
  LayoutDashboard, FileText, PlusCircle, Search, Users,
  BarChart2, Settings, AlertTriangle, MessageSquare
} from 'lucide-react'

const navItem = (to, icon, label) => ({ to, icon, label })

const CUSTOMER_NAV = [
  navItem('/customer', LayoutDashboard, 'Dashboard'),
  navItem('/customer/submit', PlusCircle, 'Submit Complaint'),
  navItem('/customer/my-complaints', FileText, 'My Complaints'),
  navItem('/customer/track', Search, 'Track Complaint'),
]

const AGENT_NAV = [
  navItem('/agent', LayoutDashboard, 'Dashboard'),
  navItem('/agent/complaints', FileText, 'All Complaints'),
]

const ADMIN_NAV = [
  navItem('/admin', LayoutDashboard, 'Dashboard'),
  navItem('/admin/complaints', FileText, 'All Complaints'),
  navItem('/admin/agents', Users, 'Agents'),
  navItem('/admin/analytics', BarChart2, 'Analytics'),
]

export function Sidebar() {
  const { isRole } = useAuth()
  const navItems = isRole('CUSTOMER') ? CUSTOMER_NAV
    : isRole('AGENT', 'TEAM_LEAD') ? AGENT_NAV
    : ADMIN_NAV

  return (
    <aside className="w-56 bg-white border-r border-gray-200 min-h-[calc(100vh-4rem)] p-4 flex-shrink-0">
      <nav className="space-y-1">
        {navItems.map(({ to, icon: Icon, label }) => (
          <NavLink key={to} to={to} end={to.split('/').length <= 2}
            className={({ isActive }) =>
              `flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm font-medium transition-colors
              ${isActive ? 'bg-primary-50 text-primary-700' : 'text-gray-600 hover:bg-gray-50 hover:text-gray-900'}`
            }>
            <Icon size={18} />
            {label}
          </NavLink>
        ))}
      </nav>
    </aside>
  )
}
