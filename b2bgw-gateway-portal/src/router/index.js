import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue')
  },
  {
    path: '/',
    component: () => import('../views/Layout.vue'),
    meta: { requiresAuth: true },
    redirect: '/file-transfer/user',
    children: [
      {
        path: 'file-transfer/user',
        name: 'UserList',
        component: () => import('../views/UserList.vue')
      },
      {
        path: 'file-transfer/user/create',
        name: 'UserCreate',
        component: () => import('../views/UserEdit.vue')
      },
      {
        path: 'file-transfer/user/:id/edit',
        name: 'UserEdit',
        component: () => import('../views/UserEdit.vue')
      },
      {
        path: 'file-transfer/service',
        name: 'ServiceList',
        component: () => import('../views/ServiceList.vue')
      },
      {
        path: 'file-transfer/service/:id/config',
        name: 'ServiceConfig',
        component: () => import('../views/ServiceConfig.vue')
      },
      {
        path: 'file-transfer/log',
        name: 'LogList',
        component: () => import('../views/LogList.vue')
      },
      {
        path: 'file-transfer/log/:id',
        name: 'LogDetail',
        component: () => import('../views/LogDetail.vue')
      },
      {
        path: 'file-transfer/traffic',
        name: 'Traffic',
        component: () => import('../views/Placeholder.vue')
      },
      {
        path: 'file-transfer/plugin-definitions',
        name: 'PluginDefList',
        component: () => import('../views/PluginDefList.vue')
      },
      {
        path: 'file-transfer/plugin-definitions/:id',
        name: 'PluginDefEdit',
        component: () => import('../views/PluginDefEdit.vue')
      },
      {
        path: 'dict',
        name: 'DictList',
        component: () => import('../views/DictList.vue')
      },
      {
        path: 'ai',
        name: 'ChatAI',
        component: () => import('../views/ChatAI.vue')
      },
      {
        path: 'rosettanet',
        redirect: '/rosettanet/partners'
      },
      {
        path: 'rosettanet/partners',
        name: 'RosettaNetPartner',
        component: () => import('../views/RosettaNetPartner.vue')
      },
      {
        path: 'rosettanet/personality',
        name: 'RosettaNetPersonality',
        component: () => import('../views/RosettaNetPersonality.vue')
      },
      {
        path: 'rosettanet/pips',
        name: 'RosettaNetPip',
        component: () => import('../views/RosettaNetPip.vue')
      },
      {
        path: 'rosettanet/configs',
        name: 'RosettaNetConfig',
        component: () => import('../views/RosettaNetConfig.vue')
      },
      {
        path: 'as2',
        name: 'AS2',
        component: () => import('../views/Placeholder.vue')
      },
      {
        path: 'open-api',
        name: 'OpenApi',
        component: () => import('../views/Placeholder.vue')
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  if (to.meta.requiresAuth && !token) {
    next('/login')
  } else {
    next()
  }
})

export default router
