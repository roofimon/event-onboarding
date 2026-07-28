"use client";

import { FormEvent, useState } from "react";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { useOnboarding } from "@/components/onboarding-provider";
import { login } from "@/lib/api";

export default function LoginPage() {
  const router = useRouter();
  const { state, update } = useOnboarding();
  const [email, setEmail] = useState(state.email);
  const [password, setPassword] = useState("");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  async function submit(event: FormEvent) {
    event.preventDefault();
    setError("");
    setLoading(true);
    try {
      update({ profile: await login(email, password), email });
      router.push("/profile");
    } catch (error) {
      setError(error instanceof Error ? error.message : "Something went wrong");
    } finally {
      setLoading(false);
    }
  }

  return (
    <>
      <p className="step-label">Account access</p>
      <h2>Welcome back</h2>
      <p className="hint">Use the credentials issued when your application was approved.</p>
      <form onSubmit={submit}>
        <label htmlFor="login-email">Email address</label>
        <input id="login-email" value={email} onChange={(e) => setEmail(e.target.value)} type="email" required autoFocus />
        <label htmlFor="login-password">Password</label>
        <input id="login-password" value={password} onChange={(e) => setPassword(e.target.value)} type="password" required autoComplete="current-password" />
        {error && <p className="error">{error}</p>}
        <button disabled={loading}>{loading ? "Signing in…" : "Log in"}</button>
      </form>
      <Link className="text-link" href="/">Start a new application</Link>
    </>
  );
}
