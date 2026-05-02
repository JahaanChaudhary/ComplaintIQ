import { useState, useEffect } from 'react'
import { useParams } from 'react-router-dom'
import { getComplaintById, updateStatus, resolveComplaint, reassignComplaint, escalateComplaint } from '../../api/complaints'
import { getAllAgents } from '../../api/agents'
import { Modal } from '../../components/ui/Modal'
import { Spinner } from '../../components/ui/Spinner'
import { formatDate, getUrgencyClass, getStatusClass, getSlaClass, getErrorMsg } from '../../utils/helpers'
import { useAuth } from '../../context/AuthContext'
import { Brain, Clock, User, Building, CheckCircle, AlertTriangle, RefreshCw, ArrowUp } from 'lucide-react'
import toast from 'react-hot-toast'

const STATUS_OPTIONS = ['IN_PROGRESS', 'RESOLVED', 'CLOSED']
const ESCALATION_REASONS = ['SLA_BREACH', 'CUSTOMER_REQUEST', 'AGENT_REQUEST', 'LEGAL_THREAT']

export default function AgentComplaintDetail() {
  const { ticketId } = useParams()
  const { isRole } = useAuth()
  const [complaint, setComplaint] = useState(null)
  const [agents, setAgents] = useState([])
  const [loading, setLoading] = useState(true)
  const [modals, setModals] = useState({ status: false, resolve: false, reassign: false, escalate: false })
  const [forms, setForms] = useState({ status: '', note: '', resolutionNote: '', resolutionType: 'INFORMATION_PROVIDED', newAgentId: '', reason: '', escalateReason: 'AGENT_REQUEST', escalateNotes: '' })
  const [submitting, setSubmitting] = useState(false)

  const open = (key) => setModals(m => ({ ...m, [key]: true }))
  const close = (key) => setModals(m => ({ ...m, [key]: false }))

  const load = () => {
    setLoading(true)
    getComplaintById(ticketId)
      .then(({ data }) => setComplaint(data.data))
      .catch(() => toast.error('Complaint not found'))
      .finally(() => setLoading(false))
  }

  useEffect(() => {
    load()
    getAllAgents(0, 50).then(({ data }) => setAgents(data.data?.content || [])).catch(() => {})
  }, [ticketId])

  const handleStatus = async (e) => {
    e.preventDefault(); setSubmitting(true)
    try {
      await updateStatus(ticketId, { status: forms.status, note: forms.note })
      toast.success('Status updated'); close('status'); load()
    } catch (err) { toast.error(getErrorMsg(err)) }
    finally { setSubmitting(false) }
  }

  const handleResolve = async (e) => {
    e.preventDefault(); setSubmitting(true)
    try {
      await resolveComplaint(ticketId, { resolutionNote: forms.resolutionNote, resolutionType: forms.resolutionType })
      toast.success('Complaint resolved!'); close('resolve'); load()
    } catch (err) { toast.error(getErrorMsg(err)) }
    finally { setSubmitting(false) }
  }

  const handleReassign = async (e) => {
    e.preventDefault(); setSubmitting(true)
    try {
      await reassignComplaint(ticketId, { newAgentId: parseInt(forms.newAgentId), reason: forms.reason })
      toast.success('Complaint reassigned'); close('reassign'); load()
    } catch (err) { toast.error(getErrorMsg(err)) }
    finally { setSubmitting(false) }
  }

  const handleEscalate = async (e) => {
    e.preventDefault(); setSubmitting(true)
    try {
      await escalateComplaint(ticketId, { reason: forms.escalateReason, notes: forms.escalateNotes })
      toast.success('Complaint escalated'); close('escalate'); load()
    } catch (err) { toast.error(getErrorMsg(err)) }
    finally { setSubmitting(false) }
  }

  if (loading) return <div className="flex justify-center py-16"><Spinner /></div>
  if (!complaint) return <div className="card text-center py-12 text-gray-400">Complaint not found</div>

  const ai = complaint.aiAnalysis
  const assign = complaint.assignment
  const isResolved = complaint.status === 'RESOLVED' || complaint.status === 'CLOSED'
  const canReassign = isRole('TEAM_LEAD', 'MANAGER', 'ADMIN')
  const canEscalate = isRole('TEAM_LEAD', 'MANAGER', 'ADMIN')

  return (
    <div className="space-y-5 max-w-3xl">
      <div className="flex items-start justify-between gap-3 flex-wrap">
        <div>
          <div className="flex items-center gap-2 flex-wrap mb-1">
            <span className="font-mono text-primary-700 font-bold">{complaint.ticketId}</span>
            {complaint.urgency && <span className={getUrgencyClass(complaint.urgency)}>{complaint.urgency}</span>}
            <span className={getStatusClass(complaint.status)}>{complaint.status?.replace('_', ' ')}</span>
            {complaint.slaStatus && <span className={getSlaClass(complaint.slaStatus)}>{complaint.slaStatus}</span>}
          </div>
          <h1 className="text-xl font-bold text-gray-900">{complaint.title}</h1>
        </div>
        {!isResolved && (
          <div className="flex gap-2 flex-wrap">
            <button onClick={() => open('status')} className="btn-secondary btn-sm flex items-center gap-1">
              <RefreshCw size={13} /> Status
            </button>
            <button onClick={() => open('resolve')} className="btn-primary btn-sm flex items-center gap-1">
              <CheckCircle size={13} /> Resolve
            </button>
            {canReassign && (
              <button onClick={() => open('reassign')} className="btn-secondary btn-sm flex items-center gap-1">
                <User size={13} /> Reassign
              </button>
            )}
            {canEscalate && (
              <button onClick={() => open('escalate')} className="btn-danger btn-sm flex items-center gap-1">
                <ArrowUp size={13} /> Escalate
              </button>
            )}
          </div>
        )}
      </div>

      <div className="card">
        <div className="flex items-center gap-2 mb-2">
          <User size={14} className="text-gray-400" />
          <span className="font-semibold text-gray-700">{complaint.customer?.name}</span>
          <span className="text-xs text-gray-400">{complaint.customer?.email}</span>
          {complaint.customer?.tier !== 'NORMAL' && (
            <span className="bg-yellow-100 text-yellow-700 text-xs px-2 py-0.5 rounded-full font-medium">{complaint.customer?.tier}</span>
          )}
        </div>
        <p className="text-gray-600 text-sm leading-relaxed">{complaint.description}</p>
        {complaint.orderId && <p className="text-xs text-gray-400 mt-2">Order ID: {complaint.orderId}</p>}
      </div>

      <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
        {assign && (
          <div className="card text-sm space-y-1.5">
            <h3 className="font-semibold text-gray-700 flex items-center gap-1 mb-2"><Building size={14} /> Assignment</h3>
            <p className="text-gray-500">Dept: <span className="font-medium text-gray-800">{assign.departmentName}</span></p>
            {assign.agentName && <p className="text-gray-500">Agent: <span className="font-medium text-gray-800">{assign.agentName}</span></p>}
            <p className="text-gray-500">Since: <span className="text-gray-800">{formatDate(assign.assignedAt)}</span></p>
          </div>
        )}
        <div className="card text-sm space-y-1.5">
          <h3 className="font-semibold text-gray-700 flex items-center gap-1 mb-2"><Clock size={14} /> Timing</h3>
          <p className="text-gray-500">Created: <span className="text-gray-800">{formatDate(complaint.createdAt)}</span></p>
          {complaint.slaDeadline && <p className="text-gray-500">SLA: <span className="text-gray-800">{formatDate(complaint.slaDeadline)}</span></p>}
          {complaint.resolvedAt && <p className="text-gray-500">Resolved: <span className="text-gray-800">{formatDate(complaint.resolvedAt)}</span></p>}
        </div>
      </div>

      {ai && (
        <div className="card bg-blue-50 border-blue-100">
          <h3 className="font-semibold text-blue-800 flex items-center gap-1 mb-3"><Brain size={14} /> AI Analysis</h3>
          <div className="grid grid-cols-2 sm:grid-cols-3 gap-3 text-sm">
            {[['Urgency', ai.urgency], ['Category', ai.category], ['Sentiment', ai.sentiment], ['Intent', ai.intent], ['Confidence', ai.confidenceScore ? `${(ai.confidenceScore * 100).toFixed(0)}%` : '-']].map(([k, v]) => (
              <div key={k}><p className="text-blue-500 text-xs">{k}</p><p className="text-blue-900 font-medium">{v}</p></div>
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

      {/* Status Modal */}
      <Modal isOpen={modals.status} onClose={() => close('status')} title="Update Status">
        <form onSubmit={handleStatus} className="space-y-4">
          <div>
            <label className="label">New Status</label>
            <select className="input" value={forms.status} required onChange={e => setForms(f => ({ ...f, status: e.target.value }))}>
              <option value="">Select status</option>
              {STATUS_OPTIONS.map(s => <option key={s} value={s}>{s.replace('_', ' ')}</option>)}
            </select>
          </div>
          <div>
            <label className="label">Note (optional)</label>
            <input className="input" value={forms.note} onChange={e => setForms(f => ({ ...f, note: e.target.value }))} placeholder="Add a note..." />
          </div>
          <button type="submit" disabled={submitting} className="btn-primary w-full flex items-center justify-center gap-2">
            {submitting ? <Spinner size="sm" /> : 'Update Status'}
          </button>
        </form>
      </Modal>

      {/* Resolve Modal */}
      <Modal isOpen={modals.resolve} onClose={() => close('resolve')} title="Resolve Complaint">
        <form onSubmit={handleResolve} className="space-y-4">
          <div>
            <label className="label">Resolution Note *</label>
            <textarea className="input resize-none" rows={3} required value={forms.resolutionNote}
              onChange={e => setForms(f => ({ ...f, resolutionNote: e.target.value }))} placeholder="Describe what was done to resolve this..." />
          </div>
          <div>
            <label className="label">Resolution Type</label>
            <select className="input" value={forms.resolutionType} onChange={e => setForms(f => ({ ...f, resolutionType: e.target.value }))}>
              {['REFUND_ISSUED', 'REPLACEMENT_SENT', 'INFORMATION_PROVIDED', 'ESCALATED_EXTERNALLY'].map(t => (
                <option key={t} value={t}>{t.replace(/_/g, ' ')}</option>
              ))}
            </select>
          </div>
          <button type="submit" disabled={submitting} className="btn-primary w-full flex items-center justify-center gap-2">
            {submitting ? <Spinner size="sm" /> : 'Mark as Resolved'}
          </button>
        </form>
      </Modal>

      {/* Reassign Modal */}
      <Modal isOpen={modals.reassign} onClose={() => close('reassign')} title="Reassign Complaint">
        <form onSubmit={handleReassign} className="space-y-4">
          <div>
            <label className="label">New Agent *</label>
            <select className="input" value={forms.newAgentId} required onChange={e => setForms(f => ({ ...f, newAgentId: e.target.value }))}>
              <option value="">Select agent</option>
              {agents.map(a => <option key={a.id} value={a.id}>{a.name} — {a.departmentName} ({a.role})</option>)}
            </select>
          </div>
          <div>
            <label className="label">Reason * (min 10 chars)</label>
            <textarea className="input resize-none" rows={2} required minLength={10} value={forms.reason}
              onChange={e => setForms(f => ({ ...f, reason: e.target.value }))} placeholder="Reason for reassignment..." />
          </div>
          <button type="submit" disabled={submitting} className="btn-primary w-full flex items-center justify-center gap-2">
            {submitting ? <Spinner size="sm" /> : 'Reassign'}
          </button>
        </form>
      </Modal>

      {/* Escalate Modal */}
      <Modal isOpen={modals.escalate} onClose={() => close('escalate')} title="Escalate Complaint">
        <form onSubmit={handleEscalate} className="space-y-4">
          <div>
            <label className="label">Reason</label>
            <select className="input" value={forms.escalateReason} onChange={e => setForms(f => ({ ...f, escalateReason: e.target.value }))}>
              {ESCALATION_REASONS.map(r => <option key={r} value={r}>{r.replace(/_/g, ' ')}</option>)}
            </select>
          </div>
          <div>
            <label className="label">Notes (optional)</label>
            <textarea className="input resize-none" rows={2} value={forms.escalateNotes}
              onChange={e => setForms(f => ({ ...f, escalateNotes: e.target.value }))} placeholder="Additional context..." />
          </div>
          <button type="submit" disabled={submitting} className="btn-danger w-full flex items-center justify-center gap-2">
            {submitting ? <Spinner size="sm" /> : 'Escalate Complaint'}
          </button>
        </form>
      </Modal>
    </div>
  )
}
