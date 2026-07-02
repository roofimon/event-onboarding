// Shared API types, mirroring the backend DTOs.

export type OnboardingStep =
  | 'EMAIL'
  | 'TOKEN_VERIFY'
  | 'FULFILLMENT'
  | 'SCORING'
  | 'COMPLETED'
  | 'DECLINED'

export interface StartResponse {
  applicationId: string
  step: OnboardingStep
}

export interface VerifyTokenResponse {
  verified: boolean
  step: OnboardingStep
}

export interface StepResponse {
  step: OnboardingStep
}

export interface ScoreResponse {
  score: number
  approved: boolean
  step: OnboardingStep
}

export interface FulfillmentPayload {
  name: string
  email: string
  phone: string
  salary: number
  yearsOfExperience: number
}

export interface LoginPayload {
  email: string
  password: string
}

// Profile returned by POST /api/auth/login.
export interface Profile {
  name: string
  email: string
  phone: string
  salary: number
  yearsOfExperience: number
}

// Body for PUT /api/auth/profile — re-sends the password to authorize the edit.
export interface UpdateProfilePayload {
  email: string
  password: string
  name: string
  phone: string
  salary: number
  yearsOfExperience: number
}
