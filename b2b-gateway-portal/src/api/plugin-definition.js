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

export const pluginDefApi = {
  list() {
    return api.get('/plugin-definitions')
  },
  get(id) {
    return api.get(`/plugin-definitions/${id}`)
  },
  create(data) {
    return api.post('/plugin-definitions', data)
  },
  update(id, data) {
    return api.put(`/plugin-definitions/${id}`, data)
  },
  delete(id) {
    return api.delete(`/plugin-definitions/${id}`)
  },
  getAvailable(userType, triggerType, invokeMode) {
    return api.get('/plugin-definitions/available', {
      params: { userType, triggerType, invokeMode }
    })
  }
}

export default api
