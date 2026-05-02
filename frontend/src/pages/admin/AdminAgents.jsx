import { useState, useEffect } from 'react'
import { getAllAgents, createAgent, toggleAvailability } from '../../api/agents'
import { getAllDepartments } from '../../api/departments'
import { Modal } from '../../components/ui/Modal'
import { Pagination } from '../../components/ui/Pagination'
import { Spinner } from '../../components/ui/Spinner'
import { getErrorMsg } from '../../utils/helpers'
import { Plus, ToggleLeft, ToggleRight, User } from 'lucide-react'
import toast from 'react-hot-toast'

const ROLES = ['JUNIOR', 'SENIOR', 'TEAM_LEAD', 'MANAGER']

export default function AdminAgents() {
  const [agents, setAgents] = useState([])
  const [departments, setDepartments] = useState([])
  const [loading, setLoading] = useState(true)
  const [page, setPage] = useState(0)
  const [totalPages, setTotalPages] = useState(0)
  const [modalOpen, setModalOpen] = useState(false)
  const [submitting, setSubmitting] = useState(false)
  const [form, setForm] = useState({ name: '', email: '', phone: '', departmentId: '', role: 'JUNIOR', maxLoad: 10, password: 'Agent@123' })

  const load = (p = 0) => {
    setLoading(true)
    getAllAgents(p, 10)
      .then(({ data }) => { setAgents(data.data?.content || []); setTotalPages(data.data?.totalPages || 0) })
      .finally(() => setLoading(false))
  }

  useEffect(() => { load(); getAllDepartments().then(({ data }) => setDepartments(data.data || [])).catch(() => {}) }, [])
  useEffect(() => { load(page) }, [page])

  const handleCreate = async (e) => {
    e.preventDefault(); setSubmitting(true)
    try {
      await createAgent({ ...form, departmentId: parseInt(form.departmentId), maxLoad: parseInt(form.maxLoad) })
      toast.success('Agent created!'); setModalOpen(false); load(page)
      setForm({ name: '', email: '', phone: '', departmentId: '', role: 'JUNIOR', maxLoad: 10, password: 'Agent@123' })
    } catch (err) { toast.error(getErrorMsg(err)) }
    finally { setSubmitting(false) }
  }

  const handleToggle = async (id) => {
    try { await toggleAvailability(id); load(page) } catch (err) { toast.error(getErrorMsg(err)) }
  }

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-bold text-gray-900">Agents</h1>
        <button onClick={() => setModalOpen(true)} className="btn-primary flex items-center gap-2">
          <Plus size={16} /> Add Agent
        </button>
      </div>

      {loading ? (
        <div className="flex justify-center py-12"><Spinner /></div>
      ) : (
        <div className="grid gap-4">
          {agents.map(agent => (
            <div key={agent.id} className="card flex items-center justify-between gap-4 flex-wrap">
              <div className="flex items-center gap-3">
                <div className="w-10 h-10 bg-primary-100 text-primary-700 rounded-full flex items-center justify-center font-bold text-sm">
                  {agent.name.charAt(0)}
                </div>
                <div>
                  <div className="flex items-center gap-2">
                    <p className="font-semibold text-gray-900">{agent.name}</p>
                    <span className="bg-gray-100 text-gray-600 text-xs px-2 py-0.5 rounded-full">{agent.role?.replace('_', ' ')}</span>
                    {!agent.isAvailable && <span className="bg-red-100 text-red-600 text-xs px-2 py-0.5 rounded-full">Unavailable</span>}
                  </div>
                  <p className="text-sm text-gray-500">{agent.email} · {agent.departmentName}</p>
                  <p className="text-xs text-gray-400 mt-0.5">Load: {agent.currentLoad}/{agent.maxLoad} · Resolved: {agent.totalResolved}</p>
                </div>
              </div>
              <button onClick={() => handleToggle(agent.id)}
                className={`flex items-center gap-1.5 text-sm px-3 py-1.5 rounded-lg font-medium transition-colors ${agent.isAvailable ? 'bg-green-50 text-green-700 hover:bg-green-100' : 'bg-gray-50 text-gray-600 hover:bg-gray-100'}`}>
                {agent.isAvailable ? <ToggleRight size={16} /> : <ToggleLeft size={16} />}
                {agent.isAvailable ? 'Available' : 'Unavailable'}
              </button>
            </div>
          ))}
        </div>
      )}
      <Pagination page={page} totalPages={totalPages} onPageChange={setPage} />

      <Modal isOpen={modalOpen} onClose={() => setModalOpen(false)} title="Create Agent">
        <form onSubmit={handleCreate} className="space-y-3">
          {[['Name', 'name', 'text', true], ['Email', 'email', 'email', true], ['Phone', 'phone', 'tel', false], ['Password', 'password', 'password', true]].map(([label, field, type, req]) => (
            <div key={field}>
              <label className="label">{label}</label>
              <input className="input" type={type} required={req} value={form[field]}
                onChange={e => setForm(f => ({ ...f, [field]: e.target.value }))} />
            </div>
          ))}
          <div className="grid grid-cols-2 gap-3">
            <div>
              <label className="label">Department</label>
              <select className="input" required value={form.departmentId} onChange={e => setForm(f => ({ ...f, departmentId: e.target.value }))}>
                <option value="">Select...</option>
                {departments.map(d => <option key={d.id} value={d.id}>{d.name}</option>)}
              </select>
            </div>
            <div>
              <label className="label">Role</label>
              <select className="input" value={form.role} onChange={e => setForm(f => ({ ...f, role: e.target.value }))}>
                {ROLES.map(r => <option key={r} value={r}>{r.replace('_', ' ')}</option>)}
              </select>
            </div>
          </div>
          <div>
            <label className="label">Max Load</label>
            <input className="input" type="number" min={1} max={50} value={form.maxLoad}
              onChange={e => setForm(f => ({ ...f, maxLoad: e.target.value }))} />
          </div>
          <button type="submit" disabled={submitting} className="btn-primary w-full flex items-center justify-center gap-2">
            {submitting ? <Spinner size="sm" /> : 'Create Agent'}
          </button>
        </form>
      </Modal>
    </div>
  )
}
