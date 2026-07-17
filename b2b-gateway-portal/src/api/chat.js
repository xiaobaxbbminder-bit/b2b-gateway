import axios from 'axios'

const api = axios.create({
  baseURL: '/api',
  timeout: 300000
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

export const chatApi = {
  sendMessage(message, conversationId) {
    return fetch('/api/ai/chat', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'satoken': localStorage.getItem('token')
      },
      body: JSON.stringify({ message, conversationId })
    })
  },
  sendMessageSync(message, conversationId) {
    return api.post('/ai/chat/sync', { message, conversationId })
  },
  reindexDocs() {
    return api.post('/docs/reindex')
  }
}

export default api
