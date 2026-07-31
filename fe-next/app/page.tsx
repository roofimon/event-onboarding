"use client";

import { FormEvent, useState } from "react";
import { useRouter } from "next/navigation";
import { StepIndicator } from "@/components/step-indicator";
import { useOnboarding } from "@/components/onboarding-provider";
import { start } from "@/lib/api";

export default function EmailPage() {
  const router = useRouter();
  const { state, reset, update } = useOnboarding();
  const [email, setEmail] = useState(state.email);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  async function submit(event: FormEvent) {
    event.preventDefault();
    setError("");
    setLoading(true);
    try {
      const result = await start(email);
      reset();
      update({ applicationId: result.applicationId, email });
      router.push("/verify");
    } catch (error) {
      setError(error instanceof Error ? error.message : "Something went wrong");
    } finally {
      setLoading(false);
    }
  }

  return (
    <>
      <StepIndicator current={1} />
      <p className="step-label">Step 1 of 4</p>
      <h2>Let&apos;s start with your email</h2>
      <p className="hint">We&apos;ll use it to keep your application secure and send updates.</p>
      <form onSubmit={submit}>
        <label htmlFor="email">Email address</label>
        <input id="email" value={email} onChange={(e) => setEmail(e.target.value)} type="email" required placeholder="you@example.com" autoFocus />
        {error && <p className="error">{error}</p>}
        <button disabled={loading}>{loading ? "Sending…" : "Continue"}</button>
      </form>
      <a className="text-link" href="/login">Already have an account? Log in</a>
    </>
  );
}
