import api from './axios'

export const submitComplaint = (data) => api.post('/complaints', data)
export const trackComplaint = (ticketId) => api.get(`/complaints/track/${ticketId}`)
export const getMyComplaints = (page = 0, size = 10) => api.get(`/complaints/my?page=${page}&size=${size}`)
export const getAllComplaints = (params = {}) => api.get('/complaints', { params })
export const getComplaintById = (ticketId) => api.get(`/complaints/${ticketId}`)
export const updateStatus = (ticketId, data) => api.put(`/complaints/${ticketId}/status`, data)
export const resolveComplaint = (ticketId, data) => api.post(`/complaints/${ticketId}/resolve`, data)
export const submitFeedback = (ticketId, data) => api.post(`/complaints/${ticketId}/feedback`, data)
export const reassignComplaint = (ticketId, data) => api.post(`/complaints/${ticketId}/reassign`, data)
export const escalateComplaint = (ticketId, data) => api.post(`/complaints/${ticketId}/escalate`, data)
