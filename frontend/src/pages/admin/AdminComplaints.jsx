import { useState, useEffect, useCallback } from 'react'
import { getAllComplaints } from '../../api/complaints'
import { ComplaintCard } from '../../components/ui/ComplaintCard'
import { Pagination } from '../../components/ui/Pagination'
import { Spinner } from '../../components/ui/Spinner'
import { Filter, X } from 'lucide-react'

const STATUSES = ['', 'OPEN', 'ASSIGNED', 'IN_PROGRESS', 'RESOLVED', 'CLOSED', 'ESCALATED']
const URGENCIES = ['', 'CRITICAL', 'HIGH', 'MEDIUM', 'LOW']
const CATEGORIES = ['', 'DELIVERY', 'PAYMENT', 'PRODUCT', 'REFUND', 'TECHNICAL', 'OTHER']

export default function AdminComplaints() {
  const [complaints, setComplaints] = useState([])
  const [loading, setLoading] = useState(true)
  const [page, setPage] = useState(0)
  const [totalPages, setTotalPages] = useState(0)
  const [totalElements, setTotalElements] = useState(0)
  const [filters, setFilters] = useState({ status: '', urgency: '', category: '', keyword: '' })

  const load = useCallback((p = 0) => {
    setLoading(true)
    const params = { page: p, size: 10 }
    if (filters.status) params.status = filters.status
    if (filters.urgency) params.urgency = filters.urgency
    if (filters.category) params.category = filters.category
    if (filters.keyword) params.keyword = filters.keyword
    getAllComplaints(params)
      .then(({ data }) => {
        setComplaints(data.data?.content || [])
        setTotalPages(data.data?.totalPages || 0)
        setTotalElements(data.data?.totalElements || 0)
      })
      .finally(() => setLoading(false))
  }, [filters])

  useEffect(() => { setPage(0); load(0) }, [filters])
  useEffect(() => { load(page) }, [page])

  const clearFilters = () => setFilters({ status: '', urgency: '', category: '', keyword: '' })

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between flex-wrap gap-3">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">All Complaints</h1>
          <p className="text-sm text-gray-500">{totalElements} total</p>
        </div>
      </div>

      <div className="card p-4 flex flex-wrap gap-3 items-end">
        <div className="flex items-center gap-1 text-sm text-gray-500"><Filter size={14} /> Filters:</div>
        {[
          ['Status', 'status', STATUSES],
          ['Urgency', 'urgency', URGENCIES],
          ['Category', 'category', CATEGORIES],
        ].map(([label, key, options]) => (
          <div key={key}>
            <label className="label text-xs">{label}</label>
            <select className="input text-sm" value={filters[key]}
              onChange={e => setFilters(f => ({ ...f, [key]: e.target.value }))}>
              {options.map(o => <option key={o} value={o}>{o || 'All'}</option>)}
            </select>
          </div>
        ))}
        <div>
          <label className="label text-xs">Search</label>
          <input className="input text-sm" value={filters.keyword} placeholder="Title, ticket ID..."
            onChange={e => setFilters(f => ({ ...f, keyword: e.target.value }))} />
        </div>
        {Object.values(filters).some(Boolean) && (
          <button onClick={clearFilters} className="btn-secondary btn-sm flex items-center gap-1 self-end">
            <X size={13} /> Clear
          </button>
        )}
      </div>

      {loading ? (
        <div className="flex justify-center py-12"><Spinner /></div>
      ) : complaints.length === 0 ? (
        <div className="card text-center py-10 text-gray-400">No complaints found</div>
      ) : (
        <>
          <div className="space-y-3">
            {complaints.map(c => <ComplaintCard key={c.ticketId} complaint={c} linkTo={`/admin/complaint/${c.ticketId}`} />)}
          </div>
          <Pagination page={page} totalPages={totalPages} onPageChange={setPage} />
        </>
      )}
    </div>
  )
}
