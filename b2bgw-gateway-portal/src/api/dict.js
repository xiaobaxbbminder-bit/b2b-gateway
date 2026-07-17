import axios from 'axios'

const api = axios.create({
  baseURL: '/api',
  timeout: 10000
})

api.interceptors.request.use(config => {
  const token = localStorage.getItem('token')
  if (token) config.headers['satoken'] = token
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

export const dictApi = {
  getTypes() {
    return api.get('/dict/types')
  },
  getByType(type) {
    return api.get(`/dict/${type}`)
  },
  create(data) {
    return api.post('/dict', data)
  },
  update(id, data) {
    return api.put(`/dict/${id}`, data)
  },
  delete(id) {
    return api.delete(`/dict/${id}`)
  }
}

export default api
