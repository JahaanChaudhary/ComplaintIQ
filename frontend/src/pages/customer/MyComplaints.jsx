import { useState, useEffect } from 'react'
import { getMyComplaints } from '../../api/complaints'
import { ComplaintCard } from '../../components/ui/ComplaintCard'
import { Pagination } from '../../components/ui/Pagination'
import { Spinner } from '../../components/ui/Spinner'
import { FileText } from 'lucide-react'

export default function MyComplaints() {
  const [complaints, setComplaints] = useState([])
  const [loading, setLoading] = useState(true)
  const [page, setPage] = useState(0)
  const [totalPages, setTotalPages] = useState(0)

  const load = (p) => {
    setLoading(true)
    getMyComplaints(p, 10)
      .then(({ data }) => {
        setComplaints(data.data?.content || [])
        setTotalPages(data.data?.totalPages || 0)
      })
      .catch(() => {})
      .finally(() => setLoading(false))
  }

  useEffect(() => { load(page) }, [page])

  return (
    <div className="space-y-4">
      <h1 className="text-2xl font-bold text-gray-900">My Complaints</h1>
      {loading ? (
        <div className="flex justify-center py-12"><Spinner /></div>
      ) : complaints.length === 0 ? (
        <div className="card text-center py-12 text-gray-400">
          <FileText size={40} className="mx-auto mb-3 opacity-50" />
          <p>No complaints found</p>
        </div>
      ) : (
        <>
          <div className="space-y-3">
            {complaints.map(c => (
              <ComplaintCard key={c.ticketId} complaint={c} linkTo={`/customer/complaint/${c.ticketId}`} />
            ))}
          </div>
          <Pagination page={page} totalPages={totalPages} onPageChange={setPage} />
        </>
      )}
    </div>
  )
}
