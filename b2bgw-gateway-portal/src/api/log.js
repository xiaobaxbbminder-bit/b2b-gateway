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

export const logApi = {
  list(page = 0, size = 10) {
    return api.get('/operation-logs', { params: { page, size } })
  },
  get(id) {
    return api.get(`/operation-logs/${id}`)
  },
  getDetails(id) {
    return api.get(`/operation-logs/${id}/details`)
  },
  delete(id) {
    return api.delete(`/operation-logs/${id}`)
  }
}

export default api
