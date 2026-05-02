import api from './axios'

export const getDashboardStats = () => api.get('/analytics/dashboard')
export const getComplaintTrends = (period = 'daily') => api.get(`/analytics/trends?period=${period}`)
export const getSLAReport = () => api.get('/analytics/sla-report')
export const getAgentPerformance = () => api.get('/analytics/agent-performance')
export const getHeatmap = () => api.get('/analytics/heatmap')
