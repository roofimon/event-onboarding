import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'
import { store } from '../stores/onboarding'
import EmailStep from '../views/EmailStep.vue'
import TokenStep from '../views/TokenStep.vue'
import FulfillmentStep from '../views/FulfillmentStep.vue'
import WelcomePage from '../views/WelcomePage.vue'
import DeclinePage from '../views/DeclinePage.vue'
import LoginPage from '../views/LoginPage.vue'
import ProfilePage from '../views/ProfilePage.vue'

const routes: RouteRecordRaw[] = [
  { path: '/', name: 'email', component: EmailStep },
  { path: '/verify', name: 'verify', component: TokenStep, meta: { needsApplication: true } },
  { path: '/fulfillment', name: 'fulfillment', component: FulfillmentStep, meta: { needsApplication: true } },
  { path: '/welcome', name: 'welcome', component: WelcomePage, meta: { needsApplication: true } },
  { path: '/declined', name: 'declined', component: DeclinePage, meta: { needsApplication: true } },
  { path: '/login', name: 'login', component: LoginPage },
  { path: '/profile', name: 'profile', component: ProfilePage, meta: { needsProfile: true } },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

// Can't deep-link into a step without having started an application,
// nor into the profile without having logged in.
router.beforeEach((to) => {
  if (to.meta.needsApplication && !store.applicationId) {
    return { name: 'email' }
  }
  if (to.meta.needsProfile && !store.profile) {
    return { name: 'login' }
  }
  return true
})

export default router
