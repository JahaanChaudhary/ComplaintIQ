import { useState } from 'react'
import { trackComplaint } from '../../api/complaints'
import { useWebSocket } from '../../hooks/useWebSocket'
import { getUrgencyClass, getStatusClass, getSlaClass, formatDate, getErrorMsg } from '../../utils/helpers'
import { Spinner } from '../../components/ui/Spinner'
import { Search, Clock, RefreshCw, Wifi } from 'lucide-react'
import toast from 'react-hot-toast'

export default function TrackComplaint() {
  const [ticketId, setTicketId] = useState('')
  const [complaint, setComplaint] = useState(null)
  const [loading, setLoading] = useState(false)
  const [liveUpdate, setLiveUpdate] = useState(null)

  const search = async (e) => {
    e?.preventDefault()
    if (!ticketId.trim()) return
    setLoading(true)
    try {
      const { data } = await trackComplaint(ticketId.trim().toUpperCase())
      setComplaint(data.data)
      setLiveUpdate(null)
    } catch (err) {
      toast.error(getErrorMsg(err))
      setComplaint(null)
    } finally { setLoading(false) }
  }

  useWebSocket(complaint?.ticketId, (update) => {
    setLiveUpdate(update)
    toast.success(`Status update: ${update.newStatus}`, { icon: '🔔' })
    setComplaint(c => c ? { ...c, status: update.newStatus } : c)
  })

  const currentStatus = liveUpdate?.newStatus || complaint?.status

  return (
    <div className="max-w-2xl space-y-5">
      <div>
        <h1 className="text-2xl font-bold text-gray-900">Track Complaint</h1>
        <p className="text-gray-500 text-sm mt-1">Enter your ticket ID to get live status updates</p>
      </div>

      <div className="card">
        <form onSubmit={search} className="flex gap-3">
          <input className="input flex-1" value={ticketId}
            onChange={e => setTicketId(e.target.value)} placeholder="e.g. CIQ-2024-00001" />
          <button type="submit" disabled={loading} className="btn-primary flex items-center gap-2 whitespace-nowrap">
            {loading ? <Spinner size="sm" /> : <Search size={16} />} Search
          </button>
        </form>
      </div>

      {complaint && (
        <div className="card space-y-5">
          <div className="flex items-start justify-between">
            <div>
              <div className="flex items-center gap-2 flex-wrap">
                <span className="font-mono text-primary-700 font-bold text-lg">{complaint.ticketId}</span>
                {liveUpdate && (
                  <span className="flex items-center gap-1 text-xs text-green-600 bg-green-50 px-2 py-0.5 rounded-full">
                    <Wifi size={10} /> Live
                  </span>
                )}
              </div>
              <h2 className="text-lg font-semibold text-gray-900 mt-1">{complaint.title}</h2>
            </div>
            <button onClick={search} className="btn-secondary btn-sm flex items-center gap-1">
              <RefreshCw size={13} /> Refresh
            </button>
          </div>

          <div className="flex gap-2 flex-wrap">
            <span className={getStatusClass(currentStatus)}>{currentStatus?.replace('_', ' ')}</span>
            {complaint.slaStatus && <span className={getSlaClass(complaint.slaStatus)}>{complaint.slaStatus?.replace('_', ' ')}</span>}
          </div>

          {liveUpdate && (
            <div className="bg-green-50 border border-green-200 rounded-lg p-3 text-sm text-green-700">
              🔔 {liveUpdate.message}
            </div>
          )}

          <div className="grid grid-cols-2 gap-4 text-sm">
            {complaint.assignment?.departmentName && (
              <div><p className="text-gray-400 text-xs">Department</p>
                <p className="font-medium">{complaint.assignment.departmentName}</p></div>
            )}
            {complaint.slaDeadline && (
              <div><p className="text-gray-400 text-xs">SLA Deadline</p>
                <p className="font-medium">{formatDate(complaint.slaDeadline)}</p></div>
            )}
            <div><p className="text-gray-400 text-xs">Submitted</p>
              <p className="font-medium">{formatDate(complaint.createdAt)}</p></div>
            {complaint.assignment?.assignedAt && (
              <div><p className="text-gray-400 text-xs">Assigned</p>
                <p className="font-medium">{formatDate(complaint.assignment.assignedAt)}</p></div>
            )}
          </div>

          <div className="bg-blue-50 rounded-lg p-4 text-sm text-blue-700">
            <p className="font-medium mb-1 flex items-center gap-1"><Wifi size={13} /> Real-time Updates Active</p>
            <p className="text-xs text-blue-600">This page will automatically update when the status of your complaint changes.</p>
          </div>
        </div>
      )}
    </div>
  )
}
