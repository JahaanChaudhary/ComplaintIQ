import api from './axios'

export const getAllAgents = (page = 0, size = 20) => api.get(`/agents?page=${page}&size=${size}`)
export const getAgentById = (id) => api.get(`/agents/${id}`)
export const getAgentDashboard = (id) => api.get(`/agents/${id}/dashboard`)
export const createAgent = (data) => api.post('/agents', data)
export const toggleAvailability = (id) => api.put(`/agents/${id}/availability`)
export const getAgentComplaints = (id, page = 0, size = 10) => api.get(`/agents/${id}/complaints?page=${page}&size=${size}`)
