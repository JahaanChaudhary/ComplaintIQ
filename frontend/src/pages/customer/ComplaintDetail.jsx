import { useState, useEffect } from 'react'
import { useParams } from 'react-router-dom'
import { getComplaintById, submitFeedback } from '../../api/complaints'
import { useWebSocket } from '../../hooks/useWebSocket'
import { Modal } from '../../components/ui/Modal'
import { Spinner } from '../../components/ui/Spinner'
import { formatDate, getUrgencyClass, getStatusClass, getSlaClass, getErrorMsg } from '../../utils/helpers'
import { Star, Clock, Brain, User, Building, Wifi } from 'lucide-react'
import toast from 'react-hot-toast'

export default function ComplaintDetail() {
  const { ticketId } = useParams()
  const [complaint, setComplaint] = useState(null)
  const [loading, setLoading] = useState(true)
  const [feedbackOpen, setFeedbackOpen] = useState(false)
  const [feedback, setFeedback] = useState({ satisfactionScore: 5, feedbackComment: '' })
  const [submitting, setSubmitting] = useState(false)

  const load = () => getComplaintById(ticketId)
    .then(({ data }) => setComplaint(data.data))
    .catch(() => toast.error('Complaint not found'))
    .finally(() => setLoading(false))

  useEffect(() => { load() }, [ticketId])

  useWebSocket(ticketId, (update) => {
    toast.success(`Status: ${update.newStatus}`, { icon: '🔔' })
    setComplaint(c => c ? { ...c, status: update.newStatus } : c)
  })

  const handleFeedback = async (e) => {
    e.preventDefault()
    setSubmitting(true)
    try {
      await submitFeedback(ticketId, feedback)
      toast.success('Feedback submitted! Thank you.')
      setFeedbackOpen(false)
      load()
    } catch (err) {
      toast.error(getErrorMsg(err))
    } finally { setSubmitting(false) }
  }

  if (loading) return <div className="flex justify-center py-16"><Spinner /></div>
  if (!complaint) return <div className="card text-center py-12 text-gray-400">Complaint not found</div>

  const ai = complaint.aiAnalysis
  const assign = complaint.assignment

  return (
    <div className="space-y-5 max-w-3xl">
      <div className="flex items-start justify-between gap-4 flex-wrap">
        <div>
          <div className="flex items-center gap-2 flex-wrap mb-1">
            <span className="font-mono text-primary-700 font-bold">{complaint.ticketId}</span>
            {complaint.urgency && <span className={getUrgencyClass(complaint.urgency)}>{complaint.urgency}</span>}
            <span className={getStatusClass(complaint.status)}>{complaint.status?.replace('_', ' ')}</span>
            {complaint.slaStatus && <span className={getSlaClass(complaint.slaStatus)}>{complaint.slaStatus?.replace('_', ' ')}</span>}
          </div>
          <h1 className="text-xl font-bold text-gray-900">{complaint.title}</h1>
        </div>
        {complaint.status === 'RESOLVED' && (
          <button onClick={() => setFeedbackOpen(true)} className="btn-primary btn-sm flex items-center gap-1">
            <Star size={14} /> Rate
          </button>
        )}
      </div>

      <div className="card">
        <h3 className="font-semibold mb-2 text-gray-700">Description</h3>
        <p className="text-gray-600 text-sm leading-relaxed">{complaint.description}</p>
        {complaint.orderId && <p className="text-xs text-gray-400 mt-2">Order ID: {complaint.orderId}</p>}
      </div>

      <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
        {assign && (
          <div className="card space-y-2 text-sm">
            <h3 className="font-semibold text-gray-700 flex items-center gap-1"><Building size={14} /> Assignment</h3>
            {assign.departmentName && <p className="text-gray-500">Dept: <span className="text-gray-800 font-medium">{assign.departmentName}</span></p>}
            {assign.agentName && <p className="text-gray-500">Agent: <span className="text-gray-800 font-medium">{assign.agentName}</span></p>}
            <p className="text-gray-500">Assigned: <span className="text-gray-800">{formatDate(assign.assignedAt)}</span></p>
          </div>
        )}
        <div className="card space-y-2 text-sm">
          <h3 className="font-semibold text-gray-700 flex items-center gap-1"><Clock size={14} /> Timeline</h3>
          <p className="text-gray-500">Submitted: <span className="text-gray-800">{formatDate(complaint.createdAt)}</span></p>
          {complaint.slaDeadline && <p className="text-gray-500">SLA: <span className="text-gray-800">{formatDate(complaint.slaDeadline)}</span></p>}
          {complaint.resolvedAt && <p className="text-gray-500">Resolved: <span className="text-gray-800">{formatDate(complaint.resolvedAt)}</span></p>}
        </div>
      </div>

      {ai && (
        <div className="card bg-blue-50 border-blue-100">
          <h3 className="font-semibold text-blue-800 flex items-center gap-1 mb-3"><Brain size={14} /> AI Analysis</h3>
          <div className="grid grid-cols-2 sm:grid-cols-3 gap-3 text-sm">
            {[['Urgency', ai.urgency], ['Category', ai.category], ['Sentiment', ai.sentiment], ['Intent', ai.intent], ['Confidence', ai.confidenceScore ? `${(ai.confidenceScore * 100).toFixed(0)}%` : '-']].map(([k, v]) => (
              <div key={k}><p className="text-blue-500 text-xs">{k}</p>
              <p className="text-blue-900 font-medium">{v}</p></div>
            ))}
            {ai.suggestedAction && <div className="col-span-2 sm:col-span-3">
              <p className="text-blue-500 text-xs">Suggested Action</p>
              <p className="text-blue-900 font-medium">{ai.suggestedAction}</p>
            </div>}
          </div>
        </div>
      )}

      {complaint.timeline?.length > 0 && (
        <div className="card">
          <h3 className="font-semibold text-gray-700 mb-4">Activity Timeline</h3>
          <div className="space-y-3">
            {complaint.timeline.map((a, i) => (
              <div key={i} className="flex gap-3 text-sm">
                <div className="w-2 h-2 bg-primary-400 rounded-full mt-1.5 flex-shrink-0" />
                <div>
                  <p className="font-medium text-gray-800">{a.action}</p>
                  {a.notes && <p className="text-gray-500 text-xs">{a.notes}</p>}
                  <p className="text-gray-400 text-xs mt-0.5">{formatDate(a.performedAt)} · {a.performedBy}</p>
                </div>
              </div>
            ))}
          </div>
        </div>
      )}

      <Modal isOpen={feedbackOpen} onClose={() => setFeedbackOpen(false)} title="Rate Your Experience">
        <form onSubmit={handleFeedback} className="space-y-4">
          <div>
            <label className="label">Satisfaction Score</label>
            <div className="flex gap-2">
              {[1, 2, 3, 4, 5].map(n => (
                <button type="button" key={n}
                  onClick={() => setFeedback(f => ({ ...f, satisfactionScore: n }))}
                  className={`w-10 h-10 rounded-lg text-lg transition-all ${feedback.satisfactionScore >= n ? 'bg-yellow-400 text-white' : 'bg-gray-100 text-gray-400'}`}>
                  ★
                </button>
              ))}
            </div>
            <p className="text-xs text-gray-400 mt-1">Note: Score 1-2 will reopen the complaint</p>
          </div>
          <div>
            <label className="label">Comment (optional)</label>
            <textarea className="input resize-none" rows={3} value={feedback.feedbackComment}
              onChange={e => setFeedback(f => ({ ...f, feedbackComment: e.target.value }))}
              placeholder="Share your experience..." />
          </div>
          <button type="submit" disabled={submitting} className="btn-primary w-full flex items-center justify-center gap-2">
            {submitting ? <Spinner size="sm" /> : 'Submit Feedback'}
          </button>
        </form>
      </Modal>
    </div>
  )
}
