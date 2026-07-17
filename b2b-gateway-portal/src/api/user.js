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

export const authApi = {
  login(data) {
    return api.post('/auth/login', data)
  },
  logout() {
    return api.post('/auth/logout')
  }
}

export const userApi = {
  list() {
    return api.get('/sftp-users')
  },
  get(id) {
    return api.get(`/sftp-users/${id}`)
  },
  create(data) {
    return api.post('/sftp-users', data)
  },
  update(id, data) {
    return api.put(`/sftp-users/${id}`, data)
  },
  delete(id) {
    return api.delete(`/sftp-users/${id}`)
  },
  changePassword(id, newPassword) {
    return api.put(`/sftp-users/${id}/password`, { newPassword })
  },
  toggleStatus(id) {
    return api.put(`/sftp-users/${id}/status`)
  },
  generateKeypair(keyType) {
    return api.post('/keypairs/generate', { keyType })
  }
}

export default api
