import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useAuth } from '../../context/AuthContext'
import { submitComplaint } from '../../api/complaints'
import { getErrorMsg } from '../../utils/helpers'
import { Spinner } from '../../components/ui/Spinner'
import { CheckCircle, Brain } from 'lucide-react'
import toast from 'react-hot-toast'

const CHANNELS = ['WEB', 'EMAIL', 'WHATSAPP', 'API']

export default function SubmitComplaint() {
  const { user } = useAuth()
  const navigate = useNavigate()
  const [form, setForm] = useState({
    customerEmail: user?.email || '',
    title: '',
    description: '',
    orderId: '',
    channel: 'WEB'
  })
  const [loading, setLoading] = useState(false)
  const [success, setSuccess] = useState(null)

  const handleSubmit = async (e) => {
    e.preventDefault()
    if (form.description.length < 20) {
      toast.error('Description must be at least 20 characters')
      return
    }
    setLoading(true)
    try {
      const { data } = await submitComplaint(form)
      setSuccess(data.data)
      toast.success('Complaint submitted successfully!')
    } catch (err) {
      toast.error(getErrorMsg(err))
    } finally {
      setLoading(false)
    }
  }

  if (success) {
    const ai = success.aiAnalysis
    return (
      <div className="max-w-2xl">
        <div className="card text-center">
          <CheckCircle size={56} className="mx-auto mb-4 text-green-500" />
          <h2 className="text-2xl font-bold text-gray-900 mb-2">Complaint Submitted!</h2>
          <div className="bg-gray-50 rounded-xl p-4 my-4 font-mono text-primary-700 font-bold text-xl">
            {success.ticketId}
          </div>
          <p className="text-gray-500 text-sm mb-4">Save this ticket ID to track your complaint status</p>

          {ai && (
            <div className="bg-blue-50 border border-blue-100 rounded-xl p-4 text-left mb-4">
              <div className="flex items-center gap-2 mb-3 text-blue-700 font-semibold text-sm">
                <Brain size={16} /> AI Analysis
              </div>
              <div className="grid grid-cols-2 gap-2 text-sm">
                {[['Urgency', ai.urgency], ['Category', ai.category], ['Sentiment', ai.sentiment], ['Suggested', ai.suggestedAction]].map(([k, v]) => (
                  <div key={k} className={k === 'Suggested' ? 'col-span-2' : ''}>
                    <span className="text-gray-500 text-xs">{k}</span>
                    <p className="font-medium text-gray-800">{v}</p>
                  </div>
                ))}
              </div>
            </div>
          )}

          <div className="flex gap-3 justify-center">
            <button onClick={() => navigate(`/customer/complaint/${success.ticketId}`)} className="btn-primary">
              Track Complaint
            </button>
            <button onClick={() => { setSuccess(null); setForm(f => ({ ...f, title: '', description: '', orderId: '' })) }}
              className="btn-secondary">
              Submit Another
            </button>
          </div>
        </div>
      </div>
    )
  }

  return (
    <div className="max-w-2xl">
      <div className="mb-6">
        <h1 className="text-2xl font-bold text-gray-900">Submit a Complaint</h1>
        <p className="text-gray-500 text-sm mt-1 flex items-center gap-1">
          <Brain size={14} /> AI will automatically analyze, classify and route your complaint
        </p>
      </div>

      <div className="card">
        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <label className="label">Your Email</label>
            <input className="input" type="email" value={form.customerEmail} readOnly
              onChange={e => setForm(f => ({ ...f, customerEmail: e.target.value }))} />
          </div>
          <div>
            <label className="label">Complaint Title *</label>
            <input className="input" value={form.title} required maxLength={200}
              onChange={e => setForm(f => ({ ...f, title: e.target.value }))}
              placeholder="Brief summary of your issue" />
            <p className="text-xs text-gray-400 mt-1">{form.title.length}/200</p>
          </div>
          <div>
            <label className="label">Description * <span className="text-gray-400 font-normal">(min 20 chars)</span></label>
            <textarea className="input resize-none" rows={5} value={form.description} required minLength={20} maxLength={2000}
              onChange={e => setForm(f => ({ ...f, description: e.target.value }))}
              placeholder="Describe your issue in detail — the more information you provide, the better our AI can classify and route it." />
            <p className="text-xs text-gray-400 mt-1">{form.description.length}/2000</p>
          </div>
          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="label">Order ID <span className="text-gray-400 font-normal">(optional)</span></label>
              <input className="input" value={form.orderId}
                onChange={e => setForm(f => ({ ...f, orderId: e.target.value }))} placeholder="ORD-12345" />
            </div>
            <div>
              <label className="label">Channel</label>
              <select className="input" value={form.channel}
                onChange={e => setForm(f => ({ ...f, channel: e.target.value }))}>
                {CHANNELS.map(c => <option key={c} value={c}>{c}</option>)}
              </select>
            </div>
          </div>
          <button type="submit" disabled={loading} className="btn-primary w-full flex items-center justify-center gap-2">
            {loading ? <><Spinner size="sm" /> Analyzing with AI...</> : 'Submit Complaint'}
          </button>
        </form>
      </div>
    </div>
  )
}
