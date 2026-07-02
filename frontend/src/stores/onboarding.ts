import { reactive } from 'vue'
import type { Profile } from '../types'

interface OnboardingState {
  applicationId: string | null
  email: string
  name: string
  phone: string
  salary: number | null
  yearsOfExperience: number | null
  score: number | null
  approved: boolean | null
  profile: Profile | null
  reset(): void
  logout(): void
}

// Lightweight shared state for the wizard — no Pinia needed for this scope.
export const store = reactive<OnboardingState>({
  applicationId: null,
  email: '',
  name: '',
  phone: '',
  salary: null,
  yearsOfExperience: null,
  score: null,
  approved: null,
  profile: null,

  reset() {
    this.applicationId = null
    this.email = ''
    this.name = ''
    this.phone = ''
    this.salary = null
    this.yearsOfExperience = null
    this.score = null
    this.approved = null
    this.profile = null
  },

  logout() {
    this.profile = null
  },
})
