import { format, formatDistanceToNow } from 'date-fns'

export const formatDate = (date) => {
  if (!date) return '-'
  return format(new Date(date), 'dd MMM yyyy, HH:mm')
}

export const formatDateShort = (date) => {
  if (!date) return '-'
  return format(new Date(date), 'dd MMM yyyy')
}

export const timeAgo = (date) => {
  if (!date) return '-'
  return formatDistanceToNow(new Date(date), { addSuffix: true })
}

export const getUrgencyClass = (urgency) => {
  const map = { CRITICAL: 'badge-critical', HIGH: 'badge-high', MEDIUM: 'badge-medium', LOW: 'badge-low' }
  return map[urgency] || 'badge-low'
}

export const getStatusClass = (status) => {
  return `badge-${status?.toLowerCase()}`
}

export const getSlaClass = (sla) => {
  return `sla-${sla?.toLowerCase()}`
}

export const getErrorMsg = (err) => {
  return err?.response?.data?.message || err?.message || 'Something went wrong'
}

export const ROLES = {
  CUSTOMER: 'CUSTOMER',
  AGENT: 'AGENT',
  TEAM_LEAD: 'TEAM_LEAD',
  MANAGER: 'MANAGER',
  ADMIN: 'ADMIN'
}
