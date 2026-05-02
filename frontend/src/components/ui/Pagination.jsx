import { ChevronLeft, ChevronRight } from 'lucide-react'

export function Pagination({ page, totalPages, onPageChange }) {
  if (totalPages <= 1) return null
  return (
    <div className="flex items-center justify-between mt-4 pt-4 border-t">
      <p className="text-sm text-gray-500">Page {page + 1} of {totalPages}</p>
      <div className="flex gap-2">
        <button onClick={() => onPageChange(page - 1)} disabled={page === 0}
          className="btn-secondary btn-sm flex items-center gap-1 disabled:opacity-40">
          <ChevronLeft size={14} /> Prev
        </button>
        <button onClick={() => onPageChange(page + 1)} disabled={page >= totalPages - 1}
          className="btn-secondary btn-sm flex items-center gap-1 disabled:opacity-40">
          Next <ChevronRight size={14} />
        </button>
      </div>
    </div>
  )
}
