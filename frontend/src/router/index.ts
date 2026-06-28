import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'
import { store } from '../stores/onboarding'
import EmailStep from '../views/EmailStep.vue'
import TokenStep from '../views/TokenStep.vue'
import FulfillmentStep from '../views/FulfillmentStep.vue'
import WelcomePage from '../views/WelcomePage.vue'
import DeclinePage from '../views/DeclinePage.vue'

const routes: RouteRecordRaw[] = [
  { path: '/', name: 'email', component: EmailStep },
  { path: '/verify', name: 'verify', component: TokenStep, meta: { needsApplication: true } },
  { path: '/fulfillment', name: 'fulfillment', component: FulfillmentStep, meta: { needsApplication: true } },
  { path: '/welcome', name: 'welcome', component: WelcomePage, meta: { needsApplication: true } },
  { path: '/declined', name: 'declined', component: DeclinePage, meta: { needsApplication: true } },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

// Can't deep-link into a step without having started an application.
router.beforeEach((to) => {
  if (to.meta.needsApplication && !store.applicationId) {
    return { name: 'email' }
  }
  return true
})

export default router
