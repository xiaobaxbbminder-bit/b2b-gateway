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

const base = '/rosettanet'

export const rosettanetApi = {
  personalities: {
    list() { return api.get(`${base}/personalities`) },
    create(d) { return api.post(`${base}/personalities`, d) },
    update(id, d) { return api.put(`${base}/personalities/${id}`, d) },
    delete(id) { return api.delete(`${base}/personalities/${id}`) }
  },
  partners: {
    list() { return api.get(`${base}/partners`) },
    create(d) { return api.post(`${base}/partners`, d) },
    update(id, d) { return api.put(`${base}/partners/${id}`, d) },
    delete(id) { return api.delete(`${base}/partners/${id}`) }
  },
  pips: {
    list() { return api.get(`${base}/pips`) },
    create(d) { return api.post(`${base}/pips`, d) },
    update(id, d) { return api.put(`${base}/pips/${id}`, d) },
    delete(id) { return api.delete(`${base}/pips/${id}`) }
  },
  configs: {
    list() { return api.get(`${base}/configs`) },
    create(d) { return api.post(`${base}/configs`, d) },
    update(id, d) { return api.put(`${base}/configs/${id}`, d) },
    delete(id) { return api.delete(`${base}/configs/${id}`) }
  }
}

export default api
