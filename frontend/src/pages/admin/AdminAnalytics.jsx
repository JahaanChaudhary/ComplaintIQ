import { useState, useEffect } from 'react'
import { getComplaintTrends, getSLAReport, getAgentPerformance, getHeatmap } from '../../api/analytics'
import { Spinner } from '../../components/ui/Spinner'
import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, BarChart, Bar, Cell } from 'recharts'

const HEATMAP_COLORS = (val, max) => {
  if (!val || val === 0) return 'bg-gray-100'
  const pct = val / max
  if (pct > 0.75) return 'bg-blue-600 text-white'
  if (pct > 0.5) return 'bg-blue-400 text-white'
  if (pct > 0.25) return 'bg-blue-200 text-blue-800'
  return 'bg-blue-100 text-blue-700'
}

export default function AdminAnalytics() {
  const [period, setPeriod] = useState('daily')
  const [trends, setTrends] = useState(null)
  const [slaReport, setSlaReport] = useState(null)
  const [agentPerf, setAgentPerf] = useState([])
  const [heatmap, setHeatmap] = useState(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    Promise.all([
      getSLAReport(),
      getAgentPerformance(),
      getHeatmap()
    ]).then(([sla, perf, heat]) => {
      setSlaReport(sla.data.data)
      setAgentPerf(perf.data.data || [])
      setHeatmap(heat.data.data)
    }).finally(() => setLoading(false))
  }, [])

  useEffect(() => {
    getComplaintTrends(period).then(({ data }) => setTrends(data.data)).catch(() => {})
  }, [period])

  if (loading) return <div className="flex justify-center py-16"><Spinner /></div>

  const maxHeatmap = Math.max(...(heatmap?.cells?.map(c => c.complaintCount) || [1]))

  return (
    <div className="space-y-6">
      <h1 className="text-2xl font-bold text-gray-900">Analytics</h1>

      {/* SLA Report */}
      {slaReport && (
        <div className="card">
          <h3 className="font-semibold text-gray-800 mb-4">SLA Report</h3>
          <div className="grid grid-cols-2 sm:grid-cols-4 gap-4 mb-4">
            {[
              ['Total', slaReport.totalComplaints, 'text-gray-800'],
              ['On Time', slaReport.onTimeCount, 'text-green-600'],
              ['Breached', slaReport.breachedCount, 'text-red-600'],
              ['Breach Rate', `${slaReport.breachRatePercent?.toFixed(1)}%`, 'text-orange-600'],
            ].map(([label, val, cls]) => (
              <div key={label} className="text-center p-3 bg-gray-50 rounded-xl">
                <p className={`text-2xl font-bold ${cls}`}>{val}</p>
                <p className="text-xs text-gray-500 mt-0.5">{label}</p>
              </div>
            ))}
          </div>
          {slaReport.recentBreaches?.length > 0 && (
            <div>
              <p className="text-sm font-medium text-gray-700 mb-2">Recent Breaches</p>
              <div className="space-y-1">
                {slaReport.recentBreaches.slice(0, 5).map(b => (
                  <div key={b.ticketId} className="flex items-center justify-between text-sm bg-red-50 px-3 py-1.5 rounded-lg">
                    <span className="font-mono text-red-700 font-medium">{b.ticketId}</span>
                    <span className="text-red-600">{b.urgency}</span>
                    <span className="text-red-500 text-xs">{b.hoursOverdue?.toFixed(1)}h overdue</span>
                  </div>
                ))}
              </div>
            </div>
          )}
        </div>
      )}

      {/* Trends */}
      <div className="card">
        <div className="flex items-center justify-between mb-4">
          <h3 className="font-semibold text-gray-800">Complaint Volume Trend</h3>
          <div className="flex gap-2">
            {['daily', 'weekly'].map(p => (
              <button key={p} onClick={() => setPeriod(p)}
                className={`btn-sm rounded-lg capitalize ${period === p ? 'btn-primary' : 'btn-secondary'}`}>
                {p}
              </button>
            ))}
          </div>
        </div>
        {trends?.dataPoints?.length > 0 ? (
          <ResponsiveContainer width="100%" height={220}>
            <LineChart data={trends.dataPoints}>
              <CartesianGrid strokeDasharray="3 3" stroke="#f0f0f0" />
              <XAxis dataKey="date" tick={{ fontSize: 10 }} tickFormatter={d => d.slice(5)} />
              <YAxis tick={{ fontSize: 10 }} />
              <Tooltip />
              <Line type="monotone" dataKey="count" stroke="#3b82f6" strokeWidth={2} dot={false} />
            </LineChart>
          </ResponsiveContainer>
        ) : <p className="text-gray-400 text-sm text-center py-8">No trend data available</p>}
      </div>

      {/* Heatmap */}
      {heatmap?.cells && (
        <div className="card">
          <h3 className="font-semibold text-gray-800 mb-4">Hourly Volume Heatmap</h3>
          <div className="grid grid-cols-12 gap-1">
            {heatmap.cells.map(cell => (
              <div key={cell.hour} className={`rounded p-1.5 text-center text-xs transition-all ${HEATMAP_COLORS(cell.complaintCount, maxHeatmap)}`}>
                <div className="font-bold">{cell.complaintCount}</div>
                <div className="text-xs opacity-70">{cell.label}</div>
              </div>
            ))}
          </div>
          <p className="text-xs text-gray-400 mt-2">Complaint volume by hour of day</p>
        </div>
      )}

      {/* Agent Performance */}
      {agentPerf.length > 0 && (
        <div className="card">
          <h3 className="font-semibold text-gray-800 mb-4">Agent Performance Leaderboard</h3>
          <div className="overflow-x-auto">
            <table className="w-full text-sm">
              <thead>
                <tr className="text-left text-xs text-gray-500 border-b">
                  <th className="pb-2 font-medium">Rank</th>
                  <th className="pb-2 font-medium">Agent</th>
                  <th className="pb-2 font-medium">Dept</th>
                  <th className="pb-2 font-medium">Resolved</th>
                  <th className="pb-2 font-medium">Avg Time</th>
                  <th className="pb-2 font-medium">SLA Breaches</th>
                  <th className="pb-2 font-medium">Load</th>
                </tr>
              </thead>
              <tbody>
                {agentPerf.map((a, i) => (
                  <tr key={a.agentId} className="border-b border-gray-50 hover:bg-gray-50">
                    <td className="py-2.5">
                      <span className={`w-6 h-6 rounded-full flex items-center justify-center text-xs font-bold ${i < 3 ? 'bg-yellow-100 text-yellow-700' : 'bg-gray-100 text-gray-500'}`}>
                        {i + 1}
                      </span>
                    </td>
                    <td className="py-2.5">
                      <p className="font-medium text-gray-800">{a.agentName}</p>
                      <p className="text-xs text-gray-400">{a.role?.replace('_', ' ')}</p>
                    </td>
                    <td className="py-2.5 text-gray-600">{a.departmentName}</td>
                    <td className="py-2.5 font-semibold text-green-600">{a.totalResolved}</td>
                    <td className="py-2.5 text-gray-600">{a.avgResolutionTimeHours?.toFixed(1)}h</td>
                    <td className="py-2.5">
                      <span className={a.slaBreachCount > 0 ? 'text-red-600 font-medium' : 'text-green-600'}>{a.slaBreachCount}</span>
                    </td>
                    <td className="py-2.5 text-gray-600">{a.currentLoad}/{a.maxLoad}</td>
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
