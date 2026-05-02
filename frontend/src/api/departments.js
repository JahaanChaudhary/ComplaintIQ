import api from './axios'

export const getAllDepartments = () => api.get('/departments')
export const createDepartment = (data) => api.post('/departments', data)
