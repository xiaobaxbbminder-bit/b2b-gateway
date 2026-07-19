import axios from 'axios'

const authApi_ = axios.create({
  baseURL: '/api',
  timeout: 10000
})

const api = axios.create({
  baseURL: '/api/sftp',
  timeout: 10000
})

const addInterceptors = (instance) => {
  instance.interceptors.request.use(config => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers['satoken'] = token
    }
    return config
  })

  instance.interceptors.response.use(
    response => response,
    error => {
      if (error.response?.status === 401) {
        localStorage.removeItem('token')
        window.location.href = '/login'
      }
      return Promise.reject(error)
    }
  )
}

addInterceptors(authApi_)
addInterceptors(api)

export const authApi = {
  login(data) {
    return authApi_.post('/auth/login', data)
  },
  logout() {
    return authApi_.post('/auth/logout')
  }
}

export const userApi = {
  list() {
    return api.get('/users')
  },
  get(id) {
    return api.get(`/users/${id}`)
  },
  create(data) {
    return api.post('/users', data)
  },
  update(id, data) {
    return api.put(`/users/${id}`, data)
  },
  delete(id) {
    return api.delete(`/users/${id}`)
  },
  changePassword(id, newPassword) {
    return api.put(`/users/${id}/password`, { newPassword })
  },
  toggleStatus(id) {
    return api.put(`/users/${id}/status`)
  },
  generateKeypair(keyType) {
    return api.post('/keypairs/generate', { keyType })
  }
}

export default api
