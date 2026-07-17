import axios from 'axios'

const api = axios.create({
  baseURL: '/api',
  timeout: 10000
})

api.interceptors.request.use(config => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers['satoken'] = token
  }
  return config
})

api.interceptors.response.use(
  response => response,
  error => {
    if (error.response?.status === 401) {
      localStorage.removeItem('token')
      window.location.href = '/login'
    }
    return Promise.reject(error)
  }
)

export const serviceApi = {
  list() {
    return api.get('/sftp-services')
  },
  get(id) {
    return api.get(`/sftp-services/${id}`)
  },
  create(data) {
    return api.post('/sftp-services', data)
  },
  update(id, data) {
    return api.put(`/sftp-services/${id}`, data)
  },
  delete(id) {
    return api.delete(`/sftp-services/${id}`)
  },
  getUserOptions() {
    return api.get('/sftp-users/options')
  },
  getConfig(serviceId) {
    return api.get(`/sftp-services/${serviceId}/config`)
  },
  updateConfig(serviceId, data) {
    return api.put(`/sftp-services/${serviceId}/config`, data)
  }
}

export default api
