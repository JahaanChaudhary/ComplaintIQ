import { useState, useEffect } from 'react'
import { getDashboardStats } from '../../api/analytics'
import { StatCard } from '../../components/ui/StatCard'
import { Spinner } from '../../components/ui/Spinner'
import { BarChart, Bar, XAxis, YAxis, Tooltip, ResponsiveContainer, PieChart, Pie, Cell, Legend } from 'recharts'
import { FileText, CheckCircle, AlertTriangle, Users, TrendingUp, Clock } from 'lucide-react'

const COLORS = ['#3b82f6', '#ef4444', '#f59e0b', '#22c55e', '#8b5cf6', '#06b6d4']

export default function AdminDashboard() {
  const [stats, setStats] = useState(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    getDashboardStats()
      .then(({ data }) => setStats(data.data))
      .catch(() => {})
      .finally(() => setLoading(false))
  }, [])

  if (loading) return <div className="flex justify-center py-16"><Spinner /></div>
  if (!stats) return <div className="card text-center py-12 text-gray-400">Could not load stats</div>

  const categoryData = Object.entries(stats.complaintsByCategory || {}).map(([name, value]) => ({ name, value }))
  const urgencyData = Object.entries(stats.complaintsByUrgency || {}).map(([name, value]) => ({ name, value }))
  const statusData = Object.entries(stats.complaintsByStatus || {}).map(([name, value]) => ({ name: name.replace('_', ' '), value }))

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-gray-900">Admin Dashboard</h1>
        <p className="text-gray-500 text-sm mt-1">System-wide analytics overview</p>
      </div>

      <div className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-4 gap-4">
        <StatCard title="Total Complaints" value={stats.totalComplaints} icon={FileText} color="blue" />
        <StatCard title="Open & Active" value={stats.openComplaints} icon={TrendingUp} color="orange" />
        <StatCard title="Resolved Today" value={stats.resolvedToday} icon={CheckCircle} color="green" />
        <StatCard title="Critical Open" value={stats.criticalOpen} icon={AlertTriangle} color="red" />
        <StatCard title="Escalated" value={stats.escalatedComplaints} icon={AlertTriangle} color="purple" />
        <StatCard title="SLA Breach Rate" value={stats.slaBreachRate ? `${stats.slaBreachRate.toFixed(1)}%` : '0%'} icon={Clock} color="yellow" />
        <StatCard title="Avg Satisfaction" value={stats.avgSatisfactionScore ? `${stats.avgSatisfactionScore.toFixed(1)}/5` : '-'} icon={Users} color="green" />
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <div className="card">
          <h3 className="font-semibold text-gray-800 mb-4">Complaints by Category</h3>
          {categoryData.length > 0 ? (
            <ResponsiveContainer width="100%" height={220}>
              <BarChart data={categoryData}>
                <XAxis dataKey="name" tick={{ fontSize: 11 }} />
                <YAxis tick={{ fontSize: 11 }} />
                <Tooltip />
                <Bar dataKey="value" fill="#3b82f6" radius={[4, 4, 0, 0]} />
              </BarChart>
            </ResponsiveContainer>
          ) : <p className="text-gray-400 text-sm text-center py-8">No data</p>}
        </div>

        <div className="card">
          <h3 className="font-semibold text-gray-800 mb-4">Complaints by Urgency</h3>
          {urgencyData.length > 0 ? (
            <ResponsiveContainer width="100%" height={220}>
              <PieChart>
                <Pie data={urgencyData} dataKey="value" nameKey="name" cx="50%" cy="50%" outerRadius={80} label={({ name, percent }) => `${name} ${(percent * 100).toFixed(0)}%`} labelLine={false}>
                  {urgencyData.map((_, i) => <Cell key={i} fill={COLORS[i % COLORS.length]} />)}
                </Pie>
                <Tooltip />
              </PieChart>
            </ResponsiveContainer>
          ) : <p className="text-gray-400 text-sm text-center py-8">No data</p>}
        </div>
      </div>

      <div className="card">
        <h3 className="font-semibold text-gray-800 mb-4">Complaints by Status</h3>
        {statusData.length > 0 ? (
          <ResponsiveContainer width="100%" height={180}>
            <BarChart data={statusData} layout="vertical">
              <XAxis type="number" tick={{ fontSize: 11 }} />
              <YAxis dataKey="name" type="category" tick={{ fontSize: 11 }} width={90} />
              <Tooltip />
              <Bar dataKey="value" radius={[0, 4, 4, 0]}>
                {statusData.map((_, i) => <Cell key={i} fill={COLORS[i % COLORS.length]} />)}
              </Bar>
            </BarChart>
          </ResponsiveContainer>
        ) : <p className="text-gray-400 text-sm text-center py-8">No data</p>}
      </div>

      {stats.topPerformingAgents?.length > 0 && (
        <div className="card">
          <h3 className="font-semibold text-gray-800 mb-4">Top Performing Agents</h3>
          <div className="overflow-x-auto">
            <table className="w-full text-sm">
              <thead>
                <tr className="text-left text-xs text-gray-500 border-b">
                  <th className="pb-2 font-medium">Agent</th>
                  <th className="pb-2 font-medium">Department</th>
                  <th className="pb-2 font-medium">Resolved</th>
                  <th className="pb-2 font-medium">Avg Time</th>
                  <th className="pb-2 font-medium">Load</th>
                </tr>
              </thead>
              <tbody>
                {stats.topPerformingAgents.map((a, i) => (
                  <tr key={a.agentId} className="border-b border-gray-50 hover:bg-gray-50">
                    <td className="py-2.5">
                      <div className="flex items-center gap-2">
                        <span className="w-6 h-6 bg-primary-100 text-primary-700 rounded-full flex items-center justify-center text-xs font-bold">{i + 1}</span>
                        <div>
                          <p className="font-medium text-gray-800">{a.agentName}</p>
                          <p className="text-xs text-gray-400">{a.role?.replace('_', ' ')}</p>
                        </div>
                      </div>
                    </td>
                    <td className="py-2.5 text-gray-600">{a.departmentName}</td>
                    <td className="py-2.5 font-semibold text-green-600">{a.totalResolved}</td>
                    <td className="py-2.5 text-gray-600">{a.avgResolutionTimeHours?.toFixed(1)}h</td>
                    <td className="py-2.5"><span className="text-gray-600">{a.currentLoad}/{a.maxLoad}</span></td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}
    </div>
  )
}
