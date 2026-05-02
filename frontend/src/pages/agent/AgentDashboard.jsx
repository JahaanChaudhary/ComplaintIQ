import { useState, useEffect } from 'react'
import { useAuth } from '../../context/AuthContext'
import { getAgentDashboard, getAllAgents, toggleAvailability } from '../../api/agents'
import { ComplaintCard } from '../../components/ui/ComplaintCard'
import { StatCard } from '../../components/ui/StatCard'
import { Spinner } from '../../components/ui/Spinner'
import { FileText, CheckCircle, AlertTriangle, Clock, ToggleLeft, ToggleRight } from 'lucide-react'
import toast from 'react-hot-toast'
import { getErrorMsg } from '../../utils/helpers'

export default function AgentDashboard() {
  const { user } = useAuth()
  const [dashboard, setDashboard] = useState(null)
  const [agentId, setAgentId] = useState(null)
  const [loading, setLoading] = useState(true)
  const [toggling, setToggling] = useState(false)

  useEffect(() => {
    // Find agent by matching the current user's agentId
    if (user?.agentId) {
      setAgentId(user.agentId)
      loadDashboard(user.agentId)
    } else {
      // Fallback: load all agents and find by email
      getAllAgents(0, 50).then(({ data }) => {
        const agents = data.data?.content || []
        const found = agents.find(a => a.email === user?.email)
        if (found) { setAgentId(found.id); loadDashboard(found.id) }
        else setLoading(false)
      }).catch(() => setLoading(false))
    }
  }, [user])

  const loadDashboard = (id) => {
    setLoading(true)
    getAgentDashboard(id)
      .then(({ data }) => setDashboard(data.data))
      .catch(() => toast.error('Failed to load dashboard'))
      .finally(() => setLoading(false))
  }

  const handleToggle = async () => {
    if (!agentId) return
    setToggling(true)
    try {
      await toggleAvailability(agentId)
      toast.success('Availability updated')
      loadDashboard(agentId)
    } catch (err) { toast.error(getErrorMsg(err)) }
    finally { setToggling(false) }
  }

  if (loading) return <div className="flex justify-center py-16"><Spinner /></div>
  if (!dashboard) return <div className="card text-center py-12 text-gray-400">Dashboard unavailable</div>

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between flex-wrap gap-3">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">Agent Dashboard</h1>
          <p className="text-gray-500 text-sm">{dashboard.agentName} · {dashboard.departmentName} · {dashboard.role?.replace('_', ' ')}</p>
        </div>
        <button onClick={handleToggle} disabled={toggling}
          className={`flex items-center gap-2 px-4 py-2 rounded-lg font-medium text-sm transition-all ${dashboard.isAvailable ? 'bg-green-100 text-green-700 hover:bg-green-200' : 'bg-gray-100 text-gray-600 hover:bg-gray-200'}`}>
          {dashboard.isAvailable ? <ToggleRight size={18} /> : <ToggleLeft size={18} />}
          {dashboard.isAvailable ? 'Available' : 'Unavailable'}
        </button>
      </div>

      <div className="grid grid-cols-2 sm:grid-cols-4 gap-4">
        <StatCard title="Current Load" value={`${dashboard.currentLoad}/${dashboard.maxLoad}`} icon={FileText} color="blue" />
        <StatCard title="Total Resolved" value={dashboard.totalResolved} icon={CheckCircle} color="green" />
        <StatCard title="SLA Breaches" value={dashboard.slaBreachCount} icon={AlertTriangle} color="red" />
        <StatCard title="Avg Resolution" value={dashboard.avgResolutionTimeHours ? `${dashboard.avgResolutionTimeHours.toFixed(1)}h` : '-'} icon={Clock} color="purple" />
      </div>

      <div>
        <h2 className="text-lg font-semibold text-gray-900 mb-3">Assigned Complaints ({dashboard.assignedComplaints?.length || 0})</h2>
        {dashboard.assignedComplaints?.length === 0 ? (
          <div className="card text-center py-10 text-gray-400">
            <CheckCircle size={36} className="mx-auto mb-2 opacity-40" />
            <p>No active complaints assigned</p>
          </div>
        ) : (
          <div className="space-y-3">
            {dashboard.assignedComplaints?.map(c => (
              <ComplaintCard key={c.ticketId} complaint={c} linkTo={`/agent/complaint/${c.ticketId}`} />
            ))}
          </div>
        )}
      </div>
    </div>
  )
}
