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
}
