import axios from 'axios'
import type {
  FulfillmentPayload,
  Profile,
  ScoreResponse,
  StartResponse,
  StepResponse,
  UpdateProfilePayload,
  VerifyTokenResponse,
} from '../types'

// Relative base URL — Vite proxies /api to the Spring Boot backend in dev.
const http = axios.create({ baseURL: '/api/onboarding' })
const authHttp = axios.create({ baseURL: '/api/auth' })

export function start(email: string): Promise<StartResponse> {
  return http.post<StartResponse>('/start', { email }).then((r) => r.data)
}

export function verifyToken(id: string, token: string): Promise<VerifyTokenResponse> {
  return http.post<VerifyTokenResponse>(`/${id}/verify-token`, { token }).then((r) => r.data)
}

export function fulfillment(id: string, payload: FulfillmentPayload): Promise<StepResponse> {
  return http.post<StepResponse>(`/${id}/fulfillment`, payload).then((r) => r.data)
}

export function score(id: string): Promise<ScoreResponse> {
  return http.post<ScoreResponse>(`/${id}/score`).then((r) => r.data)
}

export function login(email: string, password: string): Promise<Profile> {
  return authHttp.post<Profile>('/login', { email, password }).then((r) => r.data)
}

export function updateProfile(payload: UpdateProfilePayload): Promise<Profile> {
  return authHttp.put<Profile>('/profile', payload).then((r) => r.data)
}

/** Pull a human-readable message out of an axios error. */
export function errorMessage(err: unknown): string {
  if (axios.isAxiosError(err)) {
    const data = err.response?.data as { error?: string } | undefined
    return data?.error ?? err.message
  }
  return err instanceof Error ? err.message : 'Something went wrong'
}
