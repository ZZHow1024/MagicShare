import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'Home',
      redirect: '/home',
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
  ],
})

export default router
