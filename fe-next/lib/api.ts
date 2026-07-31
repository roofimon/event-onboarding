import type { FulfillmentPayload, Profile } from "./types";

async function request<T>(path: string, init?: RequestInit): Promise<T> {
  const response = await fetch(path, {
    ...init,
    headers: {
      "Content-Type": "application/json",
      ...init?.headers,
    },
  });

  if (!response.ok) {
    const body = (await response.json().catch(() => null)) as { error?: string } | null;
    throw new Error(body?.error ?? `Request failed (${response.status})`);
  }

  return response.json() as Promise<T>;
}

export function start(email: string) {
  return request<{ applicationId: string }>("/api/onboarding/start", {
    method: "POST",
    body: JSON.stringify({ email }),
  });
}

export function verifyToken(id: string, token: string) {
  return request("/api/onboarding/" + id + "/verify-token", {
    method: "POST",
    body: JSON.stringify({ token }),
  });
}

export function fulfill(id: string, payload: FulfillmentPayload) {
  return request("/api/onboarding/" + id + "/fulfillment", {
    method: "POST",
    body: JSON.stringify(payload),
  });
}

export function score(id: string) {
  return request<{ score: number; approved: boolean }>(
    "/api/onboarding/" + id + "/score",
    { method: "POST" },
  );
}

export function login(email: string, password: string) {
  return request<Profile>("/api/auth/login", {
    method: "POST",
    body: JSON.stringify({ email, password }),
  });
}

export function updateProfile(payload: Profile & { password: string }) {
  return request<Profile>("/api/auth/profile", {
    method: "PUT",
    body: JSON.stringify(payload),
  });
}
