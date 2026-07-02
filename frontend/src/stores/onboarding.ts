import { reactive } from 'vue'

interface OnboardingState {
  applicationId: string | null
  email: string
  name: string
  phone: string
  salary: number | null
  yearsOfExperience: number | null
  score: number | null
  approved: boolean | null
  reset(): void
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

  reset() {
    this.applicationId = null
    this.email = ''
    this.name = ''
    this.phone = ''
    this.salary = null
    this.yearsOfExperience = null
    this.score = null
    this.approved = null
  },
})
