import { useNavigate } from 'react-router-dom'
import { formatDate, getUrgencyClass, getStatusClass, getSlaClass } from '../../utils/helpers'
import { Clock, User, Building } from 'lucide-react'

export function ComplaintCard({ complaint, linkTo }) {
  const navigate = useNavigate()
  const handleClick = () => linkTo && navigate(linkTo)

  return (
    <div onClick={handleClick}
      className={`card hover:shadow-md transition-all ${linkTo ? 'cursor-pointer' : ''}`}>
      <div className="flex items-start justify-between gap-3 mb-3">
        <div className="flex-1 min-w-0">
          <div className="flex items-center gap-2 mb-1 flex-wrap">
            <span className="font-mono text-xs text-primary-600 font-semibold">{complaint.ticketId}</span>
            {complaint.urgency && <span className={getUrgencyClass(complaint.urgency)}>{complaint.urgency}</span>}
            <span className={getStatusClass(complaint.status)}>{complaint.status?.replace('_', ' ')}</span>
            {complaint.slaStatus && <span className={getSlaClass(complaint.slaStatus)}>{complaint.slaStatus?.replace('_', ' ')}</span>}
          </div>
          <h3 className="font-medium text-gray-900 truncate">{complaint.title}</h3>
        </div>
      </div>
      <div className="flex items-center gap-4 text-xs text-gray-500 flex-wrap">
        {complaint.customerName && (
          <span className="flex items-center gap-1"><User size={12} />{complaint.customerName}</span>
        )}
        {complaint.assignedAgentName && (
          <span className="flex items-center gap-1"><User size={12} />Agent: {complaint.assignedAgentName}</span>
        )}
        {complaint.departmentName && (
          <span className="flex items-center gap-1"><Building size={12} />{complaint.departmentName}</span>
        )}
        <span className="flex items-center gap-1"><Clock size={12} />{formatDate(complaint.createdAt)}</span>
      </div>
    </div>
  )
}
