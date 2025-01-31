import { createRouter, createWebHistory } from 'vue-router'
import { useWSocketStore } from '@/stores/index.js'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'Home',
      redirect: '/login',
      component: () => import('@/views/layout/LayoutContainer.vue'),
      children: [
        {
          path: '/home',
          name: 'HomeIndex',
          component: () => import('@/views/home/HomeIndex.vue'),
        },
        {
          path: '/about',
          name: 'AboutPage',
          component: () => import('@/views/about/AboutPage.vue'),
        },
      ],
    },
    {
      path: '/login',
      name: 'LoginPage',
      component: () => import('@/views/login/LoginPage.vue'),
    },
  ],
})

router.beforeEach(async (to) => {
  const wSocketStore = useWSocketStore()

  if (wSocketStore.wSocket === null && to.name !== 'LoginPage') {
    return { name: 'LoginPage' }
  }
})

export default router
