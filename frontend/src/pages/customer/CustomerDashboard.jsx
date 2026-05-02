import { useState, useEffect } from 'react'
import { Link } from 'react-router-dom'
import { useAuth } from '../../context/AuthContext'
import { getMyComplaints } from '../../api/complaints'
import { ComplaintCard } from '../../components/ui/ComplaintCard'
import { Spinner } from '../../components/ui/Spinner'
import { PlusCircle, Search, FileText } from 'lucide-react'

export default function CustomerDashboard() {
  const { user } = useAuth()
  const [complaints, setComplaints] = useState([])
  const [allComplaints, setAllComplaints] = useState([])
  const [totalComplaints, setTotalComplaints] = useState(0)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    getMyComplaints(0, 1000)
      .then(({ data }) => {
        const all = data.data?.content || []
        setAllComplaints(all)
        setComplaints(all.slice(0, 5))
        setTotalComplaints(data.data?.totalElements ?? all.length)
      })
      .catch(() => {})
      .finally(() => setLoading(false))
  }, [])

  const stats = {
    total: totalComplaints,
    open: allComplaints.filter(c => ['OPEN', 'ASSIGNED', 'IN_PROGRESS', 'ESCALATED'].includes(c.status)).length,
    resolved: allComplaints.filter(c => c.status === 'RESOLVED' || c.status === 'CLOSED').length,
  }

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-gray-900">Welcome, {user?.name} 👋</h1>
        <p className="text-gray-500 text-sm mt-1">Manage and track your complaints</p>
      </div>

      <div className="grid grid-cols-3 gap-4">
        {[['Total', stats.total, 'blue'], ['Active', stats.open, 'orange'], ['Resolved', stats.resolved, 'green']].map(([label, val, color]) => (
          <div key={label} className={`card text-center border-t-4 border-t-${color === 'blue' ? 'blue' : color === 'orange' ? 'orange' : 'green'}-500`}>
            <p className="text-3xl font-bold text-gray-900">{val}</p>
            <p className="text-sm text-gray-500 mt-1">{label}</p>
          </div>
        ))}
      </div>

      <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
        <Link to="/customer/submit" className="card flex flex-col items-center gap-3 hover:shadow-md transition-shadow text-center group">
          <div className="w-12 h-12 bg-primary-50 text-primary-600 rounded-xl flex items-center justify-center group-hover:bg-primary-100 transition-colors">
            <PlusCircle size={24} />
          </div>
          <div><p className="font-semibold text-gray-900">Submit Complaint</p>
          <p className="text-xs text-gray-500 mt-0.5">Raise a new issue</p></div>
        </Link>
        <Link to="/customer/track" className="card flex flex-col items-center gap-3 hover:shadow-md transition-shadow text-center group">
          <div className="w-12 h-12 bg-green-50 text-green-600 rounded-xl flex items-center justify-center group-hover:bg-green-100 transition-colors">
            <Search size={24} />
          </div>
          <div><p className="font-semibold text-gray-900">Track Complaint</p>
          <p className="text-xs text-gray-500 mt-0.5">Enter ticket ID</p></div>
        </Link>
        <Link to="/customer/my-complaints" className="card flex flex-col items-center gap-3 hover:shadow-md transition-shadow text-center group">
          <div className="w-12 h-12 bg-purple-50 text-purple-600 rounded-xl flex items-center justify-center group-hover:bg-purple-100 transition-colors">
            <FileText size={24} />
          </div>
          <div><p className="font-semibold text-gray-900">My Complaints</p>
          <p className="text-xs text-gray-500 mt-0.5">View all history</p></div>
        </Link>
      </div>

      <div>
        <div className="flex items-center justify-between mb-4">
          <h2 className="text-lg font-semibold text-gray-900">Recent Complaints</h2>
          <Link to="/customer/my-complaints" className="text-sm text-primary-600 hover:underline">View all</Link>
        </div>
        {loading ? (
          <div className="flex justify-center py-8"><Spinner /></div>
        ) : complaints.length === 0 ? (
          <div className="card text-center py-12 text-gray-400">
            <FileText size={40} className="mx-auto mb-3 opacity-50" />
            <p>No complaints yet. <Link to="/customer/submit" className="text-primary-600">Submit one</Link></p>
          </div>
        ) : (
          <div className="space-y-3">
            {complaints.map(c => <ComplaintCard key={c.ticketId} complaint={c} linkTo={`/customer/complaint/${c.ticketId}`} />)}
          </div>
        )}
      </div>
    </div>
  )
}
